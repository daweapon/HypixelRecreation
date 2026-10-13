package net.swofty.type.skyblockgeneric.garden.visitor;

import net.swofty.commons.StringUtility;
import net.swofty.commons.skyblock.item.ItemType;
import net.swofty.commons.text.Text;
import net.swofty.type.skyblockgeneric.essence.EssenceType;
import net.swofty.type.skyblockgeneric.essence.Essences;
import net.swofty.type.skyblockgeneric.garden.GardenProfile;
import net.swofty.type.skyblockgeneric.garden.GardenProgress;
import net.swofty.type.skyblockgeneric.garden.GardenService;
import net.swofty.type.skyblockgeneric.garden.Gardens;
import net.swofty.type.skyblockgeneric.garden.quest.GardenQuests;
import net.swofty.type.skyblockgeneric.item.SkyBlockItem;
import net.swofty.type.skyblockgeneric.loottable.LootRoll;
import net.swofty.type.skyblockgeneric.skill.SkillCategories;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.random.RandomGenerator;

public final class VisitorService {
    public static final double CROP_COOLDOWN_SECONDS = 0.1;
    private static final int BLUEPRINT_GARDEN_LEVEL = 7;
    private static final RandomGenerator RANDOM = new java.util.Random();

    public enum GiveResult {
        NOTHING, PARTIAL, COMPLETED
    }

    private VisitorService() {
    }

    public static void tick(SkyBlockPlayer player) {
        GardenProfile profile = Gardens.profile(player);
        if (advance(profile, player, System.currentTimeMillis())) {
            Gardens.save(player, profile);
        }
    }

    static boolean advance(GardenProfile profile, @Nullable SkyBlockPlayer player, long now) {
        VisitorData data = profile.visitors();
        VisitorCatalogue catalogue = VisitorCatalogue.get();
        long cooldown = cooldownMillis(catalogue, data);
        boolean changed = false;
        if (data.getNextVisitorAt() == 0) {
            data.setNextVisitorAt(now + cooldown);
            changed = true;
        }
        if (data.getPresent().size() >= VisitorData.MAX_PRESENT) {
            if (data.getNextVisitorAt() < now + cooldown) {
                data.setNextVisitorAt(now + cooldown);
                changed = true;
            }
            return changed;
        }
        while (now >= data.getNextVisitorAt() && data.getPresent().size() < VisitorData.MAX_PRESENT) {
            if (!spawn(profile, player, now)) {
                data.setNextVisitorAt(now + cooldown);
                return true;
            }
            data.setNextVisitorAt(data.getNextVisitorAt() + cooldown);
            changed = true;
        }
        return changed;
    }

    static long cooldownMillis(VisitorCatalogue catalogue, VisitorData data) {
        return (long) (catalogue.cooldownMinutes(data.uniqueServed()) * 60_000);
    }

    public static boolean eligible(GardenProfile profile, VisitorCatalogue.Visitor visitor) {
        VisitorData data = profile.visitors();
        if (!visitor.available() || visitor.event() != null) return false;
        if (visitor.gardenLevel() > GardenProgress.level(profile)) return false;
        VisitorCatalogue.Requirement requirement = visitor.requirement();
        return switch (requirement.kind()) {
            case "TALK_TO_NPC" -> data.getTalkedTo().contains(talkTarget(requirement.text()));
            case "SERVE_UNIQUE" -> requirement.count() == null || data.uniqueServed() >= requirement.count();
            default -> true;
        };
    }

    public static String talkTarget(@Nullable String text) {
        if (text == null) return "";
        return text.replaceFirst("(?i)^(talk to|listen to) (the )?", "").trim().toLowerCase(Locale.ROOT);
    }

    public static boolean spawn(GardenProfile profile, @Nullable SkyBlockPlayer player, long now) {
        VisitorData data = profile.visitors();
        VisitorCatalogue catalogue = VisitorCatalogue.get();
        VisitorCatalogue.Visitor visitor = null;
        if (data.getForcedNext() != null) {
            visitor = catalogue.visitor(data.getForcedNext());
            data.setForcedNext(null);
        }
        if (visitor == null && !profile.greenhouse().isBlueprintOffered() && GardenProgress.level(profile) >= BLUEPRINT_GARDEN_LEVEL) {
            visitor = catalogue.visitor("CARPENTER");
        }
        if (visitor == null) {
            var present = data.getPresent().stream().map(VisitorData.Present::getId).collect(java.util.stream.Collectors.toSet());
            visitor = VisitorGenerator.pick(catalogue, candidate -> eligible(profile, candidate), present, 0, RANDOM);
        }
        return visitor != null && add(profile, player, visitor, now);
    }

