package dev.daweapon.type.hub.carnival;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import net.minestom.server.MinecraftServer;
import net.minestom.server.component.DataComponents;
import net.minestom.server.coordinate.BlockVec;
import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.ItemEntity;
import net.minestom.server.entity.metadata.other.PrimedTntMeta;
import net.minestom.server.event.EventListener;
import net.minestom.server.event.player.PlayerPacketEvent;
import net.minestom.server.instance.block.Block;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.minestom.server.network.packet.client.play.ClientPlayerActionPacket;
import net.minestom.server.network.packet.server.play.BlockBreakAnimationPacket;
import net.minestom.server.network.packet.server.play.ParticlePacket;
import net.minestom.server.network.packet.server.play.WorldEventPacket;
import net.minestom.server.particle.Particle;
import net.minestom.server.potion.Potion;
import net.minestom.server.potion.PotionEffect;
import dev.daweapon.commons.StringUtility;
import dev.daweapon.commons.skyblock.item.ItemType;
import dev.daweapon.commons.text.Text;
import dev.daweapon.type.generic.entity.hologram.HologramEntity;
import dev.daweapon.type.generic.gui.inventory.ItemStacks;
import dev.daweapon.type.skyblockgeneric.carnival.CarnivalGame;
import dev.daweapon.type.skyblockgeneric.carnival.CarnivalGameResult;
import dev.daweapon.type.skyblockgeneric.carnival.games.FruitDiggingBoard;
import dev.daweapon.type.skyblockgeneric.carnival.games.FruitDiggingBoard.CellState;
import dev.daweapon.type.skyblockgeneric.carnival.games.FruitDiggingBoard.Dig;
import dev.daweapon.type.skyblockgeneric.carnival.games.FruitDiggingBoard.DowsingMode;
import dev.daweapon.type.skyblockgeneric.carnival.games.FruitDiggingBoard.Reveal;
import dev.daweapon.type.skyblockgeneric.carnival.games.FruitTile;
import dev.daweapon.type.skyblockgeneric.item.SkyBlockItem;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Fruit Digging on the sand board beside the Carnival Pirateman, as captured on Hypixel: the server breaks the sand
 * over 26 ticks while the shovel is held on it, the tile turns to sandstone and the find floats above it as a head
 * with a hologram for three seconds.
 */
public class FruitDiggingGame extends CarnivalGameBase {
    public static final int SECONDS = 5 * 60;
    private static final BlockVec BOARD_CORNER = new BlockVec(-112, 72, 19);
    private static final Pos START = new Pos(-106.5, 73, 24.5, 135, 0);
    private static final Pos END = new Pos(-105.5, 73, 30.5, 135, 0);
    private static final int DIG_TICKS = 26;
    private static final int DISPLAY_TICKS = 60;
    private static final int TNT_FUSE_TICKS = 21;
    private static final int WATERMELON_DELAY_TICKS = 10;
    private static final int END_DELAY_TICKS = 60;
    private static final int BREAKER_ID = 0;
    private static final double LOWER_LINE_Y = 74.0;
    private static final double UPPER_LINE_Y = 74.34375;
    private static final float HIGH_PITCH = 1.4920635f;
    private static final Block DESTROYED_TILE = Block.SANDSTONE_STAIRS.withProperty("facing", "east").withProperty("half", "top");
    private static final Map<FruitTile, String> HEAD_TEXTURES = new EnumMap<>(Map.of(
            FruitTile.APPLE, "17ea278d6225c447c5943d652798d0bbbd1418434ce8c54c54fdac79994ddd6c",
            FruitTile.MANGO, "f363a62126a35537f8189343a22660de75e810c6ac004a7d3da65f1c040a839",
            FruitTile.CHERRY, "c92b099a62cd2fbf8ada09dec145c75d7fda4dc57b968bea3a8fa11e37aa48b2",
            FruitTile.POMEGRANATE, "40824d18079042d5769f264f44394b95b9b99ce689688cc10c9eec3f882ccc08",
            FruitTile.WATERMELON, "efe4ef83baf105e8dee6cf03dfe7407f1911b3b9952c891ae34139560f2931d6",
            FruitTile.RUM, "407b275d28b927b1bf7f6dd9f45fbdad2af8571c54c8f027d1bff6956fbf3c16",
            FruitTile.BOMB, "a76a2811d1e176a07b6d0a657b910f134896ce30850f6e80c7c83732d85381ea"));
    private static final Map<FruitTile, Material> UNCAPTURED_ITEMS = new EnumMap<>(Map.of(
            FruitTile.COCONUT, Material.COCOA_BEANS,
            FruitTile.DURIAN, Material.SLIME_BALL,
            FruitTile.DRAGONFRUIT, Material.CHORUS_FRUIT));

