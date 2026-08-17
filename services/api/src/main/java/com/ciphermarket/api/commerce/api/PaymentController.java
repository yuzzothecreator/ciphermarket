package com.ciphermarket.api.commerce.api;

import com.ciphermarket.api.commerce.payment.MockPaymentProvider;
import com.ciphermarket.api.security.AuthenticatedUser;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/payments")
@Tag(name = "Payments", description = "Payment simulation (mock provider)")
public class PaymentController {

    private final MockPaymentProvider mockPaymentProvider;

    public PaymentController(MockPaymentProvider mockPaymentProvider) {
        this.mockPaymentProvider = mockPaymentProvider;
    }

    @PostMapping("/{paymentId}/simulate")
    @ResponseStatus(HttpStatus.ACCEPTED)
    @Operation(summary = "Simulate mock payment completion (triggers signed webhook)")
    public void simulate(
            @AuthenticationPrincipal AuthenticatedUser user,
            @PathVariable UUID paymentId
    ) {
        mockPaymentProvider.simulatePayment(user, paymentId);
    }
}
