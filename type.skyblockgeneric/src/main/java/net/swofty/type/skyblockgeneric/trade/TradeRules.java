package net.swofty.type.skyblockgeneric.trade;

import net.minestom.server.item.ItemStack;
import net.swofty.commons.skyblock.item.ItemType;
import net.swofty.type.skyblockgeneric.data.monogdb.CoopDatabase;
import net.swofty.type.skyblockgeneric.item.SkyBlockItem;
import net.swofty.commons.skyblock.item.attribute.attributes.ItemAttributeSoulbound;
import net.swofty.type.skyblockgeneric.item.components.SackComponent;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

import java.time.Duration;
import java.util.Optional;

public final class TradeRules {
    public static final double MAX_DISTANCE = 9;
    public static final int OFFER_SLOTS = 16;
    public static final Duration REQUEST_EXPIRY = Duration.ofSeconds(60);
    public static final Duration DEAL_COOLDOWN = Duration.ofSeconds(3);

    private TradeRules() {}

    public static Optional<String> refuseProfile(SkyBlockPlayer player) {
        if (player.isIronman()) return Optional.of("<c>Ironman profiles can't trade with other players!");
        return Optional.empty();
    }

    public static Optional<String> refuseDistance(SkyBlockPlayer player, SkyBlockPlayer other) {
        if (player.getInstance() != other.getInstance()
                || player.getPosition().distance(other.getPosition()) > MAX_DISTANCE) {
            return Optional.of("<c>You are too far away from " + other.getUsername() + " <c>to trade!");
        }
        return Optional.empty();
    }

    public static Optional<String> refuseItem(ItemStack stack, SkyBlockPlayer owner, SkyBlockPlayer receiver) {
        if (stack.isAir() || !SkyBlockItem.isSkyBlockItem(stack)) return Optional.of("<c>You can't trade this item!");
        SkyBlockItem item = new SkyBlockItem(stack);
        if (item.isNA() || item.getItemType() == ItemType.SKYBLOCK_MENU || item.hasComponent(SackComponent.class)) {
            return Optional.of("<c>You can't trade this item!");
        }
        ItemAttributeSoulbound.SoulBoundData soulbound = item.getAttributeHandler().getSoulBoundData();
        if (soulbound == null) return Optional.empty();
        if (soulbound.isCoopAllowed() && inSameCoop(owner, receiver)) return Optional.empty();
        return Optional.of(soulbound.isCoopAllowed()
                ? "<c>Co-op Soulbound items can only be traded with members of your co-op!"
                : "<c>You can't trade Soulbound items!");
    }

    private static boolean inSameCoop(SkyBlockPlayer first, SkyBlockPlayer second) {
        if (!first.isCoop() || !second.isCoop()) return false;
        CoopDatabase.Coop coop = first.getCoop();
        return coop != null && coop.members().contains(second.getUuid());
    }
}
