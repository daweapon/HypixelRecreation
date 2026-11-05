package dev.daweapon.type.catacombs.secret;

import dev.daweapon.commons.skyblock.item.ItemType;
import dev.daweapon.commons.text.Text;
import dev.daweapon.dungeons.catacombs.CatacombsFloor;
import dev.daweapon.dungeons.catacombs.blessing.BlessingType;
import dev.daweapon.dungeons.catacombs.layout.FloorLayout;
import dev.daweapon.dungeons.catacombs.layout.PlacedRoom;
import dev.daweapon.dungeons.catacombs.layout.SecretType;
import dev.daweapon.dungeons.catacombs.secret.CatacombsSecretType;
import dev.daweapon.type.catacombs.blessing.BlessingMessages;
import dev.daweapon.type.catacombs.blessing.PartyBlessings;
import dev.daweapon.type.catacombs.engine.DungeonHost;
import dev.daweapon.type.generic.block.PlayerHeadBlockHandler;
import dev.daweapon.type.skyblockgeneric.essence.EssenceType;
import dev.daweapon.type.skyblockgeneric.essence.Essences;
import dev.daweapon.type.skyblockgeneric.item.SkyBlockItem;
import dev.daweapon.type.skyblockgeneric.loottable.LootRoll;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockActionBar;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.nbt.BinaryTagTypes;
import net.kyori.adventure.nbt.CompoundBinaryTag;
import net.kyori.adventure.nbt.ListBinaryTag;
import net.kyori.adventure.sound.Sound;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.ItemEntity;
import net.minestom.server.entity.Player;
import net.minestom.server.instance.block.Block;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.minestom.server.network.packet.server.play.BlockActionPacket;
import net.minestom.server.tag.Tag;
import org.jetbrains.annotations.Nullable;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;
import java.util.random.RandomGenerator;

/**
 * The secrets of a run's rooms: chests to open, items to pick up, bats to kill and Wither Essence to right-click. Each
 * counts once for the run, pays its reward through {@link LootRoll}, and the room's found/total shows on the action bar.
 */
public final class SecretEngine {
    private static final double PICKUP_RANGE = 1.5;
    private static final String ESSENCE_TEXTURE = "c4db4adfa9bf48ff5d41707ae34ea78bd2371659fcd8cd8934749af4cce9b";
    private static final List<BlessingType> SECRET_BLESSINGS = List.of(BlessingType.LIFE, BlessingType.POWER,
            BlessingType.STONE, BlessingType.WISDOM);
    private static final int MAX_SECRET_BLESSING_LEVEL = 2;

    private record GroundItem(ItemEntity entity, @Nullable ItemType item) {
    }

    private final DungeonHost host;
    private final CatacombsFloor floor;
    private final PartyBlessings blessings;
    private final List<SecretSpot> spots;
    private final SecretLedger ledger;
    private final Map<SecretSpot, GroundItem> groundItems = new ConcurrentHashMap<>();
    private final Map<SecretSpot, SecretBat> bats = new ConcurrentHashMap<>();
    private final RandomGenerator random = ThreadLocalRandom.current();

    public SecretEngine(DungeonHost host, FloorLayout layout, CatacombsFloor floor, PartyBlessings blessings) {
        this.host = host;
        this.floor = floor;
        this.blessings = blessings;
        this.spots = SecretSpots.of(layout);
        this.ledger = SecretLedger.of(layout);
    }

    public void start() {
        for (SecretSpot spot : spots) host.instance().loadChunk(spot.x(), spot.z()).thenRun(() -> place(spot));
    }

    private void place(SecretSpot spot) {
        switch (spot.type()) {
            case CHEST -> host.instance().setBlock(spot.x(), spot.y(), spot.z(), Block.CHEST.withProperty("facing", "north"));
            case ESSENCE -> host.instance().setBlock(spot.x(), spot.y(), spot.z(), essenceHead());
            case ITEM, REDSTONE_KEY -> dropGroundItem(spot);
            case BAT -> spawnBat(spot);
        }
    }

