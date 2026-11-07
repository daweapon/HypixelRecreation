package dev.daweapon.type.catacombs.puzzle;

import dev.daweapon.dungeons.catacombs.layout.PlacedRoom;
import dev.daweapon.dungeons.catacombs.puzzle.CatacombsPuzzle;
import dev.daweapon.dungeons.catacombs.puzzle.CatacombsPuzzleFactory;
import dev.daweapon.dungeons.catacombs.puzzle.PuzzleCatalogue;
import dev.daweapon.dungeons.catacombs.puzzle.weirdos.WeirdosRiddle;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.PlayerSkin;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Three Weirdos (M7 run 3 capture): three named NPCs beside three chests. Clicking one makes it say its line in chat;
 * opening the chest of the weirdo who has the reward solves the puzzle and finds a blessing, any other chest fails it.
 */
public final class ThreeWeirdosRoom extends PuzzleRoom {
    private static final String WRONG_CHEST = "wrong_chest";

    private final PuzzleCatalogue.WeirdosRoom data;
    private final List<PuzzleNpc> npcs = new ArrayList<>();
    private final List<PuzzleChest> chests = new ArrayList<>();
    private WeirdosRiddle riddle;

    public ThreeWeirdosRoom(PuzzleContext context, PlacedRoom room, PuzzleCatalogue.WeirdosRoom data) {
        super(context, room, CatacombsPuzzle.THREE_WEIRDOS);
        this.data = data;
    }

    @Override
    public void start() {
        riddle = WeirdosRiddle.deal(data.cases(), data.names(), ThreadLocalRandom.current(), PuzzleMessages::npcName);
        List<PuzzleCatalogue.SkinData> skins = new ArrayList<>(data.skins());
        Collections.shuffle(skins, ThreadLocalRandom.current());
        for (int spot = 0; spot < data.weirdos().size(); spot++) {
            PuzzleCatalogue.Weirdo weirdo = data.weirdos().get(spot);
            PuzzleCatalogue.SkinData skin = skins.get(spot % skins.size());
            PuzzleNpc npc = new PuzzleNpc(new PlayerSkin(skin.texture(), skin.signature()),
                    List.of("<c>" + riddle.names().get(spot), "<e><l>CLICK"));
            npc.spawn(instance(), frame.spot(weirdo.spot()));
            npcs.add(npc);
            int rewardSpot = spot;
            chests.add(new PuzzleChest(context, frame.block(weirdo.chest()), _ -> {
                if (riddle.rewarded(rewardSpot)) completeRoom();
            }));
        }
    }

    @Override
    public boolean interactEntity(SkyBlockPlayer player, Entity entity) {
        for (int spot = 0; spot < npcs.size(); spot++) {
            if (!npcs.get(spot).is(entity)) continue;
            player.sendMessage(PuzzleMessages.npc(riddle.names().get(spot), riddle.lines().get(spot)));
            return true;
        }
        return false;
    }

    @Override
    public synchronized boolean interact(SkyBlockPlayer player, int x, int y, int z) {
        for (int spot = 0; spot < chests.size(); spot++) {
            PuzzleChest chest = chests.get(spot);
            if (!chest.at(x, y, z)) continue;
            if (!over()) choose(player, spot);
            else if (chest.unlocked()) chest.click(player);
            return true;
        }
        return false;
    }

    private void choose(SkyBlockPlayer player, int spot) {
        String rewarded = riddle.rewardedName();
        if (!riddle.rewarded(spot)) {
            select(WRONG_CHEST);
            fail(player, PuzzleMessages.failed(player, "was fooled by <c>" + riddle.names().get(spot) + "<e>!"));
            return;
        }
        select(CatacombsPuzzleFactory.RIGHT_CHEST);
        context.host().broadcast(PuzzleMessages.solved(player, "wasn't fooled by <c>" + rewarded + "<e>!"));
        PuzzleChest chest = chests.get(spot);
        chest.unlock();
        chest.click(player);
    }

    @Override
    public synchronized void solve(SkyBlockPlayer staff) {
        if (over()) return;
        choose(staff, riddle.rewardedSpot());
    }

    @Override
    public List<String> info() {
        List<String> lines = new ArrayList<>();
        for (int spot = 0; spot < npcs.size(); spot++) {
            lines.add((riddle.rewarded(spot) ? "<a>" : "<7>") + riddle.names().get(spot) + ": <f>" + riddle.lines().get(spot));
        }
        return lines;
    }

    @Override
    public void dispose() {
        npcs.forEach(PuzzleNpc::remove);
        npcs.clear();
    }
}
