package dev.daweapon.redisapi.events;

import dev.daweapon.redisapi.api.ChannelRegistry;
import dev.daweapon.redisapi.api.RedisAPI;
import dev.daweapon.redisapi.api.RedisChannel;
import lombok.SneakyThrows;
import dev.daweapon.redisapi.exceptions.ChannelDefinitionError;
import dev.daweapon.redisapi.exceptions.InvalidMessageException;
import redis.clients.jedis.JedisPubSub;

import java.util.Objects;
import java.util.Optional;

public class EventRegistry {

      public static JedisPubSub pubSub = null;

      @SneakyThrows
      public static void handleAll(String channel, String message) {
            String filterID;
            if (message != null && message.contains(";")) {
                  filterID = message.split(";", 2)[0];
            } else {
                  throw new InvalidMessageException("Received message is not properly formatted with a filter ID: " + message);
            }

            if (filterID.equals("all") || filterID.equals(RedisAPI.getInstance().getFilterId())) {
                  Optional<RedisChannel> optionalChannelBeingCalled = ChannelRegistry.registeredChannels.stream().filter(channel2 -> Objects.equals(channel2.channelName, channel)).findAny();
                  if (optionalChannelBeingCalled.isPresent()) {
                        RedisChannel channelBeingCalled = optionalChannelBeingCalled.get();

                        switch (channelBeingCalled.functionType) {
                              case CLASS:
                                    RedisMessagingReceiveInterface receiveInterface = channelBeingCalled.receiveInterface.getDeclaredConstructor().newInstance();
                                    receiveInterface.onMessage(channel, message);
                                    return;

                              case CONSUMER:
                                    channelBeingCalled.receiveEvent.accept(new RedisMessagingReceiveEvent(channel, message));
                                    return;
                        }
                        throw new ChannelDefinitionError("No receive event or receive interface was set for the channel '" + channel + "'");
                  }
            }
      }

}