    private static final Map<DowsingMode, String> SHOVEL_MODELS = new EnumMap<>(Map.of(
            DowsingMode.MINES, "hypixel_skyblock:item/uncategorized/carnival_shovel_iron",
            DowsingMode.ANCHOR, "hypixel_skyblock:item/uncategorized/carnival_shovel_diamond",
            DowsingMode.TREASURE, "hypixel_skyblock:item/uncategorized/carnival_shovel_gold"));

    private record TileSound(String sound, float pitch) {}

    private static final Map<FruitTile, List<TileSound>> SOUNDS = new EnumMap<>(Map.of(
            FruitTile.APPLE, List.of(new TileSound("entity.generic.eat", HIGH_PITCH)),
            FruitTile.MANGO, List.of(new TileSound("entity.iron_golem.attack", 1.1904762f)),
            FruitTile.POMEGRANATE, List.of(new TileSound("entity.generic.splash", HIGH_PITCH)),
            FruitTile.CHERRY, List.of(new TileSound("entity.chicken.egg", HIGH_PITCH)),
            FruitTile.WATERMELON, List.of(new TileSound("entity.creeper.hurt", HIGH_PITCH),
                    new TileSound("entity.player.splash", 0.74603176f)),
            FruitTile.RUM, List.of(new TileSound("entity.generic.drink", 1f)),
            FruitTile.BOMB, List.of(new TileSound("entity.creeper.hurt", 1f)),
            FruitTile.COCONUT, List.of(new TileSound("entity.generic.eat", 1f)),
            FruitTile.DURIAN, List.of(new TileSound("entity.generic.eat", 0.5f)),
            FruitTile.DRAGONFRUIT, List.of(new TileSound("entity.player.levelup", HIGH_PITCH))));

    private record Scheduled(long at, Runnable action) {}

    private final FruitDiggingBoard board = new FruitDiggingBoard(ThreadLocalRandom.current());
    private final Queue<Runnable> packets = new ConcurrentLinkedQueue<>();
    private final List<Scheduled> scheduled = new ArrayList<>();
    private final List<Entity> entities = new ArrayList<>();
    private final EventListener<PlayerPacketEvent> digListener = EventListener.of(PlayerPacketEvent.class, this::onPacket);
    private DowsingMode mode = DowsingMode.MINES;
    private int[] digging;
    private long digStart;
    private int lastStage = -1;
    private long lastDigTick;
    private long shownScore;
    private @Nullable Entity anchorMarker;
    private @Nullable int[] anchorCell;

    public FruitDiggingGame(SkyBlockPlayer player) {
        super(player, CarnivalGame.FRUIT_DIGGING, SECONDS);
    }

    @Override
    protected Pos startPosition() {
        return START;
    }

    @Override
    protected Pos endPosition() {
        return END;
    }

    @Override
    protected void onStart() {
        player.addEffect(new Potion(PotionEffect.MINING_FATIGUE, 255, Potion.INFINITE_DURATION,
                (byte) (Potion.AMBIENT_FLAG | Potion.PARTICLES_FLAG)));
        MinecraftServer.getGlobalEventHandler().addListener(digListener);
        updateShovelLore();
    }

    @Override
    public void onRightClick() {
        mode = mode.next();
        play("block.lever.click", 1f, mode.getSwitchPitch());
        updateShovelLore();
    }

