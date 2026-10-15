package dev.daweapon.type.crimsonisle.npcs.faction;

import net.minestom.server.coordinate.Pos;
import dev.daweapon.type.crimsonisle.faction.CrimsonNPC;
import dev.daweapon.type.crimsonisle.faction.CrimsonSkins;
import dev.daweapon.type.crimsonisle.faction.FactionShopNPC;
import dev.daweapon.type.crimsonisle.gui.faction.GUIMinionMerchant;
import dev.daweapon.type.skyblockgeneric.crimson.faction.CrimsonFaction;
import dev.daweapon.type.skyblockgeneric.crimson.questline.CrimsonScene;
import dev.daweapon.type.skyblockgeneric.crimson.questline.CrimsonSideQuests;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;

// The faction town shops: both Emissaries, An and Ulyn (members only), Ezekiel, Grog, Lys, and the Tier XII
// minion merchants Hilda and Marthos.
public final class NPCFactionShops {
    private NPCFactionShops() {
    }

    public static class MageEmissary extends FactionShopNPC {
        public MageEmissary() {
            super("<5>Mage Emissary", CrimsonSkins.MAGE_EMISSARY, new Pos(-131.5, 89, -722.5), CrimsonFaction.MAGE,
                    "crimson_mage_emissary", "You don't look like a <5>Mage <f>to me!",
                    "Welcome new Faction Member!", "I provide the faction with important items.",
                    "You may need more reputation to unlock all of my supplies!");
        }
    }

    public static class BarbarianEmissary extends FactionShopNPC {
        public BarbarianEmissary() {
            super("<d>Barbarian Emissary", CrimsonSkins.BARBARIAN_EMISSARY, new Pos(-580.5, 99, -710.5), CrimsonFaction.BARBARIAN,
                    "crimson_barbarian_emissary", "You don't look like a <d>Barbarian <f>to me!",
                    "Welcome to the <d>Barbarian Faction<f>, <player>!", "I provide the Piglins with important items.",
                    "You may need more <a>reputation<f> to unlock all of my supplies!");
        }
    }

    public static class An extends FactionShopNPC {
        public An() {
            super("<d>An", CrimsonSkins.AN, new Pos(-621.5, 108, -741.5), CrimsonFaction.BARBARIAN,
                    "crimson_an", "I don't sell my items to people who don't work with us.");
        }
    }

    public static class Ulyn extends FactionShopNPC {
        public Ulyn() {
            super("<5>Ulyn", CrimsonSkins.ULYN, new Pos(-125.5, 99, -783.5), CrimsonFaction.MAGE,
                    "crimson_ulyn", "I don't sell my items to players who don't work with us.");
        }
    }

    public static class Ezekiel extends FactionShopNPC {
        public Ezekiel() {
            super("<5>Ezekiel", CrimsonSkins.EZEKIEL, new Pos(-27.5, 107, -770.5), CrimsonFaction.MAGE,
                    "crimson_ezekiel", null, "Try some of <5>Scarleton's <f>finest spirits!");
        }
    }

    public static class Grog extends FactionShopNPC {
        public Grog() {
            super("<d>Grog", CrimsonSkins.GROG, new Pos(-663.5, 107, -736.5), CrimsonFaction.BARBARIAN,
                    "crimson_grog", null, "Me Grog, you thirsty. Buy drink!");
        }
    }

    public static class Lys extends FactionShopNPC {
        public Lys() {
            super("<5>Lys", CrimsonSkins.LYS, new Pos(-108.5, 99, -803.5), CrimsonFaction.MAGE,
                    "crimson_lys", null, "I make cute lamps!", "They emit light without any combustible.", "Revolutionary!");
        }
    }

    public static class Hilda extends CrimsonNPC {
        public Hilda() {
            super("<e>Hilda", CrimsonSkins.HILDA, new Pos(-46.5, 107, -779.5));
        }

        @Override
        protected void interact(SkyBlockPlayer player) {
            MinionMerchant.open(this, player, "hilda", CrimsonFaction.MAGE, "Morning, adventurer!",
                    "Wait, is it? I'm not sure; it's always so dark here...",
                    "Oh well, anyway, I can provide all manners of fun minions to you!");
        }
    }

    public static class Marthos extends CrimsonNPC {
        public Marthos() {
            super("<d>Marthos", CrimsonSkins.MARTHOS, new Pos(-644.5, 101, -825.5));
        }

        @Override
        protected void interact(SkyBlockPlayer player) {
            MinionMerchant.open(this, player, "marthos", CrimsonFaction.BARBARIAN, "Evening, adventurer!",
                    "Wait, is it? I'm not sure; it's always so bright in here...",
                    "Oh well, anyway, I can sell all kinds of functional minions to you!");
        }
    }

    private static final class MinionMerchant {
        private static void open(CrimsonNPC npc, SkyBlockPlayer player, String id,
                                 CrimsonFaction faction, String... introduction) {
            String title = npc.getName();
            if (CrimsonSideQuests.firstMeeting(player, id)) {
                CrimsonScene.create().say(npc.speaker(), introduction)
                        .play(player).thenRun(() -> player.openView(new GUIMinionMerchant(title, faction)));
                return;
            }
            player.openView(new GUIMinionMerchant(title, faction));
        }
    }
}
