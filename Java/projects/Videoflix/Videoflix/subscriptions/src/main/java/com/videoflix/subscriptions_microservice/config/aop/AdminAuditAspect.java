package com.videoflix.subscriptions_microservice.config.aop;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class AdminAuditAspect {

    private static final Logger auditLogger = LoggerFactory.getLogger("audit.admin");

    @AfterReturning("@annotation(com.videoflix.subscriptions_microservice.annotations.AdminAction)")
    public void logAdminAction(JoinPoint joinPoint) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication != null ? authentication.getName() : "SYSTEM";
        String methodName = joinPoint.getSignature().getName();
        Object[] args = joinPoint.getArgs();

        auditLogger.info("ADMIN ACTION - Utilisateur: {}, Action: {}, Arguments: {}", username, methodName, args);
        // Possibilité d'extraire des informations plus spécifiques des arguments si
        // nécessaire
    }
}