    private void onPacket(PlayerPacketEvent event) {
        if (event.getPlayer() != player || !(event.getPacket() instanceof ClientPlayerActionPacket action)) return;
        Point position = action.blockPosition();
        switch (action.status()) {
            case STARTED_DIGGING -> packets.add(() -> startDigging(position));
            case CANCELLED_DIGGING -> packets.add(() -> {
                if (digging != null && cellPosition(digging).sameBlock(position)) stopDigging();
            });
            default -> {}
        }
    }

    private void startDigging(Point position) {
        int[] cell = cellAt(position);
        if (digging != null && cell != null && digging[0] == cell[0] && digging[1] == cell[1]) return;
        stopDigging();
        if (cell == null || board.isFinished() || board.stateAt(cell[0], cell[1]) != CellState.HIDDEN) return;
        digging = cell;
        digStart = tick;
        lastStage = -1;
    }

    private void stopDigging() {
        if (digging == null) return;
        player.sendPacket(new BlockBreakAnimationPacket(BREAKER_ID, cellPosition(digging), (byte) -1));
        digging = null;
    }

    private @Nullable int[] cellAt(Point position) {
        int x = position.blockX() - BOARD_CORNER.blockX();
        int z = position.blockZ() - BOARD_CORNER.blockZ();
        boolean inside = position.blockY() == BOARD_CORNER.blockY() && x >= 0 && z >= 0
                && x < FruitDiggingBoard.SIZE && z < FruitDiggingBoard.SIZE;
        return inside ? new int[]{x, z} : null;
    }

    private BlockVec cellPosition(int[] cell) {
        return BOARD_CORNER.add(cell[0], 0, cell[1]);
    }

    private Pos above(int x, int z, double y) {
        return new Pos(BOARD_CORNER.blockX() + x + 0.5, y, BOARD_CORNER.blockZ() + z + 0.5);
    }

    @Override
    protected void onTick() {
        Runnable packet;
        while ((packet = packets.poll()) != null) packet.run();
        runScheduled();
        if (digging == null) return;
        if (board.stateAt(digging[0], digging[1]) != CellState.HIDDEN) {
            stopDigging();
            return;
        }
        long elapsed = tick - digStart;
        if (elapsed >= DIG_TICKS) {
            finishDigging();
            return;
        }
        int stage = (int) (1 + elapsed * 9 / DIG_TICKS);
        if (stage != lastStage) {
            lastStage = stage;
            player.sendPacket(new BlockBreakAnimationPacket(BREAKER_ID, cellPosition(digging), (byte) stage));
        }
    }

    private void runScheduled() {
        List<Scheduled> due = scheduled.stream().filter(entry -> entry.at() <= tick).toList();
        scheduled.removeAll(due);
        due.forEach(entry -> entry.action().run());
    }

    private void schedule(long delay, Runnable action) {
        scheduled.add(new Scheduled(tick + delay, action));
    }

    private void finishDigging() {
        int[] cell = digging;
        digging = null;
        BlockVec position = cellPosition(cell);
        player.sendPacket(new BlockBreakAnimationPacket(BREAKER_ID, position, (byte) 10));
        Dig dig = board.dig(cell[0], cell[1], mode);
        if (dig == null) return;
        if (anchorCell != null && anchorCell[0] == cell[0] && anchorCell[1] == cell[1]) removeAnchor();
        lastDigTick = tick;
        player.sendPacket(new WorldEventPacket(2001, position, Block.SAND.stateId(), false));
        showBlock(position, Block.SANDSTONE);
        player.sendPacket(new BlockBreakAnimationPacket(BREAKER_ID, position, (byte) -1));

        Reveal dug = dig.reveals().getFirst();
        SOUNDS.get(dug.tile()).forEach(sound -> play(sound.sound(), 1f, sound.pitch()));
        play("entity.chicken.egg", 0.3f, 1f);
        showReveal(dug, dig.bombDefused());
        if (dig.dowsing() != null) player.sendMessage(dig.dowsing());
        if (dig.anchor() != null) showAnchor(dig.anchor());
        if (!dig.destroyed().isEmpty()) primeTnt(dig.destroyed());

        List<Reveal> chained = dig.reveals().subList(1, dig.reveals().size());
        for (int i = 0; i < chained.size(); i++) {
            Reveal reveal = chained.get(i);
            schedule((long) WATERMELON_DELAY_TICKS * (i + 1), () -> showChained(reveal));
        }
    }

