package dev.daweapon.type.kuudra.fight;

import dev.daweapon.type.skyblockgeneric.crimson.kuudra.data.KuudraConfig;
import dev.daweapon.type.skyblockgeneric.crimson.kuudra.data.KuudraPerkShop;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class KuudraTokensAndPerksTest {
    private static final KuudraConfig CONFIG = KuudraConfig.load(Path.of("../configuration/skyblock/kuudra"));
    private final UUID alice = UUID.randomUUID();
    private final UUID bob = UUID.randomUUID();

    @Test
    void tokensAreSharedButSpentFromEachBalance() {
        KuudraTokens tokens = new KuudraTokens();
        tokens.join(alice, 50);
        tokens.join(bob, 0);
        tokens.gain(150);
        assertEquals(200, tokens.balance(alice));
        assertEquals(150, tokens.balance(bob));
        assertTrue(tokens.spend(bob, 150));
        assertFalse(tokens.spend(bob, 1));
        assertEquals(0, tokens.balance(bob));
        assertEquals(150, tokens.earned(bob));
        assertEquals(200, tokens.earned(alice));
        tokens.join(alice, 999);
        assertEquals(200, tokens.balance(alice));
    }

    private KuudraPerks perks(KuudraTokens tokens) {
        return new KuudraPerks(CONFIG.perkShop(), tokens, CONFIG.routeUnlockTokens());
    }

    @Test
    void aRouteCostsFiftyTokensAndIsForTheWholeRun() {
        KuudraTokens tokens = new KuudraTokens();
        tokens.join(alice, 0);
        KuudraPerks perks = perks(tokens);
        assertEquals(KuudraPerks.Result.NOT_ENOUGH_TOKENS, perks.chooseRoute(alice, "CANNONEER"));
        tokens.gain(60);
        assertEquals(KuudraPerks.Result.UNKNOWN, perks.chooseRoute(alice, "NOPE"));
        assertEquals(KuudraPerks.Result.BOUGHT, perks.chooseRoute(alice, "CANNONEER"));
        assertEquals(10, tokens.balance(alice));
        assertEquals(KuudraPerks.Result.ROUTE_ALREADY_CHOSEN, perks.chooseRoute(alice, "SUPPORT"));
    }

    @Test
    void perksLevelUpWithinTheRouteAtTheirListedCosts() {
        KuudraTokens tokens = new KuudraTokens();
        tokens.join(alice, 0);
        KuudraPerks perks = perks(tokens);
        assertEquals(KuudraPerks.Result.NO_ROUTE, perks.upgrade(alice, "RAPID_FIRE"));
        tokens.gain(1_000);
        perks.chooseRoute(alice, "CANNONEER");
        assertEquals(KuudraPerks.Result.WRONG_ROUTE, perks.upgrade(alice, "HEALING_AURA"));
        assertEquals(KuudraPerks.Result.BOUGHT, perks.upgrade(alice, "RAPID_FIRE"));
        assertEquals(KuudraPerks.Result.BOUGHT, perks.upgrade(alice, "RAPID_FIRE"));
        assertEquals(1_000 - 50 - 50 - 100, tokens.balance(alice));
        assertEquals(0.6, perks.value(alice, "RAPID_FIRE"), 1e-9);
        assertEquals(KuudraPerks.Result.BOUGHT, perks.upgrade(alice, "CANNON_PROFICIENCY"));
        assertEquals(1.8, perks.value(alice, "CANNON_PROFICIENCY"), 1e-9);
        assertEquals(300_000, perks.secondValue(alice, "CANNON_PROFICIENCY"), 1e-9);
        assertEquals(KuudraPerks.Result.BOUGHT, perks.upgrade(alice, "STEADY_AIM"));
        assertEquals(0.05, perks.fraction(alice, "STEADY_AIM"), 1e-9);
        assertEquals(0, perks.value(bob, "STEADY_AIM"));
    }

    @Test
    void perksStopAtTheirMaximumLevel() {
        KuudraTokens tokens = new KuudraTokens();
        tokens.join(alice, 10_000);
        KuudraPerks perks = perks(tokens);
        perks.chooseRoute(alice, "SPECIALIST");
        KuudraPerkShop.Perk mechanic = perks.perk("SPECIALIST", "BALLISTA_MECHANIC");
        for (int level = 1; level <= mechanic.maxLevel(); level++) {
            assertEquals(KuudraPerks.Result.BOUGHT, perks.upgrade(alice, "BALLISTA_MECHANIC"));
        }
        assertEquals(KuudraPerks.Result.MAXED, perks.upgrade(alice, "BALLISTA_MECHANIC"));
        assertEquals(1.0, perks.fraction(alice, "BALLISTA_MECHANIC"), 1e-9);
    }

    @Test
    void basicPurchasesAndSpecialistsCheapCannonball() {
        KuudraTokens tokens = new KuudraTokens();
        tokens.join(alice, 2_000);
        tokens.join(bob, 2_000);
        KuudraPerks perks = perks(tokens);
        KuudraPerkShop.Purchase cannonball = CONFIG.perkShop().basic().stream()
                .filter(purchase -> purchase.id().equals("HUMAN_CANNONBALL")).findFirst().orElseThrow();
        assertEquals(80, perks.cost(alice, cannonball));
        perks.chooseRoute(alice, "SPECIALIST");
        assertEquals(40, perks.cost(alice, cannonball));
        assertEquals(KuudraPerks.Result.BOUGHT, perks.buy(alice, "HUMAN_CANNONBALL"));
        assertEquals(2_000 - 50 - 40, tokens.balance(alice));
        assertTrue(perks.of(alice).useHumanCannonball());
        assertFalse(perks.of(alice).useHumanCannonball());

        assertEquals(KuudraPerks.Result.BOUGHT, perks.buy(bob, "AUTO_REVIVE"));
        assertEquals(KuudraPerks.Result.BOUGHT, perks.buy(bob, "AUTO_REVIVE"));
        assertEquals(2, perks.of(bob).autoRevives());
        assertEquals(KuudraPerks.Result.NOT_ENOUGH_TOKENS, perks.buy(bob, "AUTO_REVIVE"));
        assertEquals(KuudraPerks.Result.NO_ROUTE, perks.buy(bob, "MINIATURE_NUKE"));
        assertEquals(KuudraPerks.Result.UNKNOWN, perks.buy(bob, "REMOTE_PERK_SHOP"));
    }

    @Test
    void routeSpecialsNeedThatRoute() {
        KuudraTokens tokens = new KuudraTokens();
        tokens.join(alice, 2_000);
        KuudraPerks perks = perks(tokens);
        perks.chooseRoute(alice, "SUPPORT");
        assertEquals(KuudraPerks.Result.WRONG_ROUTE, perks.buy(alice, "MINIATURE_NUKE"));
        assertEquals(KuudraPerks.Result.BOUGHT, perks.buy(alice, "REVIVE_DEAD"));
        assertEquals(2_000 - 50 - 500, tokens.balance(alice));
    }

    @Test
    void perkValuesParseEveryFormatInTheShop() {
        assertEquals(List.of(1.8, 300_000.0), KuudraPerks.numbers("1.8% + 300,000"));
        assertEquals(List.of(0.7), KuudraPerks.numbers("0.7s"));
        assertEquals(List.of(10.0, 0.1), KuudraPerks.numbers("+10% / 0.1"));
        assertEquals(List.of(5.0), KuudraPerks.numbers("-5%"));
        assertEquals(List.of(1.35), KuudraPerks.numbers("x1.35"));
    }
}
