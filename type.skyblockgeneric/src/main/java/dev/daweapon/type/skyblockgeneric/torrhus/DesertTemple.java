package dev.daweapon.type.skyblockgeneric.torrhus;

import dev.daweapon.commons.ServerType;
import dev.daweapon.type.generic.HypixelConst;
import dev.daweapon.type.generic.entity.InteractionEntity;
import dev.daweapon.type.skyblockgeneric.SkyBlockGenericLoader;
import dev.daweapon.type.skyblockgeneric.chapters.ChapterTriggers;
import dev.daweapon.type.skyblockgeneric.gui.inventories.starborn.GUIStarbornAltar;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;
import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.LivingEntity;
import net.minestom.server.instance.block.Block;
import net.minestom.server.network.packet.server.play.BlockChangePacket;
import net.minestom.server.timer.ExecutionType;
import net.minestom.server.timer.Scheduler;
import net.minestom.server.timer.TaskSchedule;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;

public final class DesertTemple {
    public static final String ACTIVATE_TRIGGER = "starborn.activate";
    private static final int INTERIOR_MIN_X = -621, INTERIOR_MAX_X = -603, INTERIOR_MIN_Z = 236, INTERIOR_MAX_Z = 253;
    private static final int INTERIOR_MIN_Y = 40, INTERIOR_MAX_Y = 56;
    private static final int ALTAR_MIN_X = -614, ALTAR_MAX_X = -610, ALTAR_Y = 44, ALTAR_Z = 252;
    private static final int BUTTON_Z = 232;
    private static final int DOOR_MIN_X = -613, DOOR_MAX_X = -611, DOOR_MIN_Y = 43, DOOR_MAX_Y = 44, DOOR_MIN_Z = 233, DOOR_MAX_Z = 235;
    private static final double DOOR_RANGE_SQUARED = 70 * 70;
    private static final String BANNER = "<5><l>" + "▬".repeat(40);

    private static final Map<String, ColorPuzzle.Color> BUTTONS = new HashMap<>();
    private static final Map<StatuePuzzle.Animal, Pos> PEDESTALS = new EnumMap<>(StatuePuzzle.Animal.class);
    private static final Map<StatuePuzzle.Animal, EntityType> MODELS = new EnumMap<>(StatuePuzzle.Animal.class);
    private static final Map<UUID, Session> SESSIONS = new ConcurrentHashMap<>();

    static {
        button(-613, 49, ColorPuzzle.Color.ORANGE);
        button(-612, 49, ColorPuzzle.Color.RED);
        button(-611, 49, ColorPuzzle.Color.BROWN);
        button(-614, 48, ColorPuzzle.Color.YELLOW);
        button(-614, 47, ColorPuzzle.Color.LIME);
        button(-614, 46, ColorPuzzle.Color.GREEN);
        button(-610, 48, ColorPuzzle.Color.PINK);
        button(-610, 47, ColorPuzzle.Color.MAGENTA);
        button(-610, 46, ColorPuzzle.Color.PURPLE);
        button(-613, 45, ColorPuzzle.Color.CYAN);
        button(-612, 45, ColorPuzzle.Color.LIGHT_BLUE);
        button(-611, 45, ColorPuzzle.Color.BLUE);
        PEDESTALS.put(StatuePuzzle.Animal.BIRD, new Pos(-616.5, 44, 240.5));
        PEDESTALS.put(StatuePuzzle.Animal.SNAKE, new Pos(-606.5, 44, 240.5));
        PEDESTALS.put(StatuePuzzle.Animal.FROG, new Pos(-606.5, 44, 248.5));
        PEDESTALS.put(StatuePuzzle.Animal.FLY, new Pos(-616.5, 44, 248.5));
        MODELS.put(StatuePuzzle.Animal.BIRD, EntityType.PARROT);
        MODELS.put(StatuePuzzle.Animal.SNAKE, EntityType.SILVERFISH);
        MODELS.put(StatuePuzzle.Animal.FROG, EntityType.FROG);
        MODELS.put(StatuePuzzle.Animal.FLY, EntityType.BEE);
    }

    private DesertTemple() {
    }

