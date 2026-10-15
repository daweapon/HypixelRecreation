package dev.daweapon.service.control.watchdog;

import org.tinylog.Logger;

import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedDeque;

/** Fans an alert out to every configured transport and keeps a short history for the app. */
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
        } else {
            Logger.info("No ALERT_WEBHOOK_URL set - alerts will only be logged");
        }

        return new AlertDispatcher(transports);
    }

    public void dispatch(Alert alert) {
        history.addFirst(alert);
        while (history.size() > HISTORY_LIMIT) {
            history.removeLast();
        }

        for (AlertTransport transport : transports) {
            try {
                transport.send(alert);
            } catch (Exception e) {
                // An alert transport failing must never take down the watchdog that produced it.
                Logger.error(e, "Alert transport {} failed", transport.name());
            }
        }
    }

    public List<Alert> recent() {
        return List.copyOf(history);
    }

    public List<String> transportNames() {
        return transports.stream().map(AlertTransport::name).toList();
    }
}