    private void showAnchor(Reveal anchor) {
        removeAnchor();
        ItemEntity item = new ItemEntity(itemFor(anchor.tile()));
        item.setPickable(false);
        item.setMergeable(false);
        item.setNoGravity(true);
        spawn(item, above(anchor.x(), anchor.z(), 73), DISPLAY_TICKS);
        anchorMarker = item;
        anchorCell = new int[]{anchor.x(), anchor.z()};
    }

    private void removeAnchor() {
        if (anchorMarker == null) return;
        anchorMarker.remove();
        entities.remove(anchorMarker);
        anchorMarker = null;
        anchorCell = null;
    }

    private void showChained(Reveal reveal) {
        showBlock(BOARD_CORNER.add(reveal.x(), 0, reveal.z()), Block.SANDSTONE);
        Pos at = above(reveal.x(), reveal.z(), 73);
        player.sendPacket(new ParticlePacket(Particle.FIREWORK, true, true, at.x(), at.y(), at.z(), 0.4f, 0.05f, 0.4f, 0f, 20));
        play("entity.firework_rocket.blast", 1f, 1f);
        showReveal(reveal, false);
    }

    private void showReveal(Reveal reveal, boolean defused) {
        long points = Math.round(reveal.points());
        if (points > 0) {
            shownScore += points;
            gain(points);
        }
        ItemEntity item = new ItemEntity(itemFor(reveal.tile()));
        item.setPickable(false);
        item.setMergeable(false);
        item.setNoGravity(true);
        spawn(item, above(reveal.x(), reveal.z(), 73), DISPLAY_TICKS);

        List<String> lines = hologramLines(reveal, defused);
        if (lines.size() == 1) {
            spawn(new HologramEntity(lines.getFirst()), above(reveal.x(), reveal.z(), LOWER_LINE_Y), DISPLAY_TICKS);
            return;
        }
        spawn(new HologramEntity(lines.get(0)), above(reveal.x(), reveal.z(), UPPER_LINE_Y), DISPLAY_TICKS);
        spawn(new HologramEntity(lines.get(1)), above(reveal.x(), reveal.z(), LOWER_LINE_Y), DISPLAY_TICKS);
    }

    private static List<String> hologramLines(Reveal reveal, boolean defused) {
        FruitTile tile = reveal.tile();
        if (tile == FruitTile.RUM) return List.of("<e>Rum", "<c>✘ <7>Dowsing");
        if (tile == FruitTile.BOMB) {
            return defused ? List.of("<c>Bomb", "<7>Bomb protection (" + FruitTile.COCONUT.colored() + "<7>)") : List.of("<c>Bomb");
        }
        String name = tile.colored() + " <7>(+" + StringUtility.commaify(Math.round(reveal.points())) + ")";
        FruitTile source = reveal.multiplierSource();
        if (source == null) return List.of(name);
        String multiplier = multiplier(reveal.multiplier()) + "x points (" + source.colored();
        return List.of(name, "<7>" + multiplier + (reveal.fromWatermelon() ? " <7>explosion)" : "<7>)"));
    }

    private static String multiplier(double value) {
        return value == Math.rint(value) ? String.valueOf((long) value) : String.valueOf(value);
    }

    private static ItemStack itemFor(FruitTile tile) {
        String texture = HEAD_TEXTURES.get(tile);
        if (texture != null) return ItemStacks.head(texture, Text.empty(), List.of()).build();
        return ItemStack.of(UNCAPTURED_ITEMS.get(tile));
    }

    private void primeTnt(List<int[]> cells) {
        List<Entity> primed = new ArrayList<>();
        ThreadLocalRandom random = ThreadLocalRandom.current();
        for (int[] cell : cells) {
            Entity tnt = new Entity(EntityType.TNT);
            tnt.editEntityMeta(PrimedTntMeta.class, meta -> meta.setFuseTime(TNT_FUSE_TICKS));
            tnt.setVelocity(new Vec(random.nextDouble(-0.4, 0.4), 4, random.nextDouble(-0.4, 0.4)));
            spawn(tnt, above(cell[0], cell[1], 73.03125), TNT_FUSE_TICKS);
            primed.add(tnt);
        }
        schedule(TNT_FUSE_TICKS, () -> {
            for (int i = 0; i < cells.size(); i++) {
                Pos at = primed.get(i).getPosition();
                player.sendPacket(new ParticlePacket(Particle.EXPLOSION, true, true, at.x(), 73.25, at.z(), 0f, 0f, 0f, 0f, 1));
                play("entity.generic.explode", 1f, 1f);
                showBlock(cellPosition(cells.get(i)), DESTROYED_TILE);
            }
        });
    }

