package dev.daweapon.type.skyblockgeneric.gui.inventories.sbmenu;

import net.kyori.adventure.text.format.NamedTextColor;
import net.minestom.server.component.DataComponents;
import net.minestom.server.inventory.InventoryType;
import net.minestom.server.inventory.click.Click;
import net.minestom.server.item.Material;
import dev.daweapon.commons.ServerType;
import dev.daweapon.commons.StringUtility;
import dev.daweapon.commons.skyblock.statistics.ItemStatistic;
import dev.daweapon.commons.text.Text;
import dev.daweapon.type.generic.gui.inventory.ItemStacks;
import dev.daweapon.type.generic.gui.v2.*;
import dev.daweapon.type.generic.gui.v2.context.ViewContext;
import dev.daweapon.type.generic.user.HypixelPlayer;
import dev.daweapon.type.skyblockgeneric.bank.BankInterestCalculator;
import dev.daweapon.type.skyblockgeneric.bank.PersonalBankService;
import dev.daweapon.type.skyblockgeneric.calendar.CalendarEvent;
import dev.daweapon.type.skyblockgeneric.calendar.SkyBlockCalendar;
import dev.daweapon.type.skyblockgeneric.data.SkyBlockDataHandler;
import dev.daweapon.type.skyblockgeneric.data.datapoints.DatapointBankData;
import dev.daweapon.type.skyblockgeneric.gui.inventories.banker.GUIBanker;
import dev.daweapon.type.skyblockgeneric.gui.inventories.islandsettings.GUIIslandSettings;
import dev.daweapon.type.skyblockgeneric.gui.inventories.sbmenu.bags.GUIYourBags;
import dev.daweapon.type.skyblockgeneric.gui.inventories.sbmenu.calendar.GUICalendar;
import dev.daweapon.type.skyblockgeneric.gui.inventories.sbmenu.collection.GUICollections;
import dev.daweapon.type.skyblockgeneric.gui.inventories.sbmenu.fasttravel.GUIFastTravel;
import dev.daweapon.type.skyblockgeneric.gui.inventories.sbmenu.levels.GUISkyBlockLevels;
import dev.daweapon.type.skyblockgeneric.gui.inventories.sbmenu.profiles.GUIProfileManagement;
import dev.daweapon.type.skyblockgeneric.gui.inventories.sbmenu.questlog.GUIMissionLog;
import dev.daweapon.type.skyblockgeneric.gui.inventories.sbmenu.recipe.GUIRecipeBook;
import dev.daweapon.type.skyblockgeneric.gui.inventories.sbmenu.skills.GUISkills;
import dev.daweapon.type.skyblockgeneric.gui.inventories.sbmenu.storage.GUIStorage;
import dev.daweapon.type.skyblockgeneric.item.crafting.SkyBlockRecipe;
import dev.daweapon.type.skyblockgeneric.levels.SkyBlockLevelRequirement;
import dev.daweapon.type.skyblockgeneric.skill.SkillCategories;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;
import dev.daweapon.type.skyblockgeneric.potion.CookieBuffService;
import dev.daweapon.type.skyblockgeneric.user.statistics.PlayerStatistics;
import org.jspecify.annotations.NonNull;

import java.time.Duration;
import java.util.*;

// Main SkyBlock menu hub, linking out to bags, calendar, bank, skills, storage, and other sub-menus
public class GUISkyBlockMenu extends StatelessView {
    private static final List<ItemStatistic> PROFILE_LORE_STATS = List.of(ItemStatistic.SPEED, ItemStatistic.STRENGTH,
        ItemStatistic.DEFENSE, ItemStatistic.CRITICAL_DAMAGE, ItemStatistic.CRITICAL_CHANCE, ItemStatistic.HEALTH,
        ItemStatistic.INTELLIGENCE);

    @Override
    public ViewConfiguration<DefaultState> configuration() {
        return ViewConfiguration.translatable("gui_sbmenu.main.title", InventoryType.CHEST_6_ROW);
    }

