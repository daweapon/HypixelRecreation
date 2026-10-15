package dev.daweapon.type.skyblockgeneric.gui.inventories.islandsettings;

import net.minestom.server.MinecraftServer;
import net.minestom.server.entity.PlayerSkin;
import net.minestom.server.inventory.InventoryType;
import net.minestom.server.item.Material;
import dev.daweapon.commons.text.Text;
import dev.daweapon.type.generic.gui.inventory.ItemStacks;
import dev.daweapon.type.generic.gui.v2.Components;
import dev.daweapon.type.generic.gui.v2.DefaultState;
import dev.daweapon.type.generic.gui.v2.StatelessView;
import dev.daweapon.type.generic.gui.v2.ViewConfiguration;
import dev.daweapon.type.generic.gui.v2.ViewLayout;
import dev.daweapon.type.generic.gui.v2.context.ViewContext;
import dev.daweapon.type.skyblockgeneric.data.monogdb.CoopDatabase;
import dev.daweapon.type.skyblockgeneric.gui.inventories.coop.GUICoopInviteSender;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

// Shows the player's co-op roster (real Hypixel co-op has no owner/rank hierarchy - every member
// is equal) with a per-member kick button. Kicking still goes through /coopkick, which runs the
// existing majority-vote flow (CoopVoteManager) rather than an instant removal - this GUI is just
// a roster view over that, not a bypass of it (Roadmap #23 / Bugs & Loose Ends #77).
public class GUIIslandSettingsCoop extends StatelessView {

    private static final int[] GRID_SLOTS = {
            10, 11, 12, 13, 14, 15, 16,
            19, 20, 21, 22, 23, 24, 25,
    };

    @Override
    public ViewConfiguration<DefaultState> configuration() {
        return ViewConfiguration.withString((state, context) -> "Co-op Members", InventoryType.CHEST_5_ROW);
    }

    @Override
    public void layout(ViewLayout<DefaultState> layout, DefaultState state, ViewContext ctx) {
        Components.fill(layout);

        SkyBlockPlayer player = (SkyBlockPlayer) ctx.player();
        CoopDatabase.Coop coop = CoopDatabase.getFromMember(player.getUuid());

        if (coop == null) {
            layout.slot(22, (s, c) -> ItemStacks.item(Material.BARRIER,
                    "<c>No co-op\n<7>You aren't part of a co-op.\n<7>Use /coopadd \\<player> to start one!"));
            Components.backOrClose(layout, 44, ctx);
            return;
        }

        List<UUID> members = new ArrayList<>(coop.members());

        for (int i = 0; i < members.size() && i < GRID_SLOTS.length; i++) {
            UUID member = members.get(i);
            boolean self = member.equals(player.getUuid());
            Text displayName = SkyBlockPlayer.getDisplayName(member);
            String rawName = SkyBlockPlayer.getRawName(member);

            layout.slot(GRID_SLOTS[i], (s, c) -> self
                    ? ItemStacks.head(PlayerSkin.fromUuid(member.toString()), "<a>{}\n<7>This is you!", displayName)
                    : ItemStacks.head(PlayerSkin.fromUuid(member.toString()),
                    "<a>{}\n<7>Co-op member.\n\n<c>Click to start a kick vote!", displayName),
                    (click, c) -> {
                        if (self) return;
                        SkyBlockPlayer clicker = (SkyBlockPlayer) c.player();
                        clicker.closeInventory();
                        MinecraftServer.getCommandManager().execute(clicker, "coopkick " + rawName);
                    });
        }

        if (!coop.memberInvites().isEmpty()) {
            layout.slot(31, (s, c) -> ItemStacks.item(Material.PAPER,
                    "<e>Pending Invites: {}\n<7>Click to manage pending co-op\n<7>invites you've sent.", coop.memberInvites().size()),
                    (click, c) -> {
                        SkyBlockPlayer clicker = (SkyBlockPlayer) c.player();
                        CoopDatabase.Coop latest = CoopDatabase.getFromMember(clicker.getUuid());
                        if (latest == null) {
                            clicker.sendMessage("<c>Your co-op no longer exists!");
                            return;
                        }
                        new GUICoopInviteSender(latest).open(clicker);
                    });
        }

        Components.backOrClose(layout, 44, ctx);
    }
}
