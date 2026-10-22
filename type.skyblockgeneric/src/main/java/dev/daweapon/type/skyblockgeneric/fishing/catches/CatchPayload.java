package dev.daweapon.type.skyblockgeneric.fishing.catches;

import dev.daweapon.commons.skyblock.item.ItemType;
import dev.daweapon.commons.text.Text;
import dev.daweapon.type.generic.event.HypixelEventHandler;
import dev.daweapon.type.skyblockgeneric.event.custom.SeaCreatureCaughtEvent;
import dev.daweapon.type.skyblockgeneric.data.SkyBlockDataHandler;
import dev.daweapon.type.skyblockgeneric.data.datapoints.DatapointCollection;
import dev.daweapon.type.skyblockgeneric.entity.mob.mobs.seacreature.SeaCreatureSpawner;
import dev.daweapon.type.skyblockgeneric.essence.EssenceType;
import dev.daweapon.type.skyblockgeneric.essence.Essences;
import dev.daweapon.type.skyblockgeneric.fishing.FishingService;
import dev.daweapon.type.skyblockgeneric.fishing.registry.FishingRegistry;
import dev.daweapon.type.skyblockgeneric.fishing.trophy.TrophyFishAwards;
import dev.daweapon.type.skyblockgeneric.fishing.registry.FishingTableDefinition;
import dev.daweapon.type.skyblockgeneric.item.SkyBlockItem;
import dev.daweapon.type.skyblockgeneric.item.handlers.pet.abilities.combat.CombatPetHooks;
import dev.daweapon.type.skyblockgeneric.fishing.registry.TrophyFishDefinition;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.List;

/**
 * Sealed view of what the rod can pull out of the water. Each variant
 * carries exactly its own fields — no bag of nullable @Nullable strings —
 * and owns its own {@link #apply(CatchAwardContext)} so the FishingService
 * dispatches by polymorphism rather than switch-on-kind.
 */
public sealed interface CatchPayload {

    /** Fishing skill XP to award; sea creatures intentionally return 0 (their kill awards XP separately). */
    double skillXp();

    /** Short human label for telemetry / chat — variants pick the format. */
    String summary();

    /** Run the catch's side-effects on the player. */
    void apply(CatchAwardContext ctx);

    /* ------------------------------------------------------------------ */
    /*  Variants                                                          */
    /* ------------------------------------------------------------------ */

    record Multi(List<CatchPayload> payloads) implements CatchPayload {
        @Override public double skillXp() { return payloads.stream().mapToDouble(CatchPayload::skillXp).sum(); }
        @Override public String summary() { return payloads.stream().map(CatchPayload::summary).toList().toString(); }
        @Override public void apply(CatchAwardContext ctx) { payloads.forEach(payload -> payload.apply(ctx)); }
    }

    record Item(String itemId, int amount, double skillXp, boolean fromTreasure) implements CatchPayload {
        @Override public String summary() { return (fromTreasure ? "treasure " : "") + amount + "x " + itemId; }

        @Override
        public void apply(CatchAwardContext ctx) {
            ItemType type = ItemType.valueOf(itemId);
            ctx.player().addAndUpdateItem(type, amount);
            ctx.player().getCollection().increase(ctx.player(), type, amount);
            ctx.player().getSkyblockDataHandler()
                    .get(SkyBlockDataHandler.Data.COLLECTION, DatapointCollection.class)
                    .setValue(ctx.player().getCollection());

            if (fromTreasure) {
                ctx.player().sendMessage(Text.of("<6><l>GOOD CATCH!</l> <b>You found {}x <a>{}<b>.", amount, itemId));
            }
        }
    }

    record Treasure(String itemId, int amount, double skillXp, String quality) implements CatchPayload {
        @Override public String summary() { return quality.toLowerCase() + " treasure " + amount + "x " + itemId; }

        private String header() {
            return switch (quality) {
                case "OUTSTANDING" -> "<d><l>OUTSTANDING CATCH!</l>";
                case "GREAT" -> "<5><l>GREAT CATCH!</l>";
                default -> "<6><l>GOOD CATCH!</l>";
            };
        }

