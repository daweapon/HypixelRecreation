package dev.daweapon.type.skyblockgeneric.warps;

import lombok.AccessLevel;
import lombok.Getter;
import net.minestom.server.coordinate.Pos;
import dev.daweapon.commons.text.Text;
import dev.daweapon.type.generic.user.categories.Rank;
import dev.daweapon.type.skyblockgeneric.warps.unlocks.ScrollUnlockCustomRecipe;
import dev.daweapon.type.skyblockgeneric.warps.unlocks.ScrollUnlockDarkAuction;
import dev.daweapon.type.skyblockgeneric.warps.unlocks.ScrollUnlockFound;
import dev.daweapon.type.skyblockgeneric.warps.unlocks.ScrollUnlockPurchase;
import dev.daweapon.type.skyblockgeneric.warps.unlocks.ScrollUnlockReward;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@Getter
public enum TravelScrollType {
    HUB_CASTLE("castle", "<b>Hub <7>- <b>Castle",
            "Spawn near the ruined castle of the past in the hub.",
            Textures.HUB,
            new ScrollUnlockPurchase("Lonely Philosopher"),
            new Pos(-250, 130, 45, -90, 0),
            Rank.MVP_PLUS),
    HUB_MUSEUM("museum", "<b>Hub <7>- <b>Museum",
            "Spawn in the <9>Museum <7>at the foot of the <b>mountain <7>in the hub.",
            Textures.HUB,
            new ScrollUnlockCustomRecipe("Unlock this scroll's recipe through Museum Rewards."),
            new Pos(29.5, 72, 1.5, 90, 0),
            Rank.DEFAULT),
    HUB_DARK_AUCTION("da", "<b>Hub <7>- <5>Dark Auction",
            "Spawn in front of <5>Sirius<7>, the Dark Auction master.",
            "67ae3e2539a442518130370d3b2d3b1837a491ee23d5aff20f015ae284a28c90",
            new ScrollUnlockDarkAuction(),
            new Pos(93.2, 75, 172.8, 22, 0),
            Rank.DEFAULT),
    HUB_WIZARD_TOWER("wizard", "<b>Hub <7>- <9>Wizard Tower",
            "Spawn at the top of the <9>Wizard Tower <7>in the hub.",
            Textures.HUB,
            new ScrollUnlockPurchase("Grumblefoot"),
            new Pos(47.5, 119, 95.5, 0, 0)),
    HUB_CRYPTS("crypts", "<b>Hub <7>- <b>Crypts",
            "Spawn in the <6>crypts <7>below the <c>graveyard <7>in the hub.",
            Textures.HUB,
            new ScrollUnlockCustomRecipe("Unlock this scroll's recipe through Recipe Unlock."),
            new Pos(-160.5, 62, -105.5, -90, 0),
            Rank.MVP_PLUS),
    HUB_ELIZABETH("elizabeth", "<b>Hub <7>- <6>Elizabeth",
            "Spawn next to <6>Elizabeth <7>in the <6>Community Center<7>.",
            Textures.HUB,
            new ScrollUnlockReward("Ask Elizabeth about the Community Center."),
            new Pos(-3.5, 79, 16.5, 145, 0)),
    HUB_TRADE_CENTER("stonks", "<b>Hub <7>- <6>Trade Center",
            "Spawn in the <6>Trade Center <7>next to the <6>Bazaar<7>.",
            Textures.HUB,
            new ScrollUnlockReward("Meet the traders of the Trade Center."),
            new Pos(-36.5, 70, -81.5, 0, 0)),
    HUB_TAYLOR("taylor", "<b>Hub <7>- <b>Taylor's Shop",
            "Spawn in front of <b>Taylor's <7>clothing shop.",
            Textures.HUB,
            new ScrollUnlockReward("Become a regular at Taylor's shop."),
            new Pos(29.5, 73, -41.5, 135, 0)),
    FARMING_MUSHROOM_DESERT("desert", "<a>Farming Islands <7>- <6>Mushroom Desert",
            "Spawn in the <6>Mushroom Desert<7>.",
            Textures.BARN,
            new ScrollUnlockCustomRecipe("Unlock this scroll's recipe through the Cocoa Beans Collection."),
            new Pos(160.5, 77, -369.5, 0, 0)),
    FARMING_TRAPPERS_DEN("trapper", "<a>Farming Islands <7>- <6>Trapper's Den",
            "Spawn at the <6>Trapper's Den<7>.",
            Textures.RABBIT,
            new ScrollUnlockPurchase("Talbot"),
            new Pos(281.5, 104, -546.5, 0, 0)),
    FARMING_GLOWING_MUSHROOM_CAVE("glowing", "<a>Farming Islands <7>- <3>Glowing Mushroom Cave",
            "Spawn in the <3>Glowing Mushroom Cave<7>.",
            Textures.BARN,
            new ScrollUnlockPurchase("Moby"),
            new Pos(206.5, 42, -514.5, 0, 0)),
    THE_PARK("park", "<a>Foraging <7>- <a>Travel Scroll I",
            "Spawn in the Birch Park",
            Textures.PARK,
            new ScrollUnlockCustomRecipe("Unlock this scroll's recipe through Foraging Skill Rewards."),
            new Pos(-265.5, 79, -17.5, 90, 0),
            Rank.DEFAULT),
    PARK_HOWLING_CAVE("howl", "<a>The Park <7>- <b>Howling Cave",
            "Spawn in the <b>Howling Cave <7>beneath the <a>Birch Park<7>.",
            Textures.WOLF,
            new ScrollUnlockFound("The Park"),
            new Pos(-372.5, 84, -60.5, -153, 0),
            Rank.MVP_PLUS),
    PARK_JUNGLE_ISLAND("jungle", "<a>The Park <7>- <2>Jungle Island",
            "Spawn on the <2>Jungle Island<7>.",
            Textures.OCELOT,
            new ScrollUnlockPurchase("Joyful Viking"),
            new Pos(-462.5, 133, -127.5, 0, 0),
            Rank.MVP_PLUS),
    DWARVEN_FORGE("forge", "<a>Dwarven Mines <7>- <6>The Forge",
            "Spawn next to the <6>Dwarven Forge<7>.",
            Textures.MITHRIL_GOLEM,
            new ScrollUnlockCustomRecipe("Forge this scroll at the Dwarven Forge."),
            new Pos(0.5, 149, -67.5, 0, 0)),
    DWARVEN_BASE_CAMP("basecamp", "<a>Dwarven Mines <7>- <b>Dwarven Base Camp",
            "Spawn at the <b>Dwarven Base Camp <7>at the edge of the <b>Glacite Tunnels<7>.",
            Textures.GLACITE_GOLEM,
            new ScrollUnlockCustomRecipe("Forge this scroll at the Dwarven Forge."),
            new Pos(0.5, 128, 200.5, 0, 0)),
    CRYSTAL_NUCLEUS("nucleus", "<a>Crystal Hollows <7>- <d>Crystal Nucleus",
            "Spawn at the <d>Crystal Nucleus <7>in the heart of the <d>Crystal Hollows<7>.",
            Textures.SCATHA,
            new ScrollUnlockReward("Reach Commission Milestone VI."),
            new Pos(513.5, 106, 526.5, 0, 0)),
    SPIDERS_DEN_NEST("nest", "<a>Spider's Den <7>- <c>Top of the Nest",
            "Spawn at the <c>top of the nest <7>on the <c>Spider Mound<7>.",
            Textures.SPIDER,
            new ScrollUnlockFound("Spider's Den"),
            new Pos(-188.5, 176, -309.5, 0, 0),
            Rank.MVP_PLUS),
    SPIDERS_DEN_ARACHNE("arachne", "<a>Spider's Den <7>- <5>Arachne's Sanctuary",
            "Spawn at the entrance of <5>Arachne's Sanctuary<7>.",
            Textures.TARANTULA,
            new ScrollUnlockPurchase("Spider Tamer"),
            new Pos(-281.5, 47, -197.5, 0, 0),
            Rank.MVP_PLUS),
    THE_END_DRAGONS_NEST("drag", "<a>The End <7>- <5>Dragon's Nest",
            "Spawn in the <5>Dragon's Nest<7>, where the dragons are summoned.",
            Textures.ENDER_DRAGON,
            new ScrollUnlockFound("The End"),
            new Pos(-606.5, 22, -275.5, 0, 0),
            Rank.MVP_PLUS),
    THE_END_VOID_SEPULTURE("void", "<a>The End <7>- <5>Void Sepulture",
            "Spawn in the <5>Void Sepulture<7>.",
            Textures.ENDERMAN,
            new ScrollUnlockCustomRecipe("Unlock this scroll's recipe from Enderman Slayer LVL 4."),
            new Pos(-569.5, 7, -317.5, 0, 0),
            Rank.MVP_PLUS),
    CRIMSON_ISLE_SPAWN("nether", "<a>Crimson Isle <7>- <b>Spawn",
            "Fight challenging bosses, discover new Sea Creatures, complete epic quests, and join your favorite faction!",
            Textures.CRIMSON_PORTAL,
            new ScrollUnlockCustomRecipe("Unlock this scroll's recipe through the Glowstone Dust Collection."),
            new Pos(-360.5, 80, -430.5, 180, 0)),
    CRIMSON_ISLE_FORGOTTEN_SKULL("kuudra", "<a>Crimson Isle <7>- <6>Forgotten Skull",
            "Spawn at the entrance of the <c>Kuudra <7>arena behind the <6>Blazing Volcano<7>.",
            Textures.CRIMSON_PORTAL,
            new ScrollUnlockFound("Crimson Isle"),
            new Pos(-372.5, 117, -981.5, 180, 0),
            Rank.MVP_PLUS),
    CRIMSON_ISLE_WASTELAND("wasteland", "<a>Crimson Isle <7>- <c>The Wasteland",
            "Spawn in <c>The Wasteland<7>.",
            Textures.CRIMSON_PORTAL,
            new ScrollUnlockFound("Crimson Isle"),
            new Pos(-368.5, 117, -975.5, 180, 0),
            Rank.MVP_PLUS),
    CRIMSON_ISLE_DRAGONTAIL("dragontail", "<a>Crimson Isle <7>- <2>Dragontail",
            "Spawn in the center of <2>Dragontail<7>.",
            Textures.CRIMSON_PORTAL,
            new ScrollUnlockFound("Crimson Isle"),
            new Pos(-614.5, 100, -806.5, 0, 0)),
    CRIMSON_ISLE_SCARLETON("scarleton", "<a>Crimson Isle <7>- <5>Scarleton",
            "Spawn in to the center of <5>Scarleton<7>.",
            Textures.CRIMSON_PORTAL,
            new ScrollUnlockFound("Crimson Isle"),
            new Pos(-60.5, 107, -801.5, 0, 0)),
    CRIMSON_ISLE_SMOLDERING_TOMB("smold", "<a>Crimson Isle <7>- <c>Smoldering Tomb",
            "Spawn in front of the <c>Smoldering Tomb <7>full of blazes.",
            Textures.CRIMSON_PORTAL,
            new ScrollUnlockCustomRecipe("Unlock this scroll's recipe from Blaze Slayer LVL 4."),
            new Pos(-283.5, 108, -1030.5, 180, 0),
            Rank.MVP_PLUS),
    MOONGLADE_MURKWATER_LOCH("murkwater", "<a>Moonglade Marsh <7>- <3>Murkwater Loch",
            "Spawn on the shore of <3>Murkwater Loch<7>.",
            Textures.SQUID,
            new ScrollUnlockCustomRecipe("Unlock this scroll's recipe through the Mangrove Log Collection."),
            new Pos(-644.5, 90, 2.5, 0, 0)),
    TORRHUS_SPRINGS("springs", "<a>Torrhus Canyon <7>- <b>Torrhus Springs",
            "Spawn at the <b>Torrhus Springs<7>.",
            Textures.SLOTH,
            new ScrollUnlockCustomRecipe("Unlock this scroll's recipe through the Ruby Veilshroom Collection."),
            new Pos(-634.5, 161, 162.5, 0, 0)),
    TORRHUS_CRITTER_SAFARI("safari", "<a>Torrhus Canyon <7>- <a>Critter Safari Entrance",
            "Spawn at the entrance of the <a>Critter Safari<7>.",
            Textures.SLOTH,
            new ScrollUnlockReward("Reach Chapter 3 of the Torrhus Canyon story."),
            new Pos(-720.5, 94, 141.5, 0, 0))
    ;

