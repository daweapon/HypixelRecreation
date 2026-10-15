package dev.daweapon.type.hub.npcs.carnival;

import net.kyori.adventure.text.format.NamedTextColor;
import net.minestom.server.coordinate.Pos;
import dev.daweapon.type.generic.entity.npc.HypixelNPC;
import dev.daweapon.type.generic.entity.npc.NPCOption;
import dev.daweapon.type.generic.event.custom.NPCInteractEvent;
import dev.daweapon.type.hub.carnival.CarnivalGameBase;
import dev.daweapon.type.hub.carnival.CarnivalSkin;
import dev.daweapon.type.skyblockgeneric.carnival.Carnival;
import dev.daweapon.type.skyblockgeneric.carnival.CarnivalNpc;
import dev.daweapon.type.skyblockgeneric.carnival.CarnivalProfile;
import dev.daweapon.type.skyblockgeneric.carnival.CarnivalSessions;
import dev.daweapon.type.skyblockgeneric.carnival.Carnivals;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.List;

/**
 * A Carnival game host. Subclasses supply the wiki dialogue under the keys {@code introduction},
 * {@code welcome}, {@code rules}, {@code start}, {@code no-tickets}, {@code decline} and
 * {@code closed}, plus the wording of each reply option.
 */
public abstract class CarnivalGameNPC extends HypixelNPC {
    private final CarnivalNpc npc;
    private final Replies replies;

    protected record Replies(String introAccept, String introDecline, String welcomeAccept, String welcomeRules,
                             String welcomeDecline, String rulesAccept, String rulesDecline) {}

    protected CarnivalGameNPC(String name, CarnivalSkin skin, Pos position, CarnivalNpc npc, Replies replies) {
        super(new CarnivalNPCConfiguration(name, skin, position));
        this.npc = npc;
        this.replies = replies;
    }

    protected abstract CarnivalGameBase newGame(SkyBlockPlayer player);

    @Override
    public void onClick(NPCInteractEvent event) {
        SkyBlockPlayer player = (SkyBlockPlayer) event.player();
        if (isInDialogue(player) || !Carnival.isOpen()) return;
        if (CarnivalSessions.isPlaying(player)) {
            player.sendMessage("<c>You're already playing a Carnival game!");
            return;
        }
        CarnivalProfile profile = Carnivals.profile(player);
        if (!profile.hasMet(npc)) {
            setDialogue(player, "introduction").thenRun(() -> {
                CarnivalProfile met = Carnivals.profile(player);
                met.meet(npc);
                Carnivals.save(player, met);
                choose(player, replies.introAccept(), null, replies.introDecline());
            });
            return;
        }
        setDialogue(player, "welcome").thenRun(() ->
                choose(player, replies.welcomeAccept(), replies.welcomeRules(), replies.welcomeDecline()));
    }

    private void choose(SkyBlockPlayer player, String accept, String rules, String decline) {
        List<NPCOption.Option> options = new java.util.ArrayList<>();
        options.add(option("play", NamedTextColor.GREEN, accept, () -> tryStart(player)));
        if (rules != null) {
            options.add(option("rules", NamedTextColor.AQUA, rules, () -> setDialogue(player, "rules").thenRun(() ->
                    choose(player, replies.rulesAccept(), null, replies.rulesDecline()))));
        }
        options.add(option("decline", NamedTextColor.RED, decline, () -> setDialogue(player, "decline")));
        NPCOption.sendOption(player, npc.name().toLowerCase(), true, options);
    }

    private static NPCOption.Option option(String key, NamedTextColor color, String text, Runnable action) {
        return NPCOption.Option.builder().key(key).color(color).bold(false).name(text).action(_ -> action.run()).build();
    }

    private void tryStart(SkyBlockPlayer player) {
        if (!Carnival.acceptsNewGames()) {
            setDialogue(player, "closed");
            return;
        }
        if (!Carnivals.hasTicketsToPlay(player)) {
            setDialogue(player, "no-tickets");
            return;
        }
        if (CarnivalSessions.freeHotbarSlot(player) < 0) {
            player.sendMessage("<c>You need an empty hotbar slot to play!");
            return;
        }
        setDialogue(player, "start").thenRun(() -> {
            int slot = CarnivalSessions.freeHotbarSlot(player);
            if (slot < 0 || !Carnivals.hasTicketsToPlay(player) || CarnivalSessions.isPlaying(player)) return;
            newGame(player).start(slot);
        });
    }
}
