package net.swofty.type.skyblockgeneric.crimson.matriarch;

import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;
import net.minestom.server.component.DataComponents;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.metadata.display.ItemDisplayMeta;
import net.minestom.server.entity.metadata.other.InteractionMeta;
import net.minestom.server.instance.Instance;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.minestom.server.network.packet.server.play.HitAnimationPacket;
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
 * One player's trip through the Belly of the Beast (wiki: Belly_of_the_Beast, Heavy_Pearl). Three piles of Heavy
 * Pearls each pay one pearl after 3 hits while the Matriarch has pearls left, and the Matriarch Cubs attribute can add
 * one more. Stomach acid floods the belly a layer at a time and burns for 5 Health a second. Punching both tonsils, the
 * towers of pigs hanging from the roof, gets the player spat out; a Flaming Fist pops each tonsil in one hit.
 */
final class BellyRun {
    static final Pos ENTRY = new Pos(-541.5, 12, -888.5);
    static final List<Pos> PILES = List.of(new Pos(-537.5, 12, -896.5), new Pos(-545.5, 12, -894.5), new Pos(-540.5, 12, -881.5));
    static final List<Pos> TONSILS = List.of(new Pos(-544.5, 23.2, -884.5), new Pos(-535.5, 24.2, -884.5));
    static final double TONSIL_BOTTOM_Y = 14.2;
    static final double PIG_HEIGHT = 0.9;
    static final int HITS_PER_PILE = 3;
    static final int HITS_PER_TONSIL = 3;
    static final int HIT_COOLDOWN_TICKS = 5;
    static final float ACID_DAMAGE_PER_SECOND = 5;
    static final String HEAVY_PEARL_MODEL = "hypixel_skyblock:item/uncategorized/heavy_pearl";
    private static final AttributeId MATRIARCH_CUBS = AttributeId.parse("L27");

    private final SkyBlockPlayer player;
    private final List<Entity> visuals = new ArrayList<>();
    private final Map<Entity, Pile> piles = new HashMap<>();
    private final Map<Entity, Tonsil> tonsilParts = new HashMap<>();
    private final List<Tonsil> tonsils = new ArrayList<>();
    private StomachAcid acid;
    private int ticks;

    BellyRun(SkyBlockPlayer player) {
        this.player = player;
    }

    static ItemStack heavyPearlModel() {
        return ItemStack.builder(Material.PAPER).set(DataComponents.ITEM_MODEL, HEAVY_PEARL_MODEL).build();
    }

    void begin(Instance instance) {
        acid = new StomachAcid(player, instance);
        player.teleport(ENTRY);
        player.setHealth(player.getMaxHealth());
        player.sendMessage("<6>The Matriarch <7>swallowed you whole! Grab the <6>Heavy Pearls <7>and punch the tonsils to escape!");
        for (Pos at : PILES) {
            Pile pile = new Pile();
            piles.put(hitbox(instance, at, 1.2f, 1.0f), pile);
            for (int i = 0; i < HITS_PER_PILE; i++) {
                double angle = Math.PI * 2 * i / HITS_PER_PILE;
                pile.pearls.add(pearl(instance, at.add(Math.cos(angle) * 0.25, 0.25 + i * 0.12, Math.sin(angle) * 0.25)));
            }
        }
        for (Pos top : TONSILS) {
            Tonsil tonsil = new Tonsil();
            for (double y = top.y(); y >= TONSIL_BOTTOM_Y; y -= PIG_HEIGHT) {
                Entity pig = new Entity(EntityType.PIG);
                pig.setNoGravity(true);
                show(instance, pig, new Pos(top.x(), y, top.z(), (float) (y * 40), 0));
                tonsil.pigs.add(pig);
                tonsilParts.put(pig, tonsil);
            }
            tonsils.add(tonsil);
        }
    }

    private Entity hitbox(Instance instance, Pos at, float width, float height) {
        Entity box = new Entity(EntityType.INTERACTION);
        box.editEntityMeta(InteractionMeta.class, meta -> {
            meta.setWidth(width);
            meta.setHeight(height);
        });
        show(instance, box, at);
        return box;
    }

    private Entity pearl(Instance instance, Pos at) {
        Entity display = new Entity(EntityType.ITEM_DISPLAY);
        display.setNoGravity(true);
        display.editEntityMeta(ItemDisplayMeta.class, meta -> {
            meta.setItemStack(heavyPearlModel());
            meta.setScale(new Vec(0.6, 0.6, 0.6));
        });
        show(instance, display, at);
        return display;
    }

    private void show(Instance instance, Entity entity, Pos at) {
        entity.setAutoViewable(false);
        entity.setInstance(instance, at);
        entity.addViewer(player);
        visuals.add(entity);
    }