    private static Block essenceHead() {
        String json = "{\"textures\":{\"SKIN\":{\"url\":\"http://textures.minecraft.net/texture/" + ESSENCE_TEXTURE + "\"}}}";
        CompoundBinaryTag profile = CompoundBinaryTag.builder().put("properties",
                ListBinaryTag.builder(BinaryTagTypes.COMPOUND).add(CompoundBinaryTag.builder()
                        .putString("name", "textures")
                        .putString("value", Base64.getEncoder().encodeToString(json.getBytes(StandardCharsets.UTF_8)))
                        .build()).build()).build();
        return Block.PLAYER_HEAD.withProperty("rotation", "0").withHandler(new PlayerHeadBlockHandler())
                .withTag(Tag.NBT("profile"), profile);
    }

    private void dropGroundItem(SecretSpot spot) {
        ItemType item = spot.type() == SecretType.REDSTONE_KEY ? null
                : Optional.ofNullable(SecretRewardTable.roll(null, floor, CatacombsSecretType.GROUND_ITEM, random))
                .map(SecretRewardTable.Entry::item).orElse(ItemType.SPIRIT_LEAP);
        ItemEntity entity = new ItemEntity(item == null
                ? ItemStack.of(Material.REDSTONE_TORCH)
                : new SkyBlockItem(item).getItemStack());
        entity.setPickable(false);
        entity.setMergeable(false);
        entity.setInstance(host.instance(), spot.centre());
        groundItems.put(spot, new GroundItem(entity, item));
    }

    private void spawnBat(SecretSpot spot) {
        SecretBat bat = new SecretBat(killer -> {
            bats.remove(spot);
            collect(spot, killer);
        });
        bats.put(spot, bat);
        bat.setInstance(host.instance(), spot.centre().add(0, 1.5, 0));
    }

    /** A right-click on a chest or Wither Essence; true when the block was a secret. */
    public boolean interact(SkyBlockPlayer player, int x, int y, int z) {
        SecretSpot spot = spots.stream()
                .filter(each -> each.type() == SecretType.CHEST || each.type() == SecretType.ESSENCE)
                .filter(each -> each.at(x, y, z))
                .findFirst().orElse(null);
        if (spot == null) return false;
        if (spot.type() == SecretType.CHEST && ledger.isFound(spot.key())) {
            player.sendMessage(SecretMessages.CHEST_SEARCHED);
            return true;
        }
        collect(spot, player);
        return true;
    }

    public void poll() {
        for (Map.Entry<SecretSpot, GroundItem> entry : groundItems.entrySet()) {
            Pos at = entry.getValue().entity().getPosition();
            host.alivePlayers().stream()
                    .filter(player -> player.getPosition().distance(at) <= PICKUP_RANGE)
                    .findFirst()
                    .ifPresent(player -> collect(entry.getKey(), player));
        }
        for (SkyBlockPlayer player : partyInInstance()) showRoomSecrets(player);
    }

    private void showRoomSecrets(SkyBlockPlayer player) {
        Text display = roomAt(player.getPosition())
                .filter(room -> ledger.totalIn(room.index()) > 0)
                .map(room -> Text.of(SecretMessages.roomSecrets(ledger.foundIn(room.index()), ledger.totalIn(room.index()))))
                .orElse(Text.empty());
        SkyBlockActionBar.getFor(player).setDefaultDisplay(SkyBlockActionBar.BarSection.SECRETS, display);
    }

    private Optional<PlacedRoom> roomAt(Pos position) {
        return host.arena().plan().roomAtColumn(position.blockX(), position.blockZ());
    }

    /** Finds the closest secret the party hasn't found yet for the given player, as a staff shortcut. */
    public Optional<SecretSpot> collectNearest(SkyBlockPlayer player) {
        Optional<SecretSpot> nearest = spots.stream()
                .filter(spot -> !ledger.isFound(spot.key()))
                .min(Comparator.comparingDouble(spot -> player.getPosition().distanceSquared(spot.centre())));
        nearest.ifPresent(spot -> {
            SecretBat bat = bats.remove(spot);
            if (bat != null) bat.remove();
            collect(spot, player);
        });
        return nearest;
    }

