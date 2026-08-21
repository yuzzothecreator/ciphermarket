package com.ciphermarket.api.commerce.repository;

import com.ciphermarket.api.commerce.domain.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface OrderItemRepository extends JpaRepository<OrderItem, UUID> {

    List<OrderItem> findByOrderIdOrderByCreatedAtAsc(UUID orderId);

    @Query(value = """
            SELECT oi.product_id AS productId,
                   oi.product_name AS productName,
                   CAST(SUM(oi.quantity) AS BIGINT) AS unitsSold,
                   CAST(SUM(oi.line_total_cents) AS BIGINT) AS revenueCents,
                   oi.currency AS currency
            FROM order_items oi
            INNER JOIN orders o ON o.id = oi.order_id
            WHERE oi.organisation_id = :organisationId
              AND o.status = 'PAID'
            GROUP BY oi.product_id, oi.product_name, oi.currency
            ORDER BY SUM(oi.line_total_cents) DESC
            """, nativeQuery = true)
    List<ProductSalesRow> aggregatePaidSalesByOrganisation(@Param("organisationId") UUID organisationId);

    @Query(value = """
            SELECT CAST(COUNT(DISTINCT o.id) AS BIGINT) AS paidOrderCount,
                   CAST(COALESCE(SUM(oi.quantity), 0) AS BIGINT) AS unitsSold,
                   CAST(COALESCE(SUM(oi.line_total_cents), 0) AS BIGINT) AS revenueCents
            FROM order_items oi
            INNER JOIN orders o ON o.id = oi.order_id
            WHERE oi.organisation_id = :organisationId
              AND o.status = 'PAID'
            """, nativeQuery = true)
    SalesTotalsRow aggregatePaidTotals(@Param("organisationId") UUID organisationId);

    interface ProductSalesRow {
        UUID getProductId();

        String getProductName();

        Long getUnitsSold();

        Long getRevenueCents();

        String getCurrency();
    }

    interface SalesTotalsRow {
        Long getPaidOrderCount();

        Long getUnitsSold();

        Long getRevenueCents();
    }
}
