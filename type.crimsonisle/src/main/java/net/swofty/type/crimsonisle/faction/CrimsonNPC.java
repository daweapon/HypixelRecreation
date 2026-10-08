package net.swofty.type.crimsonisle.faction;

import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.EntityPose;
import net.minestom.server.entity.EntityType;
import net.swofty.commons.text.Text;
import net.swofty.type.generic.entity.npc.HypixelNPC;
import net.swofty.type.generic.entity.npc.configuration.AnimalConfiguration;
import net.swofty.type.generic.entity.npc.configuration.HumanConfiguration;
import net.swofty.type.generic.event.custom.NPCInteractEvent;
import net.swofty.type.generic.user.HypixelPlayer;
import net.swofty.type.skyblockgeneric.crimson.questline.CrimsonScene;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Base for Crimson Isle NPCs. Position, visibility and holograms can depend on the player's quest
 * progress (each player sees their own copy), and dialogue is played as a {@link CrimsonScene}.
 */
public abstract class CrimsonNPC extends HypixelNPC {
    private final String name;
    private final Pos home;

    protected CrimsonNPC(String name, String texture, Pos home) {
        super(new Human(texture));
        this.name = name;
        this.home = home;
        ((Bound) getParameters()).bind(this);
    }

    protected CrimsonNPC(String name, EntityType entityType, Pos home) {
        super(new Animal(entityType));
        this.name = name;
        this.home = home;
        ((Bound) getParameters()).bind(this);
    }

    protected abstract void interact(SkyBlockPlayer player);

    @Override
    public final void onClick(NPCInteractEvent event) {
        SkyBlockPlayer player = (SkyBlockPlayer) event.player();
        if (isInDialogue(player) || CrimsonScene.isPlaying(player)) return;
        interact(player);
    }

    public String speaker() {
        return name;
    }

    public Pos home() {
        return home;
    }

    protected Pos position(SkyBlockPlayer player) {
        return home;
    }

    protected boolean visible(SkyBlockPlayer player) {
        return true;
    }

    /** Whether {@link #position} can change while the player is watching (quest NPCs that travel). */
    protected boolean moves() {
        return false;
    }

    protected String[] holograms(SkyBlockPlayer player) {
        return new String[]{name, "<e><l>CLICK"};
    }

    protected EntityPose pose(SkyBlockPlayer player) {
        return EntityPose.STANDING;
    }

    protected CompletableFuture<Void> say(SkyBlockPlayer player, String... lines) {
        return CrimsonScene.create().say(name, lines).play(player);
    }

    protected CompletableFuture<Void> sayRandom(SkyBlockPlayer player, String... options) {
        return say(player, options[ThreadLocalRandom.current().nextInt(options.length)]);
    }

    protected CrimsonScene scene() {
        return CrimsonScene.create();
    }

    @Override
    public String getName() {
        return name.replaceAll("<[^>]*>", "");
    }

    private interface Bound {
        void bind(CrimsonNPC owner);
    }

    private static final class Human extends HumanConfiguration implements Bound {
        private final String texture;
        private CrimsonNPC owner;

        private Human(String texture) {
            this.texture = texture;
        }

        @Override
        public void bind(CrimsonNPC owner) {
            this.owner = owner;
        }

        @Override
        public String texture(HypixelPlayer player) {
            return texture;
        }

        @Override
        public String signature(HypixelPlayer player) {
            return null;
        }

        @Override
        public String[] holograms(HypixelPlayer player) {
            return owner.holograms((SkyBlockPlayer) player);
        }

        @Override
        public Text chatNameText(HypixelPlayer player) {
            return Text.read(owner.name);
        }

        @Override
        public Pos position(HypixelPlayer player) {
            return owner.position((SkyBlockPlayer) player);
        }

        @Override
        public boolean dynamicPosition() {
            return owner.moves();
        }

        @Override
        public boolean visible(HypixelPlayer player) {
            return owner.visible((SkyBlockPlayer) player);
        }

        @Override
        public boolean looking(HypixelPlayer player) {
            return true;
        }

        @Override
        public EntityPose pose(HypixelPlayer player) {
            return owner.pose((SkyBlockPlayer) player);
        }
    }

    private static final class Animal extends AnimalConfiguration implements Bound {
        private final EntityType entityType;
        private CrimsonNPC owner;

        private Animal(EntityType entityType) {
            this.entityType = entityType;
        }

        @Override
        public void bind(CrimsonNPC owner) {
            this.owner = owner;
        }

        @Override
        public EntityType entityType() {
            return entityType;
        }

        @Override
        public String[] holograms(HypixelPlayer player) {
            return owner.holograms((SkyBlockPlayer) player);
        }

        @Override
        public Text chatNameText(HypixelPlayer player) {
            return Text.read(owner.name);
        }

        @Override
        public Pos position(HypixelPlayer player) {
            return owner.position((SkyBlockPlayer) player);
        }

        @Override
        public boolean dynamicPosition() {
            return owner.moves();
        }

        @Override
        public boolean visible(HypixelPlayer player) {
            return owner.visible((SkyBlockPlayer) player);
        }

        @Override
        public boolean looking(HypixelPlayer player) {
            return true;
        }

        @Override
        public EntityPose pose(HypixelPlayer player) {
            return owner.pose((SkyBlockPlayer) player);
        }
    }
}