    static final class Textures {
        static final String HUB = "f4559d75464b2e40a518e4de8e6cf3085f0a3ca0b1b7012614c4cd96fed60378";
        static final String CRIMSON_PORTAL = "64457b2d7acf522d1912f0d6c6139440ba9d333a6bf61a2e9440e7fb3788939e";
        static final String PARK = "a221f813dacee0fef8c59f76894dbb26415478d9ddfc44c2e708a6d3b7549b";
        static final String BARN = "621668ef7cb79dd9c22ce3d1f3f4cb6e2559893b6df4a469514e667c16aa4";
        static final String RABBIT = "63438555e899bd9a051a95dbea49eb2ecfa52a69dbba8998f3673819e277fdf5";
        static final String WOLF = "81483105ab8c33f04c97c7493c9871c8a51cc49df6d9b5e23b26652094bf3030";
        static final String OCELOT = "5657cd5c2989ff97570fec4ddcdc6926a68a3393250c1be1f0b114a1db1";
        static final String MITHRIL_GOLEM = "c1b2dfe8ed5dffc5b1687bc1c249c39de2d8a6c3d90305c95f6d1a1a330a0b1";
        static final String GLACITE_GOLEM = "af132a6593876d3c377d503fd66eca3fb938743251f7b16a9870c60b7388c8a3";
        static final String SCATHA = "df03ad96092f3f789902436709cdf69de6b727c121b3c2daef9ffa1ccaed186c";
        static final String SPIDER = "cd541541daaff50896cd258bdbdd4cf80c3ba816735726078bfe393927e57f1";
        static final String TARANTULA = "b81b5cf3a65bf9cab4bca55781721a86d567d9e106551647f35b715edf00a13f";
        static final String ENDER_DRAGON = "aec3ff563290b13ff3bcc36898af7eaa988b6cc18dc254147f58374afe9b21b9";
        static final String ENDERMAN = "9f31c9b5a2bfe7443645b4e7e8ea408b04d554a9200b00afd574ce924b555ecb";
        static final String WITHER_SKELETON = "d50e972d26c3dbc4e1663aaf583388ff321e958d195f8a465b71a596a736fe9c";
        static final String JERRY = "822d8e751c8f2fd4c8942c44bdb2f5ca4d8ae8e575ed3eb34c18a86e93b";
        static final String HERMIT_CRAB = "26629dfa3fdfef04054024e0156d5e19da5401b1911f59b4bd3982685fe54c2c";
        static final String FROG = "45852a95928897746012988fbd5dbaa1b70b7a5fb65157016f4ff3f245374c08";
        static final String SQUID = "01433be242366af126da434b8735df1eb5b3cb2cede39145974e9c483607bac";
        static final String ROSE_DRAGON = "9b7c3de075a2bb238ef51431206b10d586cb2a5b1cc41fe851cc5f0b02d357c7";
        static final String RIFT_FERRET = "b6b11399448260185da1d17e54c984515faab6d8585f00972451ec2b43d46f94";
        static final String SLOTH = "5468537e1e99040b760c83e53096c86870cdcd9285d53300761d0e36ff9e192f";
        static final String TURTLE = "242f86b51c8992b3758e85fe35bb2a20399ad4685095466e7feb54c6902fad8e";
    }

