package dev.daweapon.loader;

import dev.daweapon.type.backwaterbayou.gui.GUIFishingRodParts;
import dev.daweapon.type.backwaterbayou.gui.GUIJunkerJoel;
import dev.daweapon.type.dwarvenmines.gui.GUIGemstoneGrinder;
import dev.daweapon.type.dwarvenmines.gui.GUIShopBrynmor;
import dev.daweapon.type.dwarvenmines.gui.GUIShopBulvar;
import dev.daweapon.type.hub.gui.GUIGeorge;
import dev.daweapon.type.hub.gui.GUIJax;
import dev.daweapon.type.hub.gui.GUIRunicPedestal;
import dev.daweapon.type.hub.gui.GUIShopZog;
import dev.daweapon.type.hub.gui.GUIFearMongererShop;
import dev.daweapon.type.hub.gui.GUIShopAlchemist;
import dev.daweapon.type.hub.gui.GUIShopBartender;
import dev.daweapon.type.hub.gui.GUIShopLumberMerchant;
import dev.daweapon.type.hub.gui.GUIShopMadRedstoneEngineer;
import dev.daweapon.type.hub.gui.GUIShopWoolWeaverVibrant;
import dev.daweapon.type.hub.gui.GUITravelingZoo;
import dev.daweapon.type.hub.gui.catacombs.GUIDungeonBlacksmith;
import dev.daweapon.type.hub.gui.fann.GUIFannPetTraining;
import dev.daweapon.type.hub.gui.farmingcontest.GUIAnita;
import dev.daweapon.type.hub.gui.farmingcontest.GUIJacob;
import dev.daweapon.type.hub.gui.kat.GUIKat;
import dev.daweapon.type.hub.gui.maxwell.GUIMaxwell;
import dev.daweapon.type.hub.gui.taylor.GUITaylor;
import dev.daweapon.type.hub.gui.vincent.GUIVincent;
import dev.daweapon.type.hub.gui.vincent.VincentDyes;
import dev.daweapon.type.jerrysworkshop.gui.GUIEinaryEmporium;
import dev.daweapon.type.skyblockgeneric.abiphone.AbiphoneCallActions;
import dev.daweapon.type.skyblockgeneric.abiphone.AbiphoneIncomingHooks;
import dev.daweapon.type.skyblockgeneric.abiphone.ContactCalls;
import dev.daweapon.type.skyblockgeneric.abiphone.ninef.NineFNetwork;
import dev.daweapon.type.skyblockgeneric.calendar.SkyBlockCalendar;
import dev.daweapon.type.skyblockgeneric.commission.GUICommisions;
import dev.daweapon.type.skyblockgeneric.dungeons.gui.GUICatacombsGate;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;
import dev.daweapon.type.skyblockgeneric.gui.inventories.GUIReforge;
import dev.daweapon.type.skyblockgeneric.gui.inventories.drill.GUIDrillAnvil;
import dev.daweapon.type.skyblockgeneric.essence.EssenceType;
import dev.daweapon.type.skyblockgeneric.gui.inventories.essence.GUIEssenceShop;
import dev.daweapon.type.skyblockgeneric.gui.inventories.essence.GUIGemma;
import dev.daweapon.type.skyblockgeneric.gui.inventories.garden.GUIFeast;
import dev.daweapon.type.skyblockgeneric.gui.inventories.slayer.GUISlayer;
import dev.daweapon.type.skyblockgeneric.shop.catalogue.CatalogueShopView;
import dev.daweapon.type.theend.gui.GUITyzzo;

final class AbiphoneCallBindings {
    private AbiphoneCallBindings() {
    }

