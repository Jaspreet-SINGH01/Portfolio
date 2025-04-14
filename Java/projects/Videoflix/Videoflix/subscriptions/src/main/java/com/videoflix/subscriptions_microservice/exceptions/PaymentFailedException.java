package com.videoflix.subscriptions_microservice.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.PAYMENT_REQUIRED) // Ou un autre statut plus approprié comme BAD_REQUEST ou CONFLICT
public class PaymentFailedException extends RuntimeException {

    private final String paymentErrorCode; // Code d'erreur spécifique du fournisseur de paiement (facultatif)
    private final String paymentErrorMessage; // Message d'erreur détaillé du fournisseur de paiement (facultatif)

    public PaymentFailedException(String message) {
        super(message);
        this.paymentErrorCode = "";
        this.paymentErrorMessage = "";
    }

    public PaymentFailedException(String message, Throwable cause) {
        super(message, cause);
        this.paymentErrorCode = "";
        this.paymentErrorMessage = "";
    }

    public PaymentFailedException(String message, String paymentErrorCode, String paymentErrorMessage) {
        super(message);
        this.paymentErrorCode = paymentErrorCode;
        this.paymentErrorMessage = paymentErrorMessage;
    }

    public PaymentFailedException(String message, Throwable cause, String paymentErrorCode,
            String paymentErrorMessage) {
        super(message, cause);
        this.paymentErrorCode = paymentErrorCode;
        this.paymentErrorMessage = paymentErrorMessage;
    }

    public String getPaymentErrorCode() {
        return paymentErrorCode;
    }

    public String getPaymentErrorMessage() {
        return paymentErrorMessage;
    }
}