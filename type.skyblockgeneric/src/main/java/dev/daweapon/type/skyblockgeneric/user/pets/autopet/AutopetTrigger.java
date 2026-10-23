package dev.daweapon.type.skyblockgeneric.user.pets.autopet;

import net.minestom.server.item.Material;
import org.jetbrains.annotations.Nullable;

public enum AutopetTrigger {
    LOGIN("On login", AutopetParam.NONE, Material.OAK_DOOR, null),
    GAIN_SKILL_XP("On gain skill XP", AutopetParam.SKILL, Material.EXPERIENCE_BOTTLE, null),
    ENTER_ISLAND("On enter island", AutopetParam.ISLAND, Material.COMPASS, null),
    ENTER_COMBAT("On enter combat", AutopetParam.NONE, Material.IRON_SWORD, null),
    GAIN_COLLECTION("On gain collection", AutopetParam.COLLECTION, Material.PAINTING, null),
    KILL_MOB("On kill a mob", AutopetParam.MOB, Material.ROTTEN_FLESH, null),
    SELECT_DUNGEON_CLASS("On select dungeon class", AutopetParam.NONE, Material.WITHER_SKELETON_SKULL, "Dungeon classes are not built yet"),
    EVENT_START("On event start", AutopetParam.EVENT, Material.CLOCK, null),
    BOSS_SPAWN("On boss spawn", AutopetParam.SLAYER, Material.ZOMBIE_HEAD, null),
    SLAYER_QUEST_START("On starting a slayer quest", AutopetParam.NONE, Material.BLAZE_POWDER, null),
    EXPERIMENTATION_TABLE("On clicking the experimentation table", AutopetParam.NONE, Material.ENCHANTING_TABLE, null),
    AFK("On going AFK", AutopetParam.NONE, Material.RED_BED, "AFK detection is not built yet"),
    GRIFFIN_BURROW("On digging a Griffin burrow", AutopetParam.NONE, Material.GOLDEN_SHOVEL, null),
    WINTER_GIFT("On opening a winter gift", AutopetParam.NONE, Material.CHEST, null),
    FISHING_HOOK("On throw fishing hook", AutopetParam.NONE, Material.FISHING_ROD, null),
    CATACOMBS_FLOOR("On entering a Catacombs floor", AutopetParam.NONE, Material.NETHER_STAR, "The Catacombs are not built yet");

    private final String label;
    private final AutopetParam param;
    private final Material icon;
    private final @Nullable String unavailable;

    AutopetTrigger(String label, AutopetParam param, Material icon, @Nullable String unavailable) {
        this.label = label;
        this.param = param;
        this.icon = icon;
        this.unavailable = unavailable;
    }

    public String label() {
        return label;
    }

    public AutopetParam param() {
        return param;
    }

    public Material icon() {
        return icon;
    }

    public @Nullable String unavailable() {
        return unavailable;
    }

    public boolean isAvailable() {
        return unavailable == null;
    }
}
