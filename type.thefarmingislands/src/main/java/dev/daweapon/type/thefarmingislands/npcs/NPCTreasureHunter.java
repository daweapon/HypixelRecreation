package dev.daweapon.type.thefarmingislands.npcs;

import dev.daweapon.commons.StringUtility;
import dev.daweapon.type.generic.entity.npc.NPCOption;
import dev.daweapon.type.skyblockgeneric.hiker.HikerQuest;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;
import dev.daweapon.type.thefarmingislands.treasurehunter.TreasureHunter;
import dev.daweapon.type.thefarmingislands.treasurehunter.TreasureHunterConfig;
import net.kyori.adventure.text.format.NamedTextColor;
import java.util.List;
import java.util.function.Consumer;
import net.minestom.server.coordinate.Pos;
import dev.daweapon.type.generic.user.HypixelPlayer;
import dev.daweapon.type.generic.entity.npc.HypixelNPC;
import dev.daweapon.type.generic.entity.npc.configuration.HumanConfiguration;

import dev.daweapon.type.generic.event.custom.NPCInteractEvent;

public class NPCTreasureHunter extends HypixelNPC {
    public NPCTreasureHunter() {
        super(new HumanConfiguration() {
            @Override
            public String[] holograms(HypixelPlayer player) {
                return new String[]{"Treasure Hunter", "<e><l>CLICK"};
            }

            @Override
            public String signature(HypixelPlayer player) {
                return "WJ2wuOWVJxm0DEAl9dymzo2eC8TjjxjvS0c7BuCDepoCQhv/KIZcl0FXD+ybQGeNp5NDq7NsZ0NNFoKZRcOJq/5PUuOgDQx/0JGCERxeiNRjztMxJb160H+RHPaP9qmo9fXyl4TsmjBOL33Jol88U/D2O7eUDN7694cj5MZMw4pnZ1Nj18O3aZu/zVppultNEDCJtPTAmKZcWeou32ITrXn/NJPm82kscK2gMjzqgExM6pud7aWCdOkaPYPvX8LQIL66ZIxufS2E4bqGBRR4QARDLSn55JtWhQel8alAXoh9e4cOe3i+82SjXTbrJCuuyiHSQBPIhkPfX3x5N5eu9+GtuPURotjGYfR36CiuKotmJFhNw1EDHH0U6RoXwFM4NZ+WbJJ6xE+VQirTGv4pUKp9hf6Vg6Newqf/f6gzv3DPxwGgTHt+XiUWXu4d9n5UhLocp69m5FJQHuTDpJyzVBR1vxSjsr0S4Si1zk0yDyKITovY6/i84WAnk6bl3SPWWz8Nd7B3K+RnmqCLo4TBSJZeAzbqLDNmGPGlxsEmXwp52MoGJZcMpAHqmnDzNQf6IsM/eWRDlt9s34ZE/DkzWWGmqR9z4Koe1eD+gcofKAoJNFHyR4+4St1+Evn1mxkY79mdFWNC2oLGwBTBD17WOi9Wp79Z5H9CRrR3ZbTqGx4=";
            }

            @Override
            public String texture(HypixelPlayer player) {
                return "ewogICJ0aW1lc3RhbXAiIDogMTYxMTg3OTY1ODUxNSwKICAicHJvZmlsZUlkIiA6ICJkZGVkNTZlMWVmOGI0MGZlOGFkMTYyOTIwZjdhZWNkYSIsCiAgInByb2ZpbGVOYW1lIiA6ICJEaXNjb3JkQXBwIiwKICAic2lnbmF0dXJlUmVxdWlyZWQiIDogdHJ1ZSwKICAidGV4dHVyZXMiIDogewogICAgIlNLSU4iIDogewogICAgICAidXJsIiA6ICJodHRwOi8vdGV4dHVyZXMubWluZWNyYWZ0Lm5ldC90ZXh0dXJlL2VlYWY0M2M3NDc0OTdlMjBmZTA2NjdkNjc4MTBhYTFmM2NiMGFlYjk2ZmNjOTRiN2NlMThmNWQxNjVjODI4NGQiCiAgICB9CiAgfQp9";
            }

            @Override
            public Pos position(HypixelPlayer player) {
                return new Pos(200.5, 92, -437, 0, 0);
            }

            @Override
            public boolean looking(HypixelPlayer player) {
                return true;
            }
        });
    }

    @Override
    public void onClick(NPCInteractEvent e) {
        SkyBlockPlayer player = (SkyBlockPlayer) e.player();
        TreasureHunterConfig.Hint hint = TreasureHunter.activeHint(player);
        if (hint != null) {
            sendNPCMessage(player, "Did you forget the tip?");
            sendNPCMessage(player, "Just for you I'll tell you it again at no charge!");
            sendNPCMessage(player, hint.text());
            sendNPCMessage(player, "Remember, you need to DIG to find the treasure!");
            return;
        }
        long cooldown = TreasureHunter.millisOnCooldown(player, System.currentTimeMillis());
        if (cooldown > 0) {
            sendNPCMessage(player, "Sorry I dont have any more info.");
            sendNPCMessage(player, "Come back in <b>{}<f> SkyBlock days and I might know something new.", Math.max(1, Math.ceilDiv(cooldown, HikerQuest.SKYBLOCK_DAY_MILLIS)));
            return;
        }
        int tierIndex = TreasureHunter.rollTierIndex(player);
        int fee = TreasureHunterConfig.get().tiers().get(tierIndex).fee();
        sendNPCMessage(player, "Hello adventurer!");
        sendNPCMessage(player, "I'm the Treasure Hunter, I've been searching for treasure all over this island.");
        sendNPCMessage(player, "Rumor has it that there are rare stones and other valuable bounty hidden in the ground all over the island.");
        sendNPCMessage(player, "Tell you what, for a small price I'll give you the info I know about.");
        player.sendMessage("<b><l>Pay {} coins for the info?", StringUtility.commaify(fee));
        NPCOption.sendOption(player, "treasure_hunter", true, List.of(
                option("yes", NamedTextColor.GREEN, "YES", () -> accept(player, tierIndex)),
                option("no", NamedTextColor.RED, "NO", () -> sendNPCMessage(player, "If you change your mind, you know where to find me!"))));
    }

    private void accept(SkyBlockPlayer player, int tierIndex) {
        if (TreasureHunter.activeHint(player) != null || TreasureHunter.millisOnCooldown(player, System.currentTimeMillis()) > 0) return;
        if (!TreasureHunter.buyHint(player, tierIndex)) {
            sendNPCMessage(player, "Are you trying to scam me?");
            sendNPCMessage(player, "You don't have enough money.");
            sendNPCMessage(player, "Come back when you have enough for the tip.");
            return;
        }
        sendNPCMessage(player, TreasureHunter.activeHint(player).text());
    }

    private static NPCOption.Option option(String key, NamedTextColor colour, String label, Runnable action) {
        Consumer<HypixelPlayer> consumer = _ -> action.run();
        return new NPCOption.Option(key, colour, true, label, consumer);
    }
}
