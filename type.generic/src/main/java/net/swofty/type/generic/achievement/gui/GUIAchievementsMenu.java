package net.swofty.type.generic.achievement.gui;

import net.minestom.server.event.inventory.InventoryPreClickEvent;
import net.minestom.server.inventory.InventoryType;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.swofty.commons.text.Text;
import net.swofty.type.generic.achievement.AchievementCategory;
import net.swofty.type.generic.achievement.AchievementDefinition;
import net.swofty.type.generic.achievement.AchievementType;
import net.swofty.type.generic.achievement.AchievementRegistry;
import net.swofty.type.generic.achievement.PlayerAchievementHandler;
import net.swofty.type.generic.gui.inventory.HypixelInventoryGUI;
import net.swofty.type.generic.gui.inventory.ItemStacks;
import net.swofty.type.generic.gui.inventory.item.GUIClickableItem;
import net.swofty.type.generic.gui.inventory.item.GUIItem;
import net.swofty.type.generic.user.HypixelPlayer;

import java.util.List;

public class GUIAchievementsMenu extends HypixelInventoryGUI {

    private final AchievementNav profileNav;

    public GUIAchievementsMenu(AchievementNav profileNav) {
        super("Achievements", InventoryType.CHEST_6_ROW);
        this.profileNav = profileNav;
    }

