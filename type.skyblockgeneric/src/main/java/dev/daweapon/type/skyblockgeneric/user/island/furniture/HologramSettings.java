package dev.daweapon.type.skyblockgeneric.user.island.furniture;

import org.jetbrains.annotations.Nullable;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

public record HologramSettings(List<String> lines, @Nullable Character particle, boolean modelVisible) {
    public static final int MAX_LINES = 3;
    public static final int MAX_LINE_LENGTH = 35;
    public static final String PARTICLE_COLOURS = "0123456789abcdef";
    private static final char NO_PARTICLE = '-';

    public static HologramSettings initial() {
        return new HologramSettings(List.of(), 'f', true);
    }

    public static HologramSettings decode(@Nullable String data) {
        if (data == null || data.isBlank()) return initial();
        String[] parts = new String(Base64.getUrlDecoder().decode(data), StandardCharsets.UTF_8).split("\n", -1);
        Character particle = parts[0].charAt(0) == NO_PARTICLE ? null : parts[0].charAt(0);
        List<String> lines = new ArrayList<>(List.of(parts).subList(2, parts.length));
        return new HologramSettings(lines, particle, parts[1].equals("1")).trimmed();
    }

    public String encode() {
        String raw = (particle == null ? NO_PARTICLE : particle) + "\n" + (modelVisible ? "1" : "0") + "\n" + String.join("\n", lines);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(raw.getBytes(StandardCharsets.UTF_8));
    }

    public HologramSettings withLine(int index, @Nullable String text) {
        List<String> updated = new ArrayList<>(lines);
        while (updated.size() <= index) updated.add("");
        updated.set(index, text == null ? "" : text);
        return new HologramSettings(updated, particle, modelVisible).trimmed();
    }

    public HologramSettings withParticle(@Nullable Character colour) {
        return new HologramSettings(lines, colour, modelVisible);
    }

    public HologramSettings withModelVisible(boolean visible) {
        return new HologramSettings(lines, particle, visible);
    }

    public HologramSettings nextParticle() {
        if (particle == null) return withParticle(PARTICLE_COLOURS.charAt(0));
        int next = PARTICLE_COLOURS.indexOf(particle) + 1;
        return withParticle(next >= PARTICLE_COLOURS.length() ? null : PARTICLE_COLOURS.charAt(next));
    }

    public boolean hasText() {
        return lines.stream().anyMatch(line -> !line.isBlank());
    }

    public String line(int index) {
        return index < lines.size() ? lines.get(index) : "";
    }

    private HologramSettings trimmed() {
        List<String> kept = new ArrayList<>(lines);
        while (!kept.isEmpty() && kept.getLast().isBlank()) kept.removeLast();
        if (kept.size() > MAX_LINES) kept = new ArrayList<>(kept.subList(0, MAX_LINES));
        return new HologramSettings(kept, particle, kept.isEmpty() || modelVisible);
    }
}
