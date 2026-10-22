package dev.daweapon.type.thefarmingislands.npcs;

import dev.daweapon.type.skyblockgeneric.hiker.HikerQuest;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;
import net.minestom.server.coordinate.Pos;
import dev.daweapon.type.generic.user.HypixelPlayer;
import dev.daweapon.type.generic.entity.npc.HypixelNPC;
import dev.daweapon.type.generic.entity.npc.configuration.HumanConfiguration;

import dev.daweapon.type.generic.event.custom.NPCInteractEvent;

public class NPCFriendlyHiker extends HypixelNPC {
    public NPCFriendlyHiker() {
        super(new HumanConfiguration() {
            @Override
            public String[] holograms(HypixelPlayer player) {
                return new String[]{"Friendly Hiker", "<e><l>CLICK"};
            }

            @Override
            public String signature(HypixelPlayer player) {
                return "GS3VmWn1ruRqyNcbbLP4EKdKrRQAtVAKflnqNuw7+VK+gKFmyF8om2n0hvnndEGsh1D46DxEQgclih35lzzUn+CrnwFnZZm+fbKZkAG4SYeZzJMb/zqyWu18xHHXmjsHoi+jT4tM1osLHGfQ1CEnV0GL2t+VwQv4p1EgUSxrN6lU4WI4eaUzYMQlq/lT+KcgyaMyxP3cAhqYQiDnwv3LyNJzK1sBocxzXFwaYzbPr2YlkQuqFGbEOY9zRLbIJiZB8wQvT6yMtivAEQl7Rhi+VF/aqn0+ZAHU+GIebMsuQk6DkOvgbjSWDd6P8qBcqSy9+EWiAolyeIJ25MEn4PojOauSYjnFZI4f64ts3hbcMyK/pqZUQO94bzMtgEW2Xab7Mb5oNzJm0gS7OOkObUKsVNYdpotEHjdO+UYNUVpB7rOJQ109IHV7MWzm2hU0d/eoUoWScea38qOYzdxW4lYdOgZU3ybm2zlhRg1/R9L9YomRsuqqxIzQzgwdPWzLfRtn8FpTiBFDMIwyPYd7OWIm7OJ9t49uDFPuvz+/oZSlXi3MzrVytVnZECL/1Bj3J5mCl1adLPGbwQJGgrIiwhKysagQXmEq229VGunhXPdmRETXZouA6h9EmIeDfq15ZlRVkbopoAxf6qtDL9TXWzdlhveK3vQ8fk048nKy4DC3c6w=";
            }

            @Override
            public String texture(HypixelPlayer player) {
                return "ewogICJ0aW1lc3RhbXAiIDogMTYxNzg5OTEzNTk2MSwKICAicHJvZmlsZUlkIiA6ICJkODAwZDI4MDlmNTE0ZjkxODk4YTU4MWYzODE0Yzc5OSIsCiAgInByb2ZpbGVOYW1lIiA6ICJ0aGVCTFJ4eCIsCiAgInNpZ25hdHVyZVJlcXVpcmVkIiA6IHRydWUsCiAgInRleHR1cmVzIiA6IHsKICAgICJTS0lOIiA6IHsKICAgICAgInVybCIgOiAiaHR0cDovL3RleHR1cmVzLm1pbmVjcmFmdC5uZXQvdGV4dHVyZS9jMzVhMjBmNTc4MGM2NjdjNWY1NzZiYTYzMDdkYjYxYjM1YTNjOWYzYWRjNmE4NjU0NTQ3MjVmNGU3N2ExMTIiLAogICAgICAibWV0YWRhdGEiIDogewogICAgICAgICJtb2RlbCIgOiAic2xpbSIKICAgICAgfQogICAgfQogIH0KfQ==";
            }

            @Override
            public Pos position(HypixelPlayer player) {
                return new Pos(181.5, 77, -381, 30, 0);
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
        long now = System.currentTimeMillis();
        int state = HikerQuest.state(player);
        if (state == HikerQuest.FINISHED || state == HikerQuest.REWARDED) {
            sendNPCMessage(player, "Thank you for saving my friend!");
            return;
        }
        if (state == HikerQuest.STARVED) {
            if (!HikerQuest.restartReady(player, now)) {
                sendNPCMessage(player, "My hiking buddy still hasn't returned. I'm afraid the worst has happened and he's starved to death...");
                sendNPCMessage(player, "I'm going to send someone down soon to find him.");
                return;
            }
            HikerQuest.restart(player);
            sendNPCMessage(player, "My last hiking buddy didn't make it out of the gorge...");
            sendNPCMessage(player, "My new hiking buddy went there a couple days ago to explore but he hasn't come back!");
            sendNPCMessage(player, "Could you go check up on him?");
            return;
        }
        sendNPCMessage(player, "My hiking buddy has been missing for a couple days...");
        sendNPCMessage(player, "He said he was going to go explore the gorge, but that was a while ago.");
        sendNPCMessage(player, "I'm getting worried, could you go check on him?");
    }
}
