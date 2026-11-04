package dev.daweapon.type.skyblockgeneric.entity.mob.mobs.theend;

import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.Player;
import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.metadata.other.FallingBlockMeta;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.block.Block;
import net.minestom.server.network.packet.server.play.ParticlePacket;
import net.minestom.server.particle.Particle;
import net.minestom.server.timer.TaskSchedule;
import dev.daweapon.commons.StringUtility;
import dev.daweapon.commons.skyblock.item.ItemType;
import dev.daweapon.commons.skyblock.statistics.ItemStatistic;
import dev.daweapon.commons.text.Text;
import dev.daweapon.type.skyblockgeneric.SkyBlockGenericLoader;
import dev.daweapon.type.skyblockgeneric.crimson.mob.CrimsonIsleMob;
import dev.daweapon.type.skyblockgeneric.crimson.mob.CrimsonMobLoot;
import dev.daweapon.type.skyblockgeneric.crimson.mob.CrimsonMobProfile;
import dev.daweapon.type.skyblockgeneric.entity.mob.MobType;
import dev.daweapon.type.skyblockgeneric.item.SkyBlockItem;
import dev.daweapon.type.skyblockgeneric.loottable.LootAffector;
import dev.daweapon.type.skyblockgeneric.loottable.LootRoll;
import dev.daweapon.type.skyblockgeneric.theend.EndStoneProtectorLoot;
import dev.daweapon.type.skyblockgeneric.theend.ProtectorProgress;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * End Stone Protector (wiki: End_Stone_Protector): 5M health, 1,200 damage, 150 defense, immune to knockback,
 * one target at a time. Toss throws grounded players nearby into the air for true damage; Crush drops blocks of
 * iron onto players. The wiki gives no numbers for either ability: the damage, radius and timings below are
 * estimates.
 */
public class MobEndStoneProtector extends CrimsonIsleMob {
    public static final CrimsonMobProfile PROFILE = CrimsonMobProfile.builder("CORRUPTED_PROTECTOR_100", "End Stone Protector")
            .level(310)
            .entity(EntityType.IRON_GOLEM)
            .stats(5_000_000, 1_200)
            .defense(150)
            .speed(100)
            .rewards(2_325, 775, 930)
            .types(MobType.CONSTRUCT, MobType.ENDER)
            .bestiary(8, 20, "22bcaceeb4162f400d44743315932ac820d3119ac8986a0161a726161ccc93fc")
            .neverCorrupted()
            .build();

    private static final double ARENA = 30;
    private static final int TOSS_INTERVAL = 160;
    private static final double TOSS_RADIUS = 10;
    private static final double TOSS_TRUE_DAMAGE = 500;
    private static final double TOSS_LAUNCH = 18;
    private static final int CRUSH_INTERVAL = 240;
    private static final int CRUSH_TARGETS = 5;
    private static final double CRUSH_HEIGHT = 10;
    private static final int CRUSH_FALL_TICKS = 20;
    private static final double CRUSH_RADIUS = 2.5;
    private static final double CRUSH_DAMAGE = 2_000;

    public MobEndStoneProtector() {
        super(PROFILE);
    }

    @Override public boolean countsForBestiary() { return true; }
    @Override public boolean countsTowardVanquisher() { return false; }
    @Override public float getNameDisplayHeightOffset() { return 3f; }
    @Override protected float aggroRange() { return (float) ARENA; }
    @Override protected int strollRadius() { return 0; }

    @Override
    public void takeKnockback(float strength, double x, double z) {
    }

    @Override
    protected void onAiTick(int ticks) {
        if (ticks % TOSS_INTERVAL == 0) toss();
        if (ticks % CRUSH_INTERVAL == CRUSH_INTERVAL / 2) crush();
    }

    private void toss() {
        for (SkyBlockPlayer player : playersWithin(TOSS_RADIUS)) {
            if (!player.isOnGround()) continue;
            player.setVelocity(player.getVelocity().withY(TOSS_LAUNCH));
            trueStrike(player, TOSS_TRUE_DAMAGE);
        }
    }

    private void crush() {
        Instance instance = getInstance();
        if (instance == null) return;
        playersWithin(ARENA).stream().limit(CRUSH_TARGETS).forEach(player -> {
            Pos landing = player.getPosition();
            Entity block = new Entity(EntityType.FALLING_BLOCK);
            block.editEntityMeta(FallingBlockMeta.class, meta -> meta.setBlock(Block.IRON_BLOCK));
            block.setNoGravity(false);
            block.setInstance(instance, landing.add(0, CRUSH_HEIGHT, 0));
            block.scheduler().buildTask(() -> land(block, landing)).delay(TaskSchedule.tick(CRUSH_FALL_TICKS)).schedule();
        });
    }

