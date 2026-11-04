package dev.daweapon.type.skyblockgeneric.user.pets;

import dev.daweapon.commons.YamlFileUtils;
import dev.daweapon.type.generic.gui.inventory.ItemStacks;
import net.minestom.server.item.ItemStack;
import org.jetbrains.annotations.Nullable;
import org.tinylog.Logger;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class AnimatedSkins {
    private static final File FILE = new File("./configuration/skyblock/animated_skins.yml");
    private static volatile Map<String, Animation> loaded;

    private AnimatedSkins() {
    }

    public record Animation(int ticks, List<String> frames, List<ItemStack> heads) {
    }

    public static @Nullable Animation get(@Nullable String skinId) {
        if (skinId == null) return null;
        return all().get(skinId);
    }

    public static void preload() {
        all();
    }

    public static boolean isSupported(@Nullable String skinId) {
        return get(skinId) != null;
    }

    private static Map<String, Animation> all() {
        Map<String, Animation> result = loaded;
        if (result == null) {
            synchronized (AnimatedSkins.class) {
                if (loaded == null) loaded = load();
                result = loaded;
            }
        }
        return result;
    }

    @SuppressWarnings("unchecked")
    private static Map<String, Animation> load() {
        Map<String, Animation> animations = new HashMap<>();
        try {
            Map<String, Object> root = YamlFileUtils.loadYaml(FILE);
            Map<String, Object> skins = (Map<String, Object>) root.get("skins");
            for (Map.Entry<String, Object> entry : skins.entrySet()) {
                Map<String, Object> raw = (Map<String, Object>) entry.getValue();
                List<String> frames = ((List<Object>) raw.get("frames")).stream().map(String::valueOf).toList();
                if (frames.isEmpty()) continue;
                int ticks = Math.max(1, ((Number) raw.get("ticks")).intValue());
                List<ItemStack> heads = frames.stream().map(frame -> ItemStacks.head(frame, "").build()).toList();
                animations.put(entry.getKey(), new Animation(ticks, frames, heads));
            }
        } catch (Exception exception) {
            Logger.error(exception, "Failed to load animated_skins.yml");
        }
        return Map.copyOf(animations);
    }
}
