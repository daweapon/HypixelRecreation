package dev.daweapon.type.catacombs.npc;

import dev.daweapon.commons.text.Text;
import dev.daweapon.dungeons.catacombs.CatacombsMode;
import dev.daweapon.dungeons.catacombs.classes.DungeonClassScaling;
import dev.daweapon.dungeons.catacombs.classes.DungeonClassType;
import dev.daweapon.type.catacombs.classes.ClassItems;
import dev.daweapon.type.catacombs.run.CatacombsRun;
import dev.daweapon.type.generic.gui.inventory.ItemStacks;
import dev.daweapon.type.generic.gui.v2.Components;
import dev.daweapon.type.generic.gui.v2.DefaultState;
import dev.daweapon.type.generic.gui.v2.StatelessView;
import dev.daweapon.type.generic.gui.v2.ViewConfiguration;
import dev.daweapon.type.generic.gui.v2.ViewLayout;
import dev.daweapon.type.generic.gui.v2.ViewSession;
import dev.daweapon.type.generic.gui.v2.context.ViewContext;
import dev.daweapon.type.skyblockgeneric.dungeons.DungeonClassProgress;
import dev.daweapon.type.skyblockgeneric.dungeons.gui.GUIDungeonClasses;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;
import net.minestom.server.component.DataComponents;
import net.minestom.server.inventory.InventoryType;
import net.minestom.server.inventory.click.Click;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.minestom.server.item.component.PotionContents;
import net.minestom.server.potion.PotionType;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

/**
 * Mort's Ready Up menu, as captured in M7 runs 1 and 3: the party's heads along the top, the ready pane under them,
 * the five classes in row four with a dye under each counting who picked it, the Dungeon Orb to claim, Close and the
 * Auto Ready Up toggle. Hypixel re-sends it about twice a second, so every live slot refreshes on that beat.
 */
public class GUIReadyUp extends StatelessView {
    static final int READY_SLOT = 13;
    static final int FIRST_CLASS_SLOT = 29;
    static final int FIRST_COUNT_SLOT = 38;
    static final int ORB_SLOT = 45;
    static final int CLOSE_SLOT = 49;
    static final int AUTO_READY_SLOT = 53;
    private static final Duration REFRESH = Duration.ofMillis(500);
    static final String TOO_LATE = "<c>You can't change your class after the dungeon has started!";
    private static final String DOUBLED = """
            <a>All positive bonus stats are doubled if only 1
            <a>player uses this class in a dungeon.""";

    private final CatacombsRun run;

    public GUIReadyUp(CatacombsRun run) {
        this.run = run;
    }

    @Override
    public ViewConfiguration<DefaultState> configuration() {
        return new ViewConfiguration<>("Ready Up", InventoryType.CHEST_6_ROW);
    }

    @Override
    public void layout(ViewLayout<DefaultState> layout, DefaultState state, ViewContext ctx) {
        Components.fill(layout);
        Components.close(layout, CLOSE_SLOT);
        List<Integer> heads = headSlots(run.roster().size());
        for (int index = 0; index < heads.size(); index++) {
            int member = index;
            layout.autoUpdating(heads.get(index), (_, _) -> memberHead(member), REFRESH);
        }
        layout.autoUpdating(READY_SLOT, (_, c) -> readyPane(run.isReady(c.player().getUuid())), (_, c) -> {
            if (c.player() instanceof SkyBlockPlayer player) run.readyUp(player);
        }, REFRESH);
        DungeonClassType[] types = DungeonClassType.values();
        for (int index = 0; index < types.length; index++) {
            DungeonClassType type = types[index];
            layout.autoUpdating(FIRST_CLASS_SLOT + index, (_, c) -> classItem((SkyBlockPlayer) c.player(), type,
                    run.classes().selectedOf(c.player().getUuid()) == type), (click, c) -> {
                if (!(c.player() instanceof SkyBlockPlayer player) || click.click() instanceof Click.Right) return;
                pick(player, type);
            }, REFRESH);
            layout.autoUpdating(FIRST_COUNT_SLOT + index, (_, c) -> pickedBy(type, run.classes().selectedOf(c.player().getUuid()) == type), REFRESH);
        }
        layout.slot(ORB_SLOT, (_, _) -> orb(), (_, c) -> {
            if (c.player() instanceof SkyBlockPlayer player) run.classes().claimOrb(player);
        });
        layout.autoUpdating(AUTO_READY_SLOT, (_, c) -> autoReady(DungeonClassProgress.autoReady((SkyBlockPlayer) c.player()),
                run.floorId().mode()), (_, c) -> {
            SkyBlockPlayer player = (SkyBlockPlayer) c.player();
            DungeonClassProgress.setAutoReady(player, !DungeonClassProgress.autoReady(player));
        }, REFRESH);
    }

    @Override
    public void onOpen(DefaultState state, ViewContext ctx) {
        run.readyUpViewers().add(ctx.player().getUuid());
    }

    @Override
    public void onClose(DefaultState state, ViewContext ctx, ViewSession.CloseReason reason) {
        run.readyUpViewers().remove(ctx.player().getUuid());
    }

    private void pick(SkyBlockPlayer player, DungeonClassType type) {
        if (run.started()) {
            player.sendMessage(TOO_LATE);
            player.closeInventory();
            return;
        }
        if (run.classes().selectedOf(player.getUuid()) == type) return;
        DungeonClassProgress.select(player, type);
        run.classes().select(player, type);
    }

    /** One head per party member along the top row, centred and a slot apart. */
    static List<Integer> headSlots(int members) {
        int count = Math.max(1, Math.min(5, members));
        int first = 4 - (count - 1);
        List<Integer> slots = new ArrayList<>();
        for (int index = 0; index < count; index++) slots.add(first + index * 2);
        return slots;
    }