    private static final class Session {
        private final ColorPuzzle colors = new ColorPuzzle();
        private boolean entrance;
        private final TempleDoorState door = new TempleDoorState();
        private StatuePuzzle statues;
        private final Map<StatuePuzzle.Animal, Entity> models = new EnumMap<>(StatuePuzzle.Animal.class);
        private final List<Entity> entities = new ArrayList<>();
    }

    private static void button(int x, int y, ColorPuzzle.Color color) {
        BUTTONS.put(key(x, y, BUTTON_Z), color);
    }

    private static String key(int x, int y, int z) {
        return x + "," + y + "," + z;
    }

    public static boolean isTorrhus() {
        return HypixelConst.getTypeLoader() != null && HypixelConst.getTypeLoader().getType() == ServerType.SKYBLOCK_TORRHUS_CANYON;
    }

    public static boolean insideTemple(Point position) {
        return position.x() >= INTERIOR_MIN_X && position.x() <= INTERIOR_MAX_X && position.z() >= INTERIOR_MIN_Z
                && position.z() <= INTERIOR_MAX_Z && position.y() >= INTERIOR_MIN_Y && position.y() <= INTERIOR_MAX_Y;
    }

    public static boolean isAltar(Point position, Block block) {
        return block.name().contains("polished_tuff_stairs") && position.blockY() == ALTAR_Y && position.blockZ() == ALTAR_Z
                && position.blockX() >= ALTAR_MIN_X && position.blockX() <= ALTAR_MAX_X;
    }

    public static boolean handle(SkyBlockPlayer player, Point position, Block block) {
        if (!isTorrhus() || player.getSkyblockDataHandler() == null) return false;
        if (block.name().contains("polished_blackstone_button")) {
            ColorPuzzle.Color color = BUTTONS.get(key(position.blockX(), position.blockY(), position.blockZ()));
            if (color == null) return false;
            pressButton(player, color);
            return true;
        }
        if (isAltar(position, block)) {
            if (!StarbornBenefactors.activated(player, StarbornTemple.DESERT)) {
                player.sendMessage("<c>The altar lies dormant. <7>Awaken the temple first.");
            } else {
                GUIStarbornAltar.open(player, StarbornTemple.DESERT);
            }
            return true;
        }
        return false;
    }

    private static void pressButton(SkyBlockPlayer player, ColorPuzzle.Color color) {
        Session session = SESSIONS.computeIfAbsent(player.getUuid(), _ -> new Session());
        switch (session.colors.press(color)) {
            case SOLVED -> {
                session.entrance = true;
                player.sendMessage("<7><o>The guardian of the temple opens the door...");
            }
            case WRONG -> player.sendMessage("<c>The buttons reset.");
            case PROGRESS -> player.sendMessage("<a>The button clicks into place. <7>({}/{})", session.colors.progress(), ColorPuzzle.SEQUENCE.size());
        }
    }

    public static void register(Scheduler scheduler) {
        scheduler.scheduleTask(DesertTemple::tick, TaskSchedule.seconds(1), TaskSchedule.seconds(1), ExecutionType.TICK_END);
    }

    private static void tick() {
        List<UUID> online = new ArrayList<>();
        SkyBlockGenericLoader.forEachLoadedPlayer(player -> {
            online.add(player.getUuid());
            if (player.getSkyblockDataHandler() == null || player.getInstance() == null) return;
            Session session = SESSIONS.get(player.getUuid());
            if (session == null && StarbornBenefactors.activated(player, StarbornTemple.DESERT) && nearDoor(player.getPosition())) {
                session = SESSIONS.computeIfAbsent(player.getUuid(), _ -> new Session());
            }
            if (session != null) updateDoor(player, session);
            boolean wanted = session != null && session.entrance && insideTemple(player.getPosition())
                    && !StarbornBenefactors.activated(player, StarbornTemple.DESERT);
            if (wanted && session.statues == null) spawnStatues(player, session);
            if (!wanted && session != null && session.statues != null) removeStatues(session);
        });
        SESSIONS.entrySet().removeIf(entry -> {
            if (online.contains(entry.getKey())) return false;
            removeStatues(entry.getValue());
            return true;
        });
    }