    @Override
    public void layout(ViewLayout<DefaultState> layout, DefaultState state, ViewContext ctx) {
        Components.fill(layout);
        Components.close(layout, 49);

        layout.slot(13, (s, c) -> {
            SkyBlockPlayer player = (SkyBlockPlayer) c.player();

            List<Text> lore = new ArrayList<>();
            lore.add(Text.key("gui_sbmenu.main.your_profile.view_equipment"));
            lore.add(Text.literal(" "));

            PlayerStatistics statistics = player.getStatistics();
            Map<ItemStatistic, Double> overall = statistics.allStatistics().getOverall();
            for (ItemStatistic statistic : PROFILE_LORE_STATS) {
                lore.add(Text.of(" <stat:{}> <f>{}{}", statistic.name(),
                    StringUtility.decimalify(overall.getOrDefault(statistic, statistic.getBaseAdditiveValue()), 2),
                    statistic.getSuffix()));
            }
            lore.add(Text.key("gui_sbmenu.main.your_profile.and_more"));

            lore.add(Text.literal(" "));
            lore.add(Text.key("gui_sbmenu.main.your_profile.stats_command"));
            lore.add(Text.literal(" "));
            lore.add(Text.key("gui_sbmenu.main.your_profile.view"));

            return ItemStacks.head(player.getPlayerSkin(), Text.key("gui_sbmenu.main.your_profile"), lore);
        }, (_, c) -> {
            SkyBlockPlayer player = (SkyBlockPlayer) c.player();
            player.openView(new GUISkyBlockProfile());
        });

        layout.slot(22, (s, c) -> {
            SkyBlockPlayer player = (SkyBlockPlayer) c.player();
            SkyBlockLevelRequirement levelRequirement = player.getSkyBlockExperience().getLevel();
            SkyBlockLevelRequirement nextLevel = levelRequirement.getNextLevel();

            return ItemStacks.head("3255327dd8e90afad681a19231665bea2bd06065a09d77ac1408837f9e0b242",
                Text.key("gui_sbmenu.main.skyblock_leveling"),
                Text.keyLines("gui_sbmenu.main.skyblock_leveling.lore",
                    Text.parse(levelRequirement.getColor() + levelRequirement),
                    nextLevel == null ? Text.of("<c>MAX") : Text.literal(String.valueOf(nextLevel)),
                    Text.parse(player.getSkyBlockExperience().getNextLevelDisplay()))
            );
        }, (click, c) -> c.push(new GUISkyBlockLevels()));

        layout.slot(29, (s, c) -> ItemStacks.head("961a918c0c49ba8d053e522cb91abc74689367b4d8aa06bfc1ba9154730985ff",
            Text.key("gui_sbmenu.main.your_bags"),
            Text.keyLines("gui_sbmenu.main.your_bags.lore")
        ), (click, c) -> {
            c.push(new GUIYourBags());
        });

        layout.slot(30, (s, c) -> {
            SkyBlockPlayer player = (SkyBlockPlayer) c.player();
            Text selectedPet = player.getPetData().getEnabledPet() == null
                ? Text.of("<c>None")
                : Text.literal(player.getPetData().getEnabledPet().getDisplayName());
            return ItemStacks.item(Material.BONE, 1,
                Text.key("gui_sbmenu.main.pets"),
                Text.keyLines("gui_sbmenu.main.pets.lore", selectedPet)
            );
        }, (click, c) -> c.push(new GUIPets(), GUIPets.createInitialState((SkyBlockPlayer) c.player())));

        layout.slot(21, (s, c) -> {
            SkyBlockPlayer player = (SkyBlockPlayer) c.player();
            List<String> missionDisplay = new ArrayList<>();
            SkyBlockRecipe.getMissionDisplay(missionDisplay, player.getUuid());

            return ItemStacks.item(Material.BOOK, 1,
                Text.key("gui_sbmenu.main.recipe_book"),
                Text.keyLines("gui_sbmenu.main.recipe_book.lore",
                    Text.parse(missionDisplay.get(0)), Text.parse(missionDisplay.get(1))));
        }, (_, c) -> {
            c.push(new GUIRecipeBook());
        });

        layout.slot(25, (s, c) -> ItemStacks.item(Material.CHEST, 1,
            Text.key("gui_sbmenu.main.storage"),
            Text.keyLines("gui_sbmenu.main.storage.lore")
        ), (click, c) -> c.push(new GUIStorage()));

        layout.slot(23, (s, c) -> ItemStacks.item(Material.WRITABLE_BOOK, 1,
            Text.key("gui_sbmenu.main.quests"),
            Text.keyLines("gui_sbmenu.main.quests.lore")
        ), (click, c) -> c.push(new GUIMissionLog()));

        layout.autoUpdating(24, (s, c) -> ItemStacks.item(Material.CLOCK, 1,
                Text.key("gui_sbmenu.main.calendar"), getCalendarLore(ctx)),
            (click, c) -> c.push(new GUICalendar()), Duration.ofSeconds(1));

        if (onMiningIsland()) {
            layout.slot(10, (s, c) -> {
                SkyBlockPlayer player = (SkyBlockPlayer) c.player();
                int tier = dev.daweapon.type.skyblockgeneric.hotm.HotmService.data(player).getTier();
                return ItemStacks.item(Material.ENDER_EYE, "<5>Heart of the Mountain\n<7>Spend Tokens and Powder on mining perks.\n\n<7>Tier: <5>" + tier + "\n\n<e>Click to view!");
            }, (click, c) -> c.push(new dev.daweapon.type.skyblockgeneric.gui.inventories.hotm.GUIHotm()));
        }

        else if (dev.daweapon.type.generic.HypixelConst.isGardenServer()) {
            layout.slot(10, (s, c) -> {
                SkyBlockPlayer player = (SkyBlockPlayer) c.player();
                var profile = dev.daweapon.type.skyblockgeneric.garden.Gardens.profile(player);
                int level = dev.daweapon.type.skyblockgeneric.garden.GardenProgress.level(profile);
                return ItemStacks.item(Material.SUNFLOWER, "<a>Garden Level " + StringUtility.getAsRomanNumeral(level)
                        + "\n<7>Earn Garden experience by accepting\n<7>visitors' offers and unlocking new\n<7>milestones!\n\n<e>Click to open the Desk!");
            }, (click, c) -> c.push(new dev.daweapon.type.skyblockgeneric.gui.inventories.garden.GUIGardenDesk()));
            layout.slot(11, (s, c) -> ItemStacks.text(
                    dev.daweapon.type.skyblockgeneric.gui.inventories.garden.GUIManageChips.icon(dev.daweapon.type.skyblockgeneric.garden.chip.GardenChip.CROPSHOT),
                    Text.of("<a>Manage Chips"),
                    List.of(Text.of("<7>Redeem chips, then level them up"), Text.of("<7>with <2>Sowdust<7>."), Text.of(""), Text.of("<e>Click to view!"))),
                    (click, c) -> c.push(new dev.daweapon.type.skyblockgeneric.gui.inventories.garden.GUIManageChips()));
        }

        layout.slot(19, (s, c) -> {
            SkyBlockPlayer player = (SkyBlockPlayer) c.player();
            double average = java.util.Arrays.stream(SkillCategories.values())
                .filter(category -> category != SkillCategories.RUNECRAFTING && category != SkillCategories.CARPENTRY && !category.isCosmetic())
                .mapToInt(category -> player.getSkills().getCurrentLevel(category))
                .average().orElse(0);
            return ItemStacks.item(Material.DIAMOND_SWORD, 1,
                Text.key("gui_sbmenu.main.skills"),
                Text.keyLines("gui_sbmenu.main.skills.lore", StringUtility.decimalify(average, 1)));
        }, (click, c) -> c.push(new GUISkills()));

        layout.slot(20, (s, c) -> {
            SkyBlockPlayer player = (SkyBlockPlayer) c.player();
            List<String> collectionDisplay = new ArrayList<>();
            player.getCollection().getDisplay(collectionDisplay);

            return ItemStacks.item(Material.PAINTING, 1,
                Text.key("gui_sbmenu.main.collections"),
                Text.keyLines("gui_sbmenu.main.collections.lore",
                    Text.parse(collectionDisplay.get(0)), Text.parse(collectionDisplay.get(1))));
        }, (_, c) -> {
            SkyBlockPlayer player = (SkyBlockPlayer) c.player();
            player.openView(new GUICollections());
        });

        layout.slot(31, (s, c) -> ItemStacks.item(Material.CRAFTING_TABLE, 1,
            Text.key("gui_sbmenu.main.crafting_table"),
            Text.keyLines("gui_sbmenu.main.crafting_table.lore")
        ), (_, c) -> c.push(new GUICrafting()));

        layout.slot(32, (s, c) -> {
            SkyBlockPlayer player = (SkyBlockPlayer) c.player();
            return ItemStacks.item(Material.BARREL, 1,
                    Text.key("gui_sbmenu.main.wardrobe"),
                    Text.keyLines("gui_sbmenu.main.wardrobe.lore")).set(DataComponents.DYED_COLOR, NamedTextColor.DARK_PURPLE);
        }, (_, c) -> {
            SkyBlockPlayer player = (SkyBlockPlayer) c.player();
            c.push(new GUILoadouts());
        });

        layout.autoUpdating(33, (s, c) -> {
            SkyBlockPlayer player = (SkyBlockPlayer) c.player();
            DatapointBankData.BankData bank = PersonalBankService.data(player);
            boolean unlocked = PersonalBankService.isUnlocked(player);
            long remaining = PersonalBankService.remaining(player);
            Text status = !unlocked ? Text.of("<c>Locked") : remaining == 0 ? Text.of("<a>Available")
                                                     : Text.of("<e>{:time}", remaining);
            double projection = BankInterestCalculator.calculate(bank.getAmount(), bank.getAccountTier(), bank.getMuseumMilestone());

            return ItemStacks.head("e36e94f6c34a35465fce4a90f2e25976389eb9709a12273574ff70fd4daa6852",
                Text.key("gui_sbmenu.main.personal_bank"),
                List.of(
                    Text.of("<7>Contact your Banker from anywhere."),
                    Text.of("<7>Cooldown: <e>5 minutes"),
                    Text.empty(),
                    Text.of("<7>Banker Status:"),
                    status,
                    Text.empty(),
                    Text.of("<7>Interest in: <b>26 Hours"),
                    Text.of("<7>Solo Projection: <6>44,859.6 coins <b>(1.805%)"),
                    Text.of("<7>Last Solo Interest: <6>44,415 coins"),
                    Text.empty(),
                    Text.of("<8>Also accessible via /bank"),
                    Text.empty(),
                    Text.of("<e>Click to open!")));
        }, (_, c) -> {
            SkyBlockPlayer player = (SkyBlockPlayer) c.player();
            if (!PersonalBankService.isUnlocked(player) || PersonalBankService.remaining(player) > 0) {
                player.sendMessage("<c>Your Personal Bank is locked or still on cooldown!");
                return;
            }
            new GUIBanker().open(player);
            DatapointBankData.BankData bank = PersonalBankService.data(player);
            bank.setLastRemoteBankUse(System.currentTimeMillis());
            player.getSkyblockDataHandler().get(SkyBlockDataHandler.Data.BANK_DATA, DatapointBankData.class).setValue(bank);
        }, Duration.ofSeconds(1));

        layout.slot(47, (s, c) -> ItemStacks.head("35f4b40cef9e017cd4112d26b62557f8c1d5b189da2e99534222bc8cec7d9196",
            Text.key("gui_sbmenu.main.fast_travel"),
            Text.keyLines("gui_sbmenu.main.fast_travel.lore")
        ), (click, c) -> {
            SkyBlockPlayer player = (SkyBlockPlayer) c.player();
            if (click.click() instanceof Click.Right) {
                player.closeInventory();
                player.sendTo(ServerType.SKYBLOCK_ISLAND);
                return;
            }
            player.openView(new GUIFastTravel());
        });

        layout.slot(48, (s, c) -> {
            HypixelPlayer player = c.player();
            return ItemStacks.item(Material.NAME_TAG, 1,
                Text.key("gui_sbmenu.main.profile_management"),
                Text.keyLines("gui_sbmenu.main.profile_management.lore",
                    ((SkyBlockPlayer) player).getProfiles().getProfiles().size())
            );
        }, (_, c) -> c.push(new GUIProfileManagement()));

        layout.slot(50, (s, c) -> ItemStacks.item(Material.REDSTONE_TORCH, 1,
            Text.key("gui_sbmenu.main.settings"),
            Text.keyLines("gui_sbmenu.main.settings.lore")), (_, c) -> c.push(new GUIIslandSettings()));

        layout.slot(52, (s, c) -> c.player().getRank().isStaff()
                ? ItemStacks.item(Material.COMMAND_BLOCK, 1, Text.literal("Admin Menu"), List.of(Text.literal("Staff tools and command actions")))
                : net.minestom.server.item.ItemStack.builder(Material.AIR), (_, c) -> {
            if (c.player().getRank().isStaff()) new dev.daweapon.type.skyblockgeneric.gui.inventories.admin.GUIAdmin().open(c.player());
        });

        layout.slot(51, (s, c) -> {
            SkyBlockPlayer player = (SkyBlockPlayer) c.player();
            Text status = player.isBoosterCookieActive()
                    ? Text.of("<a>Active <7>({})", CookieBuffService.formatRemaining(player))
                    : Text.of("<c>Not active!");
            return ItemStacks.item(Material.COOKIE, 1,
                Text.key("gui_sbmenu.main.booster_cookie"),
                Text.keyLines("gui_sbmenu.main.booster_cookie.lore", status));
        }, (_, c) -> c.push(new GUIBoosterCookie()));
    }

