package dev.daweapon.type.skyblockgeneric.gifts;

import net.minestom.server.MinecraftServer;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.metadata.display.ItemDisplayMeta;
import net.minestom.server.instance.Instance;
import net.minestom.server.timer.Task;
import net.minestom.server.timer.TaskSchedule;
import dev.daweapon.commons.text.Text;
import dev.daweapon.type.generic.entity.InteractionEntity;
import dev.daweapon.type.generic.entity.hologram.HologramEntity;
import dev.daweapon.type.generic.user.HypixelPlayer;
import dev.daweapon.type.skyblockgeneric.item.SkyBlockItem;
import dev.daweapon.type.skyblockgeneric.item.updater.NonPlayerItemUpdater;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.ArrayList;
import java.util.List;

/**
 * A gift one player has placed for another. Only the receiver can open it; if it is left
 * unopened it is handed back to the giver.
 */
public final class PlacedGift {
    private static final int EXPIRY_SECONDS = 60;
    private static final float SIZE = 0.6f;

    private final Gift gift;
    private final SkyBlockPlayer giver;
    private final SkyBlockPlayer receiver;
    private final Instance instance;
    private final Pos position;
    private final List<Entity> entities = new ArrayList<>();
    private Task expiry;
    private boolean finished;

    private PlacedGift(Gift gift, SkyBlockPlayer giver, SkyBlockPlayer receiver) {
        this.gift = gift;
        this.giver = giver;
        this.receiver = receiver;
        this.instance = receiver.getInstance();
        Pos from = giver.getPosition();
        Pos to = receiver.getPosition();
        this.position = new Pos((from.x() + to.x()) / 2, to.y(), (from.z() + to.z()) / 2);
    }

    public static void place(Gift gift, SkyBlockPlayer giver, SkyBlockPlayer receiver) {
        new PlacedGift(gift, giver, receiver).spawn();
        giver.sendMessage(Text.of("<a>You placed a {} <a>for {}<a>!",
                new SkyBlockItem(gift.getItem()).getDisplayNameText(), receiver.getColouredName()));
        receiver.sendMessage(Text.of("{} <a>placed a {} <a>for you! Right-click it to open it!",
                giver.getColouredName(), new SkyBlockItem(gift.getItem()).getDisplayNameText()));
    }

    private void spawn() {
        Entity display = new Entity(EntityType.ITEM_DISPLAY);
        display.editEntityMeta(ItemDisplayMeta.class, meta -> {
            meta.setHasNoGravity(true);
            meta.setScale(new Vec(SIZE, SIZE, SIZE));
            meta.setItemStack(new NonPlayerItemUpdater(new SkyBlockItem(gift.getItem()).getItemStack()).getUpdatedItem().build());
        });
        display.setInstance(instance, position.add(0, SIZE / 2, 0));
        entities.add(display);

        hologram(Text.of("<e><l>CLICK TO OPEN"), 1.3);
        hologram(Text.of("<7>To: {}", receiver.getColouredName()), 1.05);

        InteractionEntity interaction = new InteractionEntity(SIZE, SIZE, (player, _) -> click(player));
        interaction.setInstance(instance, position);
        entities.add(interaction);

        expiry = MinecraftServer.getSchedulerManager().buildTask(this::expire)
                .delay(TaskSchedule.seconds(EXPIRY_SECONDS)).schedule();
    }

    private void hologram(Text text, double height) {
        HologramEntity line = new HologramEntity(text);
        line.setInstance(instance, position.add(0, height, 0));
        entities.add(line);
    }

    private void click(HypixelPlayer player) {
        if (finished) return;
        if (player != receiver) {
            player.sendMessage(Text.of("<c>Only {} <c>can open this gift!", receiver.getColouredName()));
            return;
        }
        if (!finish()) return;
        GiftOpening.open(gift, giver, receiver, instance, position);
    }

    private void expire() {
        if (!finish()) return;
        if (!GiftOpening.isPresent(giver)) return;
        giver.addAndUpdateItem(new SkyBlockItem(gift.getItem()));
        giver.sendMessage(Text.of("<c>Your gift for {} <c>wasn't opened in time and was returned to you.",
                receiver.getColouredName()));
    }

    private synchronized boolean finish() {
        if (finished) return false;
        finished = true;
        if (expiry != null) expiry.cancel();
        entities.forEach(Entity::remove);
        return true;
    }
}
