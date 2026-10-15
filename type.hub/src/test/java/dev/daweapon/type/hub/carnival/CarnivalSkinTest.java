package dev.daweapon.type.hub.carnival;

import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

import static org.junit.jupiter.api.Assertions.*;

class CarnivalSkinTest {
    @Test
    void textureIsBase64JsonPointingAtASkinUrl() {
        for (CarnivalSkin skin : CarnivalSkin.values()) {
            String json = new String(Base64.getDecoder().decode(skin.texture()), StandardCharsets.UTF_8);
            assertTrue(json.contains("textures.minecraft.net/texture/"), skin.name());
        }
    }

    @Test
    void signatureIsNonEmptyBase64() {
        for (CarnivalSkin skin : CarnivalSkin.values()) {
            assertFalse(skin.signature().isBlank(), skin.name());
            assertDoesNotThrow(() -> Base64.getDecoder().decode(skin.signature()), skin.name());
        }
    }

    @Test
    void everySkinHasDistinctTexture() {
        long distinct = java.util.Arrays.stream(CarnivalSkin.values()).map(CarnivalSkin::texture).distinct().count();
        assertEquals(CarnivalSkin.values().length, distinct);
    }
}