    static void register() {
        AbiphoneCallActions.register("alchemist", player -> player.openView(new GUIShopAlchemist()));
        AbiphoneCallActions.register("anita", player -> player.openView(new GUIAnita()));
        AbiphoneCallActions.register("bartender", player -> player.openView(new GUIShopBartender()));
        AbiphoneCallActions.register("blacksmith", player -> new GUIReforge().open(player));
        AbiphoneCallActions.register("brynmor", player -> player.openView(new GUIShopBrynmor()));
        AbiphoneCallActions.register("bulvar", player -> player.openView(new GUIShopBulvar()));
        AbiphoneCallActions.register("dalir", player -> dev.daweapon.type.skyblockgeneric.miningevent.CultOfTheFallenStar.call((dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer) player));
        AbiphoneCallActions.register("dusk", player -> new GUIRunicPedestal().open(player));
        AbiphoneCallActions.register("einary", player -> player.openView(new GUIEinaryEmporium()));
        AbiphoneCallActions.register("fann", player -> player.openView(new GUIFannPetTraining()));
        AbiphoneCallActions.register("fear_mongerer", player -> player.openView(new GUIFearMongererShop()));
        AbiphoneCallActions.register("feast_baker_scott", player -> player.openView(new CatalogueShopView("grand_bakery")));
        AbiphoneCallActions.register("feast_chef_ted", player -> player.openView(new GUIFeast()));
        AbiphoneCallActions.register("fred", player -> NineFNetwork.callFred((SkyBlockPlayer) player));
        AbiphoneCallActions.register("gemma", player -> player.openView(new GUIGemma()));
        AbiphoneCallActions.register("geo", player -> new GUIGemstoneGrinder().open(player));
        AbiphoneCallActions.register("george", player -> new GUIGeorge().open(player));
        AbiphoneCallActions.register("jacob", player -> player.openView(new GUIJacob()));
        AbiphoneCallActions.register("jax", player -> player.openView(new GUIJax()));
        AbiphoneCallActions.register("jotraeline_greatforge", player -> player.openView(new GUIDrillAnvil()));
        AbiphoneCallActions.register("junker_joel", player -> player.openView(new GUIJunkerJoel()));
        AbiphoneCallActions.register("kat", player -> new GUIKat().open(player));
        AbiphoneCallActions.register("lumber_merchant", player -> player.openView(new GUIShopLumberMerchant()));
        AbiphoneCallActions.register("mad_redstone_engineer", player -> player.openView(new GUIShopMadRedstoneEngineer()));
        AbiphoneCallActions.register("maddox_the_slayer", player -> player.openView(new GUISlayer()));
        AbiphoneCallActions.register("malik", player -> player.openView(new GUIDungeonBlacksmith()));
        AbiphoneCallActions.register("marigold", player -> player.openView(new GUIEssenceShop(EssenceType.GOLD)));
        AbiphoneCallActions.register("maxwell", player -> player.openView(new GUIMaxwell()));
        AbiphoneCallActions.register("mort", player -> player.openView(new GUICatacombsGate(_ -> {})));
        AbiphoneCallActions.register("ophelia", player -> player.openView(new CatalogueShopView("ophelia")));
        AbiphoneCallActions.register("oringo", player -> player.openView(new GUITravelingZoo()));
        AbiphoneCallActions.register("plumber_joe", player -> player.openView(new CatalogueShopView("plumber_joe")));
        AbiphoneCallActions.register("professor_wynd", ContactCalls::professorWynd);
        AbiphoneCallActions.register("queen_mismyla", player -> new GUICommisions().open(player));
        AbiphoneCallActions.register("roddy", player -> player.openView(new GUIFishingRodParts()));
        AbiphoneCallActions.register("rosemary", player -> player.openView(new GUIEssenceShop(EssenceType.FOREST)));
        AbiphoneCallActions.register("shaggy", player -> player.openView(new dev.daweapon.type.skyblockgeneric.gui.inventories.spidersden.GUIShaggy()));
        AbiphoneCallActions.register("shifty", player -> player.openView(new CatalogueShopView("shifty")));
        AbiphoneCallActions.register("sirius", ContactCalls::sirius);
        AbiphoneCallActions.register("spider_tamer", dev.daweapon.type.skyblockgeneric.spidersden.SpiderTamerCall::call);
        AbiphoneCallActions.register("st_jerry", ContactCalls::stJerry);
        AbiphoneCallActions.register("taylor", player -> player.openView(new GUITaylor()));
        AbiphoneCallActions.register("terry", player -> player.openView(new CatalogueShopView("terry")));
        AbiphoneCallActions.register("tia_the_fairy", player -> NineFNetwork.callTia((SkyBlockPlayer) player));
        AbiphoneCallActions.register("tyzzo", player -> player.openView(new GUITyzzo()));
        AbiphoneCallActions.register("vincent", player -> player.openView(new GUIVincent()));
        AbiphoneCallActions.register("wool_weaver", player -> player.openView(new GUIShopWoolWeaverVibrant()));
        AbiphoneCallActions.register("zog", player -> player.openView(new GUIShopZog()));
        AbiphoneIncomingHooks.register("vincent", new AbiphoneIncomingHooks.Hook(ContactCalls::vincentHasNews,
                player -> ContactCalls.vincentNewYear(player, VincentDyes.masterpiece(SkyBlockCalendar.getYear()).stream()
                        .map(dye -> "<" + dye.colour() + ">" + dye.name()).toList())));
    }
}