    private final @NotNull String internalName;
    @Getter(AccessLevel.NONE)
    private final @NotNull String displayName;
    @Getter(AccessLevel.NONE)
    private final @NotNull String description;
    private final @NotNull String headTexture;
    private final @NotNull ScrollUnlockReason unlockReason;
    private final @NotNull Pos location;
    private final @NotNull Rank requiredRank;

    TravelScrollType(@NotNull String internalName, @NotNull String displayName, @NotNull String description,
                     @NotNull String headTexture, @NotNull ScrollUnlockReason unlockReason, @NotNull Pos location) {
        this.internalName = internalName;
        this.displayName = displayName;
        this.description = description;
        this.headTexture = headTexture;
        this.unlockReason = unlockReason;
        this.location = location;
        this.requiredRank = Rank.DEFAULT;
    }

    TravelScrollType(@NotNull String internalName, @NotNull String displayName, @NotNull String description,
                     @NotNull String headTexture, @NotNull ScrollUnlockReason unlockReason, @NotNull Pos location,
                     @NotNull Rank requiredRank) {
        this.internalName = internalName;
        this.displayName = displayName;
        this.description = description;
        this.headTexture = headTexture;
        this.unlockReason = unlockReason;
        this.location = location;
        this.requiredRank = requiredRank;
    }

    public @NotNull Text getDisplayName() {
        return Text.of(displayName);
    }

    public @NotNull Text getDescription() {
        return Text.of(description);
    }

    public static @Nullable TravelScrollType getFromInternalName(String internalName) {
        for (TravelScrollType scroll : values()) {
            if (scroll.getInternalName().equalsIgnoreCase(internalName)) {
                return scroll;
            }
        }
        return null;
    }
}
