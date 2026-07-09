package com.pia.telekom.repository;

import com.pia.telekom.dto.InvoiceStatRow;
import com.pia.telekom.dto.RechargeStatRow;
import com.pia.telekom.entity.Invoice;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Repository
public interface InvoiceRepository extends JpaRepository<Invoice, Integer> {

    Page<Invoice> findByCustomer_CustomerId(Integer customerId, Pageable pageable);

    boolean existsByInvoiceIdAndCustomer_CustomerId(Integer invoiceId, Integer customerId);

    @Query("""
        SELECT i.customer.customerId, COUNT(i)
        FROM Invoice i
        WHERE i.paymentDate IS NULL
          AND i.dueDate < :today
          AND i.invoiceDate > :since
          AND i.customer.customerId IN :customerIds
        GROUP BY i.customer.customerId
        """)
    List<Object[]> countOverdueGroupedByCustomerIds(
            @Param("today") LocalDate today,
            @Param("since") LocalDate since,
            @Param("customerIds") List<Integer> customerIds);

    @Query("""
        SELECT i FROM Invoice i
        JOIN FETCH i.customer c
        JOIN FETCH c.region
        """)
    List<Invoice> findAllWithCustomerAndRegion();

    @Query(value = "SELECT i FROM Invoice i JOIN FETCH i.customer c LEFT JOIN FETCH i.product",
            countQuery = "SELECT COUNT(i) FROM Invoice i")
    Page<Invoice> findAllWithCustomer(Pageable pageable);

    long countByPaymentDateIsNullAndDueDateBefore(LocalDate date);

    @Query("""
        SELECT i.customer.customerId
        FROM Invoice i
        WHERE i.paymentDate IS NULL AND i.dueDate < :today AND i.invoiceDate > :since
        GROUP BY i.customer.customerId
        HAVING COUNT(i) >= :threshold
        """)
    List<Integer> findCustomerIdsWithOverdueCountAtLeast(
            @Param("today") LocalDate today,
            @Param("since") LocalDate since,
            @Param("threshold") long threshold);

    @Query("SELECT MAX(i.invoiceDate) FROM Invoice i")
    LocalDate findMaxInvoiceDate();

    @Query("SELECT COALESCE(SUM(i.invoiceAmount), 0) FROM Invoice i WHERE i.invoiceDate BETWEEN :start AND :end")
    BigDecimal sumInvoiceAmountBetween(@Param("start") LocalDate start, @Param("end") LocalDate end);

    @Query("""
    SELECT i FROM Invoice i
    JOIN FETCH i.customer
    LEFT JOIN FETCH i.product
    WHERE i.overageAmount IS NOT NULL AND i.overageAmount > 0
      AND i.invoiceDate > :since
    """)
    List<Invoice> findOverageInvoicesSince(@Param("since") LocalDate since);


    @Query("""
    SELECT new com.pia.telekom.dto.InvoiceStatRow(
        i.customer.customerId, i.invoiceDate, i.dueDate, i.paymentDate, i.overageAmount)
    FROM Invoice i
    """)
    List<InvoiceStatRow> findAllForStats();

    @Query(value = """
    SELECT AVG(sub.total_unpaid) FROM (
        SELECT SUM(due_amount) as total_unpaid
        FROM invoice
        WHERE payment_date IS NULL
        GROUP BY customer_id
    ) sub
    """, nativeQuery = true)
    BigDecimal averageUnpaidValuePerCustomer();

    @Query(value = """
    SELECT i FROM Invoice i
    JOIN FETCH i.customer c
    LEFT JOIN FETCH i.product
    WHERE (:query IS NULL OR :query = ''
        OR LOWER(CONCAT(c.name, ' ', c.surname)) LIKE LOWER(CONCAT('%', :query, '%'))
    )
    """,
            countQuery = """
    SELECT COUNT(i) FROM Invoice i
    JOIN i.customer c
    WHERE (:query IS NULL OR :query = ''
        OR LOWER(CONCAT(c.name, ' ', c.surname)) LIKE LOWER(CONCAT('%', :query, '%'))
    )
    """)
    Page<Invoice> searchInvoices(@Param("query") String query, Pageable pageable);
}