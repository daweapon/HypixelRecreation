package dev.daweapon.type.skyblockgeneric.levels;

import lombok.Getter;
import net.minestom.server.item.Material;
import dev.daweapon.commons.StringUtility;
import dev.daweapon.commons.text.Text;
import dev.daweapon.type.generic.gui.inventory.item.GUIMaterial;
import dev.daweapon.type.skyblockgeneric.levels.abstr.SkyBlockLevelCauseAbstr;
import dev.daweapon.type.skyblockgeneric.levels.causes.LevelCause;
import dev.daweapon.type.skyblockgeneric.skill.SkillCategories;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Getter
public enum SkyBlockEmblems {
    SKILLS(new GUIMaterial(Material.DIAMOND_SWORD), List.of("<7>These symbols are related to", "<7>levelling up your Skills!"), List.of(
            new SkyBlockEmblem("Mining Pickaxe", Material.STONE_PICKAXE, "<7>⸕",
                    SkyBlockLevelCause.getSkillCause(SkillCategories.MINING, 50)),
            new SkyBlockEmblem("Mining Master", Material.GOLDEN_PICKAXE, "<6>⸕",
                    SkyBlockLevelCause.getSkillCause(SkillCategories.MINING, 60)),
            new SkyBlockEmblem("Farming Flower", Material.STONE_HOE, "<7>✿",
                    SkyBlockLevelCause.getSkillCause(SkillCategories.FARMING, 50)),
            new SkyBlockEmblem("Farming Master", Material.GOLDEN_HOE, "<6>✿",
                    SkyBlockLevelCause.getSkillCause(SkillCategories.FARMING, 60)),
            new SkyBlockEmblem("Combat Explosion", Material.STONE_SWORD, "<7>❁",
                    SkyBlockLevelCause.getSkillCause(SkillCategories.COMBAT, 50)),
            new SkyBlockEmblem("Combat Master", Material.GOLDEN_SWORD, "<6>❁",
                    SkyBlockLevelCause.getSkillCause(SkillCategories.COMBAT, 60)),
            new SkyBlockEmblem("Foraging Leaf", Material.STONE_AXE, "<7>⸙",
                    SkyBlockLevelCause.getSkillCause(SkillCategories.FORAGING, 50)),
            new SkyBlockEmblem("Fishing Fish", Material.COD, "<7>α",
                    SkyBlockLevelCause.getSkillCause(SkillCategories.FISHING, 50)),
            new SkyBlockEmblem("Enchanting Pencil", Material.ENCHANTING_TABLE, "<7>✎",
                    SkyBlockLevelCause.getSkillCause(SkillCategories.ENCHANTING, 50)),
            new SkyBlockEmblem("Enchanting Master", Material.ENCHANTING_TABLE, "<6>✎",
                    SkyBlockLevelCause.getSkillCause(SkillCategories.ENCHANTING, 60)),
            //new SkyBlockEmblem("Alchemy Brew", Material.BREWING_STAND, "<7>☕",
            //        SkyBlockLevelCause.getSkillCause(SkillCategories.ALCHEMY, 50)),
            new SkyBlockEmblem("Carpentry House", Material.CRAFTING_TABLE, "<7>☖",
                    SkyBlockLevelCause.getSkillCause(SkillCategories.CARPENTRY, 50)),
            //new SkyBlockEmblem("Taming Clover", Material.WOLF_SPAWN_EGG, "<7>♣",
            //        SkyBlockLevelCause.getSkillCause(SkillCategories.TAMING, 50)),
            //new SkyBlockEmblem("Taming Master", Material.WOLF_SPAWN_EGG, "<6>♣",
            //        SkyBlockLevelCause.getSkillCause(SkillCategories.TAMING, 60)),
            new SkyBlockEmblem("Social Statement", Material.EMERALD, "<7>℻",
                    SkyBlockLevelCause.getSkillCause(SkillCategories.SOCIAL, 15)),
            new SkyBlockEmblem("Social Master", Material.EMERALD, "<6>℻",
                    SkyBlockLevelCause.getSkillCause(SkillCategories.SOCIAL, 25))
    )),
    LEVELING(new GUIMaterial("3255327dd8e90afad681a19231665bea2bd06065a09d77ac1408837f9e0b242"), List.of("<7>These symbols are unlocked by", "<7>leveling up your SkyBlock Level."), List.of(
            new SkyBlockEmblem("Diamond", Material.NAME_TAG, "<7>◆",
                    SkyBlockLevelCause.getLevelCause(10)),
            new SkyBlockEmblem("Spade", Material.NAME_TAG, "<7>♠",
                    SkyBlockLevelCause.getLevelCause(20)),
            new SkyBlockEmblem("Heart", Material.NAME_TAG, "<7>❤",
                    SkyBlockLevelCause.getLevelCause(30)),
            new SkyBlockEmblem("Pristine", Material.NAME_TAG, "<7>✧",
                    SkyBlockLevelCause.getLevelCause(40)),
            new SkyBlockEmblem("Arc Reactor", Material.NAME_TAG, "<7>⎊",
                    SkyBlockLevelCause.getLevelCause(50)),
            new SkyBlockEmblem("Marker", Material.NAME_TAG, "<7>፠",
                    SkyBlockLevelCause.getLevelCause(60)),
            new SkyBlockEmblem("Badge", Material.NAME_TAG, "<7>☬",
                    SkyBlockLevelCause.getLevelCause(70)),
            new SkyBlockEmblem("Star", Material.NAME_TAG, "<7>⚝",
                    SkyBlockLevelCause.getLevelCause(80)),
            new SkyBlockEmblem("Boxes", Material.NAME_TAG, "<7>⧉",
                    SkyBlockLevelCause.getLevelCause(90)),
            new SkyBlockEmblem("Jerry", Material.NAME_TAG, "<7>ꈔ",
                    SkyBlockLevelCause.getLevelCause(100)),
            new SkyBlockEmblem("Globe", Material.NAME_TAG, "<7>㋖",
                    SkyBlockLevelCause.getLevelCause(120)),
            new SkyBlockEmblem("Soulflow", Material.NAME_TAG, "<7>⸎",
                    SkyBlockLevelCause.getLevelCause(140)),
            new SkyBlockEmblem("Warning", Material.NAME_TAG, "<7>⚠",
                    SkyBlockLevelCause.getLevelCause(160)),
            new SkyBlockEmblem("Mustache", Material.NAME_TAG, "<7>ꕁ",
                    SkyBlockLevelCause.getLevelCause(180)),
            new SkyBlockEmblem("Helmet", Material.NAME_TAG, "<7>〠",
                    SkyBlockLevelCause.getLevelCause(200)),
            new SkyBlockEmblem("Sideways Smiley", Material.NAME_TAG, "<7>ツ",
                    SkyBlockLevelCause.getLevelCause(250)),
            new SkyBlockEmblem("Spaceship", Material.NAME_TAG, "<7>⥈",
                    SkyBlockLevelCause.getLevelCause(300)),
            new SkyBlockEmblem("Toxic", Material.NAME_TAG, "<7>☢",
                    SkyBlockLevelCause.getLevelCause(350)),
            new SkyBlockEmblem("Biohazard", Material.NAME_TAG, "<7>☣",
                    SkyBlockLevelCause.getLevelCause(400)),
            new SkyBlockEmblem("Florette", Material.NAME_TAG, "<7>✾",
                    SkyBlockLevelCause.getLevelCause(450)),
            new SkyBlockEmblem("Golden Fleur De Lis", Material.NAME_TAG, "<6>⚜",
                    SkyBlockLevelCause.getLevelCause(500))
    )),
    DUNGEONS(new GUIMaterial(Material.IRON_SWORD), List.of("<7>These symbols come from the", "<7>Catacombs and its classes."), List.of(
            new SkyBlockEmblem("Catacombs Swords", Material.IRON_SWORD, "<7>⚔", null),
            new SkyBlockEmblem("Golden Catacombs Swords", Material.GOLDEN_SWORD, "<6>⚔", null),
            new SkyBlockEmblem("Archer Spade", Material.BOW, "<7>➶", null),
            new SkyBlockEmblem("Archer Master", Material.BOW, "<6>➶", null),
            new SkyBlockEmblem("Mage Lightning", Material.BLAZE_ROD, "<7>⚡", null),
            new SkyBlockEmblem("Mage Master", Material.BLAZE_ROD, "<6>⚡", null),
            new SkyBlockEmblem("Berserk Explosion", Material.IRON_AXE, "<7>☄", null),
            new SkyBlockEmblem("Berserk Master", Material.GOLDEN_AXE, "<6>☄", null),
            new SkyBlockEmblem("Healer Staff", Material.STICK, "<7>⚚", null),
            new SkyBlockEmblem("Healer Master", Material.STICK, "<6>⚚", null),
            new SkyBlockEmblem("Tank Anchor", Material.SHIELD, "<7>⚓", null),
            new SkyBlockEmblem("Tank Master", Material.SHIELD, "<6>⚓", null),
            new SkyBlockEmblem("Dungeon Runner", Material.SKELETON_SKULL, "<7>☠", null),
            new SkyBlockEmblem("Dungeon Master", Material.WITHER_SKELETON_SKULL, "<6>☠", null),
            new SkyBlockEmblem("Class Master", Material.NETHER_STAR, "<7>⚛", null),
            new SkyBlockEmblem("Golden Class Master", Material.NETHER_STAR, "<6>⚛", null),
            new SkyBlockEmblem("Diamond Class Master", Material.NETHER_STAR, "<b>⚛", null)
    )),
    SLAYER(new GUIMaterial(Material.ZOMBIE_HEAD), List.of("<7>These symbols are earned by", "<7>defeating Slayer bosses."), List.of(
            new SkyBlockEmblem("Revenant Horror Brain", Material.ROTTEN_FLESH, "<7>ௐ", null),
            new SkyBlockEmblem("Golden Revenant Horror Brain", Material.ROTTEN_FLESH, "<6>ௐ", null),
            new SkyBlockEmblem("Broodmother String", Material.STRING, "<7>੭", null),
            new SkyBlockEmblem("Golden Broodmother String", Material.STRING, "<6>੭", null),
            new SkyBlockEmblem("Sven Shield", Material.BONE, "<7>❂", null),
            new SkyBlockEmblem("Golden Sven Shield", Material.BONE, "<6>❂", null),
            new SkyBlockEmblem("Voidgloom Rune", Material.ENDER_PEARL, "<7>ᛃ", null),
            new SkyBlockEmblem("Golden Voidgloom Rune", Material.ENDER_PEARL, "<6>ᛃ", null),
            new SkyBlockEmblem("Inferno Rods", Material.BLAZE_ROD, "<7>〣", null),
            new SkyBlockEmblem("Golden Inferno Rods", Material.BLAZE_ROD, "<6>〣", null),
            new SkyBlockEmblem("Riftstalker Strange Time", Material.CLOCK, "<7>ჶ", null),
            new SkyBlockEmblem("Golden Riftstalker Strange Time", Material.CLOCK, "<6>ჶ", null)
    )),
    MISCELLANEOUS(new GUIMaterial(Material.NETHER_STAR), List.of("<7>Symbols from all over SkyBlock,", "<7>from Harp songs to Trophy Fish."), List.of(
            new SkyBlockEmblem("Rift Time", Material.CLOCK, "<7>ф", null),
            new SkyBlockEmblem("Harp Note", Material.NOTE_BLOCK, "<7>♪", null),
            new SkyBlockEmblem("Harp Master", Material.NOTE_BLOCK, "<6>♫", null),
            new SkyBlockEmblem("Kuudra Slayer", Material.MAGMA_CREAM, "<7>Ӄ", null),
            new SkyBlockEmblem("Master Kuudra Slayer", Material.MAGMA_CREAM, "<6>Ӄ", null),
            new SkyBlockEmblem("Ultra Banker", Material.GOLD_INGOT, "<7>⛁", null),
            new SkyBlockEmblem("Extreme Banker", Material.GOLD_BLOCK, "<6>⛃", null),
            new SkyBlockEmblem("Mining Helix", Material.IRON_PICKAXE, "<7>ᛝ", null),
            new SkyBlockEmblem("Golden Mining Helix", Material.GOLDEN_PICKAXE, "<6>ᛝ", null),
            new SkyBlockEmblem("Diamond Mining Helix", Material.DIAMOND_PICKAXE, "<b>ᛝ", null),
            new SkyBlockEmblem("Foraging Clover", Material.OAK_SAPLING, "<7>☘", null),
            new SkyBlockEmblem("Golden Foraging Clover", Material.OAK_SAPLING, "<6>☘", null),
            new SkyBlockEmblem("Agatha's Jewel", Material.EMERALD, "<7>ꖘ", null),
            new SkyBlockEmblem("Gardener", Material.WHEAT, "<7>ꀾ", null),
            new SkyBlockEmblem("Golden Gardener", Material.WHEAT, "<6>ꀾ", null),
            new SkyBlockEmblem("Collecting Top Hat", Material.LEATHER_HELMET, "<7>ᱪ",
                    SkyBlockLevelCause.getMuseumMilestoneCause(12)),
            new SkyBlockEmblem("Golden Collecting Top Hat", Material.LEATHER_HELMET, "<6>ᱪ",
                    SkyBlockLevelCause.getMuseumMilestoneCause(24)),
            new SkyBlockEmblem("Magical Sigma", Material.ENCHANTED_BOOK, "<7>Σ", null),
            new SkyBlockEmblem("Golden Magical Sigma", Material.ENCHANTED_BOOK, "<6>Σ", null),
            new SkyBlockEmblem("Golden Chili Pepper", Material.RED_DYE, "<6>৫", null),
            new SkyBlockEmblem("Snowman", Material.SNOWBALL, "<7>☃", null),
            new SkyBlockEmblem("Trophy King", Material.COD, "<7>♔", null),
            new SkyBlockEmblem("Golden Trophy King", Material.COD, "<6>♔", null),
            new SkyBlockEmblem("Diamond Trophy King", Material.COD, "<b>♔", null),
            new SkyBlockEmblem("Trophy Frog King", Material.LILY_PAD, "<7>🐸", null),
            new SkyBlockEmblem("Golden Trophy Frog King", Material.LILY_PAD, "<6>🐸", null),
            new SkyBlockEmblem("Diamond Trophy Frog King", Material.LILY_PAD, "<b>🐸", null),
            new SkyBlockEmblem("Century Celebrant", Material.CAKE, "<7>⛂", null),
            new SkyBlockEmblem("Century Partygoer", Material.CAKE, "<6>⛂", null),
            new SkyBlockEmblem("Sparkling Critter Catcher", Material.GLOW_BERRIES, "<7>✨", null),
            new SkyBlockEmblem("Great Spook", Material.SOUL_LANTERN, "<7>࿇", null),
            new SkyBlockEmblem("Gift of Giving", Material.CHEST, "<7>✉", null),
            new SkyBlockEmblem("Jacob's Contest Completionist", Material.GOLDEN_HOE, "<7>ꕪ", null),
            new SkyBlockEmblem("Golden Jacob's Contest Completionist", Material.GOLDEN_HOE, "<6>ꕪ", null),
            new SkyBlockEmblem("Diamond Jacob's Contest Completionist", Material.DIAMOND_HOE, "<b>ꕪ", null),
            new SkyBlockEmblem("Chocolate Bar", Material.COCOA_BEANS, "<7>▒", null),
            new SkyBlockEmblem("Golden Chocolate Bar", Material.COCOA_BEANS, "<6>▒", null),
            new SkyBlockEmblem("Emblem of Burning Strength", Material.BLAZE_POWDER, "<7>☀", null)
    )),
    SUBSCRIPTION(new GUIMaterial(Material.NETHER_STAR), List.of("<7>These symbols are granted by", "<7>an active MVP++ subscription."), List.of(
            new SkyBlockEmblem("1-Month Subscriber's Star", Material.NETHER_STAR, "<7>✩", null),
            new SkyBlockEmblem("2-Month Subscriber's Star", Material.NETHER_STAR, "<7>✬", null),
            new SkyBlockEmblem("3-Month Subscriber's Star", Material.NETHER_STAR, "<7>✮", null),
            new SkyBlockEmblem("6-Month Subscriber's Star", Material.NETHER_STAR, "<e>✯", null),
            new SkyBlockEmblem("9-Month Subscriber's Star", Material.NETHER_STAR, "<e>✶", null),
            new SkyBlockEmblem("12-Month Subscriber's Star", Material.NETHER_STAR, "<6>✳", null),
            new SkyBlockEmblem("18-Month Subscriber's Star", Material.NETHER_STAR, "<6>✴", null),
            new SkyBlockEmblem("24-Month Subscriber's Star", Material.NETHER_STAR, "<d>✷", null),
            new SkyBlockEmblem("36-Month Subscriber's Star", Material.NETHER_STAR, "<d>✸", null),
            new SkyBlockEmblem("48-Month Subscriber's Star", Material.NETHER_STAR, "<b>✹", null),
            new SkyBlockEmblem("60-Month Subscriber's Star", Material.NETHER_STAR, "<b>✺", null)
    )),
    SPECIAL(new GUIMaterial(Material.DRAGON_EGG), List.of("<7>Rare symbols from events and", "<7>limited-time occasions."), List.of(
            new SkyBlockEmblem("Hypixel", Material.DRAGON_EGG, "<c>ዞ", null),
            new SkyBlockEmblem("Century Raffle Green", Material.GREEN_DYE, "<a>⛃", null),
            new SkyBlockEmblem("Century Raffle Yellow", Material.YELLOW_DYE, "<e>⛃", null),
            new SkyBlockEmblem("Century Raffle Red", Material.RED_DYE, "<c>⛃", null),
            new SkyBlockEmblem("Century Raffle Pink", Material.PINK_DYE, "<d>⛃", null),
            new SkyBlockEmblem("Century Raffle Blue", Material.BLUE_DYE, "<9>⛃", null),
            new SkyBlockEmblem("Discord", Material.PAPER, "<9>⌬", null)
    ));


