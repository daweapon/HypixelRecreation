package dev.daweapon.type.generic.data.datapoints;

import dev.daweapon.commons.protocol.Serializer;
import dev.daweapon.type.generic.data.Datapoint;

// Datapoint storing a player's currently selected chat channel.
public class DatapointChatType extends Datapoint<DatapointChatType.ChatType> {
    private static final Serializer<ChatType> serializer = new Serializer<>() {
        @Override
        public String serialize(ChatType value) {
            return value.currentChatType.name();
        }

        @Override
        public ChatType deserialize(String json) {
            return new ChatType(Chats.valueOf(json));
        }

        @Override
        public ChatType clone(ChatType value) {
            return new ChatType(value.currentChatType);
        }
    };

    public DatapointChatType(String key, ChatType value) {
        super(key, value, serializer);
    }

    public DatapointChatType(String key) {
        super(key, null, serializer);
    }

    public static class ChatType {
        public Chats currentChatType;

        public ChatType(Chats currentChatType) {
            this.currentChatType = currentChatType;
        }

        public void switchTo(Chats chatType) {
            this.currentChatType = chatType;
        }
    }

    public enum Chats {
        ALL,
        PARTY,
        STAFF
    }
}