    private void land(Entity block, Pos landing) {
        Instance instance = block.getInstance();
        block.remove();
        if (instance == null || isDead() || isRemoved()) return;
        instance.sendGroupedPacket(new ParticlePacket(Particle.EXPLOSION, true, true,
                landing.x(), landing.y() + 0.5, landing.z(), 0.6f, 0.3f, 0.6f, 0f, 4));
        for (SkyBlockPlayer player : playersWithin(ARENA)) {
            if (player.getPosition().distance(landing) <= CRUSH_RADIUS) strike(player, CRUSH_DAMAGE);
        }
    }

    @Override
    public void kill() {
        Instance instance = getInstance();
        Map<UUID, Double> damage = Map.copyOf(getDamageDealt());
        super.kill();
        if (instance != null) reward(instance, damage);
        ProtectorProgress.reset();
    }

    private void reward(Instance instance, Map<UUID, Double> damage) {
        List<Map.Entry<UUID, Double>> ranking = damage.entrySet().stream()
                .sorted(Map.Entry.<UUID, Double>comparingByValue(Comparator.reverseOrder())).toList();
        announce(instance, ranking);
        double top = ranking.isEmpty() ? 0 : ranking.getFirst().getValue();
        Set<String> claimed = new HashSet<>();
        for (int i = 0; i < ranking.size(); i++) {
            SkyBlockPlayer player = SkyBlockGenericLoader.getFromUUID(ranking.get(i).getKey());
            if (player == null) continue;
            int zealots = ProtectorProgress.contributed(player.getUuid());
            List<EndStoneProtectorLoot.Reward> rewards = EndStoneProtectorLoot.plan(i + 1, ranking.get(i).getValue(), top,
                    zealots, claimed, (itemId, chance) -> roll(player, itemId, chance));
            rewards.forEach(reward -> give(player, reward));
            player.getAchievementHandler().completeAchievement("skyblock.smell_like_roses");
        }
    }

    private boolean roll(SkyBlockPlayer player, String itemId, double chance) {
        ItemType type = ItemType.get(itemId);
        double boosted = LootAffector.MAGIC_FIND.apply(player, chance, this);
        if (type == ItemType.GOLEM_PET) {
            boosted *= 1 + player.getStatistics().allStatistics(player, this).getOverall(ItemStatistic.PET_LUCK) / 100;
        }
        return LootRoll.roll(player.getUuid(), type, boosted);
    }

    private void give(SkyBlockPlayer player, EndStoneProtectorLoot.Reward reward) {
        ItemType type = ItemType.get(reward.itemId());
        if (type == null) return;
        SkyBlockItem item = new SkyBlockItem(type, reward.amount());
        if (reward.rarity() != null) item.getAttributeHandler().setRarity(reward.rarity());
        CrimsonMobLoot.deliver(player, this, item, reward.rare());
    }

    private void announce(Instance instance, List<Map.Entry<UUID, Double>> ranking) {
        String bar = "<a><l>" + "\u25AC".repeat(64);
        for (Player viewer : instance.getPlayers()) {
            List<String> lines = new ArrayList<>(List.of(bar, "                    <6><l>END STONE PROTECTOR DOWN!", " "));
            for (int i = 0; i < Math.min(3, ranking.size()); i++) {
                SkyBlockPlayer damager = SkyBlockGenericLoader.getFromUUID(ranking.get(i).getKey());
                String name = damager == null ? "<7>Disconnected" : damager.getUsername();
                lines.add("            " + place(i + 1) + " Damager <7>- " + name + " <7>- <e>"
                        + StringUtility.commaify(Math.round(ranking.get(i).getValue())));
            }
            for (int i = 0; i < ranking.size(); i++) {
                if (!ranking.get(i).getKey().equals(viewer.getUuid())) continue;
                lines.add(" ");
                lines.add("            <e>Your Damage: <a>" + StringUtility.commaify(Math.round(ranking.get(i).getValue()))
                        + " <7>(Position #" + (i + 1) + ")");
            }
            lines.add(bar);
            lines.forEach(line -> viewer.sendMessage(Text.parse(line)));
        }
    }

    private static String place(int place) {
        return switch (place) {
            case 1 -> "<e><l>1st";
            case 2 -> "<6><l>2nd";
            default -> "<c><l>3rd";
        };
    }
}
