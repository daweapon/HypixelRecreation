package net.swofty.type.skyblockgeneric.gui.inventories.sbmenu.profiles;

import lombok.SneakyThrows;
import net.minestom.server.inventory.InventoryType;
import net.minestom.server.item.Material;
import net.swofty.commons.skyblock.SkyBlockPlayerProfiles;
import net.swofty.commons.text.Text;
import net.swofty.type.generic.data.datapoints.DatapointString;
import net.swofty.type.generic.data.mongodb.ProfilesDatabase;
import net.swofty.type.generic.data.mongodb.UserDatabase;
import net.swofty.type.generic.gui.inventory.ItemStacks;
import net.swofty.type.generic.gui.v2.*;
import net.swofty.type.generic.gui.v2.context.ViewContext;
import net.swofty.type.skyblockgeneric.data.ProfileSwitcher;
import net.swofty.type.skyblockgeneric.data.SkyBlockDataHandler;
import net.swofty.type.skyblockgeneric.data.datapoints.DatapointUUID;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;
import org.bson.Document;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;

// GUI for confirming creation of a new SkyBlock (or Ironman) profile
public class GUIProfileCreate extends StatelessView {
    private final net.swofty.type.skyblockgeneric.user.ProfileMode mode;
    // Guards against a laggy/double click firing the create handler twice before the async
    // ProfileSwitcher.switchTo() closes this view - one view instance is opened per GUI visit.
    private final AtomicBoolean submitted = new AtomicBoolean(false);

    public GUIProfileCreate(net.swofty.type.skyblockgeneric.user.ProfileMode mode) {
        this.mode = mode;
    }

    @Override
    public ViewConfiguration<DefaultState> configuration() {
        return new ViewConfiguration<>(mode == net.swofty.type.skyblockgeneric.user.ProfileMode.IRONMAN
                ? "Create an Ironman Profile" : "Create a Profile", InventoryType.CHEST_3_ROW);
    }

    @SneakyThrows
    @Override
    public void layout(ViewLayout<DefaultState> layout, DefaultState state, ViewContext ctx) {
        Components.fill(layout);

        String profileName = SkyBlockPlayerProfiles.getRandomName();

        layout.slot(11, (s, c) -> ItemStacks.item(Material.GREEN_TERRACOTTA, 1, """
                        <a>Create New Profile
                        <7>You are creating a new SkyBlock
                        <7>profile.

                        <7>Profile name: <e>{}
                        <7>Mode: {}

                        <7>You won't lose any progress.
                        <7>You can switch between profiles.

                        <b>You are creating a SOLO profile!
                        <b>Use /coopadd \\<name> to play with friends!
                        <e>Click to confirm new profile!""",
                        profileName, mode.getDisplayName()),
                (click, c) -> {
                    SkyBlockPlayer player = (SkyBlockPlayer) c.player();
                    SkyBlockPlayerProfiles profiles = player.getProfiles();

                    if (profiles.getProfiles().size() >= GUIProfileManagement.MAX_PROFILES) {
                        player.sendMessage("<c>You already have the maximum number of profiles!");
                        return;
                    }

                    if (!submitted.compareAndSet(false, true)) return;

                    UUID profileId = UUID.randomUUID();

                    // Create new SkyBlock data handler with the profile ID
                    SkyBlockDataHandler handler = SkyBlockDataHandler.initUserWithDefaultData(player.getUuid(), profileId);
                    handler.get(SkyBlockDataHandler.Data.PROFILE_NAME, DatapointString.class).setValue(profileName);
                    handler.get(SkyBlockDataHandler.Data.PROFILE_MODE, DatapointString.class).setValue(mode.name());
                    handler.get(SkyBlockDataHandler.Data.ISLAND_UUID, DatapointUUID.class).setValue(profileId);

                    // Convert to document for saving
                    Document document = handler.toProfileDocument();

                    new ProfilesDatabase(profileId.toString()).saveDocument(document);

                    profiles.addProfile(profileId);
                    new UserDatabase(player.getUuid()).saveProfiles(profiles);

                    ProfileSwitcher.switchTo(player, profileId);
                });

        layout.slot(15, (s, c) -> ItemStacks.item(Material.RED_TERRACOTTA, 1, Text.key("gui_sbmenu.profiles.create.cancel"), List.of()),
                (click, c) -> c.player().openView(mode == net.swofty.type.skyblockgeneric.user.ProfileMode.IRONMAN
                        ? new GUIProfileSelectSpecialMode() : new GUIProfileSelectMode()));
    }
}
