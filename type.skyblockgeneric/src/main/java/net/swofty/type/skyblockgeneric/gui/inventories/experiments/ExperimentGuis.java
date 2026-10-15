package net.swofty.type.skyblockgeneric.gui.inventories.experiments;

import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;
import net.minestom.server.MinecraftServer;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.minestom.server.timer.TaskSchedule;
import net.swofty.type.generic.gui.inventory.ItemStacks;
import net.swofty.type.generic.user.HypixelPlayer;
import net.swofty.type.skyblockgeneric.item.SkyBlockItem;
import net.swofty.type.skyblockgeneric.item.updater.PlayerItemUpdater;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.function.BooleanSupplier;

final class ExperimentGuis {
    private ExperimentGuis() {
    }

    static ItemStack.Builder item(Material material, String... lines) {
        return ItemStacks.item(material, String.join("\n", lines));
    }

    static ItemStack.Builder item(Material material, int amount, String... lines) {
        return ItemStacks.item(material, amount, String.join("\n", lines));
    }

    static ItemStack.Builder real(SkyBlockPlayer player, SkyBlockItem item, int amount) {
        return PlayerItemUpdater.playerUpdate(player, item.getItemStack(), false).amount(Math.min(64, Math.max(1, amount)));
    }

    static ItemStack.Builder glowing(ItemStack.Builder builder) {
        return ItemStacks.enchanted(builder);
    }

    static void sound(HypixelPlayer player, String key, float pitch) {
        player.playSound(Sound.sound(Key.key(key), Sound.Source.PLAYER, 1f, pitch), Sound.Emitter.self());
    }

    static void later(long millis, BooleanSupplier stillOpen, Runnable task) {
        MinecraftServer.getSchedulerManager().buildTask(() -> {
            if (stillOpen.getAsBoolean()) task.run();
        }).delay(TaskSchedule.millis(millis)).schedule();
    }

    static String commas(long value) {
        return String.format("%,d", value);
    }
}
