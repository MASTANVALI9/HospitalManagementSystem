package com.hms.service;

import com.hms.dto.request.InvoiceRequest;
import com.hms.dto.request.PaymentRequest;
import com.hms.dto.response.InvoiceResponse;
import com.hms.dto.response.PaymentResponse;
import com.hms.entity.Invoice;
import com.hms.entity.Patient;
import com.hms.entity.Payment;
import com.hms.entity.User;
import com.hms.enums.PaymentStatus;
import com.hms.exception.BadRequestException;
import com.hms.exception.ResourceNotFoundException;
import com.hms.repository.AppointmentRepository;
import com.hms.repository.InvoiceRepository;
import com.hms.repository.PatientRepository;
import com.hms.repository.PaymentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BillingServiceTest {

    @Mock
    private InvoiceRepository invoiceRepository;

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private PatientRepository patientRepository;

    @Mock
    private AppointmentRepository appointmentRepository;

    @InjectMocks
    private BillingService billingService;

    private Patient testPatient;
    private Invoice testInvoice;
    private User patientUser;

    @BeforeEach
    void setUp() {
        patientUser = User.builder()
                .id(1L)
                .email("patient@test.com")
                .firstName("Jane")
                .lastName("Doe")
                .build();

        testPatient = Patient.builder()
                .id(1L)
                .user(patientUser)
                .build();

        testInvoice = Invoice.builder()
                .id(1L)
                .invoiceNumber("INV-12345")
                .patient(testPatient)
                .totalAmount(new BigDecimal("500.00"))
                .paidAmount(BigDecimal.ZERO)
                .status(PaymentStatus.PENDING)
                .items(new ArrayList<>())
                .payments(new ArrayList<>())
                .build();
    }

    @Test
    @DisplayName("Should get all invoices")
    void getAllInvoices_Success() {
        // Given
        when(invoiceRepository.findAll()).thenReturn(Arrays.asList(testInvoice));

        // When
        List<InvoiceResponse> result = billingService.getAllInvoices();

        // Then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getInvoiceNumber()).isEqualTo("INV-12345");
    }

    @Test
    @DisplayName("Should get invoice by ID")
    void getInvoiceById_Success() {
        // Given
        when(invoiceRepository.findById(1L)).thenReturn(Optional.of(testInvoice));

        // When
        InvoiceResponse result = billingService.getInvoiceById(1L);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getTotalAmount()).isEqualByComparingTo(new BigDecimal("500.00"));
    }

    @Test
    @DisplayName("Should throw exception when invoice not found")
    void getInvoiceById_NotFound_ThrowsException() {
        // Given
        when(invoiceRepository.findById(anyLong())).thenReturn(Optional.empty());

        // When/Then
        assertThatThrownBy(() -> billingService.getInvoiceById(99L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    @DisplayName("Should create invoice successfully")
    void createInvoice_Success() {
        // Given
        InvoiceRequest.InvoiceItemRequest itemRequest = InvoiceRequest.InvoiceItemRequest.builder()
                .description("Consultation")
                .amount(new BigDecimal("500.00"))
                .quantity(1)
                .build();

        InvoiceRequest request = InvoiceRequest.builder()
                .patientId(1L)
                .items(Arrays.asList(itemRequest))
                .build();

        when(patientRepository.findById(1L)).thenReturn(Optional.of(testPatient));
        when(invoiceRepository.save(any(Invoice.class))).thenReturn(testInvoice);

        // When
        InvoiceResponse result = billingService.createInvoice(request);

        // Then
        assertThat(result).isNotNull();
        verify(invoiceRepository).save(any(Invoice.class));
    }

    @Test
    @DisplayName("Should record payment successfully")
    void recordPayment_Success() {
        // Given
        PaymentRequest request = PaymentRequest.builder()
                .invoiceId(1L)
                .amount(new BigDecimal("200.00"))
                .paymentMethod("CASH")
                .build();

        Payment savedPayment = Payment.builder()
                .id(1L)
                .invoice(testInvoice)
                .amount(new BigDecimal("200.00"))
                .paymentMethod("CASH")
                .paymentDate(LocalDateTime.now())
                .build();

        when(invoiceRepository.findById(1L)).thenReturn(Optional.of(testInvoice));
        when(paymentRepository.save(any(Payment.class))).thenReturn(savedPayment);
        when(invoiceRepository.save(any(Invoice.class))).thenReturn(testInvoice);

        // When
        PaymentResponse result = billingService.recordPayment(request);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getAmount()).isEqualByComparingTo(new BigDecimal("200.00"));
        verify(paymentRepository).save(any(Payment.class));
    }

    @Test
    @DisplayName("Should throw exception when payment exceeds balance")
    void recordPayment_ExceedsBalance_ThrowsException() {
        // Given
        PaymentRequest request = PaymentRequest.builder()
                .invoiceId(1L)
                .amount(new BigDecimal("1000.00"))
                .paymentMethod("CASH")
                .build();

        when(invoiceRepository.findById(1L)).thenReturn(Optional.of(testInvoice));

        // When/Then
        assertThatThrownBy(() -> billingService.recordPayment(request))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("exceeds remaining balance");
    }

    @Test
    @DisplayName("Should throw exception when paying cancelled invoice")
    void recordPayment_CancelledInvoice_ThrowsException() {
        // Given
        testInvoice.setStatus(PaymentStatus.CANCELLED);
        PaymentRequest request = PaymentRequest.builder()
                .invoiceId(1L)
                .amount(new BigDecimal("100.00"))
                .paymentMethod("CASH")
                .build();

        when(invoiceRepository.findById(1L)).thenReturn(Optional.of(testInvoice));

        // When/Then
        assertThatThrownBy(() -> billingService.recordPayment(request))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("cancelled");
    }

    @Test
    @DisplayName("Should cancel invoice successfully")
    void cancelInvoice_Success() {
        // Given
        when(invoiceRepository.findById(1L)).thenReturn(Optional.of(testInvoice));
        when(invoiceRepository.save(any(Invoice.class))).thenReturn(testInvoice);

        // When
        billingService.cancelInvoice(1L);

        // Then
        verify(invoiceRepository).save(any(Invoice.class));
    }

    @Test
    @DisplayName("Should throw exception when cancelling paid invoice")
    void cancelInvoice_PaidInvoice_ThrowsException() {
        // Given
        testInvoice.setStatus(PaymentStatus.PAID);
        when(invoiceRepository.findById(1L)).thenReturn(Optional.of(testInvoice));

        // When/Then
        assertThatThrownBy(() -> billingService.cancelInvoice(1L))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("paid");
    }
}