    private static @NonNull List<Text> getCalendarLore(ViewContext ctx) {
        // Perkpocalypse is Jerry's own mayor mechanic and lives in the Mayor Jerry tooltip instead
        // of general Calendar UI (matches GUICalendar/GUIFullCalendar, which skip it the same way).
        List<CalendarEvent> currentEvents = SkyBlockCalendar.getCurrentEvents().stream()
                .filter(event -> event != CalendarEvent.PERKPOCALYPSE)
                .toList();
        boolean multipleEvents = currentEvents.size() > 1;

        String date = StringUtility.ntify(SkyBlockCalendar.getDay()) + " " + SkyBlockCalendar.getMonthName() + " " + SkyBlockCalendar.getYear();
        List<Text> lore = new ArrayList<>(Text.keyLines("gui_sbmenu.main.calendar.lore_header", date));
        lore.add(Text.literal(" "));

        if (multipleEvents) {
            lore.add(Text.key("gui_sbmenu.main.calendar.current_events"));
            for (CalendarEvent event : currentEvents) {
                lore.add(event.getDisplayName(SkyBlockCalendar.getYear()));
            }
        } else if (currentEvents.size() == 1) {
            CalendarEvent currentEvent = currentEvents.getFirst();
            lore.add(Text.key("gui_sbmenu.main.calendar.current_event", currentEvent.getDisplayName(SkyBlockCalendar.getYear())));
            long ticksRemaining = getTicksRemaining(currentEvent);
            lore.add(Text.key("gui_sbmenu.main.calendar.event_ends_in", StringUtility.formatTimeLeft(ticksRemaining * 50L)));
        } else {
            lore.add(Text.key("gui_sbmenu.main.calendar.no_current_events"));
        }

        lore.add(Text.literal(" "));

        // Dark Auction and Jacob's Farming Contest recur every 3 SkyBlock days and live on the full day-by-day Calendar
        // instead of this rotating "next event" preview - it would otherwise dominate the slot.
        // Perkpocalypse lives in the Mayor Jerry tooltip instead (see filter above).
        Map<SkyBlockCalendar.EventInfo, CalendarEvent> upcomingEvents =
                SkyBlockCalendar.getEventsWithDurationUntilSkipSpecific(1,
                        List.of(CalendarEvent.DARK_AUCTION, CalendarEvent.PERKPOCALYPSE, CalendarEvent.FARMING_CONTEST));

        if (!upcomingEvents.isEmpty()) {
            Map.Entry<SkyBlockCalendar.EventInfo, CalendarEvent> entry = upcomingEvents.entrySet().iterator().next();
            SkyBlockCalendar.EventInfo info = entry.getKey();
            CalendarEvent event = entry.getValue();

            lore.add(Text.key("gui_sbmenu.main.calendar.next_event", event.getDisplayName(info.year())));
            lore.add(Text.key("gui_sbmenu.main.calendar.next_event_starting", StringUtility.formatTimeLeft(info.timeUntilBegin() * 50L)));
        } else {
            lore.add(Text.key("gui_sbmenu.main.calendar.no_upcoming_events"));
        }

        lore.addAll(Text.keyLines("gui_sbmenu.main.calendar.lore_footer"));
        return lore;
    }

    private static long getTicksRemaining(CalendarEvent currentEvent) {
        long currentElapsedInYear = SkyBlockCalendar.getElapsed() % SkyBlockCalendar.YEAR;
        long eventEndTime = 0;
        for (Long eventStartTime : currentEvent.times()) {
            if (currentElapsedInYear >= eventStartTime && currentElapsedInYear < eventStartTime + currentEvent.duration().toMillis() / 50) {
                eventEndTime = eventStartTime + currentEvent.duration().toMillis() / 50;
                break;
            }
        }
        return eventEndTime - currentElapsedInYear;
    }


    private static boolean onMiningIsland() {
        if (dev.daweapon.type.generic.HypixelConst.getTypeLoader() == null) return false;
        ServerType type = dev.daweapon.type.generic.HypixelConst.getTypeLoader().getType();
        return type == ServerType.SKYBLOCK_DWARVEN_MINES || type == ServerType.SKYBLOCK_CRYSTAL_HOLLOWS || type == ServerType.SKYBLOCK_DEEP_CAVERNS || type == ServerType.SKYBLOCK_GOLD_MINE;
    }
}
