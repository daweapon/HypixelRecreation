package dev.daweapon.type.skyblockgeneric.farming.chum;

import dev.daweapon.commons.skyblock.item.ItemType;
import dev.daweapon.type.generic.entity.InteractionEntity;
import dev.daweapon.type.generic.entity.hologram.HologramEntity;
import dev.daweapon.type.skyblockgeneric.item.SkyBlockItem;
import dev.daweapon.type.skyblockgeneric.item.updater.NonPlayerItemUpdater;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.LivingEntity;
import net.minestom.server.entity.metadata.item.ItemEntityMeta;
import net.minestom.server.instance.Instance;

import java.util.UUID;

public final class ChumBucket {
    public static final int CAPACITY = 10;
    private static final float CLICK_SIZE = 0.8f;

    private final UUID ownerId;
    private final String ownerName;
    private final Instance instance;
    private final Pos position;
    private final LivingEntity icon = new LivingEntity(EntityType.ITEM);
    private final HologramEntity title;
    private final HologramEntity subtitle;
    private final InteractionEntity click;
    private int chum;

    ChumBucket(SkyBlockPlayer owner, Instance instance, Pos position) {
        this.ownerId = owner.getUuid();
        this.ownerName = owner.getUsername();
        this.instance = instance;
        this.position = position;
        icon.setCanPickupItem(false);
        icon.setNoGravity(true);
        render();
        icon.setInstance(instance, position.add(0, 0.1, 0));
        title = new HologramEntity("<a>Chum Bucket");
        title.setInstance(instance, position.add(0, 0.9, 0));
        subtitle = new HologramEntity(subtitleMarkup());
        subtitle.setInstance(instance, position.add(0, 0.65, 0));
        click = new InteractionEntity(CLICK_SIZE, CLICK_SIZE, (player, _) -> ChumBuckets.clicked((SkyBlockPlayer) player, this));
        click.setInstance(instance, position);
    }

    public UUID ownerId() {
        return ownerId;
    }

    public String ownerName() {
        return ownerName;
    }

    public Instance instance() {
        return instance;
    }

    public Pos position() {
        return position;
    }

    public int chum() {
        return chum;
    }

    public boolean isFull() {
        return chum >= CAPACITY;
    }

    public int room() {
        return CAPACITY - chum;
    }

    public void add(int amount) {
        chum = Math.min(CAPACITY, chum + amount);
        render();
        subtitle.setText(subtitleMarkup());
    }

    public void remove() {
        icon.remove();
        title.remove();
        subtitle.remove();
        click.remove();
    }

    private String subtitleMarkup() {
        return "<7>" + ownerName + "'s bucket <8>(<a>" + chum + "<8>/" + CAPACITY + ")";
    }

    private void render() {
        ItemType type = isFull() ? ItemType.FULL_CHUM_BUCKET : ItemType.EMPTY_CHUM_BUCKET;
        icon.editEntityMeta(ItemEntityMeta.class, meta ->
                meta.setItem(new NonPlayerItemUpdater(new SkyBlockItem(type)).getUpdatedItem().build()));
    }
}
