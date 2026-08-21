package com.ciphermarket.api.commerce.service;

import com.ciphermarket.api.commerce.domain.Order;
import com.ciphermarket.api.commerce.domain.OrderItem;
import com.ciphermarket.api.commerce.domain.RefundRequest;
import com.ciphermarket.api.identity.repository.UserProfileRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class OrderNotificationService {

    private static final Logger log = LoggerFactory.getLogger(OrderNotificationService.class);

    private final JavaMailSender mailSender;
    private final UserProfileRepository userProfileRepository;

    public OrderNotificationService(JavaMailSender mailSender, UserProfileRepository userProfileRepository) {
        this.mailSender = mailSender;
        this.userProfileRepository = userProfileRepository;
    }

    public void sendOrderReceipt(Order order, List<OrderItem> items) {
        userProfileRepository.findById(order.getBuyerUserId()).ifPresent(profile -> {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(profile.getEmail());
            message.setSubject("CipherMarket order confirmation");
            message.setText(buildReceiptBody(order, items));
            try {
                mailSender.send(message);
            } catch (Exception e) {
                log.warn("Mail send failed for order {}: {}", order.getId(), e.getMessage());
            }
        });
    }

    public void sendRefundDecision(RefundRequest refund, boolean approved) {
        userProfileRepository.findById(refund.getBuyerUserId()).ifPresent(profile -> {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(profile.getEmail());
            message.setSubject(approved
                    ? "CipherMarket refund completed"
                    : "CipherMarket refund request update");
            String body = approved
                    ? """
                    Your refund request has been approved and completed.

                    Order ID: %s
                    Amount: %s
                    Reference: %s

                    Related entitlements and licences have been revoked.
                    """.formatted(
                            refund.getOrderId(),
                            formatMoney(refund.getAmountCents(), refund.getCurrency()),
                            refund.getProviderRefundRef()
                    )
                    : """
                    Your refund request was not approved.

                    Order ID: %s
                    Reason: %s
                    """.formatted(
                            refund.getOrderId(),
                            refund.getRejectionReason() != null ? refund.getRejectionReason() : "Not specified"
                    );
            message.setText(body);
            try {
                mailSender.send(message);
            } catch (Exception e) {
                log.warn("Refund mail failed for {}: {}", refund.getId(), e.getMessage());
            }
        });
    }

    private String buildReceiptBody(Order order, List<OrderItem> items) {
        StringBuilder body = new StringBuilder();
        body.append("Thank you for your purchase on CipherMarket.\n\n");
        body.append("Order ID: ").append(order.getId()).append("\n");
        body.append("Total: ").append(formatMoney(order.getSubtotalCents(), order.getCurrency())).append("\n\n");
        body.append("Items:\n");
        for (OrderItem item : items) {
            body.append("- ").append(item.getProductName())
                    .append(" x").append(item.getQuantity())
                    .append(" — ").append(formatMoney(item.getLineTotalCents(), item.getCurrency()))
                    .append("\n");
        }
        body.append("\nYour entitlements are now active. Visit the buyer portal to view purchases.\n");
        return body.toString();
    }

    private String formatMoney(long cents, String currency) {
        return String.format("%s %.2f", currency, cents / 100.0);
    }
}