    private void collect(SecretSpot spot, SkyBlockPlayer player) {
        GroundItem ground = groundItems.remove(spot);
        if (ground != null) ground.entity().remove();
        if (!ledger.find(spot.key())) return;
        host.recordSecret();
        switch (spot.type()) {
            case CHEST -> {
                openLid(spot);
                reward(spot, player);
            }
            case ESSENCE -> essence(spot, player);
            case ITEM -> pickUp(ground, player);
            case BAT -> reward(spot, player);
            case REDSTONE_KEY -> player.sendMessage(SecretMessages.REDSTONE_KEY);
        }
    }

    private void openLid(SecretSpot spot) {
        Pos at = spot.centre();
        host.instance().sendGroupedPacket(new BlockActionPacket(new Pos(spot.x(), spot.y(), spot.z()), (byte) 1, (byte) 1, Block.CHEST));
        host.instance().playSound(Sound.sound(Key.key("block.chest.open"), Sound.Source.BLOCK, 1f, 1f), at.x(), at.y(), at.z());
    }

    private void essence(SecretSpot spot, SkyBlockPlayer finder) {
        host.instance().setBlock(spot.x(), spot.y(), spot.z(), Block.AIR);
        Pos at = spot.centre();
        host.instance().playSound(Sound.sound(Key.key("entity.experience_orb.pickup"), Sound.Source.PLAYER, 1f, 1f), at.x(), at.y(), at.z());
        for (SkyBlockPlayer member : partyInInstance()) {
            Essences.add(member, EssenceType.WITHER, 1);
            if (member == finder) member.sendMessage(SecretMessages.essenceFoundByYou());
            else member.sendMessage(SecretMessages.essenceFoundBy(finder));
        }
    }

    private void pickUp(@Nullable GroundItem ground, SkyBlockPlayer player) {
        List<ItemType> candidates = SecretRewardTable.entries(floor, CatacombsSecretType.GROUND_ITEM).stream()
                .map(SecretRewardTable.Entry::item).filter(Objects::nonNull).toList();
        ItemType forced = LootRoll.claim(player.getUuid(), candidates);
        ItemType item = forced != null ? forced : ground == null ? null : ground.item();
        if (item == null) return;
        give(player, item);
    }

    private void reward(SecretSpot spot, SkyBlockPlayer player) {
        SecretRewardTable.Entry entry = SecretRewardTable.roll(player.getUuid(), floor, SecretRewardTable.kind(spot.type()), random);
        if (entry == null) return;
        if (entry.blessing()) {
            BlessingType type = SECRET_BLESSINGS.get(random.nextInt(SECRET_BLESSINGS.size()));
            host.broadcast(Text.of(BlessingMessages.obtained(type), player.getColouredName()));
            blessings.grant(type, 1 + random.nextInt(MAX_SECRET_BLESSING_LEVEL), player);
            return;
        }
        if (entry.item() != null) give(player, entry.item());
    }

    private void give(SkyBlockPlayer player, ItemType item) {
        player.addAndUpdateItem(item);
        host.broadcast(SecretMessages.obtained(player, item));
    }

    private List<SkyBlockPlayer> partyInInstance() {
        return host.instance().getPlayers().stream()
                .filter(Player::isOnline)
                .filter(SkyBlockPlayer.class::isInstance)
                .map(SkyBlockPlayer.class::cast)
                .toList();
    }

    public SecretLedger ledger() {
        return ledger;
    }

    public void dispose() {
        groundItems.values().forEach(ground -> ground.entity().remove());
        groundItems.clear();
        bats.values().forEach(SecretBat::remove);
        bats.clear();
        for (SkyBlockPlayer player : partyInInstance()) {
            SkyBlockActionBar.getFor(player).setDefaultDisplay(SkyBlockActionBar.BarSection.SECRETS, Text.empty());
        }
    }
}
