package dev.daweapon.type.skyblockgeneric.abiphone.ninef;

import dev.daweapon.commons.skyblock.statistics.ItemStatistic;
import dev.daweapon.commons.skyblock.statistics.ItemStatistics;
import dev.daweapon.type.generic.data.datapoints.DatapointString;
import dev.daweapon.type.skyblockgeneric.abiphone.AbiphoneContactListeners;
import dev.daweapon.type.skyblockgeneric.abiphone.AbiphoneIncomingCalls;
import dev.daweapon.type.skyblockgeneric.abiphone.AbiphoneNPC;
import dev.daweapon.type.skyblockgeneric.abiphone.AbiphoneRegistry;
import dev.daweapon.type.skyblockgeneric.data.SkyBlockDataHandler;
import dev.daweapon.type.skyblockgeneric.gui.inventories.forge.GUIForge;
import dev.daweapon.type.skyblockgeneric.item.SkyBlockItem;
import dev.daweapon.type.skyblockgeneric.item.components.AbiphoneComponent;
import dev.daweapon.type.skyblockgeneric.item.set.ArmorSetRegistry;
import dev.daweapon.type.skyblockgeneric.levels.SkyBlockXPSource;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public final class NineFNetwork {
    public static final String TIA = "tia_the_fairy";
    public static final int SKYBLOCK_XP_PER_RELAY = 5;
    private static final List<String> FRED_STATIC = List.of(
            "I can't really hear you right now, the signal is really bad!",
            "I'm not interested in extending my anvil warranty.",
            "You're breaking up, I CAN'T HEAR YOU!",
            "What are you saying, the forge is in the omelette???",
            "All I hear is crackling, the signal is really bad down here.");

    public record State(int repaired, int revealed, boolean chip) {
        public static final State EMPTY = new State(0, 0, false);

        public static State parse(String raw) {
            if (raw == null || raw.isBlank()) return EMPTY;
            String[] parts = raw.split(":");
            if (parts.length < 3) return EMPTY;
            try {
                return new State(Integer.parseInt(parts[0]), Integer.parseInt(parts[1]), Boolean.parseBoolean(parts[2]));
            } catch (NumberFormatException exception) {
                return EMPTY;
            }
        }

        public String serialize() {
            return repaired + ":" + revealed + ":" + chip;
        }

        public boolean complete() {
            return repaired >= NineFRelay.values().length;
        }
    }

    private NineFNetwork() {
    }

    public static void registerListeners() {
        AbiphoneContactListeners.onAdded(TIA, player -> update(player, state -> new State(state.repaired(), state.revealed(), true)));
        AbiphoneContactListeners.onRemoved(TIA, player -> update(player, state -> new State(state.repaired(), state.revealed(), false)));
    }

    public static State state(SkyBlockPlayer player) {
        if (player == null || player.getSkyblockDataHandler() == null) return State.EMPTY;
        return State.parse(datapoint(player).getValue());
    }

    private static DatapointString datapoint(SkyBlockPlayer player) {
        return player.getSkyblockDataHandler().get(SkyBlockDataHandler.Data.NINE_F_NETWORK, DatapointString.class);
    }

    private static void update(SkyBlockPlayer player, java.util.function.UnaryOperator<State> change) {
        State next = change.apply(state(player));
        datapoint(player).setValue(next.serialize());
        syncSkyBlockXP(player, next);
    }

    public static void syncSkyBlockXP(SkyBlockPlayer player, State state) {
        player.getSkyBlockExperience().setSourceXP(SkyBlockXPSource.UPGRADED_RELAYS, state.repaired() * SKYBLOCK_XP_PER_RELAY);
    }

    public static ItemStatistics statistics(SkyBlockPlayer player) {
        State state = state(player);
        if (!state.chip() || state.repaired() <= 0) return ItemStatistics.empty();
        return ItemStatistics.builder().withBase(ItemStatistic.HEALTH, (double) NineFRelay.healthFor(state.repaired())).build();
    }

    public static int bonusContacts(SkyBlockPlayer player) {
        State state = state(player);
        return state.chip() ? NineFRelay.bonusContactsFor(state.repaired()) : 0;
    }

    public static boolean wearingFairyArmor(SkyBlockPlayer player) {
        return player.getArmorSet() == ArmorSetRegistry.FAIRY_OUTFIT;
    }

    public static NineFRelay nextRelay(State state) {
        return NineFRelay.byNumber(state.repaired() + 1);
    }

    public static boolean canSee(SkyBlockPlayer player, NineFRelay relay) {
        State state = state(player);
        return state.chip() && state.repaired() == relay.number() - 1 && state.revealed() >= relay.number() && wearingFairyArmor(player);
    }

    public static int contactCount(SkyBlockPlayer player) {
        int most = 0;
        for (SkyBlockItem item : player.getAllInventoryItems()) {
            if (item.hasComponent(AbiphoneComponent.class)) most = Math.max(most, item.getAttributeHandler().getAbiphoneNPCs().size());
        }
        return most;
    }

    public static void callTia(SkyBlockPlayer player) {
        State state = state(player);
        if (!state.chip()) update(player, current -> new State(current.repaired(), current.revealed(), true));
        if (state.complete()) {
            AbiphoneIncomingCalls.speak(player, TIA, "Why is the tooth fairy so smart?", "Because she has wisdom teeth!");
            return;
        }
        NineFRelay next = nextRelay(state);
        if (contactCount(player) < next.contactsNeeded()) {
            AbiphoneIncomingCalls.speak(player, TIA, "You need more contacts before you can deal with the next relay!",
                    "Don't ask me why...", "I don't make the rules!");
            return;
        }
        if (state.revealed() < next.number()) update(player, current -> new State(current.repaired(), next.number(), true));
        AbiphoneIncomingCalls.speak(player, TIA, next.tip().toArray(String[]::new));
    }

    public static void complete(SkyBlockPlayer player, NineFRelay relay) {
        State state = state(player);
        if (state.repaired() >= relay.number()) return;
        update(player, current -> new State(relay.number(), Math.max(current.revealed(), relay.number()), current.chip()));
        player.sendMessage("<5><l>9f™ RELAY REPAIRED! <r><7>You upgraded <5>9f™ Network Relay #{}<7>!", relay.number());
        player.sendMessage("<7>Your <5>9f™ Operator Chip <7>now grants <c>+{} ❤ Health<7>{}<7>.", NineFRelay.healthFor(relay.number()),
                NineFRelay.bonusContactsFor(relay.number()) > 0 ? " and <d>+" + NineFRelay.bonusContactsFor(relay.number()) + " bonus contacts" : "");
        if (relay.number() >= NineFRelay.values().length) {
            player.sendMessage("<a>Every <5>9f™ Network Relay <a>is repaired! Fred's signal at the Forge is crystal clear now.");
        } else {
            player.sendMessage("<7>Call <d>Tia the Fairy <7>for the location of the next relay.");
        }
    }

    public static void repairNext(SkyBlockPlayer player) {
        NineFRelay next = nextRelay(state(player));
        if (next == null) {
            player.sendMessage("<c>Every 9f™ Network Relay is already repaired.");
            return;
        }
        complete(player, next);
    }

    public static void maxOut(SkyBlockPlayer player) {
        int all = NineFRelay.values().length;
        update(player, _ -> new State(all, all, true));
    }

    public static void callFred(SkyBlockPlayer player) {
        if (state(player).complete()) {
            player.openView(new GUIForge());
            return;
        }
        int roll = ThreadLocalRandom.current().nextInt(FRED_STATIC.size() + 1);
        if (roll == FRED_STATIC.size()) {
            player.openView(new GUIForge());
            return;
        }
        AbiphoneNPC fred = AbiphoneRegistry.getFromId("fred");
        if (fred != null) fred.sendNPCMessage(player, garble(FRED_STATIC.get(roll)));
    }

    static String garble(String line) {
        StringBuilder out = new StringBuilder();
        for (char character : line.toCharArray()) {
            if (character != ' ' && ThreadLocalRandom.current().nextInt(6) == 0) out.append("<k>").append(character).append("</k>");
            else out.append(character);
        }
        return out.toString();
    }
}
