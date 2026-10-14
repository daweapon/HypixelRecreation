package net.swofty.type.murdermysterygame.maphandler;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class MapHandlerRegistryTest {
    private static MapHandler handler(String id) {
        return new MapHandler() {
            @Override
            public String getMapId() {
                return id;
            }

            @Override
            public List<InteractiveElement> getInteractiveElements() {
                return List.of();
            }
        };
    }

    @AfterEach
    void reset() {
        MapHandlerRegistry.clear();
    }

    @Test
    void lookupIsCaseInsensitive() {
        MapHandler aquarium = handler("Aquarium");
        MapHandlerRegistry.register(aquarium);
        assertSame(aquarium, MapHandlerRegistry.getHandler("AQUARIUM").orElseThrow());
    }

    @Test
    void unknownOrNullMapHasNoHandler() {
        assertTrue(MapHandlerRegistry.getHandler("missing").isEmpty());
        assertTrue(MapHandlerRegistry.getHandler(null).isEmpty());
    }

    @Test
    void registeringSameIdReplacesHandler() {
        MapHandlerRegistry.register(handler("a"));
        MapHandler second = handler("A");
        MapHandlerRegistry.register(second);
        assertSame(second, MapHandlerRegistry.getHandler("a").orElseThrow());
    }

    @Test
    void clearRemovesHandlers() {
        MapHandlerRegistry.register(handler("a"));
        MapHandlerRegistry.clear();
        assertTrue(MapHandlerRegistry.getHandler("a").isEmpty());
    }
}
