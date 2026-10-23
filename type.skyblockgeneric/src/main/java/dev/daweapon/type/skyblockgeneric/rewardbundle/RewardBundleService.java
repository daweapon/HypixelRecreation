package dev.daweapon.type.skyblockgeneric.rewardbundle;

import dev.daweapon.commons.StringUtility;
import dev.daweapon.commons.skyblock.item.ItemType;
import dev.daweapon.type.generic.data.HypixelDataHandler;
import dev.daweapon.type.generic.data.datapoints.DatapointStringList;
import dev.daweapon.type.skyblockgeneric.spray.SprayCatalogue;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.ArrayList;
import java.util.List;

public final class RewardBundleService {
    private RewardBundleService() {
    }

    public static List<String> ownedSprays(SkyBlockPlayer player) {
        return player.getDataHandler().get(HypixelDataHandler.Data.OWNED_SPRAYS, DatapointStringList.class).getValue();
    }

    public static boolean grantSpray(SkyBlockPlayer player, String sprayId) {
        List<String> owned = new ArrayList<>(ownedSprays(player));
        if (owned.contains(sprayId)) return false;
        owned.add(sprayId);
        player.getDataHandler().get(HypixelDataHandler.Data.OWNED_SPRAYS, DatapointStringList.class).setValue(owned);
        return true;
    }

    public static void ownEverySpray(SkyBlockPlayer player) {
        List<String> owned = new ArrayList<>(ownedSprays(player));
        SprayCatalogue.all().forEach(spray -> {
            if (!owned.contains(spray.id())) owned.add(spray.id());
        });
        player.getDataHandler().get(HypixelDataHandler.Data.OWNED_SPRAYS, DatapointStringList.class).setValue(owned);
    }

    public static List<String> claimed(SkyBlockPlayer player) {
        return player.getDataHandler().get(HypixelDataHandler.Data.REWARD_BUNDLE_CLAIMS, DatapointStringList.class).getValue();
    }

    public static boolean isClaimed(SkyBlockPlayer player, BundleReward reward) {
        return claimed(player).contains(reward.key());
    }

    public static boolean meetsRank(SkyBlockPlayer player, BundleReward reward) {
        return player.getRank().isEqualOrHigherThan(reward.rank());
    }

    public static String displayName(BundleReward reward) {
        return switch (reward.type()) {
            case SPRAY -> SprayCatalogue.byId(reward.id()).map(spray -> spray.name() + " Spray").orElse(StringUtility.toNormalCase(reward.id()));
            default -> StringUtility.toNormalCase(reward.id());
        };
    }

    public static boolean claim(SkyBlockPlayer player, BundleReward reward) {
        return claim(player, reward, true);
    }

    private static boolean claim(SkyBlockPlayer player, BundleReward reward, boolean announce) {
        if (isClaimed(player, reward)) {
            player.sendMessage("<c>Already claimed!");
            return false;
        }
        if (!meetsRank(player, reward)) {
            player.sendMessage("<c>Requires " + reward.rank().prefixMarkup() + "<c>!");
            return false;
        }
        List<String> claimed = new ArrayList<>(claimed(player));
        claimed.add(reward.key());
        player.getDataHandler().get(HypixelDataHandler.Data.REWARD_BUNDLE_CLAIMS, DatapointStringList.class).setValue(claimed);
        if (reward.type() == BundleReward.Type.SPRAY) {
            grantSpray(player, reward.id());
        } else {
            player.addAndUpdateItem(ItemType.get(reward.id()), reward.amount());
        }
        if (announce) player.sendMessage("<a>You claimed the " + reward.rank().prefixMarkup() + " <a>reward {}! <e>Yay!", displayName(reward) + (reward.amount() > 1 ? " x" + reward.amount() : ""));
        return true;
    }

    public static int claimAll(SkyBlockPlayer player, BundleSeason season) {
        int claimed = 0;
        for (BundleReward reward : RewardBundles.of(season)) {
            if (isClaimed(player, reward) || !meetsRank(player, reward)) continue;
            if (claim(player, reward, false)) claimed++;
        }
        if (claimed > 0) player.sendMessage("<a>You claimed <e>{} <a>{} rewards! <e>Yay!", claimed, season.label());
        return claimed;
    }
}