    @Override
    public void onOpen(InventoryGUIOpenEvent e) {
        HypixelPlayer player = e.player();
        PlayerAchievementHandler handler = player.getAchievementHandler();

        set(createCategoryItem(1, AchievementCategory.GENERAL, handler));
        set(createCategoryItem(2, AchievementCategory.HOUSING, handler));
        set(createCategoryItem(3, AchievementCategory.SKYBLOCK, handler));
        set(createCategoryItem(4, AchievementCategory.ARCADE, handler));
        set(createClassicGamesItem(5, handler));
        set(createSeasonalItem(6, handler));
        set(createLegacyItem(7, handler));

        set(createCategoryItem(19, AchievementCategory.TNT_GAMES, handler));
        set(createCategoryItem(20, AchievementCategory.BLITZ_SG, handler));
        set(createCategoryItem(21, AchievementCategory.MEGA_WALLS, handler));
        set(createCategoryItem(22, AchievementCategory.COPS_AND_CRIMS, handler));
        set(createCategoryItem(23, AchievementCategory.UHC_CHAMPIONS, handler));
        set(createCategoryItem(24, AchievementCategory.WARLORDS, handler));
        set(createCategoryItem(25, AchievementCategory.SKYWARS, handler));

        set(createCategoryItem(28, AchievementCategory.SMASH_HEROES, handler));
        set(createCategoryItem(29, AchievementCategory.SPEED_UHC, handler));
        set(createCategoryItem(30, AchievementCategory.BEDWARS, handler));
        set(createCategoryItem(31, AchievementCategory.MURDER_MYSTERY, handler));
        set(createCategoryItem(32, AchievementCategory.BUILD_BATTLE, handler));
        set(createCategoryItem(33, AchievementCategory.DUELS, handler));
        set(createCategoryItem(34, AchievementCategory.PIT, handler));

        set(createCategoryItem(40, AchievementCategory.WOOL_GAMES, handler));

        set(new GUIItem(45) {
            @Override
            public ItemStack.Builder getItem(HypixelPlayer player) {
                int totalPoints = handler.getTotalPoints();
                boolean unlocked = totalPoints >= 10000;
                return ItemStacks.item(Material.CLOCK, 1,
                        unlocked ? Text.of("<6>Gold Achievement Menu") : Text.of("<c>Gold Achievement Menu"),
                        List.of(
                                Text.of("<7>Changes achievement unlocks within"),
                                Text.of("<7>the menu to gold."),
                                Text.empty(),
                                unlocked ? Text.of("<a>Unlocked!")
                                        : Text.of("<c>Unlocked with <b>10,000 <c>Achievement Points")));
            }
        });

        if (profileNav != null) {
            set(new GUIClickableItem(48) {
                @Override
                public ItemStack.Builder getItem(HypixelPlayer player) {
                    return ItemStacks.item(Material.ARROW, """
                            <a>Go Back
                            <7>To {}""", profileNav.label());
                }

                @Override
                public void run(InventoryPreClickEvent e, HypixelPlayer player) {
                    profileNav.back().accept(player);
                }
            });
        }

        set(new GUIItem(49) {
            @Override
            public ItemStack.Builder getItem(HypixelPlayer player) {
                int totalUnlocked = handler.getTotalUnlockedCount();
                int totalAchievements = AchievementRegistry.getTotalCompletionCount();
                int totalPoints = handler.getTotalPoints();
                int maxPoints = AchievementRegistry.getTotalCompletionPoints();
                double unlockedPercent = totalAchievements > 0 ? (totalUnlocked * 100.0 / totalAchievements) : 0;
                double pointsPercent = maxPoints > 0 ? (totalPoints * 100.0 / maxPoints) : 0;

                return ItemStacks.item(Material.DIAMOND, """
                        <a>Hypixel Achievements Completion
                        <7>Player: {}
                        <7>Unlocked: <b>{}<7>/<b>{} <8>({}%)
                        <7>Points: <e>{}<7>/<e>{} <8>({}%)

                        <7>Legacy Unlocked: <b>{}
                        <7>Legacy Points: <e>{}""",
                        player.getFullDisplayName(),
                        totalUnlocked, totalAchievements, (int) unlockedPercent,
                        totalPoints, maxPoints, (int) pointsPercent,
                        handler.getAchievementData().getLegacyUnlockedCount(),
                        handler.getAchievementData().getLegacyPoints());
            }
        });

        set(new GUIItem(50) {
            @Override
            public ItemStack.Builder getItem(HypixelPlayer player) {
                return ItemStacks.item(Material.REPEATER, """
                        <a>Achievements Tracking <b>[MVP<c>+<b>]
                        <7>Track achievements to access them
                        <7>quickly in this menu and get instant
                        <7>feedback of your progress.""");
            }
        });

        set(new GUIClickableItem(51) {
            @Override
            public ItemStack.Builder getItem(HypixelPlayer player) {
                return ItemStacks.item(Material.GOLD_INGOT, """
                        <6>Achievement Rewards
                        <7>Unlock exclusive rewards for
                        <7>achievement hunting efforts.""");
            }

            @Override
            public void run(InventoryPreClickEvent e, HypixelPlayer player) {
                player.sendMessage("<6>Achievement Rewards coming soon!");
            }
        });

        set(new GUIItem(53) {
            @Override
            public ItemStack.Builder getItem(HypixelPlayer player) {
                return ItemStacks.item(Material.OAK_SIGN, """
                        <a>Search
                        <7>Search for an achievement by name,
                        <7>description or points value.""");
            }
        });

        updateItemStacks(getInventory(), player);
    }

    private GUIClickableItem createCategoryItem(int slot, AchievementCategory category, PlayerAchievementHandler handler) {
        return new GUIClickableItem(slot) {
            @Override
            public ItemStack.Builder getItem(HypixelPlayer player) {
                int unlocked = handler.getUnlockedCount(category);
                int total = AchievementRegistry.getCompletionCount(category);
                int points = handler.getTotalPoints(category);
                int maxPoints = AchievementRegistry.getCompletionPoints(category);
                double unlockedPercent = total > 0 ? (unlocked * 100.0 / total) : 0;
                double pointsPercent = maxPoints > 0 ? (points * 100.0 / maxPoints) : 0;

                return ItemStacks.of(category.getMaterial(), 1, """
                        <a>{} Achievements
                        <7>Unlocked: <b>{}<7>/<b>{} <8>({}%)
                        <7>Points: <e>{}<7>/<e>{} <8>({}%)

                        <e>Click to view achievements!""",
                        category.getDisplayName(),
                        unlocked, total, (int) unlockedPercent,
                        points, maxPoints, (int) pointsPercent);
            }

            @Override
            public void run(InventoryPreClickEvent e, HypixelPlayer player) {
                new GUIGameAchievements(category, new AchievementNav("Achievements", back -> new GUIAchievementsMenu(profileNav).open(back))).open(player);
            }
        };
    }

