package dev.daweapon.type.thefarmingislands.npcs;

import dev.daweapon.commons.skyblock.item.ItemType;
import dev.daweapon.type.skyblockgeneric.hiker.HikerQuest;
import dev.daweapon.type.skyblockgeneric.romero.RomeroInteraction;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;
import net.minestom.server.item.ItemStack;
import net.minestom.server.coordinate.Pos;
import dev.daweapon.type.generic.user.HypixelPlayer;
import dev.daweapon.type.generic.entity.npc.HypixelNPC;
import dev.daweapon.type.generic.entity.npc.configuration.HumanConfiguration;

import dev.daweapon.type.generic.event.custom.NPCInteractEvent;

public class NPCHungryHiker extends HypixelNPC {
    public NPCHungryHiker() {
        this(new Pos(269.5, 48, -480.5, -130, 0), false);
    }

    protected NPCHungryHiker(Pos position, boolean atHut) {
        super(new HumanConfiguration() {
            @Override
            public String chatName(HypixelPlayer player) {
                return "<e>Hungry Hiker";
            }

            @Override
            public boolean visible(HypixelPlayer player) {
                SkyBlockPlayer skyBlockPlayer = (SkyBlockPlayer) player;
                if (skyBlockPlayer.getSkyblockDataHandler() == null) return false;
                int state = HikerQuest.state(skyBlockPlayer);
                return atHut
                        ? state == HikerQuest.FINISHED || state == HikerQuest.REWARDED
                        : state == HikerQuest.NOT_STARTED || state == HikerQuest.ACTIVE;
            }

            @Override
            public String[] holograms(HypixelPlayer player) {
                return new String[]{"Hungry Hiker", "<e><l>CLICK"};
            }

            @Override
            public String signature(HypixelPlayer player) {
                return "cIr8X0Mtz1Ok7wybAmWuIpersxILt4FbKNsIyDaQ3g90dd1Uq5qlvUT4CvRJigKBZZhMAeJOrRVWe+U+f4BqAXwo5V7lhLxhvBhVNYzE6+AsAh7V8kTJtMiymLtVVeRsuVWaeH2C5lMPQUHl9bzEgCPnMdLGEF4CRogVSXMoXcSj+ev6q7Vk7eNPEIPD2ws4p+N+MZJQ9lQHvh1GuCMqomW4TQ805fUJtY1d23xQ7fSVZr4M81kBEQSQWdVLoq38+LPYUr5RYxUfXYPyb10bET2mIS27blnwrEdpv+h5nFrpibCXHvxEQzfL7r8jf7jmn08pzdptbOebrnktzbsa+LQw5yHfkAgK0Z7NJMBADpeG3ek/mv5TGbIerNQo4KVx/OZNfR+jCr1gxnSKDKUB5ye59BuSqKVzi2XICvNynxnl6UfN3CQXK3iZPVXA7laFDmULftKXx5FFCQCFUzitxdOEcDO6GBhp0ZIC2FyIMGfA9attcmu6QDx0L0H6O54sDrOQRVBKfyBR5DW0isQQ6OpBVxsEQtL0ny+wZ8VAlQNuVaiYTQMp7O9J0sjJfjtgjyfGvRFHF0G+MFCH3NuY/ch8jt02CKEUDEaSSaCtbvPM/b9DtEPSUkEMrXWiVyvhLfiFuolQnfX8KXS6l6Dbq5wpTS/DQIPtUTLTDO8zLnI=";
            }

            @Override
            public String texture(HypixelPlayer player) {
                return "ewogICJ0aW1lc3RhbXAiIDogMTYxNzAzNjc3MTUyOSwKICAicHJvZmlsZUlkIiA6ICJiMGQ3MzJmZTAwZjc0MDdlOWU3Zjc0NjMwMWNkOThjYSIsCiAgInByb2ZpbGVOYW1lIiA6ICJPUHBscyIsCiAgInNpZ25hdHVyZVJlcXVpcmVkIiA6IHRydWUsCiAgInRleHR1cmVzIiA6IHsKICAgICJTS0lOIiA6IHsKICAgICAgInVybCIgOiAiaHR0cDovL3RleHR1cmVzLm1pbmVjcmFmdC5uZXQvdGV4dHVyZS9kNmY5YjliNjI0MDRjNWNmOTRjMzg1YzdmMWM4NmFkZGUwZDlhZWY5YTljZDI5ZTJhYjU4M2ZhMWRlNjNmNDI2IgogICAgfQogIH0KfQ==";
            }

            @Override
            public Pos position(HypixelPlayer player) {
                return position;
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
        if (state == HikerQuest.FINISHED) {
            sendNPCMessage(player, "Thank you for feeding me all those days.");
            sendNPCMessage(player, "To show my gratitude, please have this!");
            player.addAndUpdateItem(ItemType.PET_ITEM_FARMING_SKILL_BOOST_UNCOMMON);
            HikerQuest.markRewarded(player);
            return;
        }
        if (state == HikerQuest.REWARDED) {
            sendNPCMessage(player, "Thank you again for feeding me all those days!");
            return;
        }
        if (state == HikerQuest.NOT_STARTED) {
            HikerQuest.start(player);
            sendNPCMessage(player, "Hello there, stranger!");
            sendNPCMessage(player, "I fell down into this ravine a couple days ago and can't climb out.");
            sendNPCMessage(player, "My friend Jake said he would come get me but he hasn't arrived yet.");
            sendNPCMessage(player, "Could you bring me food until he gets here?");
            askForFood(player);
            return;
        }
        ItemStack held = player.getItemInMainHand();
        String heldId = RomeroInteraction.heldId(player);
        HikerQuest.Food food = HikerQuest.food(player);
        switch (HikerQuest.feed(player, heldId, held.amount(), now)) {
            case TOO_SOON -> {
                sendNPCMessage(player, "I appreciate the effort but you recently gave me food.");
                sendNPCMessage(player, "Come back later with more food!");
            }
            case NOTHING_HELD -> askForFood(player);
            case WRONG_FOOD -> {
                sendNPCMessage(player, "This isn't the food I wanted, please get me the correct food.");
                sendNPCMessage(player, "I asked for food that is <7>{}<f>.", food.clue());
            }
            case WRONG_AMOUNT -> {
                sendNPCMessage(player, "This isn't the correct amount I asked for.");
                sendNPCMessage(player, "I asked for <7>{}<f> of this item.", food.amount());
            }
            case FED -> {
                player.setItemInMainHand(ItemStack.AIR);
                sendNPCMessage(player, "Thanks for the food.");
                sendNPCMessage(player, "This should fill me up for 144 SkyBlock days.");
                sendNPCMessage(player, "Come back before then so I don't perish!");
            }
            case FINISHED -> {
                player.setItemInMainHand(ItemStack.AIR);
                sendNPCMessage(player, "Thanks for the food!");
                sendNPCMessage(player, "I think I see Jake on the edge of the ravine!");
                sendNPCMessage(player, "Thank you for feeding me all these days, meet me at the house near the portal and I will give you something to show my gratitude!");
            }
        }
    }

    private void askForFood(SkyBlockPlayer player) {
        HikerQuest.Food food = HikerQuest.food(player);
        sendNPCMessage(player, "Could you get me <7>{}<f> of this food?", food.amount());
        sendNPCMessage(player, "The food I want is <7>{}<f>.", food.clue());
    }
}
