package dev.daweapon.type.catacombs.engine.bonzo;

import dev.daweapon.dungeons.catacombs.mob.DungeonMobDefinition;
import dev.daweapon.type.catacombs.engine.CatacombsMob;
import dev.daweapon.type.catacombs.engine.look.DungeonNameTag;
import dev.daweapon.type.catacombs.engine.look.MobDresser;
import dev.daweapon.type.catacombs.engine.look.MobLook;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.ai.GoalSelector;
import net.minestom.server.entity.ai.TargetSelector;
import net.minestom.server.entity.damage.Damage;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Bonzo as a player-skinned NPC, as captured: 250,000 health per life, movement speed 0.40 (SkyBlock speed 160), the
 * BOSS header over his name, each line he says shown in bold above it for three seconds. He never really dies: at zero
 * health he stays standing with a red 0 while the fight decides what happens next (his revive, or the end of the run).
 * In his first life he wanders the arena and never attacks in melee; in his second he chases the nearest player and
 * hits for the wiki's 2,664.
 */
public final class BonzoBoss extends CatacombsMob {
    public static final double SPEED = 160;
    private static final int LINE_TICKS = 60;
    private static final double WANDER_RADIUS = 15;
    private static final int REPATH_TICKS = 40;
    private static final double MELEE_RANGE = 2.6;
    private static final int MELEE_COOLDOWN = 20;

    public interface Fight extends Listener {
        void bonzoHurt(BonzoBoss bonzo);

        void bonzoLifeLost(BonzoBoss bonzo);
    }

    public enum Stance {STILL, WANDER, CHASE}

    private final Fight fight;
    private final Pos centre;
    private MobLook current;
    private boolean down;
    private boolean vulnerable;
    private Stance stance = Stance.STILL;
    private @Nullable String line;
    private int lineTicks;
    private int lastMelee;

    public BonzoBoss(DungeonMobDefinition definition, MobLook firstLife, Fight fight, Pos centre) {
        super(definition, -1, false, fight, firstLife, SPEED);
        this.fight = fight;
        this.centre = centre;
        this.current = firstLife;
        refreshName();
    }

    @Override
    public List<GoalSelector> getGoalSelectors() {
        return List.of();
    }

    @Override
    public List<TargetSelector> getTargetSelectors() {
        return List.of();
    }

    public void say(String said) {
        line = said;
        lineTicks = LINE_TICKS;
        refreshName();
    }

    public void stance(Stance next) {
        stance = next;
        if (next == Stance.STILL) getVanillaNavigator().stop();
    }

    public void vulnerable(boolean value) {
        vulnerable = value;
    }

    public boolean down() {
        return down;
    }

    /** His second life: the second look (skin and Bonzo's Staff), full health, back on his feet. */
    public void revive(MobLook revived) {
        current = revived;
        MobDresser.dress(this, revived.pick(ThreadLocalRandom.current()));
        down = false;
        heal();
        refreshName();
    }

    @Override
    protected String nameHeader() {
        String header = current == null || current.header() == null ? "" : current.header();
        return line == null ? header : "<l>" + line + "\n" + header;
    }

    @Override
    protected String nameLine() {
        if (current == null) return super.nameLine();
        return DungeonNameTag.format(current.types(), false, current.nameColour(), current.bold(), getProfile().displayName(),
                down ? 0 : getHealth(), getMaxHealth(), current.level(), current.showMaxHealth());
    }

    @Override
    protected Damage filterIncomingDamage(Damage damage) {
        if (!vulnerable || down) return null;
        if (damage.getAmount() >= getHealth()) damage.setAmount(Math.max(0, getHealth() - 1));
        return damage;
    }

    @Override
    public boolean damage(@NotNull Damage damage) {
        boolean hurt = super.damage(damage);
        if (!hurt || down) return hurt;
        fight.bonzoHurt(this);
        if (getHealth() <= 1) loseLife();
        return true;
    }

    @Override
    public void kill() {
        if (down || !vulnerable) return;
        setHealth(1);
        loseLife();
    }

    private void loseLife() {
        down = true;
        vulnerable = false;
        stance(Stance.STILL);
        refreshName();
        fight.bonzoLifeLost(this);
    }

    @Override
    protected void onAiTick(int ticks) {
        if (lineTicks > 0 && --lineTicks == 0) {
            line = null;
            refreshName();
        }
        switch (stance) {
            case STILL -> {
            }
            case WANDER -> {
                if (ticks % REPATH_TICKS == 0 || getVanillaNavigator().isComplete()) wander();
            }
            case CHASE -> chase(ticks);
        }
    }

    private void wander() {
        ThreadLocalRandom random = ThreadLocalRandom.current();
        double angle = random.nextDouble(Math.PI * 2);
        double distance = random.nextDouble(WANDER_RADIUS);
        getVanillaNavigator().pathTo(centre.add(Math.cos(angle) * distance, 0, Math.sin(angle) * distance));
    }

    private void chase(int ticks) {
        SkyBlockPlayer target = nearestPlayer(48, player -> true).orElse(null);
        if (target == null) return;
        if (ticks % 5 == 0) getVanillaNavigator().pathTo(target.getPosition());
        if (getPosition().distance(target.getPosition()) <= MELEE_RANGE && ticks - lastMelee >= MELEE_COOLDOWN) {
            lastMelee = ticks;
            swingMainHand();
            strike(target, definition().damage());
        }
    }

    @Override
    protected void onKilledBy(SkyBlockPlayer killer) {
    }
}
