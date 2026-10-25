package dev.daweapon.type.skyblockgeneric.greatspook.mob;

import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.EquipmentSlot;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.kyori.adventure.title.Title;
import dev.daweapon.commons.text.Text;
import dev.daweapon.type.skyblockgeneric.greatspook.Manifestation;
import dev.daweapon.type.skyblockgeneric.greatspook.PrimalFearDialogue;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;

import java.time.Duration;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

public class DeadlineMob extends PrimalMob {
    public static final long COUNTDOWN_MILLIS = 5_000;
    private static final double MOVE_TOLERANCE = 0.08;

    private Pos anchor;
    private long countdownStart;
    private int shownSecond = -1;

    public DeadlineMob(UUID owner, String ownerName) {
        super(PrimalProfiles.of(Manifestation.DEADLINE.mobId(), Manifestation.DEADLINE.displayName()), owner, ownerName);
    }

    public static int secondsLeft(long startedAt, long now) {
        return (int) Math.max(0, Math.ceil((COUNTDOWN_MILLIS - (now - startedAt)) / 1000D));
    }

    public static boolean moved(Pos anchor, Pos now) {
        return Math.abs(anchor.x() - now.x()) > MOVE_TOLERANCE || Math.abs(anchor.y() - now.y()) > MOVE_TOLERANCE
                || Math.abs(anchor.z() - now.z()) > MOVE_TOLERANCE;
    }

    @Override
    protected Manifestation manifestation() {
        return Manifestation.DEADLINE;
    }

    @Override
    protected boolean heldAtOneHealth() {
        return true;
    }

    @Override
    protected Map<EquipmentSlot, ItemStack> appearance() {
        return Map.of(EquipmentSlot.HELMET, ItemStack.of(Material.SKELETON_SKULL),
                EquipmentSlot.CHESTPLATE, leather(Material.LEATHER_CHESTPLATE, 0x1A1A1A),
                EquipmentSlot.LEGGINGS, leather(Material.LEATHER_LEGGINGS, 0x1A1A1A),
                EquipmentSlot.BOOTS, leather(Material.LEATHER_BOOTS, 0x1A1A1A));
    }

    @Override
    protected void onChallengeStart(SkyBlockPlayer player) {
        say(player, PrimalFearDialogue.DEADLINE.get(ThreadLocalRandom.current().nextInt(PrimalFearDialogue.DEADLINE.size())));
        anchor = player.getPosition();
        countdownStart = System.currentTimeMillis();
    }

    @Override
    protected void onAiTick(int ticks) {
        if (!challengeStarted() || released()) return;
        ownerPlayer().ifPresent(this::tickCountdown);
    }

    private void tickCountdown(SkyBlockPlayer player) {
        long now = System.currentTimeMillis();
        if (moved(anchor, player.getPosition())) {
            anchor = player.getPosition();
            countdownStart = now;
        }
        int left = secondsLeft(countdownStart, now);
        if (left <= 0) {
            release();
            player.showTitle(Text.of("<c>Curse Lifted!"), Text.of(""),
                    Title.Times.times(Duration.ZERO, Duration.ofSeconds(1), Duration.ofMillis(300)));
            return;
        }
        if (left == shownSecond) return;
        shownSecond = left;
        player.showTitle(Text.of("<4><l>STAND STILL"), Text.parseLenient("<c>" + left + "s"),
                Title.Times.times(Duration.ZERO, Duration.ofMillis(1500), Duration.ofMillis(200)));
    }
}
