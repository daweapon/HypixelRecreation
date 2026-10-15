package net.swofty.type.skyblockgeneric.entity.mob.mobs.crystalhollows;

import net.minestom.server.entity.ai.TargetSelector;
import net.swofty.commons.skyblock.item.ItemType;
import net.swofty.type.skyblockgeneric.crystalhollows.mob.CrystalHollowsMob;
import net.swofty.type.skyblockgeneric.entity.mob.impl.RegionPopulator;
import net.swofty.type.skyblockgeneric.item.SkyBlockItem;
import net.swofty.type.skyblockgeneric.region.RegionType;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;

/**
 * The Kalhuiki tribe (wiki: Kalhuiki Tribe Member): passive until hit three times or until a Youngling is hit, and
 * never hostile to a player wearing the Kalhuiki Mask. The wiki does not say whether an angry member calls the rest
 * of the tribe; the alert radius, anger duration and which lines are spoken are estimates.
 */
public abstract class KalhuikiMob extends CrystalHollowsMob implements RegionPopulator {
    private static final int HITS_TO_ANGER = 3;
    private static final double ALERT_RADIUS = 20;
    private static final long ANGER_MILLIS = 60_000;
    private static final List<String> HURT_LINES = List.of("Stop that!", "This hurts!");
    private static final List<String> ANGRY_LINES = List.of("Let's settle this.", "That's enough! Get over here!");

    private final Map<UUID, Integer> hits = new ConcurrentHashMap<>();
    private final Map<UUID, Long> angryUntil = new ConcurrentHashMap<>();

    protected KalhuikiMob() {
        super(HollowsSkins.PLAYER_MODEL);
    }

    @Override
    public void onSpawn() {
        super.onSpawn();
        HollowsSkins.dressAsKalhuiki(this);
    }

    @Override
    protected RegionType homeRegion() {
        return RegionType.JUNGLE;
    }

    @Override
    public boolean countsForBestiary() {
        return false;
    }

    @Override
    public List<TargetSelector> getTargetSelectors() {
        return List.of(new KalhuikiTarget(this));
    }

    public static boolean wearingMask(SkyBlockPlayer player) {
        if (player.getHelmet().isAir()) return false;
        return new SkyBlockItem(player.getHelmet()).getAttributeHandler().getPotentialType() == ItemType.KALHUIKI_MASK;
    }

    public boolean isAngryAt(SkyBlockPlayer player) {
        if (wearingMask(player)) return false;
        Long until = angryUntil.get(player.getUuid());
        return until != null && until > System.currentTimeMillis();
    }

    protected boolean angersTribeWhenHit() {
        return false;
    }

    @Override
    protected void onHitBy(SkyBlockPlayer player) {
        if (wearingMask(player)) return;
        if (angersTribeWhenHit()) {
            alertTribe(player);
            speak(player, HURT_LINES);
            return;
        }
        if (hits.merge(player.getUuid(), 1, Integer::sum) >= HITS_TO_ANGER) {
            alertTribe(player);
            speak(player, ANGRY_LINES);
        } else {
            speak(player, HURT_LINES);
        }
    }

    private void alertTribe(SkyBlockPlayer player) {
        if (getInstance() == null) return;
        for (var entity : getInstance().getNearbyEntities(getPosition(), ALERT_RADIUS)) {
            if (entity instanceof KalhuikiMob member) member.becomeAngryAt(player);
        }
    }

    private void becomeAngryAt(SkyBlockPlayer player) {
        angryUntil.put(player.getUuid(), System.currentTimeMillis() + ANGER_MILLIS);
    }

    private void speak(SkyBlockPlayer player, List<String> lines) {
        player.sendMessage("<c>{}<f>: {}", getDisplayName(), lines.get(ThreadLocalRandom.current().nextInt(lines.size())));
    }
}
