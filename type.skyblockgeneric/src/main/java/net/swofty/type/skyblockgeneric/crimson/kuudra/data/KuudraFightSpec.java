package net.swofty.type.skyblockgeneric.crimson.kuudra.data;

import net.swofty.type.skyblockgeneric.crimson.kuudra.KuudraTier;

import java.util.HashMap;
import java.util.Map;

/** The fight's tunable numbers from {@code fight.yml}; most are estimates until real captures arrive. */
public record KuudraFightSpec(Supplies supplies, Build build, Fuel fuel, Boss boss, Tentacles tentacles,
                              Cannons cannons, Tokens tokens, Support support, Lives lives, Waves waves, Mobs mobs) {

    static KuudraFightSpec parse(Map<String, Object> yaml) {
        return new KuudraFightSpec(
                Supplies.parse(Yamls.map(yaml.get("supplies"))),
                Build.parse(Yamls.map(yaml.get("build"))),
                Fuel.parse(Yamls.map(yaml.get("fuel"))),
                Boss.parse(Yamls.map(yaml.get("kuudra"))),
                Tentacles.parse(Yamls.map(yaml.get("tentacles"))),
                Cannons.parse(Yamls.map(yaml.get("cannons"))),
                Tokens.parse(Yamls.map(yaml.get("tokens"))),
                Support.parse(Yamls.map(yaml.get("support"))),
                Lives.parse(Yamls.map(yaml.get("lives"))),
                Waves.parse(Yamls.map(yaml.get("waves"))),
                Mobs.parse(Yamls.map(yaml.get("mobs"))));
    }

    private static Map<KuudraTier, Double> perTier(Object yaml) {
        Map<KuudraTier, Double> values = Yamls.byTier(yaml, value -> ((Number) value).doubleValue());
        for (KuudraTier tier : KuudraTier.values()) {
            if (!values.containsKey(tier)) throw new IllegalArgumentException("fight.yml is missing " + tier + " in " + yaml);
        }
        return Map.copyOf(values);
    }

    private static Map<String, Double> named(Object yaml) {
        Map<String, Double> values = new HashMap<>();
        Yamls.map(yaml).forEach((name, value) -> values.put(name, ((Number) value).doubleValue()));
        return Map.copyOf(values);
    }

    public record Supplies(int crates, double crateDistance, double crateJitter, double hookRadius,
                           double reelPullBlocks, double pickupRange, Map<KuudraTier, Double> pickupSeconds,
                           double moveTolerance, int carryTimeoutSeconds, double dropDamageFraction,
                           int pickupSlownessAmplifier, double placeRange, Map<String, Double> organBonus) {
        static Supplies parse(Map<String, Object> yaml) {
            return new Supplies(Yamls.integer(yaml, "crates", 6), Yamls.decimal(yaml, "crate_distance", 8),
                    Yamls.decimal(yaml, "crate_jitter", 3), Yamls.decimal(yaml, "hook_radius", 3.5),
                    Yamls.decimal(yaml, "reel_pull_blocks", 3), Yamls.decimal(yaml, "pickup_range", 4.5),
                    perTier(yaml.get("pickup_seconds")), Yamls.decimal(yaml, "move_tolerance", 0.35),
                    Yamls.integer(yaml, "carry_timeout_seconds", 30), Yamls.decimal(yaml, "drop_damage_fraction", 0.25),
                    Yamls.integer(yaml, "pickup_slowness_amplifier", 2), Yamls.decimal(yaml, "place_range", 2.5),
                    named(yaml.get("organ_bonus")));
        }
    }

    public record Build(double pileSeconds, int clickHoldTicks, double idleDecayProgress, int idleDecaySeconds,
                        double buildRange, int mobHitIntervalSeconds,
                        double mobHitRange, double mobHitProgress, int damageBarkCooldownSeconds,
                        int freshToolsSeconds, int freshToolsRollSeconds, Map<String, Double> organBonus) {
        static Build parse(Map<String, Object> yaml) {
            return new Build(Yamls.decimal(yaml, "pile_seconds", 6), Yamls.integer(yaml, "click_hold_ticks", 10),
                    Yamls.decimal(yaml, "idle_decay_progress", 3), Yamls.integer(yaml, "idle_decay_seconds", 2),
                    Yamls.decimal(yaml, "build_range", 4.5), Yamls.integer(yaml, "mob_hit_interval_seconds", 2),
                    Yamls.decimal(yaml, "mob_hit_range", 6), Yamls.decimal(yaml, "mob_hit_progress", 2),
                    Yamls.integer(yaml, "damage_bark_cooldown_seconds", 10), Yamls.integer(yaml, "fresh_tools_seconds", 10),
                    Yamls.integer(yaml, "fresh_tools_roll_seconds", 1), named(yaml.get("organ_bonus")));
        }
    }

    public record Fuel(int cells, double loadRange, int shotCooldownTicks, double aimToleranceDegrees,
                       double boltSpeed) {
        static Fuel parse(Map<String, Object> yaml) {
            return new Fuel(Yamls.integer(yaml, "cells", 4), Yamls.decimal(yaml, "load_range", 4),
                    Yamls.integer(yaml, "shot_cooldown_ticks", 20), Yamls.decimal(yaml, "aim_tolerance_degrees", 25),
                    Yamls.decimal(yaml, "bolt_speed", 3));
        }
    }

    public record Boss(double health, int size, Map<KuudraTier, Double> shotDamage, int surfaceSeconds,
                       int shotsPerSurface, int submergedSeconds, int moveTicks, double surfacedDepth,
                       double submergedDepth, boolean playerHitsDamage, double unstunnedShotFactor,
                       int futileCooldownSeconds, int shotTokens) {
        static Boss parse(Map<String, Object> yaml) {
            return new Boss(Yamls.decimal(yaml, "health", 100_000), Yamls.integer(yaml, "size", 30),
                    perTier(yaml.get("shot_damage")), Yamls.integer(yaml, "surface_seconds", 15),
                    Yamls.integer(yaml, "shots_per_surface", 4), Yamls.integer(yaml, "submerged_seconds", 8),
                    Yamls.integer(yaml, "move_ticks", 60), Yamls.decimal(yaml, "surfaced_depth", 4),
                    Yamls.decimal(yaml, "submerged_depth", 24), Yamls.bool(yaml, "player_hits_damage"),
                    Yamls.decimal(yaml, "unstunned_shot_factor", 0.1),
                    Yamls.integer(yaml, "futile_cooldown_seconds", 10), Yamls.integer(yaml, "shot_tokens", 0));
        }
    }

    public record Tentacles(Map<KuudraTier, Double> active, Map<KuudraTier, Double> health,
                            Map<KuudraTier, Double> slamDamage, int slamIntervalSeconds, int telegraphTicks,
                            int pathLength, int pathWidth, int respawnSeconds, double buildKnockback,
                            double grabRange) {
        static Tentacles parse(Map<String, Object> yaml) {
            return new Tentacles(perTier(yaml.get("active")), perTier(yaml.get("health")),
                    perTier(yaml.get("slam_damage")), Yamls.integer(yaml, "slam_interval_seconds", 12),
                    Yamls.integer(yaml, "telegraph_ticks", 60), Yamls.integer(yaml, "path_length", 14),
                    Yamls.integer(yaml, "path_width", 3), Yamls.integer(yaml, "respawn_seconds", 25),
                    Yamls.decimal(yaml, "build_knockback", 5), Yamls.decimal(yaml, "grab_range", 8));
        }

        public int active(KuudraTier tier) {
            return active.get(tier).intValue();
        }
    }

    public record Cannons(double cooldownSeconds, double damagePercent, double damageFlat, double blastRadius,
                          double speed, double dropshipMultiplier, double tentacleMultiplier) {
        static Cannons parse(Map<String, Object> yaml) {
            return new Cannons(Yamls.decimal(yaml, "cooldown_seconds", 0.8), Yamls.decimal(yaml, "damage_percent", 1.5),
                    Yamls.decimal(yaml, "damage_flat", 100_000), Yamls.decimal(yaml, "blast_radius", 3),
                    Yamls.decimal(yaml, "speed", 1.6), Yamls.decimal(yaml, "dropship_multiplier", 1.5),
                    Yamls.decimal(yaml, "tentacle_multiplier", 2));
        }
    }

    public record Tokens(int supplyRecovered, int fuelLoaded) {
        static Tokens parse(Map<String, Object> yaml) {
            return new Tokens(Yamls.integer(yaml, "supply_recovered", 0), Yamls.integer(yaml, "fuel_loaded", 0));
        }
    }

    public record Support(double manaRegenBase, double healingAuraRange, double manaAuraRange,
                          double protectiveAuraRange, double sweepingEdgeRange, double freezingTouchRange,
                          double nukeRange, double nukeFraction) {
        static Support parse(Map<String, Object> yaml) {
            return new Support(Yamls.decimal(yaml, "mana_regen_base", 2), Yamls.decimal(yaml, "healing_aura_range", 8),
                    Yamls.decimal(yaml, "mana_aura_range", 35), Yamls.decimal(yaml, "protective_aura_range", 20),
                    Yamls.decimal(yaml, "sweeping_edge_range", 5), Yamls.decimal(yaml, "freezing_touch_range", 8),
                    Yamls.decimal(yaml, "nuke_range", 20), Yamls.decimal(yaml, "nuke_fraction", 0.8));
        }
    }

    public record Lives(int respawnSeconds, int reviveInvulnerableSeconds, double autoReviveHealth,
                        int timeLimitSeconds) {
        static Lives parse(Map<String, Object> yaml) {
            return new Lives(Yamls.integer(yaml, "respawn_seconds", 10),
                    Yamls.integer(yaml, "revive_invulnerable_seconds", 3),
                    Yamls.decimal(yaml, "auto_revive_health", 0.5), Yamls.integer(yaml, "time_limit_seconds", 0));
        }
    }

    public record Waves(int batchBase, int batchPerExtraPlayer, int maxAlive, int cycleGrowth, int dropshipEvery,
                        int chaosmiteFromWave) {
        static Waves parse(Map<String, Object> yaml) {
            return new Waves(Yamls.integer(yaml, "batch_base", 2), Yamls.integer(yaml, "batch_per_extra_player", 1),
                    Yamls.integer(yaml, "max_alive", 30), Yamls.integer(yaml, "cycle_growth", 1),
                    Yamls.integer(yaml, "dropship_every", 5), Yamls.integer(yaml, "chaosmite_from_wave", 35));
        }
    }

    public record Mobs(int blightPhaseSeconds, int landmineFuseTicks, double explosionRadius, double knockerKnockback,
                       int slasherLeapSeconds, double slasherTeleportChance, int sentrySkullSeconds,
                       int sentryMaxSkulls, int blazeFireSeconds, int blazeHitWindowSeconds,
                       double blazeHitMultiplier, int dropshipLoopSeconds, double dropshipAttackFraction,
                       double dropshipAltitude, double magmaMergeRange, int omegagmaFuseSeconds,
                       double omegagmaLookDot, int golemThrowSeconds, int golemLeapSeconds,
                       double golemThrowMultiplier, double golemLeapMultiplier, int freezeSeconds,
                       Map<String, Double> speeds) {
        public double speed(String mobId) {
            return speeds.getOrDefault(mobId, 100D);
        }

        static Mobs parse(Map<String, Object> yaml) {
            Map<String, Double> speeds = new HashMap<>();
            Yamls.map(yaml.get("speed")).forEach((mob, speed) -> speeds.put(mob, ((Number) speed).doubleValue()));
            return new Mobs(Yamls.integer(yaml, "blight_phase_seconds", 4), Yamls.integer(yaml, "landmine_fuse_ticks", 40),
                    Yamls.decimal(yaml, "explosion_radius", 4), Yamls.decimal(yaml, "knocker_knockback", 30),
                    Yamls.integer(yaml, "slasher_leap_seconds", 4), Yamls.decimal(yaml, "slasher_teleport_chance", 0.15),
                    Yamls.integer(yaml, "sentry_skull_seconds", 3), Yamls.integer(yaml, "sentry_max_skulls", 6),
                    Yamls.integer(yaml, "blaze_fire_seconds", 3), Yamls.integer(yaml, "blaze_hit_window_seconds", 20),
                    Yamls.decimal(yaml, "blaze_hit_multiplier", 1), Yamls.integer(yaml, "dropship_loop_seconds", 40),
                    Yamls.decimal(yaml, "dropship_attack_fraction", 0.75), Yamls.decimal(yaml, "dropship_altitude", 30),
                    Yamls.decimal(yaml, "magma_merge_range", 1.5), Yamls.integer(yaml, "omegagma_fuse_seconds", 5),
                    Yamls.decimal(yaml, "omegagma_look_dot", 0.6), Yamls.integer(yaml, "golem_throw_seconds", 5),
                    Yamls.integer(yaml, "golem_leap_seconds", 8), Yamls.decimal(yaml, "golem_throw_multiplier", 0.6),
                    Yamls.decimal(yaml, "golem_leap_multiplier", 1.5), Yamls.integer(yaml, "freeze_seconds", 4),
                    Map.copyOf(speeds));
        }
    }
}
