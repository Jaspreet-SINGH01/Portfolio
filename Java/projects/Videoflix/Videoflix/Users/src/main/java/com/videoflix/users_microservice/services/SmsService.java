package com.videoflix.users_microservice.services;

import com.twilio.Twilio;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class SmsService {

    @Value("${twilio.account.sid}")
    private String accountSid; // SID du compte Twilio, injecté depuis les propriétés de l'application.

    @Value("${twilio.auth.token}")
    private String authToken; // Token d'authentification Twilio, injecté depuis les propriétés de
                              // l'application.

    @Value("${twilio.phone.number}")
    private String twilioPhoneNumber; // Numéro de téléphone Twilio, injecté depuis les propriétés de l'application.

    /**
     * Envoie un code de vérification par SMS à un numéro de téléphone donné.
     *
     * @param to   Le numéro de téléphone du destinataire au format E.164.
     * @param code Le code de vérification à envoyer.
     */
    public void sendVerificationCode(String to, String code) {
        // Initialise le client Twilio avec les informations d'identification du compte.
        Twilio.init(accountSid, authToken);

        // Crée et envoie un message SMS en utilisant l'API Twilio.
        Message.creator(
                new PhoneNumber(to), // Numéro de téléphone du destinataire.
                new PhoneNumber(twilioPhoneNumber), // Numéro de téléphone Twilio utilisé pour envoyer le SMS.
                "Votre code de vérification Videoflix est : " + code // Corps du message SMS.
        ).create(); // Envoie le message.
    }
}