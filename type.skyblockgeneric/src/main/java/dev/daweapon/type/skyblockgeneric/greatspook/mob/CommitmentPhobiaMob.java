package dev.daweapon.type.skyblockgeneric.greatspook.mob;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.minestom.server.entity.EquipmentSlot;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import dev.daweapon.commons.text.Text;
import dev.daweapon.type.skyblockgeneric.greatspook.Manifestation;
import dev.daweapon.type.skyblockgeneric.greatspook.PrimalFearDialogue;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;

public class CommitmentPhobiaMob extends PrimalMob {
    private static final Map<UUID, CommitmentPhobiaMob> PAPERS = new ConcurrentHashMap<>();

    private UUID paper;

    public CommitmentPhobiaMob(UUID owner, String ownerName) {
        super(PrimalProfiles.of(Manifestation.COMMITMENT_PHOBIA.mobId(), Manifestation.COMMITMENT_PHOBIA.displayName()), owner, ownerName);
    }

    public static void sign(SkyBlockPlayer player, UUID token) {
        CommitmentPhobiaMob mob = PAPERS.get(token);
        if (mob == null) {
            player.sendMessage(Text.parseLenient(PrimalFearDialogue.line(Manifestation.COMMITMENT_PHOBIA, PrimalFearDialogue.COMMITMENT_OUTDATED)));
            return;
        }
        if (!mob.owner().equals(player.getUuid())) return;
        mob.signed(player);
    }

    @Override
    protected Manifestation manifestation() {
        return Manifestation.COMMITMENT_PHOBIA;
    }

    @Override
    protected boolean heldAtOneHealth() {
        return true;
    }

    @Override
    protected Map<EquipmentSlot, ItemStack> appearance() {
        return Map.of(EquipmentSlot.HELMET, leather(Material.LEATHER_HELMET, 0xE8E8E8),
                EquipmentSlot.CHESTPLATE, leather(Material.LEATHER_CHESTPLATE, 0x202020),
                EquipmentSlot.LEGGINGS, leather(Material.LEATHER_LEGGINGS, 0x202020),
                EquipmentSlot.BOOTS, leather(Material.LEATHER_BOOTS, 0x101010),
                EquipmentSlot.MAIN_HAND, ItemStack.of(Material.PAPER));
    }

    @Override
    protected void onChallengeStart(SkyBlockPlayer player) {
        say(player, PrimalFearDialogue.COMMITMENT_ASK);
        paper = UUID.randomUUID();
        PAPERS.put(paper, this);
        String document = PrimalFearDialogue.DOCUMENTS.get(ThreadLocalRandom.current().nextInt(PrimalFearDialogue.DOCUMENTS.size()));
        Component sign = Component.text("HERE", NamedTextColor.YELLOW, TextDecoration.BOLD)
                .clickEvent(ClickEvent.runCommand("/primalfearsign " + paper))
                .hoverEvent(HoverEvent.showText(Text.of("<e>Your signature is VERY important to us.")));
        player.sendMessage(Component.text("Click ", NamedTextColor.GREEN).append(sign)
                .append(Component.text(" to sign the ", NamedTextColor.GREEN))
                .append(Component.text("<" + document + ">", NamedTextColor.DARK_GRAY))
                .append(Component.text(".", NamedTextColor.GREEN)));
    }

    private void signed(SkyBlockPlayer player) {
        PAPERS.remove(paper);
        say(player, PrimalFearDialogue.COMMITMENT_SIGNED.get(ThreadLocalRandom.current().nextInt(PrimalFearDialogue.COMMITMENT_SIGNED.size())));
        finishWithKill(player);
    }

    @Override
    protected void onGone() {
        if (paper != null) PAPERS.remove(paper);
    }
}
