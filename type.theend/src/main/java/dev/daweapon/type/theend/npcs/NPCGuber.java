package dev.daweapon.type.theend.npcs;

import net.minestom.server.MinecraftServer;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.timer.TaskSchedule;
import dev.daweapon.type.generic.event.custom.NPCInteractEvent;
import dev.daweapon.type.generic.user.HypixelPlayer;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;
import dev.daweapon.type.theend.TypeTheEndLoader;
import dev.daweapon.type.theend.race.EndRaceProgress;
import dev.daweapon.type.theend.race.EndRaceTier;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class NPCGuber extends AbstractEndNPC {
    private static final int LINE_GAP_TICKS = 30;
    private static final List<String> DONE_IDLE = List.of(
            "It's a great day to go for a run!",
            "You're always welcome to try the race again. Just run over the pressure plate to begin!");
    private static final String DONE_RACING =
            "You should know by now talking to me during the race is only going to hurt your time! Yeesh!";

    private static NPCGuber instance;

    public NPCGuber() {
        super(null, "Guber", "<d>", "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHBzOi8vdGV4dHVyZXMubWluZWNyYWZ0Lm5ldC90ZXh0dXJlLzhmY2M1M2E4NjBjNGQ2MjFiYjc1ZmM4NmI5N2IxMmNiM2Q2ZDk3N2Y4ZWI5ODNlNzQ1OGY1ZTI2MWIzZDEyNzkifX19",
                new Pos(-494.5, 121, -241.5, 0, 0));
        instance = this;
    }

    public static void say(SkyBlockPlayer player, String line) {
        if (instance != null) instance.sendNPCMessage(player, line);
    }

    public static void sayInTurn(SkyBlockPlayer player, List<String> lines) {
        for (int i = 0; i < lines.size(); i++) {
            String line = lines.get(i);
            if (i == 0) {
                say(player, line);
                continue;
            }
            MinecraftServer.getSchedulerManager().buildTask(() -> {
                if (player.isOnline()) say(player, line);
            }).delay(TaskSchedule.tick(i * LINE_GAP_TICKS)).schedule();
        }
    }

    @Override
    public void onClick(NPCInteractEvent event) {
        SkyBlockPlayer player = skyBlock(event.player());
        if (isInDialogue(player)) return;
        if (!EndRaceProgress.introduced(player)) {
            setDialogue(player, "intro").thenRun(() -> EndRaceProgress.introduce(player));
            return;
        }
        EndRaceTier next = EndRaceProgress.nextTier(player);
        if (TypeTheEndLoader.endRace().getPerPlayerStartTime().containsKey(player.getUuid())) {
            List<String> racing = next == null ? List.of(DONE_RACING) : next.racingLines();
            sendNPCMessage(player, racing.get(ThreadLocalRandom.current().nextInt(racing.size())));
            return;
        }
        setDialogue(player, "idle");
    }

    @Override
    protected DialogueSet[] dialogues(HypixelPlayer player) {
        EndRaceTier next = player == null ? EndRaceTier.THREE_MINUTES : EndRaceProgress.nextTier(skyBlock(player));
        List<String> idle = next == null ? DONE_IDLE : next.idleLines();
        return new DialogueSet[]{
                DialogueSet.builder().key("intro").lines(
                        "The End is a large island... my favorite hobby is racing across it!",
                        "My best time is pretty fast, you probably will never beat me. But you can try!",
                        "To complete the race, you'll need to reach the <e>opposite side of the island <f>and come all the way back.",
                        "Start the race by walking over the pressure plate. If you finish in under 3 minutes, I'll reward you for it! Good luck!"
                ).build(),
                DialogueSet.builder().key("idle").lines(idle.toArray(String[]::new)).build()
        };
    }
}
