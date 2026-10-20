package dev.daweapon.type.theend.events;

import net.minestom.server.event.player.PlayerBlockInteractEvent;
import net.minestom.server.instance.block.Block;
import net.minestom.server.item.ItemStack;
import dev.daweapon.commons.skyblock.item.ItemType;
import dev.daweapon.commons.text.Text;
import dev.daweapon.type.generic.event.EventNodes;
import dev.daweapon.type.generic.event.HypixelEventClass;
import dev.daweapon.type.generic.event.phase.EventPhase;
import dev.daweapon.type.generic.event.phase.PhasedEvent;
import dev.daweapon.type.skyblockgeneric.item.SkyBlockItem;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;
import dev.daweapon.type.theend.dragon.DragonArena;
import dev.daweapon.type.theend.dragon.EnderDragonFight;
import dev.daweapon.type.theend.dragon.EyeLedger;

public class ActionAltarInteract implements HypixelEventClass {

    @PhasedEvent(node = EventNodes.PLAYER, requireDataLoaded = true, phase = EventPhase.GAMEPLAY)
    public void run(PlayerBlockInteractEvent event) {
        if (event.getHand() != net.minestom.server.entity.PlayerHand.MAIN) return;
        var position = event.getBlockPosition();
        int frame = DragonArena.frameIndex(position.blockX(), position.blockY(), position.blockZ());
        if (frame < 0) return;
        event.setBlockingItemUse(true);
        SkyBlockPlayer player = (SkyBlockPlayer) event.getPlayer();
        if (EnderDragonFight.ledger().at(frame).isPresent()) {
            withdraw(player, frame, event);
            return;
        }
        place(player, frame, event);
    }

    private void place(SkyBlockPlayer player, int frame, PlayerBlockInteractEvent event) {
        SkyBlockItem held = new SkyBlockItem(player.getItemInMainHand());
        if (held.getAttributeHandler().getPotentialType() != ItemType.SUMMONING_EYE) return;
        if (EnderDragonFight.eggRespawning()) {
            player.sendMessage("<c>The Dragon Egg is still respawning!");
            return;
        }
        if (!EnderDragonFight.acceptsEyes()) {
            player.sendMessage("<c>The altar is sealed while the dragon is awake!");
            return;
        }
        boolean recombobulated = held.getAttributeHandler().isRecombobulated();
        if (!EnderDragonFight.ledger().place(frame, player.getUuid(), recombobulated)) return;
        ItemStack stack = player.getItemInMainHand();
        player.setItemInMainHand(stack.amount() > 1 ? stack.withAmount(stack.amount() - 1) : ItemStack.AIR);
        player.addAndUpdateItem(ItemType.SLEEPING_EYE, 1);
        setEye(event, true);
        EyeLedger ledger = EnderDragonFight.ledger();
        String ending = ledger.complete() ? " Brace yourselves!" : "";
        player.getInstance().getPlayers().forEach(nearby -> {
            if (nearby == player) {
                nearby.sendMessage(Text.of("<d>You placed a Summoning Eye!{} <a>({}/{})", ending, ledger.count(), EyeLedger.FRAMES));
                return;
            }
            nearby.sendMessage(Text.of("{}{}<d> placed a Summoning Eye!{} <a>({}/{})", player.getRankPrefix(),
                    player.getUsername(), ending, ledger.count(), EyeLedger.FRAMES));
        });
        EnderDragonFight.onAltarFilled(player.getInstance());
    }

    private void withdraw(SkyBlockPlayer player, int frame, PlayerBlockInteractEvent event) {
        if (!EnderDragonFight.acceptsEyes()) return;
        EyeLedger ledger = EnderDragonFight.ledger();
        var placed = ledger.at(frame);
        if (placed.isEmpty() || !placed.get().owner().equals(player.getUuid())) {
            player.sendMessage("<c>That Summoning Eye is not yours to take back!");
            return;
        }
        if (ledger.onCooldown(player.getUuid(), System.currentTimeMillis())) {
            player.sendMessage("<c>You can only withdraw a Summoning Eye every 3 seconds!");
            return;
        }
        if (!player.removeItemFromPlayer(ItemType.SLEEPING_EYE, 1)) {
            player.sendMessage("<c>You do not have the Sleeping Eye that belongs to this frame!");
            return;
        }
        ledger.withdraw(frame, player.getUuid(), System.currentTimeMillis());
        SkyBlockItem eye = new SkyBlockItem(ItemType.SUMMONING_EYE, 1);
        if (placed.get().recombobulated()) eye.getAttributeHandler().setRecombobulated(true);
        player.addAndUpdateItem(eye);
        setEye(event, false);
        player.sendMessage(Text.of("<d>You withdrew your Summoning Eye! <a>({}/{})", ledger.count(), EyeLedger.FRAMES));
    }

    private void setEye(PlayerBlockInteractEvent event, boolean eye) {
        Block block = event.getBlock();
        event.getInstance().setBlock(event.getBlockPosition(), block.withProperty("eye", String.valueOf(eye)));
    }
}
