package net.swofty.service.control.watchdog;

import org.tinylog.Logger;

/** Always-on transport so an alert is never lost just because no push channel is configured. */
public final class LoggingAlertTransport implements AlertTransport {
    @Override
    public String name() {
        return "log";
    }

    @Override
    public void send(Alert alert) {
        switch (alert.severity()) {
            case CRITICAL -> Logger.error("[ALERT/{}] {} - {}", alert.severity(), alert.title(), alert.body());
            case WARNING -> Logger.warn("[ALERT/{}] {} - {}", alert.severity(), alert.title(), alert.body());
            case RECOVERY -> Logger.info("[ALERT/{}] {} - {}", alert.severity(), alert.title(), alert.body());
        }
    }
}