    /** Returns true when the hit belonged to this run. */
    boolean hit(Entity target) {
        Tonsil tonsil = tonsilParts.get(target);
        if (tonsil != null) {
            punchTonsil(tonsil);
            return true;
        }
        Pile pile = piles.get(target);
        if (pile == null) return false;
        if (ticks - pile.lastHit < HIT_COOLDOWN_TICKS) return true;
        pile.lastHit = ticks;
        if (!pile.pearls.isEmpty()) pile.pearls.removeLast().remove();
        sound("block.amethyst_block.hit", 0.8f + pile.pearls.size() * 0.2f);
        if (++pile.hits < HITS_PER_PILE) return true;
        piles.remove(target);
        target.remove();
        pile.pearls.forEach(Entity::remove);
        collectPearl();
        return true;
    }

    private void punchTonsil(Tonsil tonsil) {
        if (tonsil.popped || ticks - tonsil.lastHit < HIT_COOLDOWN_TICKS) return;
        tonsil.lastHit = ticks;
        boolean flamingFist = new SkyBlockItem(player.getItemInMainHand()).getAttributeHandler().getPotentialType() == ItemType.FLAMING_FIST;
        tonsil.hits += flamingFist ? HITS_PER_TONSIL : 1;
        tonsil.pigs.forEach(pig -> player.sendPacket(new HitAnimationPacket(pig.getEntityId(), 0f)));
        sound("entity.pig.hurt", 0.6f);
        if (tonsil.hits < HITS_PER_TONSIL) return;
        tonsil.popped = true;
        for (Entity pig : tonsil.pigs) {
            MythologicalCombat.particles(player.getInstance(), Particle.POOF, pig.getPosition().add(0, 0.4, 0), 0.3f, 4);
            pig.remove();
        }
        if (tonsils.stream().allMatch(each -> each.popped)) {
            MatriarchService.escape(player);
        } else {
            player.sendMessage("<7>The Matriarch gags! Punch the other tonsil to get out!");
        }
    }

    private void collectPearl() {
        if (!HeavyPearls.take(player)) {
            player.sendMessage("<c>The Matriarch has no Heavy Pearls left today!");
            return;
        }
        player.addAndUpdateItem(new SkyBlockItem(ItemType.HEAVY_PEARL));
        player.sendMessage("<a>You collected a <6>Heavy Pearl<a>!");
        sound("entity.experience_orb.pickup", 1f);
        double cubs = AttributeEffectService.value(player.getHuntingData(), MATRIARCH_CUBS);
        if (cubs > 0 && LootRoll.roll(player.getUuid(), ItemType.HEAVY_PEARL, cubs)) {
            player.addAndUpdateItem(new SkyBlockItem(ItemType.HEAVY_PEARL));
            player.sendMessage("<7>Your <6>Matriarch Cubs <7>attribute has granted you <61 <7>additional <6>Heavy Pearl<7>!");
        }
    }

    /** Returns false once the run is over. */
    boolean tick() {
        ticks++;
        if (!player.isOnline() || player.getInstance() == null) return false;
        if (player.isDead()) {
            HeavyPearls.startCooldown(player);
            return false;
        }
        acid.rise(ticks);
        if (ticks % 10 == 0) {
            double surface = acid.surfaceLayer(ticks) + 0.9;
            for (Pos pile : PILES) {
                MythologicalCombat.particles(player.getInstance(), Particle.ITEM_SLIME, new Pos(pile.x(), surface, pile.z()), 3f, 6);
            }
        }
        if (ticks % 20 == 0 && acid.touches(player.getPosition(), ticks)) {
            player.sendMessage("<2>The stomach acid burns you!");
            if (player.getHealth() <= ACID_DAMAGE_PER_SECOND) {
                HeavyPearls.startCooldown(player);
                player.sendMessage("<c>You were digested by the Matriarch! You can't be eaten again for 10 minutes.");
                MatriarchService.escape(player);
                return false;
            }
            player.setHealth(player.getHealth() - ACID_DAMAGE_PER_SECOND);
        }
        return true;
    }

    void end() {
        if (acid != null && player.isOnline()) acid.drain();
        visuals.forEach(entity -> {
            if (!entity.isRemoved()) entity.remove();
        });
        visuals.clear();
        piles.clear();
        tonsilParts.clear();
        tonsils.clear();
    }

    private void sound(String key, float pitch) {
        player.playSound(Sound.sound(Key.key(key), Sound.Source.MASTER, 1f, pitch), Sound.Emitter.self());
    }

    private static final class Pile {
        private final List<Entity> pearls = new ArrayList<>();
        private int hits;
        private int lastHit = -HIT_COOLDOWN_TICKS;
    }

    private static final class Tonsil {
        private final List<Entity> pigs = new ArrayList<>();
        private int hits;
        private int lastHit = -HIT_COOLDOWN_TICKS;
        private boolean popped;
    }
}
