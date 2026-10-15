package dev.daweapon.type.skyblockgeneric.item.set.sets.terror;

import dev.daweapon.type.skyblockgeneric.item.set.impl.TieredArmorSet;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;

import java.math.BigDecimal;
import java.util.List;

public abstract class HydraStrikeArmorSet implements TieredArmorSet {
    private final TerrorTier tier;

    protected HydraStrikeArmorSet(TerrorTier tier) {
        this.tier = tier;
    }

    @Override
    public String getName() {
        return "Hydra Strike";
    }

    @Override
    public int minimumPieces() {
        return TerrorTier.MINIMUM_PIECES;
    }

    @Override
    public int getWornPieceCount(SkyBlockPlayer player) {
        HydraStrike.Wear wear = HydraStrike.wear(player);
        return wear == null ? 0 : wear.pieces();
    }

    @Override
    public List<String> getDescription(int wornPieces) {
        return List.of(
                "<7>Every <a>0.2s<7>, arrow attacks grant <c>1",
                "<7>stack of <6>⁑ Hydra Strike<7>. <8>Lose 1 stack",
                "<8>after " + number(TerrorTier.decayMillis(wornPieces) / 1000.0) + "s of not gaining a stack.",
                "",
                "<7>Each stack grants <c>+" + number(tier.damagePerStack(wornPieces)) + "% Damage<7> and <b>+"
                        + number(tier.arrowSpeedPerStack()) + "%",
                "<b>Arrow Speed<7>.",
                "",
                "<7>At <c>10<7> stacks shoot <a>+" + HydraStrike.EXTRA_ARROWS + "<7> arrows that deal <c>"
                        + number(tier.extraArrowDamage()) + "%",
                "<c>Arrow Damage<8>.");
    }

    private static String number(double value) {
        return BigDecimal.valueOf(value).stripTrailingZeros().toPlainString();
    }

    public static final class Basic extends HydraStrikeArmorSet {
        public Basic() {
            super(TerrorTier.BASIC);
        }
    }

    public static final class Hot extends HydraStrikeArmorSet {
        public Hot() {
            super(TerrorTier.HOT);
        }
    }

    public static final class Burning extends HydraStrikeArmorSet {
        public Burning() {
            super(TerrorTier.BURNING);
        }
    }

    public static final class Fiery extends HydraStrikeArmorSet {
        public Fiery() {
            super(TerrorTier.FIERY);
        }
    }

    public static final class Infernal extends HydraStrikeArmorSet {
        public Infernal() {
            super(TerrorTier.INFERNAL);
        }
    }
}
