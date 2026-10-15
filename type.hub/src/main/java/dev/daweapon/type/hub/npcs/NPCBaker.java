package dev.daweapon.type.hub.npcs;

import net.minestom.server.coordinate.Pos;
import dev.daweapon.commons.skyblock.item.ItemType;
import dev.daweapon.type.generic.data.datapoints.DatapointInteger;
import dev.daweapon.type.generic.entity.npc.HypixelNPC;
import dev.daweapon.type.generic.entity.npc.configuration.HumanConfiguration;
import dev.daweapon.type.generic.event.custom.NPCInteractEvent;
import dev.daweapon.type.generic.user.HypixelPlayer;
import dev.daweapon.type.hub.gui.GUIBakerShop;
import dev.daweapon.type.skyblockgeneric.calendar.CalendarEvent;
import dev.daweapon.type.skyblockgeneric.calendar.SkyBlockCalendar;
import dev.daweapon.type.skyblockgeneric.data.SkyBlockDataHandler;
import dev.daweapon.type.skyblockgeneric.gui.inventories.ClaimRewardView;
import dev.daweapon.type.skyblockgeneric.item.SkyBlockItem;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.List;

// Real bug fix (found live-testing Roadmap #19/20/21/24): this used to gate visibility by
// toggling position() between y=0 (hidden underground) and y=70, driven by getCurrentEvents().
// HypixelNPC only re-teleports an already-spawned NPC when dynamicPosition() is true (which
// HumanConfiguration doesn't set by default), and holograms are computed once at spawn time and
// never regenerated - so a player already connected when New Year activates stayed stuck looking
// at Baker's spawn-time state (invisible, underground, blank hologram) forever. Switched to
// visible(), which the framework properly despawns/respawns off - a fresh entity gets correct
// position and holograms every time visibility flips. Same fix applied to NPCOringo (Roadmap #21)
// and NPCFearMongerer (Roadmap #19), which copied this exact broken pattern.
public class NPCBaker extends HypixelNPC {

    public NPCBaker() {
        super(new HumanConfiguration() {
            @Override
            public boolean visible(HypixelPlayer player) {
                return SkyBlockCalendar.getCurrentEvents().contains(CalendarEvent.NEW_YEAR);
            }

            @Override
            public String[] holograms(HypixelPlayer player) {
                return new String[]{"<f>Baker", "<e><l>CLICK"};
            }

            @Override
            public String signature(HypixelPlayer player) {
                return "lu9SuKpv/U8XqaZTkleKzPDg8S1pcqA7LSWiWimR9x0BnkpK5CkyLwkWA1AMKCibQZSMPjoFFySNMVRcIhylv3yN0V6/Y6moJi1/SmRIeJJL/FovCUykzTSvbWsqJXfRyoi+5mUt6REj6bvJQruNtCedIHQD5a0Mrw3d8LbvZ0OlGPUbaAv1O7dW1O2uxmxCDSWMOL8PN+6fb/zYgA/XeJvSj97LafK4YAeb1YV362CeMkhmMP0uE5wj11+BnexEN+WaBzbRIUlBuSMB+Pw+7RoS4Nk7kxxKSNAR/pzlSqFHLkTlL88ljrLeyEooccpETSuqLh55/wsWSdesEDpSNjmfRYVX9EXOk783VRz3Btb+MItjiqmos5Mgmjelnx34utIPkAFbLyn/AUvWaNImxhWw/iDFYod+C/QNbUqR/H9ahIHzZXun4+6tKhVBgaCfLqaqF+V9Js8miapUpW16EEnElTNJ843+/HFgqex18q2vCTUX0tixtzHrFmwhhbBnT02DSvbvIxm9ucyNMwTpYhJ33I433pB67i1iQxiNBxaTTVSn2bGs4AKLgOjkTg3TsixEix02fCOzFl8bau/JlZMDmk7/2SAI74VRnreBVTEHjIAb7SRRXNy+zOxQJLzyMB+TwpGBBIUbNpCgjKu0aqu+Ld/FOO37dvBke8bv7Uw=";
            }

            @Override
            public String texture(HypixelPlayer player) {
                return "ewogICJ0aW1lc3RhbXAiIDogMTU5OTQ1NzU3MjI1NCwKICAicHJvZmlsZUlkIiA6ICI2MTI4MTA4MjU5M2Q0OGQ2OWIzMmI3YjlkMzIxMGUxMiIsCiAgInByb2ZpbGVOYW1lIiA6ICJuaWNyb25pYzcyMTk2IiwKICAic2lnbmF0dXJlUmVxdWlyZWQiIDogdHJ1ZSwKICAidGV4dHVyZXMiIDogewogICAgIlNLSU4iIDogewogICAgICAidXJsIiA6ICJodHRwOi8vdGV4dHVyZXMubWluZWNyYWZ0Lm5ldC90ZXh0dXJlL2U5ZTIzYmU3ZjA0NTU2ZmEzMzM1MWE0Yzc3MWEzZjA1ZjRhNmQyN2RlNDEzYTM2ZDAyMzBjNjFmNzE2OTg3OTkiLAogICAgICAibWV0YWRhdGEiIDogewogICAgICAgICJtb2RlbCIgOiAic2xpbSIKICAgICAgfQogICAgfQogIH0KfQ==";
            }

            @Override
            public Pos position(HypixelPlayer player) {
                // Village pond - user-confirmed coordinates. VillagerJerry (a decorative,
                // unrelated NPC that previously stood in this exact spot) was removed at the
                // user's request so the pond is free for seasonal event booths, matching real
                // Hypixel.
                return new Pos(-29.5, 69, 8.5, -90, 0);
            }

            @Override
            public boolean looking(HypixelPlayer player) {
                return true;
            }
        });
    }

    @Override
    public void onClick(NPCInteractEvent event) {
        List<CalendarEvent> events = SkyBlockCalendar.getCurrentEvents();
        if (!events.contains(CalendarEvent.NEW_YEAR)) return;

        SkyBlockPlayer player = (SkyBlockPlayer) event.player();
        if (isInDialogue(player)) return;
        SkyBlockDataHandler dataHandler = player.getSkyblockDataHandler();
        if (dataHandler.get(SkyBlockDataHandler.Data.LATEST_NEW_YEAR_CAKE_YEAR, DatapointInteger.class).getValue() >= SkyBlockCalendar.getYear()) {
            player.openView(new GUIBakerShop());
            return;
        }
        setDialogue(player, "initial-hello").thenRun(() -> {
            SkyBlockItem item = new SkyBlockItem(ItemType.NEW_YEAR_CAKE);
            item.getAttributeHandler().setNewYearCakeYear(SkyBlockCalendar.getYear());
            player.openView(new ClaimRewardView(), new ClaimRewardView.State(
                    item, () -> SkyBlockCalendar.getCurrentEvents().contains(CalendarEvent.NEW_YEAR) && (dataHandler.get(SkyBlockDataHandler.Data.LATEST_NEW_YEAR_CAKE_YEAR, DatapointInteger.class).getValue() < SkyBlockCalendar.getYear()), () -> {
                dataHandler.get(SkyBlockDataHandler.Data.LATEST_NEW_YEAR_CAKE_YEAR, DatapointInteger.class).setValue(SkyBlockCalendar.getYear());
                player.getAchievementHandler().completeAchievement("skyblock.happy_new_year");
            }
            ));
        });
    }

    @Override
    protected DialogueSet[] dialogues(HypixelPlayer player) {
        return new DialogueSet[] {
            DialogueSet.ofTranslation("initial-hello", "npcs_hub.baker.dialogue.initial_hello")
        };
    }
}
