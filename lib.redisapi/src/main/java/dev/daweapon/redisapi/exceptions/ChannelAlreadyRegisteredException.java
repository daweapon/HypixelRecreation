package dev.daweapon.redisapi.exceptions;

public class ChannelAlreadyRegisteredException extends RuntimeException {

    public ChannelAlreadyRegisteredException(String errorMessage) {
        super(errorMessage);
    }

}