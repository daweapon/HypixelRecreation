package net.swofty.type.skyblockgeneric.server.attribute;

import lombok.Getter;
import net.minestom.server.MinecraftServer;
import net.minestom.server.timer.ExecutionType;
import net.minestom.server.timer.TaskSchedule;
import net.swofty.type.generic.HypixelConst;
import net.swofty.type.generic.HypixelTypeLoader;
import net.swofty.type.generic.data.mongodb.AttributeDatabase;
import net.swofty.type.skyblockgeneric.calendar.SkyBlockCalendar;
import net.swofty.type.skyblockgeneric.server.attribute.attributes.AttributeLong;
import org.bson.Document;
import org.tinylog.Logger;
import tools.jackson.core.JacksonException;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.function.BinaryOperator;
import java.util.function.Consumer;
import java.util.function.Function;

public class SkyBlockServerAttributes {
    private static final Map<Attributes, ServerAttribute> serverAttributes = new HashMap<>();

    public static void loadAttributes(Document document) {
        Arrays.stream(Attributes.values()).forEach(data -> {
            if (document == null || !document.containsKey(data.getKey())) {
                serverAttributes.put(data, data.getDefaultAttribute());
                return;
            }

            String jsonValue = document.getString(data.getKey());

            try {
                ServerAttribute<?> attribute = data.getDefaultAttribute().getClass().getDeclaredConstructor(String.class).newInstance(data.getKey());
                attribute.deserializeValue(jsonValue);
                serverAttributes.put(data, attribute);
                data.onLoad.accept(attribute);
            } catch (Exception e) {
                Logger.error(e, "Failed to load or save server attributes");
            }
        });
    }

    public static Document toDocument() {
        Document document = new Document();
        document.append("_id", "attributes");
        serverAttributes.forEach((key, attribute) -> {
            attribute.setValue(key.onSave.apply(HypixelConst.getTypeLoader()).getValue());

            try {
                document.append(key.getKey(), attribute.getSerializedValue());
            } catch (JacksonException e) {
                Logger.error(e, "Failed to load or save server attributes");
            }
        });
        return document;
    }

    public <R extends ServerAttribute<T>, T> R get(Attributes attribute, Class<R> type) {
        if (!serverAttributes.containsKey(attribute)) {
            return type.cast(attribute.getDefaultAttribute());
        }
        return type.cast(serverAttributes.get(attribute));
    }

    // Every game server type runs its own JVM with no single "main" instance, so a value like the
    // calendar's elapsed time otherwise drifts per-server the moment two servers boot at different
    // times (each only ever loaded the shared document once, at its own boot). This loop instead
    // reconciles against the shared document every cycle: for a "convergent" attribute (see
    // Attributes.reconcile), the higher of this server's local value and the last-saved shared
    // value wins, is adopted locally if this server was behind, and is saved back if it was ahead -
    // so every server converges on the same value within a couple of cycles regardless of boot order.
    public static void saveAttributeLoop() {
        MinecraftServer.getSchedulerManager().scheduleTask(SkyBlockServerAttributes::reconcileAndSave,
                TaskSchedule.seconds(3), TaskSchedule.seconds(3), ExecutionType.TICK_END);
    }

    // Bypasses the "highest value wins" reconciliation to push this server's current attribute
    // values straight to the shared document. Only safe to call when every server's local value is
    // being deliberately forced to the same new value at once (e.g. a coordinated calendar reset) -
    // otherwise the next reconcileAndSave cycle on another server will just overwrite it back.
    public static void forceSave() {
        AttributeDatabase.saveDocument(toDocument());
    }

    @SuppressWarnings("unchecked")
    private static void reconcileAndSave() {
        Document remoteDocument = AttributeDatabase.getDocument("attributes");

        for (Attributes data : Attributes.values()) {
            Object localValue = data.onSave.apply(HypixelConst.getTypeLoader()).getValue();
            if (remoteDocument == null || !remoteDocument.containsKey(data.getKey())) continue;

            try {
                ServerAttribute remoteAttribute = data.getDefaultAttribute().getClass().getDeclaredConstructor(String.class).newInstance(data.getKey());
                remoteAttribute.deserializeValue(remoteDocument.getString(data.getKey()));

                Object reconciled = data.reconcile.apply(localValue, remoteAttribute.getValue());
                if (!reconciled.equals(localValue)) {
                    remoteAttribute.setValue(reconciled);
                    data.onLoad.accept(remoteAttribute);
                }
            } catch (Exception e) {
                Logger.error(e, "Failed to reconcile server attribute {}", data.getKey());
            }
        }

        AttributeDatabase.saveDocument(toDocument());
    }

    public enum Attributes {
        // Calendar time only ever advances, so the reconcile strategy is "highest value wins" -
        // this is what makes it converge across servers instead of just persisting/restoring
        // per-server state.
        CALENDER_ELAPSED_TIME("elapsed", AttributeLong.class, new AttributeLong("elapsed", 0L), (attribute) -> {
            SkyBlockCalendar.setElapsed((Long) attribute.getValue());
        }, (server) -> new AttributeLong("elapsed", SkyBlockCalendar.getElapsed()),
                (local, remote) -> Math.max((Long) local, (Long) remote));
        @Getter
        private final String key;
        @Getter
        private final Class<? extends ServerAttribute> type;
        @Getter
        private final ServerAttribute defaultAttribute;
        public final Consumer<ServerAttribute> onLoad;
        public final Function<HypixelTypeLoader, ServerAttribute> onSave;
        public final BinaryOperator<Object> reconcile;

        Attributes(String key, Class<? extends ServerAttribute> type, ServerAttribute defaultAttribute, Consumer<ServerAttribute> onLoad, Function<HypixelTypeLoader, ServerAttribute> onSave, BinaryOperator<Object> reconcile) {
            this.key = key;
            this.type = type;
            this.defaultAttribute = defaultAttribute;
            this.onLoad = onLoad;
            this.onSave = onSave;
            this.reconcile = reconcile;
        }

        public static Attributes fromKey(String key) {
            for (Attributes data : values()) {
                if (data.getKey().equals(key)) {
                    return data;
                }
            }
            return null;
        }
    }
}
