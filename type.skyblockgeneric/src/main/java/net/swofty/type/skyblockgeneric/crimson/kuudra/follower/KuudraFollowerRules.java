package net.swofty.type.skyblockgeneric.crimson.kuudra.follower;

import net.minestom.server.coordinate.Point;
import net.minestom.server.entity.Entity;
import net.swofty.commons.skyblock.item.ItemType;
import net.swofty.commons.skyblock.statistics.ItemStatistic;
import net.swofty.type.skyblockgeneric.crimson.kuudra.KuudraAvailability;
import net.swofty.type.skyblockgeneric.crimson.kuudra.KuudraProgress;
import net.swofty.type.skyblockgeneric.crimson.volcano.VolcanoEruptionService;
import net.swofty.type.skyblockgeneric.crimson.volcano.VolcanoExplosivity;
import net.swofty.type.skyblockgeneric.enchantment.EnchantmentType;
import net.swofty.type.skyblockgeneric.entity.mob.SkyBlockMob;
import net.swofty.type.skyblockgeneric.item.SkyBlockItem;
import net.swofty.type.skyblockgeneric.item.components.DrillComponent;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;
import net.swofty.type.skyblockgeneric.user.pets.PetLevels;

import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

/** The conditions the Kuudra Follower NPCs check, kept in one place so each NPC stays small. */
public final class KuudraFollowerRules {
    public static final ZoneOffset QUEST_ZONE = ZoneOffset.ofHours(-5);
    public static final long HERRING_DONATION = 4_202_022L;
    public static final int RELIC_INTELLIGENCE = 4_000;
    public static final int DECAY_INTELLIGENCE = 4_500;
    public static final int ARCH_PET_LEVEL = 81;
    public static final int DECAY_PET_LEVEL = 100;
    public static final double PLHLEGBLAST_RANGE = 3;

    private static final List<String> FOLLOWER_ARMOR = List.of("KUUDRA_FOLLOWER_HELMET",
            "KUUDRA_FOLLOWER_CHESTPLATE", "KUUDRA_FOLLOWER_LEGGINGS", "KUUDRA_FOLLOWER_BOOTS");
    private static final List<String> EMBER_ARMOR = List.of("EMBER_HELMET", "EMBER_CHESTPLATE",
            "EMBER_LEGGINGS", "EMBER_BOOTS");
    private static final List<String> REKINDLED_EMBER_ARMOR = List.of("REKINDLED_EMBER_HELMET",
            "REKINDLED_EMBER_CHESTPLATE", "REKINDLED_EMBER_LEGGINGS", "REKINDLED_EMBER_BOOTS");

    private static final List<String> ELEGANT_TUXEDO = List.of("ELEGANT_TUXEDO_CHESTPLATE",
            "ELEGANT_TUXEDO_LEGGINGS", "ELEGANT_TUXEDO_BOOTS");

    private KuudraFollowerRules() {
    }

    /**
     * On Hypixel the Follower NPCs appear after your first Kuudra win. Until the fight exists they
     * stand for everyone, so the parts of the line that don't need a win can be played.
     */
    public static boolean npcsVisible(SkyBlockPlayer player) {
        return !KuudraAvailability.isFightAvailable() || KuudraProgress.hasDefeatedKuudra(player);
    }

    /** The coin offering that turns a Wet Napkin into the Follower Helmet: 13331996 + 2·day² − 58·name length. */
    public static long helmetDonation(String playerName, Instant now) {
        long day = now.atOffset(QUEST_ZONE).getDayOfYear();
        return 13_331_996L + 2 * day * day - 58L * playerName.length();
    }

    public static boolean isCataclysmicEruption() {
        return VolcanoEruptionService.isErupting()
                && VolcanoEruptionService.currentExplosivity() == VolcanoExplosivity.CATACLYSMIC;
    }

    public static boolean wearingFollowerArmor(SkyBlockPlayer player) {
        return wearingSet(player, FOLLOWER_ARMOR);
    }

    public static boolean wearingEmberArmor(SkyBlockPlayer player) {
        return wearingSet(player, EMBER_ARMOR) || wearingSet(player, REKINDLED_EMBER_ARMOR);
    }

    public static boolean wearingElegantTuxedo(SkyBlockPlayer player) {
        return wearingSet(player, ELEGANT_TUXEDO);
    }

    public static boolean wearingBonzoMask(SkyBlockPlayer player) {
        List<String> worn = worn(player);
        return worn.contains("BONZO_MASK") || worn.contains("STARRED_BONZO_MASK");
    }

    public static boolean wearingRespirationHelmet(SkyBlockPlayer player) {
        EnchantmentType respiration = Arrays.stream(EnchantmentType.values())
                .filter(type -> type.name().equals("RESPIRATION")).findFirst().orElse(null);
        SkyBlockItem helmet = player.getArmor()[0];
        return respiration != null && helmet != null && helmet.getAttributeHandler().hasEnchantment(respiration);
    }

    public static boolean ownsFollowerArtifact(SkyBlockPlayer player) {
        ItemType artifact = ItemType.get("KUUDRA_FOLLOWER_ARTIFACT");
        return artifact != null && player.countItem(artifact) > 0;
    }

    public static int kuudraPetLevel(SkyBlockPlayer player) {
        SkyBlockItem pet = player.getPetData().getEnabledPet();
        if (pet == null || pet.getAttributeHandler().getPotentialType() == null
                || !"KUUDRA_PET".equals(pet.getAttributeHandler().getPotentialType().name())) return 0;
        return PetLevels.level(pet);
    }

    public static double intelligence(SkyBlockPlayer player) {
        return player.getStatistics().allStatistics().getOverall(ItemStatistic.INTELLIGENCE);
    }

    public static boolean holdingDrill(SkyBlockPlayer player) {
        SkyBlockItem held = new SkyBlockItem(player.getItemInMainHand());
        return held.hasComponent(DrillComponent.class);
    }

    public static boolean holding(SkyBlockPlayer player, String itemId) {
        SkyBlockItem held = new SkyBlockItem(player.getItemInMainHand());
        ItemType type = held.getAttributeHandler().getPotentialType();
        return type != null && type.name().equals(itemId);
    }

    public static boolean isPlhlegblastNear(SkyBlockPlayer player, Point point) {
        if (player.getInstance() == null) return false;
        for (Entity entity : player.getInstance().getNearbyEntities(point, PLHLEGBLAST_RANGE)) {
            if (entity instanceof SkyBlockMob mob && !mob.isDead() && "Plhlegblast".equals(mob.getDisplayName())) {
                return true;
            }
        }
        return false;
    }

    private static boolean wearingSet(SkyBlockPlayer player, List<String> ids) {
        return worn(player).containsAll(ids);
    }

    private static List<String> worn(SkyBlockPlayer player) {
        return Arrays.stream(player.getArmor())
                .filter(Objects::nonNull)
                .map(item -> item.getAttributeHandler().getPotentialType())
                .filter(Objects::nonNull)
                .map(ItemType::name)
                .toList();
    }
}
