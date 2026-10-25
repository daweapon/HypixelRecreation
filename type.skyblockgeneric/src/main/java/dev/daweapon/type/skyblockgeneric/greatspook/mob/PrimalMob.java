package dev.daweapon.type.skyblockgeneric.greatspook.mob;

import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.EquipmentSlot;
import net.minestom.server.entity.damage.Damage;
import net.minestom.server.instance.Instance;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import dev.daweapon.commons.skyblock.item.ItemType;
import dev.daweapon.commons.text.Text;
import dev.daweapon.type.skyblockgeneric.crimson.mob.CrimsonIsleMob;
import dev.daweapon.type.skyblockgeneric.crimson.mob.CrimsonMobProfile;
import dev.daweapon.type.skyblockgeneric.greatspook.GreatSpookFear;
import dev.daweapon.type.skyblockgeneric.greatspook.Manifestation;
import dev.daweapon.type.skyblockgeneric.greatspook.PrimalFearDialogue;
import dev.daweapon.type.skyblockgeneric.greatspook.PrimalFearRules;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;
import dev.daweapon.type.skyblockgeneric.user.pets.PetAbilities;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

public abstract class PrimalMob extends CrimsonIsleMob {
    private final UUID owner;
    private final String ownerName;
    private boolean challengeStarted;
    private boolean released;

    protected PrimalMob(CrimsonMobProfile profile, UUID owner, String ownerName) {
        super(profile);
        this.owner = owner;
        this.ownerName = ownerName;
    }

    public UUID owner() {
        return owner;
    }

    public String ownerName() {
        return ownerName;
    }

    protected boolean heldAtOneHealth() {
        return false;
    }

    protected void onChallengeStart(SkyBlockPlayer player) {
    }

    protected void onOwnerHit(SkyBlockPlayer player) {
    }

    protected void onGone() {
    }

    @Override
    public void kill() {
        super.kill();
        onGone();
    }

    @Override
    public void remove() {
        super.remove();
        onGone();
    }

    protected abstract Map<EquipmentSlot, ItemStack> appearance();

    protected @Nullable Manifestation manifestation() {
        return null;
    }

    protected boolean challengeStarted() {
        return challengeStarted;
    }

    protected boolean released() {
        return released;
    }

    void markChallengeStarted() {
        challengeStarted = true;
    }

    protected void release() {
        released = true;
        refreshName();
    }

    protected Optional<SkyBlockPlayer> ownerPlayer() {
        Instance instance = getInstance();
        if (instance == null) return Optional.empty();
        return instance.getPlayers().stream()
                .filter(player -> player.getUuid().equals(owner) && player instanceof SkyBlockPlayer)
                .map(player -> (SkyBlockPlayer) player)
                .findFirst();
    }

    protected void say(SkyBlockPlayer player, String text) {
        Manifestation kind = manifestation();
        player.sendMessage(Text.parseLenient(kind == null
                ? "<4>[FEAR] Primal Fear<r>: " + text : PrimalFearDialogue.line(kind, text)));
    }

    protected void finishWithKill(SkyBlockPlayer player) {
        released = true;
        setHealth(Math.min(getHealth(), 1));
        dev.daweapon.type.skyblockgeneric.utility.AttackService.applyHit(player, this, 1, false);
    }

    @Override
    protected boolean canTarget(SkyBlockPlayer player) {
        return player.getUuid().equals(owner);
    }

    @Override
    protected float aggroRange() {
        return 48;
    }

    @Override
    protected int strollRadius() {
        return 0;
    }

    @Override
    public boolean countsTowardVanquisher() {
        return false;
    }

    @Override
    protected boolean acceptsDamageFrom(SkyBlockPlayer player, Damage damage) {
        return player.getUuid().equals(owner);
    }

    @Override
    public float adjustHitDamage(SkyBlockPlayer attacker, float damage) {
        double fear = GreatSpookFear.fear(attacker);
        return PrimalFearRules.hitDamage(fear, PetAbilities.has(attacker, ItemType.EERIE_PET, "Fearama"));
    }

    @Override
    public double outgoingDamageMultiplier(SkyBlockPlayer target) {
        return PrimalFearRules.damageTakenMultiplier(GreatSpookFear.fear(target));
    }

    @Override
    protected Damage filterIncomingDamage(Damage damage) {
        float amount = damage.getAmount();
        if (heldAtOneHealth() && !released) {
            if (challengeStarted || getHealth() <= 1) return null;
            amount = Math.min(amount, getHealth() - 1);
        }
        if (amount <= 0) return null;
        return new Damage(damage.getType(), damage.getSource(), damage.getAttacker(), damage.getSourcePosition(), amount);
    }

    @Override
    protected void onDamagedBy(SkyBlockPlayer player, Damage damage) {
        if (isDead()) return;
        onOwnerHit(player);
        if (heldAtOneHealth() && !challengeStarted && !released && getHealth() <= 1) {
            challengeStarted = true;
            onChallengeStart(player);
        }
    }

    @Override
    public void refreshName() {
        Manifestation kind = manifestation();
        String name = kind == null ? "Primal Fear" : kind.displayName();
        updateCustomName(Text.parse("<c>" + name + " <c>" + "☠".repeat(PrimalFearRules.skulls(getHealth()))
                + "\n<e>Spawned by: <6>" + ownerName));
    }

    @Override
    public void onSpawn() {
        super.onSpawn();
        appearance().forEach(this::setEquipment);
        refreshName();
    }

    @Override
    protected void onKilledBy(SkyBlockPlayer killer) {
        killer.sendMessage(Text.parseLenient("<5><l>[FEAR] <r><e>You got some rewards from killing a <c>Primal Fear<e>!"));
        Instance instance = getInstance();
        if (instance == null) return;
        afterRewards(killer, instance, getPosition());
    }

    protected void afterRewards(SkyBlockPlayer killer, Instance instance, Pos position) {
    }

    protected static ItemStack leather(Material material, int rgb) {
        return ItemStack.builder(material)
                .set(net.minestom.server.component.DataComponents.DYED_COLOR, new net.minestom.server.color.Color(rgb))
                .build();
    }

    protected static double randomUnit() {
        return ThreadLocalRandom.current().nextDouble();
    }
}
