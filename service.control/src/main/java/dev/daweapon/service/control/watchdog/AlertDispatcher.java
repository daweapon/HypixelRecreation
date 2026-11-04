package dev.daweapon.service.control.watchdog;

import org.tinylog.Logger;

import java.util.ArrayList;
import java.util.Deque;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentLinkedDeque;

/**
 * Fans an alert out to every configured transport and keeps a short history for the app.
 *
 * Each push channel is one {@link AlertTransport}. FCM would be another: an FcmAlertTransport
 * registered in {@link #fromEnvironment()}, fed by device tokens the app registers through
 * service.api. It is not built because it needs a Firebase project.
 */
public final class AlertDispatcher {
    private static final int HISTORY_LIMIT = 100;

    private final List<AlertTransport> transports;
    private final Deque<Alert> history = new ConcurrentLinkedDeque<>();

    public AlertDispatcher(List<AlertTransport> transports) {
        this.transports = transports;
    }

    public static AlertDispatcher fromEnvironment() {
        List<AlertTransport> transports = new ArrayList<>();
        transports.add(new LoggingAlertTransport());

        String webhookUrl = System.getenv("ALERT_WEBHOOK_URL");
        if (webhookUrl != null && !webhookUrl.isBlank()) {
            transports.add(new WebhookAlertTransport(webhookUrl.trim()));
            Logger.info("Alert webhook configured");
        }

        String ntfyUrl = System.getenv("ALERT_NTFY_URL");
        if (ntfyUrl != null && !ntfyUrl.isBlank()) {
            try {
                transports.add(new NtfyAlertTransport(ntfyUrl, System.getenv("ALERT_NTFY_TOKEN")));
                Logger.info("ntfy alerts configured");
            } catch (IllegalArgumentException e) {
                Logger.error(e.getMessage());
            }
        }

        if (transports.size() == 1) {
            Logger.info("No ALERT_NTFY_URL or ALERT_WEBHOOK_URL set - alerts will only be logged");
        }
        return new AlertDispatcher(transports);
    }

    public void dispatch(Alert alert) {
        dispatchReporting(alert);
    }

    /** Dispatches and reports each transport's outcome: "ok" or the error it raised. */
    public Map<String, String> dispatchReporting(Alert alert) {
        history.addFirst(alert);
        while (history.size() > HISTORY_LIMIT) {
            history.removeLast();
        }

        Map<String, String> results = new LinkedHashMap<>();
        for (AlertTransport transport : transports) {
            try {
                transport.send(alert);
                results.put(transport.name(), "ok");
            } catch (Exception e) {
                // An alert transport failing must never take down the watchdog that produced it.
                Logger.error(e, "Alert transport {} failed", transport.name());
                results.put(transport.name(), String.valueOf(e.getMessage()));
            }
        }
        return results;
    }

    public List<Alert> recent() {
        return List.copyOf(history);
    }

    public List<String> transportNames() {
        return transports.stream().map(AlertTransport::name).toList();
    }
}
