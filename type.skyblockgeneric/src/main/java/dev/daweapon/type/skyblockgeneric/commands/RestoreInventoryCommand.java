package dev.daweapon.type.skyblockgeneric.commands;

import net.minestom.server.item.ItemStack;
import dev.daweapon.commons.skyblock.item.ItemType;
import dev.daweapon.commons.skyblock.item.UnderstandableSkyBlockItem;
import dev.daweapon.type.generic.command.CommandParameters;
import dev.daweapon.type.generic.command.HypixelCommand;
import dev.daweapon.type.generic.user.categories.Rank;
import dev.daweapon.type.skyblockgeneric.data.datapoints.DatapointInventory;
import dev.daweapon.type.skyblockgeneric.item.SkyBlockItem;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockInventory;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;
import org.tinylog.Logger;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Supplier;

@CommandParameters(labels = "restoreinventory",
        description = "Gives back the items from a staged inventory backup for your current profile",
        usage = "/restoreinventory",
        permission = Rank.STAFF,
        allowsConsole = false)
public class RestoreInventoryCommand extends HypixelCommand {
    public static final Path DIRECTORY = Path.of("./configuration/skyblock/restores");

    @Override
    public void registerUsage(MinestomCommand command) {
        command.addSyntax((sender, context) -> {
            if (!permissionCheck(sender)) return;
            SkyBlockPlayer player = (SkyBlockPlayer) sender;
            Path file = DIRECTORY.resolve(player.getProfiles().getCurrentlySelected() + ".json");
            if (!Files.isRegularFile(file)) {
                player.sendMessage("<c>There is no staged inventory backup for this profile.");
                return;
            }
            try {
                DatapointInventory datapoint = new DatapointInventory("inventory");
                datapoint.deserializeValue(Files.readString(file).trim());
                List<String> restored = restore(player, datapoint.getValue());
                Files.move(file, file.resolveSibling(file.getFileName() + ".applied"), StandardCopyOption.REPLACE_EXISTING);
                player.sendMessage(restored.isEmpty()
                        ? "<e>Nothing to restore: you already have every item from the backup."
                        : "<a>Restored " + restored.size() + " items: <7>" + String.join("<8>, <7>", restored));
            } catch (IOException | RuntimeException e) {
                Logger.error(e, "Inventory restore failed for {}", player.getUsername());
                player.sendMessage("<c>The inventory restore failed; nothing was applied. Check the server log.");
            }
        });
    }

    private static List<String> restore(SkyBlockPlayer player, SkyBlockInventory backup) {
        Set<ItemType> owned = new HashSet<>();
        for (ItemStack stack : player.getInventory().getItemStacks()) owned.add(typeOf(stack));
        for (ItemStack stack : List.of(player.getHelmet(), player.getChestplate(), player.getLeggings(), player.getBoots()))
            owned.add(typeOf(stack));

        List<String> restored = new ArrayList<>();
        armor(player, backup.getHelmet(), player::getHelmet, player::setHelmet, owned, restored);
        armor(player, backup.getChestplate(), player::getChestplate, player::setChestplate, owned, restored);
        armor(player, backup.getLeggings(), player::getLeggings, player::setLeggings, owned, restored);
        armor(player, backup.getBoots(), player::getBoots, player::setBoots, owned, restored);
        backup.getItems().values().forEach(item -> {
            SkyBlockItem loaded = new SkyBlockItem(item);
            ItemType type = loaded.getItemType();
            if (type == null || type == ItemType.SKYBLOCK_MENU || owned.contains(type)) return;
            player.addAndUpdateItem(loaded);
            owned.add(type);
            restored.add(name(loaded));
        });
        return restored;
    }

    private static void armor(SkyBlockPlayer player, UnderstandableSkyBlockItem item, Supplier<ItemStack> current,
                              Consumer<ItemStack> equip, Set<ItemType> owned, List<String> restored) {
        if (item == null) return;
        SkyBlockItem loaded = new SkyBlockItem(item);
        ItemType type = loaded.getItemType();
        if (type == null || owned.contains(type)) return;
        if (current.get().isAir()) equip.accept(loaded.getItemStack());
        else player.addAndUpdateItem(loaded);
        owned.add(type);
        restored.add(name(loaded));
    }

    private static ItemType typeOf(ItemStack stack) {
        return stack == null || stack.isAir() ? null : new SkyBlockItem(stack).getItemType();
    }

    private static String name(SkyBlockItem item) {
        return item.getDisplayName() + (item.getAmount() > 1 ? " x" + item.getAmount() : "");
    }
}
