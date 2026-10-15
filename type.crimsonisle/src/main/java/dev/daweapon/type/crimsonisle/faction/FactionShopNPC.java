package dev.daweapon.type.crimsonisle.faction;

import net.minestom.server.coordinate.Pos;
import dev.daweapon.type.skyblockgeneric.crimson.faction.CrimsonFaction;
import dev.daweapon.type.skyblockgeneric.crimson.questline.CrimsonSideQuests;
import dev.daweapon.type.skyblockgeneric.shop.catalogue.CatalogueShopView;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;
import org.jetbrains.annotations.Nullable;

/**
 * A town villager with a shop: an optional members-only refusal, an introduction the first time,
 * then the shop (a YAML catalogue unless {@link #openShop} is overridden).
 */
public abstract class FactionShopNPC extends FactionVillagerNPC {
    private final String shopId;
    private final @Nullable String refusal;
    private final String[] introduction;

    protected FactionShopNPC(String name, String texture, Pos home, CrimsonFaction faction, String shopId,
                             @Nullable String refusal, String... introduction) {
        super(name, texture, home, faction);
        this.shopId = shopId;
        this.refusal = refusal;
        this.introduction = introduction;
    }

    protected void openShop(SkyBlockPlayer player) {
        player.openView(new CatalogueShopView(shopId));
    }

    @Override
    protected void talk(SkyBlockPlayer player) {
        if (refusal != null && !isMember(player)) {
            say(player, refusal);
            return;
        }
        if (introduction.length > 0 && CrimsonSideQuests.firstMeeting(player, shopId)) {
            say(player, introduction).thenRun(() -> openShop(player));
            return;
        }
        openShop(player);
    }
}