        @Override
        public void apply(CatchAwardContext ctx) {
            if (!"GOOD".equals(quality)) ctx.player().getAchievementHandler().completeAchievement("skyblock.treasure_fishing");
            if (FishingTableDefinition.COINS.equals(itemId)) {
                ctx.player().addCoins(amount);
                ctx.player().sendMessage(header() + " <b>You found <6>" + String.format("%,d", amount) + " Coins<b>.");
                return;
            }
            var shard = ShardCatches.definitionOf(itemId);
            if (shard.isPresent()) {
                ShardCatches.award(ctx.player(), shard.get(), amount, header());
                return;
            }
            ItemType type = ItemType.valueOf(itemId);
            SkyBlockItem item = new SkyBlockItem(type);
            item.setAmount(amount);
            ctx.player().addAndUpdateItem(item);
            ctx.player().getCollection().increase(ctx.player(), type, amount);
            ctx.player().getSkyblockDataHandler()
                    .get(SkyBlockDataHandler.Data.COLLECTION, DatapointCollection.class)
                    .setValue(ctx.player().getCollection());
            ctx.player().sendMessage(header() + " <b>You found " + (amount > 1 ? "<8>" + amount + "x " : "")
                    + "<f>" + item.getDisplayName() + "<b>.");
        }
    }

    record SeaCreature(String seaCreatureId, double skillXp, int spawnCount) implements CatchPayload {
        public SeaCreature(String seaCreatureId, double skillXp) {
            this(seaCreatureId, skillXp, 1);
        }

        public SeaCreature withDoubleHook() {
            return new SeaCreature(seaCreatureId, skillXp, 2);
        }

        @Override public String summary() {
            return spawnCount > 1 ? "double-hook " + seaCreatureId : "sea creature " + seaCreatureId;
        }

        @Override
        public void apply(CatchAwardContext ctx) {
            if (spawnCount > 1) {
                ctx.player().sendMessage("<a><l>DOUBLE HOOK!");
            }
            for (int i = 0; i < spawnCount; i++) {
                if (SeaCreatureSpawner.spawn(ctx.player(), seaCreatureId, ctx.hookPosition()) != null) {
                    HypixelEventHandler.callCustomEvent(new SeaCreatureCaughtEvent(ctx.player(), seaCreatureId));
                }
            }
            SeaCreatureSpawner.announceCatch(ctx.player(), seaCreatureId, spawnCount);
        }
    }

    record TrophyFish(String definitionId, TrophyTier tier, String itemId, double skillXp) implements CatchPayload {
        @Override public String summary() { return "trophy " + tier.name() + " " + definitionId; }

        @Override
        public void apply(CatchAwardContext ctx) {
            ItemType type = ItemType.valueOf(itemId);
            ctx.player().addAndUpdateItem(type, 1);

            TrophyFishDefinition definition = FishingRegistry.getTrophyFish(definitionId);
            String name = definition == null ? definitionId : definition.coloredName();
            ctx.player().sendMessage("<6><l>TROPHY FISH!</l> <b>You caught a " + name + " " + tier.label() + "<b>.");
            TrophyFishAwards.record(ctx.player(), definitionId, tier, itemId);
        }
    }

    record Essence(EssenceType type, int amount, double skillXp) implements CatchPayload {
        @Override public String summary() { return "treasure " + amount + "x " + type.getFullName(); }

        @Override
        public void apply(CatchAwardContext ctx) {
            long granted = type == EssenceType.CRIMSON ? CombatPetHooks.crimsonEssence(ctx.player(), amount) : amount;
            Essences.add(ctx.player(), type, granted);
            ctx.player().sendMessage("<b><l>TREASURE! </l><7>You found <d>{} {}<7>!", granted, type.getFullName());
        }
    }

    record Quest(String itemId, int amount, double skillXp, String message) implements CatchPayload {
        @Override public String summary() { return "quest " + itemId; }

        @Override
        public void apply(CatchAwardContext ctx) {
            ctx.player().addAndUpdateItem(ItemType.valueOf(itemId), amount);
            ctx.player().sendMessage("<d><l>QUEST CATCH! </l><7>{}", message);
        }
    }

    record Special(String itemId, int amount, double skillXp, boolean corrupted) implements CatchPayload {
        @Override public String summary() { return "special " + itemId; }

        @Override
        public void apply(CatchAwardContext ctx) {
            ItemType type = ItemType.valueOf(itemId);
            ctx.player().addAndUpdateItem(type, amount);
            ctx.player().getCollection().increase(ctx.player(), type, amount);
            Text prefix = corrupted ? Text.of("<5><l>CORRUPTED! ") : Text.of("<d><l>WONDROUS CATCH! ");
            ctx.player().sendMessage(prefix.append("<7>You found <a>{}<7>!", new SkyBlockItem(type).getDisplayName()));
        }
    }
}
