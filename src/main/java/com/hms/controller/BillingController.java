package com.hms.controller;

import com.hms.dto.request.InvoiceRequest;
import com.hms.dto.request.PaymentRequest;
import com.hms.dto.response.ApiResponse;
import com.hms.dto.response.InvoiceResponse;
import com.hms.dto.response.PaymentResponse;
import com.hms.enums.PaymentStatus;
import com.hms.service.BillingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/billing")
@RequiredArgsConstructor
@Slf4j
@PreAuthorize("hasAnyRole('ADMIN', 'RECEPTIONIST')")
@Tag(name = "Billing", description = "Invoice and payment management APIs")
public class BillingController {

    private final BillingService billingService;

    // Invoice Endpoints
    @GetMapping("/invoices")
    @Operation(summary = "Get all invoices", description = "Retrieves list of all invoices")
    public ResponseEntity<ApiResponse<List<InvoiceResponse>>> getAllInvoices() {
        log.info("GET /api/v1/billing/invoices");
        List<InvoiceResponse> invoices = billingService.getAllInvoices();
        return ResponseEntity.ok(ApiResponse.success(invoices));
    }

    @GetMapping("/invoices/{id}")
    @Operation(summary = "Get invoice by ID", description = "Retrieves a specific invoice by ID")
    public ResponseEntity<ApiResponse<InvoiceResponse>> getInvoiceById(@PathVariable Long id) {
        log.info("GET /api/v1/billing/invoices/{}", id);
        InvoiceResponse invoice = billingService.getInvoiceById(id);
        return ResponseEntity.ok(ApiResponse.success(invoice));
    }

    @GetMapping("/invoices/number/{invoiceNumber}")
    @Operation(summary = "Get invoice by number", description = "Retrieves an invoice by its invoice number")
    public ResponseEntity<ApiResponse<InvoiceResponse>> getInvoiceByNumber(
            @PathVariable String invoiceNumber) {
        log.info("GET /api/v1/billing/invoices/number/{}", invoiceNumber);
        InvoiceResponse invoice = billingService.getInvoiceByNumber(invoiceNumber);
        return ResponseEntity.ok(ApiResponse.success(invoice));
    }

    @GetMapping("/invoices/patient/{patientId}")
    @Operation(summary = "Get invoices by patient", description = "Retrieves all invoices for a patient")
    public ResponseEntity<ApiResponse<List<InvoiceResponse>>> getInvoicesByPatient(
            @PathVariable Long patientId) {
        log.info("GET /api/v1/billing/invoices/patient/{}", patientId);
        List<InvoiceResponse> invoices = billingService.getInvoicesByPatient(patientId);
        return ResponseEntity.ok(ApiResponse.success(invoices));
    }

    @GetMapping("/invoices/status/{status}")
    @Operation(summary = "Get invoices by status", description = "Retrieves invoices by payment status")
    public ResponseEntity<ApiResponse<List<InvoiceResponse>>> getInvoicesByStatus(
            @PathVariable PaymentStatus status) {
        log.info("GET /api/v1/billing/invoices/status/{}", status);
        List<InvoiceResponse> invoices = billingService.getInvoicesByStatus(status);
        return ResponseEntity.ok(ApiResponse.success(invoices));
    }

    @GetMapping("/invoices/overdue")
    @Operation(summary = "Get overdue invoices", description = "Retrieves all overdue invoices")
    public ResponseEntity<ApiResponse<List<InvoiceResponse>>> getOverdueInvoices() {
        log.info("GET /api/v1/billing/invoices/overdue");
        List<InvoiceResponse> invoices = billingService.getOverdueInvoices();
        return ResponseEntity.ok(ApiResponse.success(invoices));
    }

    @PostMapping("/invoices")
    @Operation(summary = "Create invoice", description = "Creates a new invoice")
    public ResponseEntity<ApiResponse<InvoiceResponse>> createInvoice(
            @Valid @RequestBody InvoiceRequest request) {
        log.info("POST /api/v1/billing/invoices - Creating new invoice");
        InvoiceResponse invoice = billingService.createInvoice(request);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("Invoice created successfully", invoice));
    }

    @PutMapping("/invoices/{id}")
    @Operation(summary = "Update invoice", description = "Updates an existing invoice")
    public ResponseEntity<ApiResponse<InvoiceResponse>> updateInvoice(
            @PathVariable Long id,
            @Valid @RequestBody InvoiceRequest request) {
        log.info("PUT /api/v1/billing/invoices/{}", id);
        InvoiceResponse invoice = billingService.updateInvoice(id, request);
        return ResponseEntity.ok(ApiResponse.success("Invoice updated successfully", invoice));
    }

    @DeleteMapping("/invoices/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Cancel invoice", description = "Cancels an invoice (Admin only)")
    public ResponseEntity<ApiResponse<Void>> cancelInvoice(@PathVariable Long id) {
        log.info("DELETE /api/v1/billing/invoices/{}", id);
        billingService.cancelInvoice(id);
        return ResponseEntity.ok(ApiResponse.success("Invoice cancelled successfully"));
    }

    // Payment Endpoints
    @GetMapping("/invoices/{invoiceId}/payments")
    @Operation(summary = "Get payments for invoice", description = "Retrieves all payments for an invoice")
    public ResponseEntity<ApiResponse<List<PaymentResponse>>> getPaymentsByInvoice(
            @PathVariable Long invoiceId) {
        log.info("GET /api/v1/billing/invoices/{}/payments", invoiceId);
        List<PaymentResponse> payments = billingService.getPaymentsByInvoice(invoiceId);
        return ResponseEntity.ok(ApiResponse.success(payments));
    }

    @PostMapping("/payments")
    @Operation(summary = "Record payment", description = "Records a new payment for an invoice")
    public ResponseEntity<ApiResponse<PaymentResponse>> recordPayment(
            @Valid @RequestBody PaymentRequest request) {
        log.info("POST /api/v1/billing/payments - Recording payment for invoice {}", request.getInvoiceId());
        PaymentResponse payment = billingService.recordPayment(request);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("Payment recorded successfully", payment));
    }
}