    private final GUIMaterial guiMaterial;
    @Getter(lombok.AccessLevel.NONE)
    private final List<String> description;
    private final List<SkyBlockEmblem> emblems;

    public List<Text> getDescription() {
        return description.stream().map(Text::of).toList();
    }

    SkyBlockEmblems(GUIMaterial guiMaterial, List<String> description, List<SkyBlockEmblem> emblems) {
        this.guiMaterial = guiMaterial;
        this.description = description;
        this.emblems = emblems;
    }

    public int amountUnlocked(SkyBlockPlayer player) {
        int amount = 0;
        for (SkyBlockEmblem emblem : emblems) {
            if (player.hasUnlockedXPCause(emblem.cause())) {
                amount++;
            }
        }
        return amount;
    }

    @Override
    public String toString() {
        return StringUtility.toNormalCase(name());
    }

    public static List<SkyBlockEmblem> getEmblemsWithLevelCause() {
        List<SkyBlockEmblem> emblems = new ArrayList<>();
        Arrays.stream(values()).forEach(emblem -> {
            emblem.getEmblems().stream().filter(emblem1 -> emblem1.cause() instanceof LevelCause).forEach(emblems::add);
        });

        return emblems;
    }

    public static SkyBlockEmblems getCategoryFromEmblem(SkyBlockEmblem emblem) {
        for (SkyBlockEmblems emblems : values()) {
            for (SkyBlockEmblem emblem1 : emblems.emblems) {
                if (emblem1.equals(emblem)) {
                    return emblems;
                }
            }
        }
        return null;
    }

    public record SkyBlockEmblem(String displayName, Material displayMaterial, String emblemMarkup, SkyBlockLevelCauseAbstr cause) {
        public Text emblem() {
            return Text.of(emblemMarkup);
        }
    }
}
