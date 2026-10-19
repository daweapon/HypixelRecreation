package dev.daweapon.commons.protocol;

import org.reflections.Reflections;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.RecordComponent;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

final class ProtocolSamples {
    private static final String PROTOCOL_PACKAGE = "dev.daweapon.commons.protocol.objects";

    private ProtocolSamples() {
    }

    @SuppressWarnings("rawtypes")
    static List<RedisProtocol> protocols() throws Exception {
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

    static Type[] messageTypes(RedisProtocol<?, ?> protocol) {
        return ((ParameterizedType) protocol.getClass().getGenericSuperclass()).getActualTypeArguments();
    }

    static Object sample(Type type) throws Exception {
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
