package net.swofty.type.skyblockgeneric.crimson.matriarch;

import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.metadata.display.ItemDisplayMeta;
import net.minestom.server.entity.metadata.other.InteractionMeta;
import net.minestom.server.instance.Instance;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.minestom.server.particle.Particle;
import net.swofty.commons.skyblock.item.ItemType;
import net.swofty.type.skyblockgeneric.hunting.AttributeEffectService;
import net.swofty.type.skyblockgeneric.hunting.AttributeId;
import net.swofty.type.skyblockgeneric.item.SkyBlockItem;
import net.swofty.type.skyblockgeneric.loottable.LootRoll;
import net.swofty.type.skyblockgeneric.mythological.ability.MythologicalCombat;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * One player's trip through the Belly of the Beast (wiki: Belly_of_the_Beast). Three pearl piles each give a Heavy
 * Pearl after 3 hits while the Matriarch has pearls left, the Matriarch Cubs attribute can add one more, stomach
 * acid rises from the floor, and punching the tonsils (pigs hanging from the roof) gets the player out.
 */
final class BellyRun {
    static final Pos ENTRY = new Pos(-541.5, 12, -888.5);
    static final List<Pos> PILES = List.of(new Pos(-537.5, 12, -896.5), new Pos(-545.5, 12, -894.5), new Pos(-540.5, 12, -881.5));
    static final Pos TONSILS = new Pos(-538.5, 21, -884.5);
    static final int HITS_PER_PILE = 3;
    static final int HITS_TO_ESCAPE = 5;
    static final double ACID_START_Y = 11;
    static final double ACID_MAX_Y = 18;
    static final int ACID_RISE_TICKS = 200;
    static final double ACID_SHARE_PER_SECOND = 0.05;
    private static final AttributeId MATRIARCH_CUBS = AttributeId.parse("L27");

    private final SkyBlockPlayer player;
    private final Map<Entity, Integer> piles = new HashMap<>();
    private final List<Entity> visuals = new ArrayList<>();
    private Entity tonsils;
    private int tonsilHits;
    private int ticks;

    BellyRun(SkyBlockPlayer player) {
        this.player = player;
    }

    void begin(Instance instance) {
        player.clearEffects();
        player.teleport(ENTRY);
        player.sendMessage("<6>The Matriarch <7>swallowed you whole! Grab the <6>Heavy Pearls <7>and punch the tonsils to escape!");
        for (Pos pile : PILES) {
            piles.put(hitbox(instance, pile, 1.2f, 1.2f), 0);
            visual(instance, pile.add(0, 0.6, 0), Material.ENDER_PEARL);
        }
        tonsils = hitbox(instance, TONSILS.sub(0, 1.5, 0), 1.2f, 3.5f);
        for (int i = 0; i < 3; i++) {
            Entity pig = new Entity(EntityType.PIG);
            pig.setNoGravity(true);
            pig.setAutoViewable(false);
            pig.setInstance(instance, TONSILS.sub(0, i * 0.9, 0));
            pig.addViewer(player);
            visuals.add(pig);
        }
    }

    private Entity hitbox(Instance instance, Pos at, float width, float height) {
        Entity box = new Entity(EntityType.INTERACTION);
        box.editEntityMeta(InteractionMeta.class, meta -> {
            meta.setWidth(width);
            meta.setHeight(height);
        });
        box.setAutoViewable(false);
        box.setInstance(instance, at);
        box.addViewer(player);
        visuals.add(box);
        return box;
    }

    private void visual(Instance instance, Pos at, Material material) {
        Entity display = new Entity(EntityType.ITEM_DISPLAY);
        display.setNoGravity(true);
        display.editEntityMeta(ItemDisplayMeta.class, meta -> meta.setItemStack(ItemStack.of(material)));
        display.setAutoViewable(false);
        display.setInstance(instance, at);
        display.addViewer(player);
        visuals.add(display);
    }

    /** Returns true when the hit belonged to this run. */
    boolean hit(Entity target) {
        if (target == tonsils) {
            if (++tonsilHits >= HITS_TO_ESCAPE) MatriarchService.escape(player);
            return true;
        }
        Integer hits = piles.get(target);
        if (hits == null) return false;
        if (hits + 1 < HITS_PER_PILE) {
            piles.put(target, hits + 1);
            return true;
        }
        piles.remove(target);
        target.remove();
        visuals.stream().filter(entity -> entity.getEntityType() == EntityType.ITEM_DISPLAY
                && entity.getPosition().distance(target.getPosition()) < 1.5).findFirst().ifPresent(Entity::remove);
        collectPearl();
        return true;
    }

    private void collectPearl() {
        if (!HeavyPearls.take(player)) {
            player.sendMessage("<c>The Matriarch has no Heavy Pearls left today!");
            return;
        }
        player.addAndUpdateItem(new SkyBlockItem(ItemType.HEAVY_PEARL));
        player.sendMessage("<a>You collected a <6>Heavy Pearl<a>!");
        double cubs = AttributeEffectService.value(player.getHuntingData(), MATRIARCH_CUBS);
        if (cubs > 0 && LootRoll.roll(player.getUuid(), ItemType.HEAVY_PEARL, cubs)) {
            player.addAndUpdateItem(new SkyBlockItem(ItemType.HEAVY_PEARL));
            player.sendMessage("<7>Your <6>Matriarch Cubs <7>attribute has granted you <61 <7>additional <6>Heavy Pearl<7>!");
        }
    }

    double acidLevel() {
        return Math.min(ACID_MAX_Y, ACID_START_Y + (double) ticks / ACID_RISE_TICKS);
    }

    /** Returns false once the run is over. */
    boolean tick() {
        ticks++;
        if (!player.isOnline() || player.getInstance() == null) return false;
        if (player.isDead()) {
            HeavyPearls.startCooldown(player);
            return false;
        }
        double acid = acidLevel();
        if (ticks % 10 == 0) {
            for (Pos pile : PILES) {
                MythologicalCombat.particles(player.getInstance(), Particle.ITEM_SLIME, new Pos(pile.x(), acid + 0.2, pile.z()), 3f, 6);
            }
        }
        if (ticks % 20 == 0 && player.getPosition().y() < acid + 0.5) {
            player.sendMessage("<2>The stomach acid burns you!");
            float burn = (float) (player.getMaxHealth() * ACID_SHARE_PER_SECOND);
            if (player.getHealth() <= burn) {
                HeavyPearls.startCooldown(player);
                player.sendMessage("<c>You were digested by the Matriarch! You can't be eaten again for 10 minutes.");
                MatriarchService.escape(player);
                return false;
            }
            player.setHealth(player.getHealth() - burn);
        }
        return true;
    }

    void end() {
        visuals.forEach(entity -> {
            if (!entity.isRemoved()) entity.remove();
        });
        visuals.clear();
        piles.clear();
    }
}