    private ItemStack.Builder memberHead(int index) {
        List<SkyBlockPlayer> members = run.onlineMembers();
        if (index >= members.size()) return ItemStacks.filler(Material.BLACK_STAINED_GLASS_PANE);
        SkyBlockPlayer member = members.get(index);
        return ItemStacks.head(member.getPlayerSkin(), member.getFullDisplayName(),
                List.of(Text.parse(run.isReady(member.getUuid()) ? "<a>Ready" : "<c>Not Ready")));
    }

    static ItemStack.Builder readyPane(boolean ready) {
        return ready
                ? ItemStacks.item(Material.LIME_STAINED_GLASS_PANE, """
                        <a>Ready
                        <7>You are marked as ready! The fight
                        <7>will start when all players have
                        <7>readied up and have the same tier
                        <7>selected!""")
                : ItemStacks.item(Material.RED_STAINED_GLASS_PANE, """
                        <c>Not Ready
                        <7>Click to mark yourself as ready to
                        <7>start!

                        <e>Click to ready up!""");
    }

    static ItemStack.Builder classItem(SkyBlockPlayer player, DungeonClassType type, boolean selected) {
        int level = DungeonClassProgress.level(player, type);
        String body = "<7>[Lvl " + level + "] <a>" + type.displayName() + "\n"
                + bonuses(type, level) + "\n\n"
                + GUIDungeonClasses.abilityLines(type) + "\n\n"
                + DOUBLED + "\n\n"
                + "<e>Left click to select!\n<e>Right click for more details!";
        ItemStack.Builder builder = ItemStacks.item(GUIDungeonClasses.icon(type), body);
        if (type == DungeonClassType.HEALER) builder.set(DataComponents.POTION_CONTENTS, new PotionContents(PotionType.HEALING));
        return selected ? ItemStacks.enchanted(builder) : builder;
    }

    static String bonuses(DungeonClassType type, int level) {
        return switch (type) {
            case HEALER -> "<7>Vitality: <a>+" + number(DungeonClassScaling.healerVitality(level, 1)) + "\n"
                    + "<7>Mending: <a>+" + number(DungeonClassScaling.healerMending(level, 1));
            case MAGE -> "<7>Intelligence: <a>+" + number(DungeonClassScaling.mageIntelligence(level, 1)) + "\n"
                    + "<7>Ability Damage: <a>+" + number(DungeonClassScaling.mageAbilityDamage(level, 1)) + "%";
            case BERSERK -> "<7>Melee Damage: <c>+<c>" + number(DungeonClassScaling.berserkMeleePercent(level, 1)) + "%\n"
                    + "<7>Walk Speed: <a>+" + number(DungeonClassScaling.berserkSpeed(level, 1));
            case ARCHER -> "<7>Arrow Damage: <c>+<c>" + number(DungeonClassScaling.archerArrowPercent(level, 1)) + "%\n"
                    + "<7>Melee Damage: <c>-<c>" + DungeonClassScaling.ARCHER_MELEE_PENALTY_PERCENT + "%";
            case TANK -> "<7>Health: <a>+" + number(DungeonClassScaling.tankHealth(1)) + "\n"
                    + "<7>Defense: <a>+" + number(DungeonClassScaling.tankDefense(level, 1)) + "\n"
                    + "<7>Vitality: <a>+" + number(DungeonClassScaling.tankVitality(level, 1));
        };
    }

    private ItemStack.Builder pickedBy(DungeonClassType type, boolean mine) {
        List<SkyBlockPlayer> pickers = run.onlineMembers().stream()
                .filter(member -> run.classes().selectedOf(member.getUuid()) == type).toList();
        if (pickers.isEmpty()) return ItemStacks.item(Material.GRAY_DYE, "<c>0 Players\n" + DOUBLED);
        List<Text> lore = new ArrayList<>();
        lore.add(Text.parse("<e>Selected by:"));
        pickers.forEach(member -> lore.add(Text.of("<7>  - {}", member.getFullDisplayName())));
        lore.add(Text.literal(""));
        DOUBLED.lines().map(Text::parse).forEach(lore::add);
        ItemStack.Builder builder = ItemStacks.item(Material.LIME_DYE, pickers.size(), Text.parse("<a>" + pickers.size() + " Player(s)"), lore);
        return mine ? ItemStacks.enchanted(builder) : builder;
    }

    static ItemStack.Builder orb() {
        return ItemStacks.head(ClassItems.ORB_TEXTURE, """
                <6>Dungeon Orb
                <7>When entering a Dungeon, this stone
                <7>adapts to its user and allows them to
                <7>use their class abilities.

                <6><l>LEGENDARY DUNGEON ITEM<r>

                <e>Click to claim!""");
    }

    static ItemStack.Builder autoReady(boolean enabled, CatacombsMode mode) {
        String first = mode == CatacombsMode.MASTER ? "<c><l>MM<r><c> The" : "<a>The";
        String second = mode == CatacombsMode.MASTER ? "<c>Catacombs" : "<a>Catacombs";
        return ItemStacks.item(enabled ? Material.LIME_STAINED_GLASS : Material.RED_STAINED_GLASS, """
                <a>Toggle Auto Ready Up
                <7>Auto ready up when joining %s
                %s<7> if you have the
                <7>requirements, so you don't have to!

                <7>Currently: %s

                <e>Click to toggle!""".formatted(first, second, enabled ? "<a>Enabled" : "<c>Disabled"));
    }

    static String number(double value) {
        return value == Math.rint(value) ? String.valueOf((long) value) : String.format("%.1f", value);
    }
}