    private static boolean nearDoor(Point position) {
        double dx = position.x() - (DOOR_MIN_X + DOOR_MAX_X) / 2.0;
        double dy = position.y() - DOOR_MIN_Y;
        double dz = position.z() - DOOR_MIN_Z;
        return dx * dx + dy * dy + dz * dz <= DOOR_RANGE_SQUARED;
    }

    private static void updateDoor(SkyBlockPlayer player, Session session) {
        if (!nearDoor(player.getPosition())) return;
        boolean activated = StarbornBenefactors.activated(player, StarbornTemple.DESERT);
        TempleDoorState.Change change = session.door.update(session.entrance, activated, player.getPosition().z());
        if (change == TempleDoorState.Change.SLAMMED) {
            player.sendMessage("<7><o>The door slams down behind you...");
        }
        boolean open = session.door.state() == TempleDoorState.State.OPEN;
        if (open || change != TempleDoorState.Change.NONE) showDoor(player, open);
    }

    private static void showDoor(SkyBlockPlayer player, boolean open) {
        for (int x = DOOR_MIN_X; x <= DOOR_MAX_X; x++) {
            for (int y = DOOR_MIN_Y; y <= DOOR_MAX_Y; y++) {
                for (int z = DOOR_MIN_Z; z <= DOOR_MAX_Z; z++) {
                    Pos position = new Pos(x, y, z);
                    Block shown = open ? Block.AIR : player.getInstance().getBlock(position);
                    player.sendPacket(new BlockChangePacket(position, shown));
                }
            }
        }
    }

    private static void spawnStatues(SkyBlockPlayer player, Session session) {
        session.statues = StatuePuzzle.scrambled(ThreadLocalRandom.current());
        for (StatuePuzzle.Animal animal : StatuePuzzle.Animal.values()) {
            Pos base = PEDESTALS.get(animal);
            LivingEntity model = new LivingEntity(MODELS.get(animal));
            model.setNoGravity(true);
            model.setInvulnerable(true);
            model.updateViewableRule(viewer -> viewer.getUuid().equals(player.getUuid()));
            model.setInstance(player.getInstance(), base.withView(session.statues.facing(animal).yaw(), 0));
            InteractionEntity click = new InteractionEntity(1.2f, 1.5f, (clicker, _) -> rotate((SkyBlockPlayer) clicker, animal));
            click.updateViewableRule(viewer -> viewer.getUuid().equals(player.getUuid()));
            click.setInstance(player.getInstance(), base);
            session.models.put(animal, model);
            session.entities.add(model);
            session.entities.add(click);
        }
    }

    private static void removeStatues(Session session) {
        session.entities.forEach(Entity::remove);
        session.entities.clear();
        session.models.clear();
        session.statues = null;
    }

    private static void rotate(SkyBlockPlayer player, StatuePuzzle.Animal animal) {
        Session session = SESSIONS.get(player.getUuid());
        if (session == null || session.statues == null) return;
        session.statues.rotate(animal);
        Entity model = session.models.get(animal);
        model.teleport(PEDESTALS.get(animal).withView(session.statues.facing(animal).yaw(), 0));
        if (session.statues.solved()) activate(player, session);
    }

    private static void activate(SkyBlockPlayer player, Session session) {
        removeStatues(session);
        player.getHuntingData().setCounter(StarbornTemple.DESERT.activeKey(), 1);
        player.sendMessage("<7><o>The statues click into place as a secret passage reveals itself...");
        player.sendMessage(BANNER);
        player.sendMessage("<f>                            <d><l>STARBORN TEMPLE");
        player.sendMessage("");
        player.sendMessage("<f>               <a>The <e>Desert Starborn Temple <a>is activated!");
        player.sendMessage("<f>    You now permanently gain the passive effect from its <d>Will<f>.");
        player.sendMessage("<f>    Visit the <a>Altar <f>to become a <9>Benefactor <f>for bonus effects!");
        player.sendMessage(BANNER);
        ChapterTriggers.fire(player, ACTIVATE_TRIGGER);
    }
}
