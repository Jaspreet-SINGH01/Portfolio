package com.videoflix.subscriptions_microservice.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class SubscriptionMetricsService {

    private static final Logger logger = LoggerFactory.getLogger(SubscriptionMetricsService.class);

    // Utilisation d'une ConcurrentHashMap pour la thread-safety, car cette tâche
    // est planifiée
    private final Map<LocalDate, Map<String, Number>> dailyMetrics = new ConcurrentHashMap<>();

    /**
     * Enregistre une métrique quotidienne. Si la date existe déjà, la métrique est
     * ajoutée
     * ou mise à jour dans la map des métriques de cette date.
     *
     * @param metricName Le nom de la métrique (par exemple, "new_subscriptions",
     *                   "daily_revenue").
     * @param date       La date à laquelle la métrique se rapporte.
     * @param value      La valeur de la métrique.
     */
    public void recordDailyMetric(String metricName, LocalDateTime date, Number value) {
        LocalDate dateKey = date.toLocalDate();
        dailyMetrics.compute(dateKey, (key, existingMetrics) -> {
            Map<String, Number> metrics = (existingMetrics == null) ? new HashMap<>() : existingMetrics;
            metrics.put(metricName, value);
            logger.debug("Métrique '{}' enregistrée pour le {} avec la valeur: {}", metricName, date, value);
            return metrics;
        });
    }

    /**
     * Récupère toutes les métriques enregistrées pour une date spécifique.
     *
     * @param date La date pour laquelle récupérer les métriques.
     * @return Une map contenant les noms des métriques et leurs valeurs pour la
     *         date donnée,
     *         ou une map vide si aucune métrique n'a été enregistrée pour cette
     *         date.
     */
    public Map<String, Number> getDailyMetrics(LocalDate date) {
        return dailyMetrics.getOrDefault(date, new HashMap<>());
    }

    /**
     * Récupère une métrique spécifique pour une date donnée.
     *
     * @param metricName Le nom de la métrique à récupérer.
     * @param date       La date pour laquelle récupérer la métrique.
     * @return La valeur de la métrique pour la date donnée, ou null si la métrique
     *         n'existe pas.
     */
    public Number getDailyMetric(String metricName, LocalDate date) {
        Map<String, Number> metrics = dailyMetrics.get(date);
        if (metrics != null) {
            return metrics.get(metricName);
        }
        return null;
    }

    public void aggregateMonthlyMetrics() {
        logger.info("Début de l'agrégation des métriques mensuelles (implémentation à ajouter).");
    }

    public void persistMetrics() {
        logger.info("Début de la persistance des métriques (implémentation à ajouter).");
    }
}