    public static boolean add(GardenProfile profile, @Nullable SkyBlockPlayer player, VisitorCatalogue.Visitor visitor, long now) {
        VisitorData data = profile.visitors();
        if (data.getPresent().size() >= VisitorData.MAX_PRESENT || data.find(visitor.id()) != null) return false;
        VisitorOffer offer = VisitorGenerator.generate(VisitorCatalogue.get(), visitor, GardenProgress.level(profile), RANDOM);
        boolean hidden = data.visible().size() >= VisitorData.MAX_VISIBLE;
        data.getPresent().add(new VisitorData.Present(visitor.id(), offer, now, hidden));
        data.recordVisit(visitor.id());
        if (visitor.id().equals("CARPENTER")) profile.greenhouse().setBlueprintOffered(true);
        if (player != null && !hidden) {
            player.sendMessage(Text.of(visitor.coloured() + " <f>has arrived on your Garden!"));
        }
        return true;
    }

    public static void reduceCooldown(SkyBlockPlayer player, double seconds) {
        GardenProfile profile = Gardens.profile(player);
        VisitorData data = profile.visitors();
        if (data.getNextVisitorAt() == 0) return;
        double boost = 1 + net.swofty.type.skyblockgeneric.garden.chip.ChipService.bonus(profile.chips(), net.swofty.type.skyblockgeneric.garden.chip.GardenChip.QUICKDRAW) / 100.0;
        data.setNextVisitorAt(data.getNextVisitorAt() - (long) (seconds * boost * 1000));
        Gardens.save(player, profile);
    }

    public static void callNow(SkyBlockPlayer player, @Nullable String visitorId) {
        GardenProfile profile = Gardens.profile(player);
        VisitorData data = profile.visitors();
        if (visitorId != null) data.setForcedNext(visitorId);
        spawn(profile, player, System.currentTimeMillis());
        Gardens.save(player, profile);
    }

    public static void talkedTo(SkyBlockPlayer player, String npcName) {
        String normalised = npcName.trim().toLowerCase(Locale.ROOT);
        GardenProfile profile = Gardens.profile(player);
        if (profile.visitors().getTalkedTo().add(normalised)) {
            Gardens.save(player, profile);
        }
    }

    public static GiveResult give(SkyBlockPlayer player, String visitorId) {
        GardenProfile profile = Gardens.profile(player);
        VisitorData.Present present = profile.visitors().find(visitorId);
        if (present == null) return GiveResult.NOTHING;
        boolean gave = false;
        for (VisitorOffer.Required required : present.getOffer().required()) {
            ItemType type = ItemType.get(required.item());
            if (type == null || required.remaining() == 0) continue;
            int have = player.getAmountInInventory(type) + player.getSackItems().getAmount(type);
            int amount = Math.min(have, required.remaining());
            if (amount > 0 && player.removeItemFromPlayer(type, amount)) {
                required.give(amount);
                gave = true;
            }
        }
        if (!gave) return GiveResult.NOTHING;
        if (present.getOffer().complete()) {
            complete(player, profile, present);
            return GiveResult.COMPLETED;
        }
        Gardens.save(player, profile);
        return GiveResult.PARTIAL;
    }

    public static boolean hasAnythingToGive(SkyBlockPlayer player, VisitorOffer offer) {
        for (VisitorOffer.Required required : offer.required()) {
            ItemType type = ItemType.get(required.item());
            if (type != null && required.remaining() > 0
                    && player.getAmountInInventory(type) + player.getSackItems().getAmount(type) > 0) {
                return true;
            }
        }
        return false;
    }

