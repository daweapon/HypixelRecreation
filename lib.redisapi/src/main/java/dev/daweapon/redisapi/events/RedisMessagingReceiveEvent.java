package dev.daweapon.redisapi.events;

public class RedisMessagingReceiveEvent {

    public String channel;
    public String message;

    RedisMessagingReceiveEvent(String channel, String message) {
        this.channel = channel;
        this.message = message;
    }
}
