package dev.daweapon.commons.protocol;

import org.junit.jupiter.api.Test;
import org.reflections.Reflections;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.RecordComponent;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.UUID;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ProtocolRegistryTest {
    private static final String PROTOCOL_PACKAGE = "dev.daweapon.commons.protocol.objects";

    @SuppressWarnings("rawtypes")
    private static List<RedisProtocol> protocols() throws Exception {
        List<RedisProtocol> protocols = new ArrayList<>();
        Set<Class<? extends RedisProtocol>> types = new Reflections(PROTOCOL_PACKAGE).getSubTypesOf(RedisProtocol.class);
        for (Class<? extends RedisProtocol> type : types) {
            if (Modifier.isAbstract(type.getModifiers())) continue;
            Constructor<? extends RedisProtocol> constructor = type.getDeclaredConstructor();
            constructor.setAccessible(true);
            protocols.add(constructor.newInstance());
        }
        return protocols;
    }

    @Test
    void everyProtocolHasAUniqueChannel() throws Exception {
        Map<String, Integer> counts = new HashMap<>();
        for (RedisProtocol<?, ?> protocol : protocols()) {
            counts.merge(protocol.channel(), 1, Integer::sum);
        }

        Set<String> duplicates = counts.entrySet().stream()
                .filter(entry -> entry.getValue() > 1)
                .map(Map.Entry::getKey)
                .collect(Collectors.toCollection(TreeSet::new));
        assertTrue(counts.size() >= 90, "expected the full protocol set but found " + counts.size());
        assertTrue(duplicates.isEmpty(), "duplicate protocol channels: " + duplicates);
    }

    @Test
    @SuppressWarnings({"rawtypes", "unchecked"})
    void everyProtocolRoundTripsItsMessages() throws Exception {
        for (RedisProtocol protocol : protocols()) {
            Type[] arguments = ((ParameterizedType) protocol.getClass().getGenericSuperclass()).getActualTypeArguments();
            assertNotNull(protocol.getSerializer(), protocol.channel());
            assertNotNull(protocol.getReturnSerializer(), protocol.channel());

            Object request = sample(arguments[0]);
            if (request != null) {
                String json = protocol.translateToString(request);
                assertEquals(json, protocol.translateToString(protocol.translateFromString(json)), protocol.channel() + " request");
            }
            Object response = sample(arguments[1]);
            if (response != null) {
                String json = protocol.translateReturnToString(response);
                assertEquals(json, protocol.translateReturnToString(protocol.translateReturnFromString(json)), protocol.channel() + " response");
            }
        }
    }

    private static Object sample(Type type) throws Exception {
        Class<?> raw = type instanceof ParameterizedType parameterized ? (Class<?>) parameterized.getRawType() : type instanceof Class<?> c ? c : null;
        if (raw == null) return null;
        if (raw.isRecord()) {
            RecordComponent[] components = raw.getRecordComponents();
            Class<?>[] parameterTypes = new Class<?>[components.length];
            Object[] values = new Object[components.length];
            for (int i = 0; i < components.length; i++) {
                parameterTypes[i] = components[i].getType();
                values[i] = sample(components[i].getGenericType());
            }
            Constructor<?> constructor = raw.getDeclaredConstructor(parameterTypes);
            constructor.setAccessible(true);
            try {
                return constructor.newInstance(values);
            } catch (InvocationTargetException e) {
                return null;
            }
        }
        return defaultFor(raw);
    }

    private static Object defaultFor(Class<?> type) {
        if (type == boolean.class || type == Boolean.class) return false;
        if (type == int.class || type == Integer.class) return 0;
        if (type == long.class || type == Long.class) return 0L;
        if (type == double.class || type == Double.class) return 0D;
        if (type == float.class || type == Float.class) return 0F;
        if (type == short.class) return (short) 0;
        if (type == byte.class) return (byte) 0;
        if (type == String.class) return "sample";
        if (type.isEnum()) return type.getEnumConstants()[0];
        if (type == UUID.class) return new UUID(0, 1);
        if (type == List.class) return List.of();
        if (type == Map.class) return Map.of();
        if (type == Set.class) return Set.of();
        return null;
    }
}
