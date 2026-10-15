package dev.daweapon.type.skyblockgeneric.levels;

import lombok.Getter;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import dev.daweapon.commons.skyblock.item.ItemType;
import dev.daweapon.commons.text.Text;
import dev.daweapon.type.generic.gui.v2.View;
import dev.daweapon.type.generic.gui.inventory.ItemStacks;
import dev.daweapon.type.skyblockgeneric.gui.inventories.sbmenu.levels.starter.GUIStarterAccessories;
import dev.daweapon.type.skyblockgeneric.gui.inventories.sbmenu.levels.starter.GUIStarterSkills;
import dev.daweapon.type.skyblockgeneric.levels.abstr.SkyBlockLevelCauseAbstr;
import dev.daweapon.type.skyblockgeneric.skill.SkillCategories;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

@Getter
public enum LevelsGuide {
    STARTER(Text.of("<8>New Player"), Text.of("<7>You are starting on your journey through SkyBlock. Complete these tasks to get acquainted with the game"),
            Material.LIME_STAINED_GLASS_PANE, List.of(
            TasksSet.builder(ItemStacks.item(Material.DIAMOND_SWORD, 1, "Skills"), new GUIStarterSkills(), (player) -> List.of(
                    "<7>Level up your Skills.",
                    " ",
                    player.getSkills().getCurrentLevel(SkillCategories.FARMING) >= 4 ? "<a>✔ <8>Farming Skill IV" : "<c>✖ <f>Farming Skill IV",
                    player.getSkills().getCurrentLevel(SkillCategories.MINING) >= 4 ? "<a>✔ <8>Mining Skill IV" : "<c>✖ <f>Mining Skill IV",
                    player.getSkills().getCurrentLevel(SkillCategories.COMBAT) >= 4 ? "<a>✔ <8>Combat Skill IV" : "<c>✖ <f>Combat Skill IV",
                    player.getSkills().getCurrentLevel(SkillCategories.FORAGING) >= 4 ? "<a>✔ <8>Foraging Skill IV" : "<c>✖ <f>Foraging Skill IV",
                    player.getSkills().getCurrentLevel(SkillCategories.FISHING) >= 4 ? "<a>✔ <8>Fishing Skill IV" : "<c>✖ <f>Fishing Skill IV",
                    player.getSkills().getCurrentLevel(SkillCategories.ENCHANTING) >= 4 ? "<a>✔ <8>Enchanting Skill IV" : "<c>✖ <f>Enchanting Skill IV"
                    ))
                    .cause(SkyBlockLevelCause.getSkillCauses(SkillCategories.COMBAT, 6), null)
                    .cause(SkyBlockLevelCause.getSkillCause(SkillCategories.COMBAT, 7), "Combat Skill IV")
                    .build(),
            TasksSet.builder(ItemStacks.head("1a11a7f11bcd5784903c5201d08261c4df8379109d6e611c1cd3ededf031afed", "Accessories"), new GUIStarterAccessories(), (player) -> List.of(
                    "<7>Obtain unique <a>Accessories <7>in your",
                    "<a>Accessory Bag<7>."
                    ))
                    .cause(SkyBlockLevelCause.getAccessoryCause(ItemType.FARMING_TALISMAN), null)
                    .cause(SkyBlockLevelCause.getAccessoryCause(ItemType.ZOMBIE_TALISMAN), null)
                    .cause(SkyBlockLevelCause.getAccessoryCause(ItemType.SKELETON_TALISMAN), null)
                    .cause(SkyBlockLevelCause.getAccessoryCause(ItemType.VILLAGE_AFFINITY_TALISMAN), null)
                    .cause(SkyBlockLevelCause.getAccessoryCause(ItemType.MINE_AFFINITY_TALISMAN), null)
                    .build()
    )),
    AMATEUR(Text.of("<8>Very Early Game"), Text.of("<7>Looks like you're getting the hang of things!"),
            Material.YELLOW_STAINED_GLASS_PANE, List.of()),
    INTERMEDIATE(Text.of("<8>Early Game"), Text.of("<7>You have successfully navigated the base layer of SkyBlock."),
            Material.ORANGE_STAINED_GLASS_PANE, List.of()),
    SKILLED(Text.of("<8>Mid Game"), Text.of("<7>Since you've made it this far, it's clear that you know your way around SkyBlock."),
            Material.RED_STAINED_GLASS_PANE, List.of()),
    EXPERT(Text.of("<8>Late Game"), Text.of("<7>You have made significant progress and should be proud of your accomplishments."),
            Material.PURPLE_STAINED_GLASS_PANE, List.of()),
    PROFESSIONAL(Text.of("<8>Very Late Game"), Text.of("<7>You're better at this than most, but never forget your roots."),
            Material.BLUE_STAINED_GLASS_PANE, List.of()),
    MASTER(Text.of("<8>End Game"), Text.of("<7>Why are you still playing this game? To check these boxes, that's why!"),
            Material.BLACK_STAINED_GLASS_PANE, List.of()),
    ;

    private final Text title;
    private final Text description;
    private final Material glassMaterial;
    private final List<TasksSet> tasksSets;

    LevelsGuide(Text title, Text description, Material glassMaterial, List<TasksSet> tasksSets) {
        this.title = title;
        this.description = description;
        this.glassMaterial = glassMaterial;
        this.tasksSets = tasksSets;
    }

    @Getter
    public static class TasksSet {
        private Map<SkyBlockLevelCauseAbstr, String> causes;
        private ItemStack.Builder material;
        private View<?> guiToOpen;
        private Function<SkyBlockPlayer, List<String>> display;

        public static Builder builder(ItemStack.Builder material, View<?> guiToOpen, Function<SkyBlockPlayer, List<String>> display) {
            return new Builder(material, guiToOpen, display);
        }

        public static class Builder {
            private final Map<SkyBlockLevelCauseAbstr, String> causes = new HashMap<>();
            private final ItemStack.Builder material;
            private final View<?> guiToOpen;
            private final Function<SkyBlockPlayer, List<String>> display;

            public Builder(ItemStack.Builder material, View<?> guiToOpen, Function<SkyBlockPlayer, List<String>> display) {
                this.material = material;
                this.guiToOpen = guiToOpen;
                this.display = display;
            }

            public Builder cause(SkyBlockLevelCauseAbstr cause, @Nullable String display) {
                causes.put(cause, display);
                return this;
            }

            public Builder cause(List<SkyBlockLevelCauseAbstr> cause, @Nullable String display) {
                cause.forEach(c -> causes.put(c, display));
                return this;
            }

            public TasksSet build() {
                TasksSet tasksSet = new TasksSet();
                tasksSet.causes = causes;
                tasksSet.material = material;
                tasksSet.guiToOpen = guiToOpen;
                tasksSet.display = display;
                return tasksSet;
            }
        }
    }
}