    public static void refuse(SkyBlockPlayer player, String visitorId) {
        GardenProfile profile = Gardens.profile(player);
        VisitorData.Present present = profile.visitors().find(visitorId);
        if (present == null) return;
        VisitorCatalogue.Visitor visitor = VisitorCatalogue.get().visitor(visitorId);
        remove(profile.visitors(), present);
        Gardens.save(player, profile);
        if (visitor != null) {
            if (!visitor.declineLines().isEmpty()) {
                player.sendMessage(Text.of("<e>[NPC] " + visitor.coloured() + "<f>: {}", VisitorText.fill(visitor.declineLines().getFirst(), player)));
            }
            player.sendMessage(Text.of(visitor.coloured() + " <f>has left your Garden."));
        }
    }

    private static void remove(VisitorData data, VisitorData.Present present) {
        data.getPresent().remove(present);
        if (data.visible().size() < VisitorData.MAX_VISIBLE) {
            data.getPresent().stream().filter(VisitorData.Present::isHidden).findFirst().ifPresent(next -> next.setHidden(false));
        }
    }

    private static void complete(SkyBlockPlayer player, GardenProfile profile, VisitorData.Present present) {
        VisitorCatalogue catalogue = VisitorCatalogue.get();
        VisitorCatalogue.Visitor visitor = catalogue.visitor(present.getId());
        VisitorOffer offer = present.getOffer();
        VisitorData data = profile.visitors();

        long farmingXp = offer.farmingXp();
        int copper = offer.copper();
        List<String> lines = new ArrayList<>();
        if (visitor != null) {
            for (VisitorCatalogue.Reward reward : visitor.rewards()) {
                switch (reward.type()) {
                    case "FARMING_XP" -> farmingXp += (long) reward.amount();
                    case "COPPER" -> copper += (int) reward.amount();
                    case "ITEM" -> {
                        if (!"GREENHOUSE_BLUEPRINT".equals(reward.item()) || !profile.greenhouse().isBlueprintGiven()) {
                            giveItem(player, reward, lines);
                        }
                    }
                    case "ESSENCE" -> giveEssence(player, reward, lines);
                    case "SKIN", "FLAG" -> lines.add("<d>" + reward.name());
                    default -> {
                    }
                }
            }
            for (VisitorCatalogue.RareDrop drop : catalogue.rareDrops()) {
                ItemType type = ItemType.get(drop.item());
                if (type != null && visitor.rarity().atLeast(drop.minRarity())
                        && LootRoll.roll(player.getUuid(), type, drop.chancePercent(), RANDOM)) {
                    player.addAndUpdateItem(new SkyBlockItem(type, 1));
                    lines.add("<6><l>RARE DROP! <f>" + StringUtility.toNormalCase(drop.item()));
                }
            }
        }

        boolean blueprint = visitor != null && visitor.id().equals("CARPENTER") && !profile.greenhouse().isBlueprintGiven();
        if (blueprint) profile.greenhouse().setBlueprintGiven(true);
        profile.addCopper(copper);
        data.recordAccepted(present.getId());
        remove(data, present);
        Gardens.save(player, profile);
        player.getSkills().increase(player, SkillCategories.FARMING, (double) farmingXp);
        player.addBits(offer.bits());
        GardenService.addGardenXp(player, offer.gardenXp());
        claimMilestones(player);
        announce(player, visitor, farmingXp, offer, copper, lines);
        GardenQuests.visitorServed(player);
        if (blueprint) GardenQuests.carpenterServed(player);
    }

    private static void giveItem(SkyBlockPlayer player, VisitorCatalogue.Reward reward, List<String> lines) {
        ItemType type = reward.item() == null ? null : ItemType.get(reward.item());
        if (type == null) {
            lines.add("<7>" + reward.name() + " <8>(not added yet)");
            return;
        }
        if (reward.chancePercent() != null && !LootRoll.roll(player.getUuid(), type, reward.chancePercent(), RANDOM)) return;
        int amount = (int) Math.max(1, reward.amount());
        player.addAndUpdateItem(new SkyBlockItem(type, amount));
        lines.add("<f>" + (amount > 1 ? amount + "x " : "") + StringUtility.toNormalCase(type.name()));
    }

