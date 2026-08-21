package com.ciphermarket.api.commerce.service;

import com.ciphermarket.api.commerce.dto.SalesAnalyticsResponse;
import com.ciphermarket.api.commerce.repository.OrderItemRepository;
import com.ciphermarket.api.common.enums.OrganisationRole;
import com.ciphermarket.api.organisation.service.OrganisationService;
import com.ciphermarket.api.security.AuthenticatedUser;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class SalesAnalyticsService {

    private final OrderItemRepository orderItemRepository;
    private final OrganisationService organisationService;

    public SalesAnalyticsService(
            OrderItemRepository orderItemRepository,
            OrganisationService organisationService
    ) {
        this.orderItemRepository = orderItemRepository;
        this.organisationService = organisationService;
    }

    @Transactional(readOnly = true)
    public SalesAnalyticsResponse getSales(AuthenticatedUser user, UUID organisationId) {
        organisationService.requireOrganisationRole(user, organisationId, OrganisationRole.FINANCE_OFFICER);

        var totals = orderItemRepository.aggregatePaidTotals(organisationId);
        List<OrderItemRepository.ProductSalesRow> rows =
                orderItemRepository.aggregatePaidSalesByOrganisation(organisationId);

        String currency = rows.stream()
                .map(OrderItemRepository.ProductSalesRow::getCurrency)
                .filter(c -> c != null && !c.isBlank())
                .findFirst()
                .orElse("USD");

        List<SalesAnalyticsResponse.ProductSalesBreakdown> products = rows.stream()
                .map(row -> new SalesAnalyticsResponse.ProductSalesBreakdown(
                        row.getProductId(),
                        row.getProductName(),
                        row.getUnitsSold() == null ? 0L : row.getUnitsSold(),
                        row.getRevenueCents() == null ? 0L : row.getRevenueCents(),
                        row.getCurrency() == null ? currency : row.getCurrency()
                ))
                .toList();

        long paidOrders = totals == null || totals.getPaidOrderCount() == null ? 0L : totals.getPaidOrderCount();
        long units = totals == null || totals.getUnitsSold() == null ? 0L : totals.getUnitsSold();
        long revenue = totals == null || totals.getRevenueCents() == null ? 0L : totals.getRevenueCents();

        return new SalesAnalyticsResponse(paidOrders, units, revenue, currency, products);
    }
}
