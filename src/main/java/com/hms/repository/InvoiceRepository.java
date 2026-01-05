package com.hms.repository;

import com.hms.entity.Invoice;
import com.hms.enums.PaymentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface InvoiceRepository extends JpaRepository<Invoice, Long> {

    Optional<Invoice> findByInvoiceNumber(String invoiceNumber);

    List<Invoice> findByPatientId(Long patientId);

    List<Invoice> findByStatus(PaymentStatus status);

    List<Invoice> findByPatientIdAndStatus(Long patientId, PaymentStatus status);

    Optional<Invoice> findByAppointmentId(Long appointmentId);

    @Query("SELECT i FROM Invoice i WHERE i.dueDate < :date AND i.status NOT IN ('PAID', 'CANCELLED')")
    List<Invoice> findOverdueInvoices(@Param("date") LocalDate date);

    @Query("SELECT SUM(i.totalAmount) FROM Invoice i WHERE i.patient.id = :patientId AND i.status = 'PAID'")
    java.math.BigDecimal getTotalPaidByPatient(@Param("patientId") Long patientId);
}