    private void spawn(Entity entity, Pos at, int lifetimeTicks) {
        if (player.getInstance() == null) return;
        entity.setAutoViewable(false);
        entity.setInstance(player.getInstance(), at).thenRun(() -> entity.addViewer(player));
        entities.add(entity);
        schedule(lifetimeTicks, () -> {
            entity.remove();
            entities.remove(entity);
        });
    }

    private void updateShovelLore() {
        for (int slot = 0; slot < 9; slot++) {
            ItemStack stack = player.getInventory().getItemStack(slot);
            if (stack.isAir() || new SkyBlockItem(stack).getAttributeHandler().getPotentialType() != ItemType.CARNIVAL_SHOVEL) continue;
            List<Component> lore = stack.get(DataComponents.LORE);
            if (lore == null) return;
            player.getInventory().setItemStack(slot, stack.with(DataComponents.LORE, withMode(lore))
                    .with(DataComponents.ITEM_MODEL, SHOVEL_MODELS.get(mode)));
            return;
        }
    }

    private List<Component> withMode(List<Component> lore) {
        int modeLine = -1;
        int cycleLine = -1;
        for (int i = 0; i < lore.size(); i++) {
            String plain = PlainTextComponentSerializer.plainText().serialize(lore.get(i));
            if (modeLine < 0 && plain.startsWith("Dowsing Mode:")) modeLine = i;
            if (cycleLine < 0 && plain.startsWith("Right-click to cycle")) cycleLine = i;
        }
        if (cycleLine < 0) return lore;
        int from = modeLine < 0 ? cycleLine : modeLine;
        int to = modeLine < 0 ? cycleLine : cycleLine - 1;
        List<String> block = new ArrayList<>();
        block.add("<e>Dowsing Mode: " + mode.getDisplayName());
        block.addAll(mode.getDescription());
        if (modeLine < 0) block.add("");
        List<Component> updated = new ArrayList<>(lore.subList(0, from));
        block.forEach(line -> updated.add(Text.of(line).asComponent().decoration(TextDecoration.ITALIC, false)));
        updated.addAll(lore.subList(to, lore.size()));
        return updated;
    }

    @Override
    protected boolean isDone() {
        return board.isFinished() && tick - lastDigTick >= END_DELAY_TICKS;
    }

    @Override
    protected List<String> sidebarBody() {
        int dug = FruitDiggingBoard.DIGS - board.getDigsLeft();
        String total = (board.isFinished() ? "<a>" : "<c>") + FruitDiggingBoard.DIGS;
        return List.of("<f>Fruits: <a>" + dug + "<7>/" + total);
    }

    @Override
    protected long score() {
        return shownScore;
    }

    @Override
    protected void onEnd() {
        MinecraftServer.getGlobalEventHandler().removeListener(digListener);
        stopDigging();
        scheduled.clear();
        entities.forEach(Entity::remove);
        entities.clear();
        player.removeEffect(PotionEffect.MINING_FATIGUE);
    }

    @Override
    protected CarnivalGameResult result() {
        return board.result();
    }

    @Override
    protected List<String> summary(CarnivalGameResult result) {
        List<String> items = new ArrayList<>();
        board.found().forEach((tile, count) -> items.add("<f>" + count + "x " + tile.colored()));
        List<String> lines = new ArrayList<>();
        lines.add(CarnivalChat.centered("<f>Fruits Collected"));
        lines.addAll(items.isEmpty() ? List.of(CarnivalChat.centered("<7>None")) : CarnivalChat.centeredWrapped(items, "<f>, "));
        return lines;
    }
}
