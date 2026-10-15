package dev.daweapon.service.control.watchdog;

/** A way of getting an alert in front of the operator. */
public interface AlertTransport {
    String name();

    void send(Alert alert) throws Exception;
}
