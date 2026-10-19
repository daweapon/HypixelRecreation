package dev.daweapon.type.skyblockgeneric.miningevent;

import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.ItemEntity;
import net.minestom.server.instance.Instance;
import net.minestom.server.item.ItemStack;
import dev.daweapon.commons.skyblock.item.ItemType;
import dev.daweapon.type.generic.HypixelConst;
import dev.daweapon.type.generic.entity.InteractionEntity;
import dev.daweapon.type.skyblockgeneric.entity.TextDisplayEntity;
import dev.daweapon.type.skyblockgeneric.item.SkyBlockItem;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.CopyOnWriteArrayList;

public final class RaffleEvent extends MiningEvent {
    private static final double PICKUP_RANGE = 2.0;

    private final String eventId = UUID.randomUUID().toString();
    private final Map<UUID, Integer> deposits = new HashMap<>();
    private final List<ItemEntity> tickets = new CopyOnWriteArrayList<>();
    private final List<net.minestom.server.entity.Entity> props = new ArrayList<>();
    private MiningEventConfig.Site site;
    private Pos boxPosition;

    RaffleEvent(MiningEventZone zone) {
        super(MiningEventType.RAFFLE, zone);
    }

    public static @Nullable RaffleEvent active() {
        return MiningEvents.current().filter(RaffleEvent.class::isInstance).map(RaffleEvent.class::cast).orElse(null);
    }

    @Override
    protected boolean canStart() {
        return MiningEvents.players().size() >= MiningEventConfig.get().integer(10, "raffle", "min_players");
    }

    @Override
    protected void onCancelled() {
        MiningEvents.broadcast("<c>The Raffle event was cancelled because there are not enough players on this server!");
    }

    @Override
    protected void onStart() {
        MiningEventConfig config = MiningEventConfig.get();
        Instance instance = HypixelConst.getInstanceContainer();
        site = EventSites.pick(config.sites("raffle", "sites"));
        boxPosition = EventSites.center(instance, site);

        InteractionEntity box = new InteractionEntity(1.4f, 1.4f, (player, event) -> deposit((SkyBlockPlayer) player));
        TextDisplayEntity title = new TextDisplayEntity("<6><l>RAFFLE BOX", meta -> {});
        TextDisplayEntity hint = new TextDisplayEntity("<e>Click to deposit Raffle Tickets!", meta -> {});
        box.setInstance(instance, boxPosition);
        title.setInstance(instance, boxPosition.add(0, 1.9, 0));
        hint.setInstance(instance, boxPosition.add(0, 1.5, 0));
        props.addAll(List.of(box, title, hint));

        double radius = config.number(30, "raffle", "ticket_radius");
        int count = config.integer(60, "raffle", "tickets");
        for (int index = 0; index < count; index++) spawnTicket(instance, EventSites.near(instance, site, radius));

        MiningEventMessages.banner(type(), "RAFFLE STARTED!", "Mining Event", List.of(
                "<f>Collect <6>Raffle Tickets <f>around the <6>" + site.name() + " <f>and bring them to the <6>Raffle Box<f>!",
                "<f>Deposit at least <6>" + config.integer(10, "raffle", "min_tickets_for_rewards") + " <f>tickets to be eligible for rewards, three winners get more!",
                " ",
                "<hover:'<e>Teleport to Old Man Garry'><click:run:'/tptogarry'><e><l>Go to the " + site.name() + " (or talk to Garry)</click></hover>"));
    }

    @Override
    public void tick(long now) {
        for (ItemEntity ticket : tickets) {
            if (ticket.isRemoved()) {
                tickets.remove(ticket);
                continue;
            }
            for (SkyBlockPlayer player : MiningEvents.players()) {
                if (player.getInstance() != ticket.getInstance() || player.getPosition().distance(ticket.getPosition()) > PICKUP_RANGE) continue;
                tickets.remove(ticket);
                ticket.remove();
                player.addAndUpdateItem(ticketItem());
                break;
            }
        }
    }

