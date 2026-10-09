package net.swofty.type.kuudra.run;

import net.minestom.server.MinecraftServer;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.Entity;
import net.minestom.server.instance.Instance;
import net.swofty.commons.text.Text;
import net.swofty.type.generic.entity.npc.HypixelNPC;
import net.swofty.type.generic.entity.npc.configuration.HumanConfiguration;
import net.swofty.type.generic.event.custom.NPCInteractEvent;
import net.swofty.type.generic.user.HypixelPlayer;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

/** Elle in one run's Hollow, seen only by that run's party. Talking to her starts the fight. */
public final class KuudraElle extends HypixelNPC {
    private static final String TEXTURE = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMjMzM2FhMjQxNGJjZjFjMjkxZmRkZjZhOWIwZjgwNWY5OTY1NDZlYzQxNTBmZjNjZWYxMGJkNTI5Y2M5ODI2MSJ9fX0=";

    private final KuudraRun run;

    private KuudraElle(KuudraRun run, Appearance appearance) {
        super(appearance);
        this.run = run;
    }

    public static KuudraElle spawnFor(KuudraRun run) {
        KuudraElle elle = new KuudraElle(run, new Appearance(run));
        MinecraftServer.getSchedulerManager().scheduleNextTick(elle::register);
        return elle;
    }

    public void despawn() {
        MinecraftServer.getSchedulerManager().scheduleNextTick(() -> {
            unregister();
            for (PlayerNPCCache cache : HypixelNPC.getPerPlayerNPCs().values()) {
                Entity entity = cache.get(this);
                if (entity == null) continue;
                entity.remove();
                cache.remove(this);
            }
        });
    }

    @Override
    public void onClick(NPCInteractEvent event) {
        if (event.player() instanceof SkyBlockPlayer player) run.talkToElle(player);
    }

    @Override
    public String getName() {
        return "Elle";
    }

    private static final class Appearance extends HumanConfiguration {
        private final KuudraRun run;

        private Appearance(KuudraRun run) {
            this.run = run;
        }

        @Override
        public String texture(HypixelPlayer player) {
            return TEXTURE;
        }

        @Override
        public String signature(HypixelPlayer player) {
            return null;
        }

        @Override
        public String[] holograms(HypixelPlayer player) {
            return new String[]{"<c>Elle", "<e><l>CLICK"};
        }

        @Override
        public Text chatNameText(HypixelPlayer player) {
            return Text.read("<c>Elle");
        }

        @Override
        public Pos position(HypixelPlayer player) {
            return run.arena().elle();
        }

        @Override
        public boolean looking(HypixelPlayer player) {
            return true;
        }

        @Override
        public boolean visible(HypixelPlayer player) {
            return run.isMember(player.getUuid()) && player.getInstance() == run.getInstance();
        }

        @Override
        public Instance instance() {
            return run.getInstance();
        }
    }
}
