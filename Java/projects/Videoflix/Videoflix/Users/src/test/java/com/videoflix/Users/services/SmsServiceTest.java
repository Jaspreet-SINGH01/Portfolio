package com.videoflix.Users.services;

import com.twilio.Twilio;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;
import com.videoflix.users_microservice.services.SmsService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SmsServiceTest {

    private SmsService smsService;

    private static final String TEST_ACCOUNT_SID = "testAccountSid";
    private static final String TEST_AUTH_TOKEN = "testAuthToken";
    private static final String TEST_TWILIO_PHONE_NUMBER = "+1234567890";
    private static final String TEST_RECIPIENT_PHONE = "+0987654321";
    private static final String TEST_VERIFICATION_CODE = "123456";

    @BeforeEach
    void setUp() {
        // Créer une nouvelle instance de SmsService pour chaque test
        smsService = new SmsService();

        // Injecter les valeurs de configuration via ReflectionTestUtils
        ReflectionTestUtils.setField(smsService, "accountSid", TEST_ACCOUNT_SID);
        ReflectionTestUtils.setField(smsService, "authToken", TEST_AUTH_TOKEN);
        ReflectionTestUtils.setField(smsService, "twilioPhoneNumber", TEST_TWILIO_PHONE_NUMBER);
    }

    @Test
    void testSendVerificationCode_Success() {
        // Utiliser un mock statique pour Twilio.init()
        try (MockedStatic<Twilio> twilioMockedStatic = Mockito.mockStatic(Twilio.class);
             MockedStatic<Message> messageMockedStatic = Mockito.mockStatic(Message.class)) {

            // Créer un message simulé
            Message mockMessage = mock(Message.class);

            // Configurer le comportement du créateur de messages
            messageMockedStatic
                .when(() -> Message.creator(
                    any(PhoneNumber.class), 
                    any(PhoneNumber.class), 
                    Mockito.contains(TEST_VERIFICATION_CODE)
                ))
                .thenReturn(mockMessage);

            // Appeler la méthode à tester
            smsService.sendVerificationCode(TEST_RECIPIENT_PHONE, TEST_VERIFICATION_CODE);

            // Vérifications
            twilioMockedStatic.verify(() -> Twilio.init(TEST_ACCOUNT_SID, TEST_AUTH_TOKEN), times(1));
            messageMockedStatic.verify(() -> Message.creator(
                new PhoneNumber(TEST_RECIPIENT_PHONE), 
                new PhoneNumber(TEST_TWILIO_PHONE_NUMBER), 
                "Votre code de vérification Videoflix est : " + TEST_VERIFICATION_CODE
            ), times(1));
        }
    }

    @Test
    void testSendVerificationCode_MessageCreation() {
        // Vérifier que le message est correctement construit
        try (MockedStatic<Twilio> twilioMockedStatic = Mockito.mockStatic(Twilio.class);
             MockedStatic<Message> messageMockedStatic = Mockito.mockStatic(Message.class)) {

            // Appeler la méthode à tester
            smsService.sendVerificationCode(TEST_RECIPIENT_PHONE, TEST_VERIFICATION_CODE);

            // Vérifications détaillées
            messageMockedStatic.verify(() -> Message.creator(
                new PhoneNumber(TEST_RECIPIENT_PHONE),
                new PhoneNumber(TEST_TWILIO_PHONE_NUMBER),
                "Votre code de vérification Videoflix est : " + TEST_VERIFICATION_CODE
            ), times(1));
        }
    }

    @Test
    void testSendVerificationCode_TwilioInitialization() {
        // Vérifier l'initialisation de Twilio
        try (MockedStatic<Twilio> twilioMockedStatic = Mockito.mockStatic(Twilio.class)) {
            smsService.sendVerificationCode(TEST_RECIPIENT_PHONE, TEST_VERIFICATION_CODE);

            // Vérifier que Twilio.init() est appelé avec les bons paramètres
            twilioMockedStatic.verify(() -> Twilio.init(TEST_ACCOUNT_SID, TEST_AUTH_TOKEN), times(1));
        }
    }
}