    private static final List<AchievementCategory> CLASSIC_GAMES = List.of(
            AchievementCategory.QUAKECRAFT, AchievementCategory.VAMPIREZ, AchievementCategory.PAINTBALL,
            AchievementCategory.THE_WALLS, AchievementCategory.ARENA_BRAWL, AchievementCategory.TURBO_KART_RACERS);

    private GUIItem createClassicGamesItem(int slot, PlayerAchievementHandler handler) {
        return new GUIItem(slot) {
            @Override
            public ItemStack.Builder getItem(HypixelPlayer player) {
                int unlocked = CLASSIC_GAMES.stream().mapToInt(handler::getUnlockedCount).sum();
                int total = CLASSIC_GAMES.stream().mapToInt(AchievementRegistry::getCompletionCount).sum();
                int points = CLASSIC_GAMES.stream().mapToInt(handler::getTotalPoints).sum();
                int maxPoints = CLASSIC_GAMES.stream().mapToInt(AchievementRegistry::getCompletionPoints).sum();
                return ItemStacks.item(Material.JUKEBOX, """
                        <a>Classic Games Achievements
                        <7>Unlocked: <b>{}<7>/<b>{} <8>({}%)
                        <7>Points: <e>{}<7>/<e>{} <8>({}%)""",
                        unlocked, total, total > 0 ? unlocked * 100 / total : 0,
                        points, maxPoints, maxPoints > 0 ? points * 100 / maxPoints : 0);
            }
        };
    }

    private GUIItem createSeasonalItem(int slot, PlayerAchievementHandler handler) {
        return new GUIItem(slot) {
            @Override
            public ItemStack.Builder getItem(HypixelPlayer player) {
                int total = AchievementRegistry.getAll().stream()
                        .filter(a -> a.getType() == AchievementType.SEASONAL).mapToInt(AchievementDefinition::getUnlockableCount).sum();
                int maxPoints = AchievementRegistry.getAll().stream()
                        .filter(a -> a.getType() == AchievementType.SEASONAL).mapToInt(AchievementDefinition::getTotalPoints).sum();
                int unlocked = handler.getAchievementData().getSeasonalUnlockedCount();
                int points = handler.getAchievementData().getSeasonalPoints();
                return ItemStacks.head("f5612dc7b86d71afc1197301c15fd979e9f39e7b1f41d8f1ebdf8115576e2e", """
                        <a>Seasonal Achievements
                        <7>Unlocked: <b>{}<7>/<b>{} <8>({}%)
                        <7>Points: <e>{}<7>/<e>{} <8>({}%)

                        <7>Seasonal achievements are listed
                        <7>under each game.""",
                        unlocked, total, total > 0 ? unlocked * 100 / total : 0,
                        points, maxPoints, maxPoints > 0 ? points * 100 / maxPoints : 0);
            }
        };
    }

    private GUIItem createLegacyItem(int slot, PlayerAchievementHandler handler) {
        return new GUIItem(slot) {
            @Override
            public ItemStack.Builder getItem(HypixelPlayer player) {
                return ItemStacks.item(Material.DIAMOND_BLOCK, """
                        <a>Legacy Achievements
                        <7>Unlocked: <b>{}
                        <7>Points: <e>{}

                        <7>Due to events and games that are no
                        <7>longer available, these achievements
                        <7>cannot be earned anymore.

                        <7>Points from these achievements still
                        <7>count towards achievement rewards,
                        <7>but do not count towards
                        <7>leaderboards.""",
                        handler.getAchievementData().getLegacyUnlockedCount(),
                        handler.getAchievementData().getLegacyPoints());
            }
        };
    }

    @Override
    public boolean allowHotkeying() {
        return false;
    }

    @Override
    public void onBottomClick(InventoryPreClickEvent e) {
        e.setCancelled(true);
    }
}
