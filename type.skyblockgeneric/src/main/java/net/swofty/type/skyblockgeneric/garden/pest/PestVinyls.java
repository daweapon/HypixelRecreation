package net.swofty.type.skyblockgeneric.garden.pest;

import net.swofty.commons.skyblock.item.ItemType;
import net.swofty.commons.text.Text;
import net.swofty.type.skyblockgeneric.garden.GardenProfile;
import net.swofty.type.skyblockgeneric.garden.Gardens;
import net.swofty.type.skyblockgeneric.item.SkyBlockItem;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class PestVinyls {
    public static final String HOOVERIUS = "INFINI_VACUUM_HOOVERIUS";
    public static final double DROP_CHANCE_PERCENT = 5;

    private static final Map<String, String> VINYL_BY_PEST = new LinkedHashMap<>();

    static {
        VINYL_BY_PEST.put("BEETLE", "VINYL_BEETLE");
        VINYL_BY_PEST.put("CRICKET", "VINYL_CRICKET_CHOIR");
        VINYL_BY_PEST.put("DRAGONFLY", "VINYL_IMAGINE_DRAGONFLIES");
        VINYL_BY_PEST.put("EARTHWORM", "VINYL_EARTHWORM_ENSEMBLE");
        VINYL_BY_PEST.put("FIREFLY", "VINYL_FIREFLY");
        VINYL_BY_PEST.put("FLY", "VINYL_PRETTY_FLY");
        VINYL_BY_PEST.put("LOCUST", "VINYL_CICADA_SYMPHONY");
        VINYL_BY_PEST.put("MITE", "VINYL_DYNAMITES");
        VINYL_BY_PEST.put("MOSQUITO", "VINYL_BUZZIN_BEATS");
        VINYL_BY_PEST.put("MOTH", "VINYL_WINGS_OF_HARMONY");
        VINYL_BY_PEST.put("PRAYING_MANTIS", "VINYL_PRAY_FOR_ME");
        VINYL_BY_PEST.put("RAT", "VINYL_RODENT_REVOLUTION");
        VINYL_BY_PEST.put("SLUG", "VINYL_SLOW_AND_GROOVY");
    }

    private PestVinyls() {
    }

    public static @Nullable String vinylOf(String pestId) {
        return VINYL_BY_PEST.get(pestId);
    }

    public static @Nullable String pestOf(String vinylId) {
        for (Map.Entry<String, String> entry : VINYL_BY_PEST.entrySet()) {
            if (entry.getValue().equals(vinylId)) return entry.getKey();
        }
        return null;
    }

    public static Map<String, String> all() {
        return Map.copyOf(VINYL_BY_PEST);
    }

    public static String next(List<String> options, String current) {
        if (options.isEmpty()) return "";
        int index = options.indexOf(current);
        return index < 0 || index + 1 >= options.size() ? options.getFirst() : options.get(index + 1);
    }

    public static double weight(String playing, String pestId) {
        return playing.equals(pestId) ? PestRules.vinylWeight(0) : 0;
    }

    public static boolean isHooverius(SkyBlockItem item) {
        return HOOVERIUS.equals(item.getAttributeHandler().getTypeAsString());
    }

    public static String playingName(String pestId) {
        PestCatalogue.Pest pest = pestId.isEmpty() ? null : PestCatalogue.get().pest(pestId);
        return pest == null ? "<c>None" : "<a>" + vinylName(pestId);
    }

    private static String vinylName(String pestId) {
        String vinyl = VINYL_BY_PEST.get(pestId);
        ItemType type = vinyl == null ? null : ItemType.get(vinyl);
        return type == null ? pestId : new SkyBlockItem(type).getDisplayName();
    }

    public static void cycle(SkyBlockPlayer player) {
        GardenProfile profile = Gardens.profile(player);
        PestData data = profile.pests();
        List<String> options = new ArrayList<>();
        options.add("");
        for (Map.Entry<String, String> entry : VINYL_BY_PEST.entrySet()) {
            ItemType type = ItemType.get(entry.getValue());
            if (type != null && (player.getAmountInInventory(type) > 0 || player.getSackItems().getAmount(type) > 0)) {
                options.add(entry.getKey());
            }
        }
        if (options.size() == 1 && data.getPlayingVinyl().isEmpty()) {
            player.sendMessage(Text.of("<c>You don't have any Pest Vinyls to play!"));
            return;
        }
        String next = next(options, data.getPlayingVinyl());
        data.setPlayingVinyl(next);
        Gardens.save(player, profile);
        player.sendMessage(next.isEmpty()
                ? Text.of("<7>Stereo Harmony: <c>Stopped playing<7>.")
                : Text.of("<7>Stereo Harmony: <a>Now Playing {} <7>(<2>{} Pests <7>are more likely to spawn)", vinylName(next),
                        PestCatalogue.get().pest(next).name()));
    }
}
