package com.ciphermarket.api.commerce.service;

import com.ciphermarket.api.audit.service.AuditService;
import com.ciphermarket.api.commerce.domain.CartItem;
import com.ciphermarket.api.commerce.domain.Order;
import com.ciphermarket.api.commerce.domain.OrderItem;
import com.ciphermarket.api.commerce.domain.Payment;
import com.ciphermarket.api.commerce.dto.CheckoutResponse;
import com.ciphermarket.api.commerce.payment.MockPaymentProvider;
import com.ciphermarket.api.commerce.repository.OrderItemRepository;
import com.ciphermarket.api.commerce.repository.OrderRepository;
import com.ciphermarket.api.commerce.repository.PaymentRepository;
import com.ciphermarket.api.common.enums.ProductStatus;
import com.ciphermarket.api.config.PaymentProperties;
import com.ciphermarket.api.identity.domain.UserProfile;
import com.ciphermarket.api.identity.service.UserProfileService;
import com.ciphermarket.api.product.domain.Product;
import com.ciphermarket.api.product.repository.ProductRepository;
import com.ciphermarket.api.security.AuthenticatedUser;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class CheckoutService {

    private final CartService cartService;
    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final PaymentRepository paymentRepository;
    private final ProductRepository productRepository;
    private final UserProfileService userProfileService;
    private final MockPaymentProvider mockPaymentProvider;
    private final PaymentProperties paymentProperties;
    private final AuditService auditService;

    public CheckoutService(
            CartService cartService,
            OrderRepository orderRepository,
            OrderItemRepository orderItemRepository,
            PaymentRepository paymentRepository,
            ProductRepository productRepository,
            UserProfileService userProfileService,
            MockPaymentProvider mockPaymentProvider,
            PaymentProperties paymentProperties,
            AuditService auditService
    ) {
        this.cartService = cartService;
        this.orderRepository = orderRepository;
        this.orderItemRepository = orderItemRepository;
        this.paymentRepository = paymentRepository;
        this.productRepository = productRepository;
        this.userProfileService = userProfileService;
        this.mockPaymentProvider = mockPaymentProvider;
        this.paymentProperties = paymentProperties;
        this.auditService = auditService;
    }

    @Transactional
    public CheckoutResponse checkout(AuthenticatedUser user) {
        UserProfile profile = userProfileService.requireProfileEntity(user.keycloakSub());
        List<CartItem> cartItems = cartService.requireNonEmptyCartItems(profile.getId());

        long subtotal = 0;
        String currency = "USD";
        List<ResolvedCartLine> lines = new ArrayList<>();

        for (CartItem cartItem : cartItems) {
            Product product = productRepository.findByIdAndStatus(cartItem.getProductId(), ProductStatus.PUBLISHED)
                    .orElseThrow(() -> new IllegalArgumentException(
                            "Product no longer available: " + cartItem.getProductId()));

            long lineTotal = product.getPriceCents() * cartItem.getQuantity();
            subtotal += lineTotal;
            currency = product.getCurrency();
            lines.add(new ResolvedCartLine(product, cartItem.getQuantity()));
        }

        Order order = orderRepository.save(new Order(profile.getId(), subtotal, currency));

        for (ResolvedCartLine line : lines) {
            Product product = line.product();
            orderItemRepository.save(new OrderItem(
                    order.getId(),
                    product.getId(),
                    product.getOrganisationId(),
                    product.getName(),
                    product.getSlug(),
                    product.getPriceCents(),
                    product.getCurrency(),
                    line.quantity()
            ));
        }

        String externalReference = "mock_" + UUID.randomUUID();
        Payment payment = paymentRepository.save(new Payment(
                order.getId(),
                paymentProperties.mockProviderName(),
                externalReference,
                subtotal,
                currency
        ));

        cartService.clearCart(profile.getId());

        auditService.record(
                null,
                profile.getId(),
                user.keycloakSub(),
                "ORDER_CREATED",
                "Order",
                order.getId(),
                null,
                Map.of("subtotalCents", subtotal, "currency", currency)
        );

        String checkoutUrl = paymentProperties.appBaseUrl() + "/checkout/pay?paymentId=" + payment.getId();

        if (subtotal == 0) {
            mockPaymentProvider.deliverSucceededWebhook(payment);
            return new CheckoutResponse(
                    order.getId(),
                    payment.getId(),
                    payment.getProvider(),
                    subtotal,
                    currency,
                    checkoutUrl,
                    false
            );
        }

        return new CheckoutResponse(
                order.getId(),
                payment.getId(),
                payment.getProvider(),
                subtotal,
                currency,
                checkoutUrl,
                true
        );
    }

    private record ResolvedCartLine(Product product, int quantity) {
    }
}
