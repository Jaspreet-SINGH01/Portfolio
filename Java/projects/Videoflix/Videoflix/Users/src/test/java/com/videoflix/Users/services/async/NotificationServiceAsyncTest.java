package com.videoflix.Users.services.async;

import com.videoflix.users_microservice.services.EmailService;
import com.videoflix.users_microservice.services.SmsService;
import com.videoflix.users_microservice.services.async.NotificationServiceAsync;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.Logger;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class) // Active les annotations Mockito
class NotificationServiceAsyncTest {

    @Mock
    private EmailService emailService; // Mock pour EmailService

    @Mock
    private SmsService smsService; // Mock pour SmsService

    @Mock
    private Logger logger; // Mock pour le Logger

    @InjectMocks
    private NotificationServiceAsync notificationServiceAsync; // Injection des mocks dans NotificationServiceAsync

    private String to;
    private String username;
    private String code;

    @BeforeEach
    void setUp() {
        to = "test@example.com";
        username = "testUser";
        code = "123456";
    }

    @Test
    void sendWelcomeEmailAsync_ShouldCallEmailService_WhenSuccessful() {
        // Configuration du mock
        doNothing().when(emailService).sendWelcomeEmail(to, username);

        // Appel de la méthode à tester
        NotificationServiceAsync.sendWelcomeEmailAsync(to, username);

        // Vérification que la méthode sendWelcomeEmail de EmailService a été appelée
        verify(emailService, times(1)).sendWelcomeEmail(to, username);
    }

    @Test
    void sendWelcomeEmailAsync_ShouldLogError_WhenExceptionOccurs() {
        // Configuration du mock pour lancer une exception
        doThrow(new RuntimeException("Email send failed")).when(emailService).sendWelcomeEmail(to, username);

        // Appel de la méthode à tester
        NotificationServiceAsync.sendWelcomeEmailAsync(to, username);

        // Vérification que la méthode error du logger a été appelée
        verify(logger, times(1)).error(anyString(), eq(to), any(RuntimeException.class));
    }

    @Test
    void sendVerificationCodeAsync_ShouldCallSmsService_WhenSuccessful() {
        // Appel de la méthode à tester
        notificationServiceAsync.sendVerificationCodeAsync(to, code);

        // Vérification que la méthode sendVerificationCode de SmsService a été appelée
        verify(smsService, times(1)).sendVerificationCode(to, code);
    }

    @Test
    void sendVerificationCodeAsync_ShouldLogError_WhenExceptionOccurs() {
        // Configuration du mock pour lancer une exception
        doThrow(new RuntimeException("SMS send failed")).when(smsService).sendVerificationCode(to, code);

        // Appel de la méthode à tester
        notificationServiceAsync.sendVerificationCodeAsync(to, code);

        // Vérification que la méthode error du logger a été appelée
        verify(logger, times(1)).error(anyString(), eq(to), any(RuntimeException.class));
    }
}