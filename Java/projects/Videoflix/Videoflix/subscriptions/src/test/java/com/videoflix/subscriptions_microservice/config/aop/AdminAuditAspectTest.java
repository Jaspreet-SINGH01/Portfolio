package com.videoflix.subscriptions_microservice.config.aop;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.Signature;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AdminAuditAspectTest {

    @InjectMocks
    private AdminAuditAspect adminAuditAspect;

    @Mock
    private JoinPoint joinPoint;

    @Mock
    private Signature signature;

    @Mock
    private Authentication authentication;

    @Mock
    private SecurityContext securityContext;

    @Mock
    private Logger auditLogger; // Mock spécifique pour "audit.admin"

    @Test
    void logAdminAction_shouldLogAuditInformationWithAuthenticatedUser() {
        // GIVEN : Un JoinPoint pour une méthode annotée avec @AdminAction et un
        // utilisateur authentifié
        String methodName = "testAdminMethod";
        Object[] args = new Object[] { "arg1", 123 };

        when(joinPoint.getSignature()).thenReturn(signature);
        when(signature.getName()).thenReturn(methodName);
        when(joinPoint.getArgs()).thenReturn(args);

        // Mock du SecurityContextHolder pour simuler un utilisateur authentifié
        try (MockedStatic<SecurityContextHolder> securityContextHolder = mockStatic(SecurityContextHolder.class)) {
            securityContextHolder.when(SecurityContextHolder::getContext).thenReturn(securityContext);
            when(securityContext.getAuthentication()).thenReturn(authentication);
            when(authentication.getName()).thenReturn("testUser");

            // Mock du logger "audit.admin" (nécessite une configuration spécifique de
            // Mockito pour les loggers statiques)
            try (MockedStatic<LoggerFactory> loggerFactory = mockStatic(LoggerFactory.class)) {
                loggerFactory.when(() -> LoggerFactory.getLogger("audit.admin")).thenReturn(auditLogger);

                // WHEN : La méthode logAdminAction est appelée
                adminAuditAspect.logAdminAction(joinPoint);

                // THEN : Vérification que le logger "audit.admin" a été appelé avec les
                // informations correctes
                verify(auditLogger, times(1)).info(
                        "ADMIN ACTION - Utilisateur: {}, Action: {}, Arguments: {}",
                        "testUser",
                        methodName,
                        args);
            }
        }
    }

    @Test
    void logAdminAction_shouldLogAuditInformationWithSYSTEMUserWhenNotAuthenticated() {
        // GIVEN : Un JoinPoint pour une méthode annotée avec @AdminAction et aucun
        // utilisateur authentifié
        String methodName = "anotherAdminMethod";
        Object[] args = new Object[] { true };

        when(joinPoint.getSignature()).thenReturn(signature);
        when(signature.getName()).thenReturn(methodName);
        when(joinPoint.getArgs()).thenReturn(args);

        // Mock du SecurityContextHolder pour simuler l'absence d'utilisateur
        // authentifié
        try (MockedStatic<SecurityContextHolder> securityContextHolder = mockStatic(SecurityContextHolder.class)) {
            securityContextHolder.when(SecurityContextHolder::getContext).thenReturn(securityContext);
            when(securityContext.getAuthentication()).thenReturn(null);

            // Mock du logger "audit.admin"
            try (MockedStatic<LoggerFactory> loggerFactory = mockStatic(LoggerFactory.class)) {
                loggerFactory.when(() -> LoggerFactory.getLogger("audit.admin")).thenReturn(auditLogger);

                // WHEN : La méthode logAdminAction est appelée
                adminAuditAspect.logAdminAction(joinPoint);

                // THEN : Vérification que le logger "audit.admin" a été appelé avec "SYSTEM"
                // comme utilisateur
                verify(auditLogger, times(1)).info(
                        "ADMIN ACTION - Utilisateur: {}, Action: {}, Arguments: {}",
                        "SYSTEM",
                        methodName,
                        args);
            }
        }
    }
}