    @Override
    protected void onEnd(boolean cancelled) {
        MiningEventConfig config = MiningEventConfig.get();
        tickets.forEach(net.minestom.server.entity.Entity::remove);
        props.forEach(net.minestom.server.entity.Entity::remove);
        tickets.clear();
        props.clear();

        int minimum = config.integer(10, "raffle", "min_tickets_for_rewards");
        Map<UUID, Integer> eligible = new HashMap<>();
        deposits.forEach((uuid, count) -> {
            if (count >= minimum) eligible.put(uuid, count);
        });
        List<UUID> winners = drawWinners(eligible, config.integer(3, "raffle", "winners"));
        List<String> names = new ArrayList<>();
        for (SkyBlockPlayer player : MiningEvents.players()) {
            if (winners.contains(player.getUuid())) names.add(player.getUsername());
        }
        MiningEventMessages.banner(type(), "RAFFLE ENDED!", null, List.of(
                names.isEmpty() ? "<f>Nobody deposited enough tickets to win." : "<f>The winners are <6>" + String.join("<f>, <6>", names) + "<f>!"));

        for (SkyBlockPlayer player : MiningEvents.players()) {
            int count = deposits.getOrDefault(player.getUuid(), 0);
            if (count < minimum) {
                player.sendMessage(count == 0
                        ? "<7>You didn't participate in this event, so you got no rewards."
                        : "<7>You deposited fewer than <6>" + minimum + " <7>tickets, so you got no rewards.");
                continue;
            }
            boolean won = winners.contains(player.getUuid());
            if (won) player.sendMessage("<6><l>YOU WON THE RAFFLE!");
            MiningEventRewards.grant(player, config.tier("rewards", "raffle", won ? "winner" : "participant"), count);
        }
    }

    @Override
    public @Nullable Pos warpLocation() {
        return boxPosition;
    }

    public SkyBlockItem ticketItem() {
        SkyBlockItem item = new SkyBlockItem(ItemType.MINING_RAFFLE_TICKET);
        item.getAttributeHandler().setMiningEventId(eventId);
        return item;
    }

    private void spawnTicket(Instance instance, Pos at) {
        ItemEntity entity = new ItemEntity(ticketItem().getItemStack());
        entity.setPickable(false);
        entity.setMergeable(false);
        entity.setGlowing(true);
        entity.setInstance(instance, at.add(0, 0.5, 0));
        tickets.add(entity);
    }

    private void deposit(SkyBlockPlayer player) {
        int fresh = 0;
        int stale = 0;
        for (int slot = 0; slot < player.getInventory().getSize(); slot++) {
            ItemStack stack = player.getInventory().getItemStack(slot);
            if (stack.isAir()) continue;
            SkyBlockItem item = new SkyBlockItem(stack);
            if (item.getAttributeHandler().getPotentialType() != ItemType.MINING_RAFFLE_TICKET) continue;
            if (item.getAttributeHandler().getMiningEventId().equals(eventId)) fresh += stack.amount();
            else stale += stack.amount();
            player.getInventory().setItemStack(slot, ItemStack.AIR);
        }
        if (stale > 0) player.sendMessage("<c>The Raffle Box rejected <6>" + stale + " <c>tickets from a different event!");
        if (fresh == 0) {
            if (stale == 0) player.sendMessage("<c>You don't have any Raffle Tickets to deposit!");
            return;
        }
        int total = deposits.merge(player.getUuid(), fresh, Integer::sum);
        markParticipant(player);
        for (int index = 0; index < fresh; index++) {
            dev.daweapon.type.skyblockgeneric.commission.CommissionProgress.update(player,
                    dev.daweapon.type.skyblockgeneric.commission.ObjectiveType.DEPOSIT,
                    dev.daweapon.type.skyblockgeneric.commission.Objective.BlockTarget.NONE, 1, null);
        }
        player.sendMessage("<a>You deposited <6>{} <a>Raffle Tickets! <7>({} in total)", fresh, total);
    }

    private static List<UUID> drawWinners(Map<UUID, Integer> eligible, int winners) {
        Map<UUID, Integer> pool = new HashMap<>(eligible);
        List<UUID> chosen = new ArrayList<>();
        while (chosen.size() < winners && !pool.isEmpty()) {
            int total = pool.values().stream().mapToInt(Integer::intValue).sum();
            int roll = ThreadLocalRandom.current().nextInt(total);
            UUID picked = null;
            for (Map.Entry<UUID, Integer> entry : pool.entrySet()) {
                roll -= entry.getValue();
                if (roll < 0) {
                    picked = entry.getKey();
                    break;
                }
            }
            chosen.add(picked);
            pool.remove(picked);
        }
        return chosen;
    }
}
