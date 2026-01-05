package com.hms.service;

import com.hms.dto.request.InvoiceRequest;
import com.hms.dto.request.PaymentRequest;
import com.hms.dto.response.InvoiceResponse;
import com.hms.dto.response.PaymentResponse;
import com.hms.entity.*;
import com.hms.enums.PaymentStatus;
import com.hms.exception.BadRequestException;
import com.hms.exception.ResourceNotFoundException;
import com.hms.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class BillingService {

    private final InvoiceRepository invoiceRepository;
    private final PaymentRepository paymentRepository;
    private final PatientRepository patientRepository;
    private final AppointmentRepository appointmentRepository;

    // Invoice Methods
    public List<InvoiceResponse> getAllInvoices() {
        log.info("Fetching all invoices");
        return invoiceRepository.findAll().stream()
                .map(this::mapToInvoiceResponse)
                .collect(Collectors.toList());
    }

    public InvoiceResponse getInvoiceById(Long id) {
        log.info("Fetching invoice with ID: {}", id);
        Invoice invoice = invoiceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Invoice", "id", id));
        return mapToInvoiceResponse(invoice);
    }

    public InvoiceResponse getInvoiceByNumber(String invoiceNumber) {
        log.info("Fetching invoice by number: {}", invoiceNumber);
        Invoice invoice = invoiceRepository.findByInvoiceNumber(invoiceNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Invoice", "invoiceNumber", invoiceNumber));
        return mapToInvoiceResponse(invoice);
    }

    public List<InvoiceResponse> getInvoicesByPatient(Long patientId) {
        log.info("Fetching invoices for patient ID: {}", patientId);
        return invoiceRepository.findByPatientId(patientId).stream()
                .map(this::mapToInvoiceResponse)
                .collect(Collectors.toList());
    }

    public List<InvoiceResponse> getInvoicesByStatus(PaymentStatus status) {
        log.info("Fetching invoices by status: {}", status);
        return invoiceRepository.findByStatus(status).stream()
                .map(this::mapToInvoiceResponse)
                .collect(Collectors.toList());
    }

    public List<InvoiceResponse> getOverdueInvoices() {
        log.info("Fetching overdue invoices");
        return invoiceRepository.findOverdueInvoices(LocalDate.now()).stream()
                .map(this::mapToInvoiceResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public InvoiceResponse createInvoice(InvoiceRequest request) {
        log.info("Creating new invoice");

        Patient patient = patientRepository.findById(request.getPatientId())
                .orElseThrow(() -> new ResourceNotFoundException("Patient", "id", request.getPatientId()));

        Appointment appointment = null;
        if (request.getAppointmentId() != null) {
            appointment = appointmentRepository.findById(request.getAppointmentId())
                    .orElseThrow(() -> new ResourceNotFoundException("Appointment", "id", request.getAppointmentId()));

            // Check if appointment already has an invoice
            if (invoiceRepository.findByAppointmentId(request.getAppointmentId()).isPresent()) {
                throw new BadRequestException("Appointment already has an invoice");
            }
        }

        Invoice invoice = Invoice.builder()
                .patient(patient)
                .appointment(appointment)
                .dueDate(request.getDueDate())
                .notes(request.getNotes())
                .status(PaymentStatus.PENDING)
                .paidAmount(BigDecimal.ZERO)
                .totalAmount(BigDecimal.ZERO)
                .build();

        // Add invoice items
        if (request.getItems() != null && !request.getItems().isEmpty()) {
            for (InvoiceRequest.InvoiceItemRequest itemRequest : request.getItems()) {
                InvoiceItem item = InvoiceItem.builder()
                        .invoice(invoice)
                        .description(itemRequest.getDescription())
                        .amount(itemRequest.getAmount())
                        .quantity(itemRequest.getQuantity() != null ? itemRequest.getQuantity() : 1)
                        .build();
                invoice.getItems().add(item);
            }
            invoice.recalculateTotal();
        }

        Invoice savedInvoice = invoiceRepository.save(invoice);
        log.info("Invoice created with ID: {} and number: {}", savedInvoice.getId(), savedInvoice.getInvoiceNumber());

        return mapToInvoiceResponse(savedInvoice);
    }

    @Transactional
    public InvoiceResponse updateInvoice(Long id, InvoiceRequest request) {
        log.info("Updating invoice with ID: {}", id);

        Invoice invoice = invoiceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Invoice", "id", id));

        if (invoice.getStatus() == PaymentStatus.PAID) {
            throw new BadRequestException("Cannot update a fully paid invoice");
        }

        if (request.getDueDate() != null)
            invoice.setDueDate(request.getDueDate());
        if (request.getNotes() != null)
            invoice.setNotes(request.getNotes());

        // Update items if provided
        if (request.getItems() != null) {
            invoice.getItems().clear();
            for (InvoiceRequest.InvoiceItemRequest itemRequest : request.getItems()) {
                InvoiceItem item = InvoiceItem.builder()
                        .invoice(invoice)
                        .description(itemRequest.getDescription())
                        .amount(itemRequest.getAmount())
                        .quantity(itemRequest.getQuantity() != null ? itemRequest.getQuantity() : 1)
                        .build();
                invoice.getItems().add(item);
            }
            invoice.recalculateTotal();
        }

        Invoice updatedInvoice = invoiceRepository.save(invoice);
        log.info("Invoice updated successfully");

        return mapToInvoiceResponse(updatedInvoice);
    }

    @Transactional
    public void cancelInvoice(Long id) {
        log.info("Cancelling invoice with ID: {}", id);

        Invoice invoice = invoiceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Invoice", "id", id));

        if (invoice.getStatus() == PaymentStatus.PAID) {
            throw new BadRequestException("Cannot cancel a paid invoice");
        }

        invoice.setStatus(PaymentStatus.CANCELLED);
        invoiceRepository.save(invoice);
        log.info("Invoice cancelled successfully");
    }

    // Payment Methods
    public List<PaymentResponse> getPaymentsByInvoice(Long invoiceId) {
        log.info("Fetching payments for invoice ID: {}", invoiceId);
        return paymentRepository.findByInvoiceId(invoiceId).stream()
                .map(this::mapToPaymentResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public PaymentResponse recordPayment(PaymentRequest request) {
        log.info("Recording payment for invoice ID: {}", request.getInvoiceId());

        Invoice invoice = invoiceRepository.findById(request.getInvoiceId())
                .orElseThrow(() -> new ResourceNotFoundException("Invoice", "id", request.getInvoiceId()));

        if (invoice.getStatus() == PaymentStatus.PAID) {
            throw new BadRequestException("Invoice is already fully paid");
        }

        if (invoice.getStatus() == PaymentStatus.CANCELLED) {
            throw new BadRequestException("Cannot make payment on a cancelled invoice");
        }

        // Validate payment amount
        BigDecimal remainingBalance = invoice.getRemainingBalance();
        if (request.getAmount().compareTo(remainingBalance) > 0) {
            throw new BadRequestException("Payment amount exceeds remaining balance of " + remainingBalance);
        }

        Payment payment = Payment.builder()
                .invoice(invoice)
                .amount(request.getAmount())
                .paymentMethod(request.getPaymentMethod())
                .transactionId(request.getTransactionId())
                .notes(request.getNotes())
                .receivedBy(request.getReceivedBy())
                .paymentDate(LocalDateTime.now())
                .build();

        Payment savedPayment = paymentRepository.save(payment);

        // Update invoice paid amount and status
        invoice.setPaidAmount(invoice.getPaidAmount().add(request.getAmount()));
        invoice.updatePaymentStatus();
        invoiceRepository.save(invoice);

        log.info("Payment recorded with ID: {}", savedPayment.getId());

        return mapToPaymentResponse(savedPayment);
    }

    public BigDecimal getTotalPaymentsByDateRange(LocalDateTime start, LocalDateTime end) {
        BigDecimal total = paymentRepository.getTotalPaymentsByDateRange(start, end);
        return total != null ? total : BigDecimal.ZERO;
    }

    private InvoiceResponse mapToInvoiceResponse(Invoice invoice) {
        List<InvoiceResponse.InvoiceItemResponse> items = invoice.getItems().stream()
                .map(item -> InvoiceResponse.InvoiceItemResponse.builder()
                        .id(item.getId())
                        .description(item.getDescription())
                        .amount(item.getAmount())
                        .quantity(item.getQuantity())
                        .lineTotal(item.getLineTotal())
                        .build())
                .collect(Collectors.toList());

        List<InvoiceResponse.PaymentSummary> payments = invoice.getPayments().stream()
                .map(payment -> InvoiceResponse.PaymentSummary.builder()
                        .id(payment.getId())
                        .amount(payment.getAmount())
                        .paymentMethod(payment.getPaymentMethod())
                        .paymentDate(payment.getPaymentDate())
                        .build())
                .collect(Collectors.toList());

        Patient patient = invoice.getPatient();

        return InvoiceResponse.builder()
                .id(invoice.getId())
                .invoiceNumber(invoice.getInvoiceNumber())
                .appointmentId(invoice.getAppointment() != null ? invoice.getAppointment().getId() : null)
                .patient(InvoiceResponse.PatientSummary.builder()
                        .id(patient.getId())
                        .firstName(patient.getUser().getFirstName())
                        .lastName(patient.getUser().getLastName())
                        .build())
                .totalAmount(invoice.getTotalAmount())
                .paidAmount(invoice.getPaidAmount())
                .remainingBalance(invoice.getRemainingBalance())
                .status(invoice.getStatus())
                .dueDate(invoice.getDueDate())
                .notes(invoice.getNotes())
                .createdAt(invoice.getCreatedAt())
                .items(items)
                .payments(payments)
                .build();
    }

    private PaymentResponse mapToPaymentResponse(Payment payment) {
        return PaymentResponse.builder()
                .id(payment.getId())
                .invoiceId(payment.getInvoice().getId())
                .invoiceNumber(payment.getInvoice().getInvoiceNumber())
                .amount(payment.getAmount())
                .paymentMethod(payment.getPaymentMethod())
                .transactionId(payment.getTransactionId())
                .paymentDate(payment.getPaymentDate())
                .notes(payment.getNotes())
                .receivedBy(payment.getReceivedBy())
                .build();
    }
}
