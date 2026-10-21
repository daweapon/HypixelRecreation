package dev.daweapon.type.skyblockgeneric.hunting.creature.special;

final class LassoSpecials {
    private LassoSpecials() {
    }

    static void register() {
        CreatureSpecials.register("LASSO_CHARGE_BREAKS", new LassoChargeBreaks());
        CreatureSpecials.register("LASSO_DRAG_UP", new LassoDragUp());
        CreatureSpecials.register("LASSO_THROWS_PLAYER", new LassoThrowsPlayer());
        CreatureSpecials.register("LASSO_FLY_CHARGE", new LassoFlyCharge());
        CreatureSpecials.register("GRIZZLY_LASSO_THEN_BLACK_HOLE", new GrizzlyLassoThenBlackHole());
        CreatureSpecials.register("FLEE", new Flee());
        CreatureSpecials.register("TIMED_FLY_AWAY_5M", new TimedFlyAway5m());
        CreatureSpecials.register("SWOOP_AT_PLAYER", new SwoopAtPlayer());
        CreatureSpecials.register("CURL_WHEN_NEAR_10", new CurlWhenNear10());
        CreatureSpecials.register("LOOK_AWAY_TELEPORT_ATTACK", new LookAwayTeleportAttack());
    }
}
