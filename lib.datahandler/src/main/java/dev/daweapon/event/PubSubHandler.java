package dev.daweapon.event;

import java.util.List;

public interface PubSubHandler {
    void publish(String message);

    default void publishAll(List<String> messages) {
        for (String message : messages) publish(message);
    }

    void subscribe(MessageHandler handler);
    void shutdown();

    @FunctionalInterface
    interface MessageHandler {
        void onMessage(String message);
    }
}
