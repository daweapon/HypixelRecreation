package dev.daweapon.type.skyblockgeneric.item.handlers.pet.registry;

import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Every bespoke pet ability handler by id. Each content area keeps its handlers in its own
 * {@link Provider} and adds one line to {@link #PROVIDERS}.
 */
public final class PetAbilityRegistry {
    private static final Map<String, PetAbilityHandler> HANDLERS = new ConcurrentHashMap<>();

    private static final List<Provider> PROVIDERS = List.of(
            new CorePetAbilities(),
            new CombatPetAbilities(),
            new GatheringPetAbilities(),
            new MiscPetAbilities()
    );

    static {
        PROVIDERS.forEach(provider -> provider.register(PetAbilityRegistry::register));
    }

    private PetAbilityRegistry() {
    }

    public static void register(String id, PetAbilityHandler handler) {
        String key = id.toUpperCase();
        if (HANDLERS.putIfAbsent(key, handler) != null) {
            throw new IllegalStateException("Pet ability handler '" + key + "' is registered twice");
        }
    }

    public static @Nullable PetAbilityHandler get(String id) {
        return HANDLERS.get(id.toUpperCase());
    }

    public static Set<String> ids() {
        return Collections.unmodifiableSet(HANDLERS.keySet());
    }

    @FunctionalInterface
    public interface Provider {
        void register(Registrar registrar);
    }

    @FunctionalInterface
    public interface Registrar {
        void register(String id, PetAbilityHandler handler);
    }
}