    private static void giveEssence(SkyBlockPlayer player, VisitorCatalogue.Reward reward, List<String> lines) {
        String name = reward.name().replace(" Essence", "").trim().toUpperCase(Locale.ROOT);
        try {
            Essences.add(player, EssenceType.valueOf(name), (long) reward.amount());
            lines.add("<d>" + (long) reward.amount() + " " + StringUtility.toNormalCase(name) + " Essence");
        } catch (IllegalArgumentException exception) {
            lines.add("<7>" + reward.name() + " <8>(not added yet)");
        }
    }

    public static void claimMilestones(SkyBlockPlayer player) {
        GardenProfile profile = Gardens.profile(player);
        VisitorData data = profile.visitors();
        VisitorCatalogue catalogue = VisitorCatalogue.get();
        boolean changed = false;
        for (VisitorCatalogue.MilestoneTier tier : catalogue.uniqueMilestones()) {
            if (tier.tier() > data.getUniqueTier() && data.uniqueServed() >= tier.cumulative()) {
                data.setUniqueTier(tier.tier());
                changed = true;
                reward(player, "Unique Visitors Served", tier);
            }
        }
        for (VisitorCatalogue.MilestoneTier tier : catalogue.acceptedMilestones()) {
            if (tier.tier() > data.getAcceptedTier() && data.totalAccepted() >= tier.cumulative()) {
                data.setAcceptedTier(tier.tier());
                changed = true;
                reward(player, "Offers Accepted", tier);
            }
        }
        if (changed) {
            Gardens.save(player, profile);
            GardenService.syncExperience(player);
        }
    }

    private static void reward(SkyBlockPlayer player, String track, VisitorCatalogue.MilestoneTier tier) {
        player.getSkills().increase(player, SkillCategories.FARMING, (double) tier.farmingXp());
        GardenService.addGardenXp(player, tier.gardenXp());
        player.sendMessage(Text.of("<b><m>                                                    "));
        player.sendMessage(Text.of("<b><l>GARDEN MILESTONE <a>{} {}", track, StringUtility.getAsRomanNumeral(tier.tier())));
        player.sendMessage(Text.of("<a><l>REWARDS"));
        player.sendMessage(Text.of(" <8>+<a>{} Garden Experience", tier.gardenXp()));
        player.sendMessage(Text.of(" <8>+<3>{} Farming Experience", StringUtility.commaify(tier.farmingXp())));
        player.sendMessage(Text.of(" <8>+<b>{} SkyBlock XP", tier.skyblockXp()));
        player.sendMessage(Text.of("<b><m>                                                    "));
    }

    private static void announce(SkyBlockPlayer player, @Nullable VisitorCatalogue.Visitor visitor, long farmingXp,
                                 VisitorOffer offer, int copper, List<String> extra) {
        String name = visitor == null ? "Visitor" : visitor.coloured();
        String rarity = visitor == null ? "" : " <8>(" + visitor.rarity().getColour() + visitor.rarity().getLabel() + "<8>)";
        player.sendMessage(Text.of("<b><m>                                                    "));
        player.sendMessage(Text.of("<a><l>OFFER ACCEPTED <f>with " + name + rarity));
        player.sendMessage(Text.of("<a><l>REWARDS"));
        player.sendMessage(Text.of(" <8>+<3>{} Farming XP", StringUtility.commaify(farmingXp)));
        player.sendMessage(Text.of(" <8>+<a>{} Garden Experience", offer.gardenXp()));
        player.sendMessage(Text.of(" <8>+<c>{} Copper", copper));
        player.sendMessage(Text.of(" <8>+<b>{} Bits", offer.bits()));
        extra.forEach(line -> player.sendMessage(Text.of(" <8>+{}", line)));
        player.sendMessage(Text.of("<b><m>                                                    "));
        if (visitor != null && !visitor.acceptLines().isEmpty()) {
            player.sendMessage(Text.of("<e>[NPC] " + name + "<f>: {}", VisitorText.fill(visitor.acceptLines().getFirst(), player)));
        }
    }

    public static void serveEveryVisitor(SkyBlockPlayer player) {
        GardenProfile profile = Gardens.profile(player);
        for (VisitorCatalogue.Visitor visitor : VisitorCatalogue.get().visitors()) {
            profile.visitors().markServed(visitor.id());
        }
        profile.visitors().getPresent().clear();
        Gardens.save(player, profile);
        claimMilestones(player);
    }
}
