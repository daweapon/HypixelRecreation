package dev.daweapon.type.skyblockgeneric.hoppity;

import dev.daweapon.type.skyblockgeneric.calendar.SkyBlockCalendar;
import dev.daweapon.type.skyblockgeneric.data.datapoints.DatapointChocolateFactory.ChocolateFactoryData;
import dev.daweapon.type.skyblockgeneric.rabbits.ChocolateFactoryHelper;
import dev.daweapon.type.skyblockgeneric.rabbits.ChocolateRabbit;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;

public final class StrayRabbitService {
    public static final int[] STRAY_SLOTS = {0, 1, 2, 3, 4, 5, 6, 7, 9, 10, 11, 12, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26};
    private static final int EL_DORADO_CATCHES_FOR_RABBIT = 3;
    private static final int EL_DORADO_SECONDS = 10;
    private static final int JACKPOT_MULTIPLIER = 480;
    private static final int MOUNTAIN_MULTIPLIER = 1500;
    private static final Map<UUID, State> STATES = new ConcurrentHashMap<>();

    private StrayRabbitService() {
    }

    public record Stray(int slot, ChocolateRabbit rabbit, StrayRabbits.Golden golden, boolean fish, long expiresAt) {
        public boolean isGolden() {
            return golden != null;
        }
    }

    private static final class State {
        long nextCheckAt;
        final List<Stray> active = new ArrayList<>();
    }

    public static void release(UUID player) {
        STATES.remove(player);
    }

    public static List<Stray> refresh(SkyBlockPlayer player) {
        State state = STATES.computeIfAbsent(player.getUuid(), ignored -> new State());
        long now = System.currentTimeMillis();
        state.active.removeIf(stray -> now >= stray.expiresAt());
        if (!state.active.isEmpty()) return List.copyOf(state.active);

        ChocolateFactoryData data = ChocolateFactoryHelper.getData(player);
        if (data.getFoundRabbits().isEmpty() || data.getStrayDueAt() <= 0 && now < state.nextCheckAt) return List.of();

        ThreadLocalRandom random = ThreadLocalRandom.current();
        boolean guaranteed = data.getStrayDueAt() > 0 && now >= data.getStrayDueAt();
        if (data.getStrayDueAt() > 0 && !guaranteed) return List.of();
        if (!guaranteed) {
            state.nextCheckAt = now + StrayRabbits.SPAWN_INTERVAL_MS;
            if (!StrayRabbits.spawnsThisCheck(player.getUuid(), random)) return List.of();
        }

        Stray stray = create(player, data, guaranteed, now, random, state);
        if (guaranteed) ChocolateFactoryHelper.mutateData(player, d -> d.setStrayDueAt(0));
        if (stray != null) state.active.add(stray);
        return List.copyOf(state.active);
    }

    private static Stray create(SkyBlockPlayer player, ChocolateFactoryData data, boolean afterEgg, long now,
                                ThreadLocalRandom random, State state) {
        int slot = freeSlot(state, random);
        long expires = now + StrayRabbits.VISIBLE_MS;
        if (afterEgg && StrayRabbits.isGolden(player.getUuid(), data.hasFoundRabbit(ChocolateRabbit.OMEGA), random)) {
            return new Stray(slot, null, StrayRabbits.pickGolden(player.getUuid(), data.getFactoryLevel(), random), false, expires);
        }
        if (StrayRabbits.isFish(player.getUuid(), random)) {
            return new Stray(slot, ChocolateRabbit.FISH_THE_RABBIT, null, true, expires);
        }
        ChocolateRabbit rabbit = pick(player, data, random);
        return rabbit == null ? null : new Stray(slot, rabbit, null, false, expires);
    }

    private static ChocolateRabbit pick(SkyBlockPlayer player, ChocolateFactoryData data, ThreadLocalRandom random) {
        RabbitRoller.Context context = HoppityEggService.context(player, data, null);
        return StrayRabbits.pickRabbit(player.getUuid(), data.getFoundRabbits(), data.getFactoryLevel(),
                rarity -> RabbitRoller.rarityWeight(context, rarity), random);
    }

    private static int freeSlot(State state, ThreadLocalRandom random) {
        List<Integer> free = new ArrayList<>();
        for (int slot : STRAY_SLOTS) {
            if (state.active.stream().noneMatch(stray -> stray.slot() == slot)) free.add(slot);
        }
        return free.get(random.nextInt(free.size()));
    }

    public static boolean catchStray(SkyBlockPlayer player, int slot) {
        State state = STATES.get(player.getUuid());
        if (state == null) return false;
        Stray stray = state.active.stream().filter(candidate -> candidate.slot() == slot).findFirst().orElse(null);
        if (stray == null || !state.active.remove(stray)) return false;
        ChocolateFactoryHelper.updateProduction(player);
        if (stray.isGolden()) {
            catchGolden(player, stray.golden(), state);
        } else {
            catchRegular(player, stray);
        }
        return true;
    }

    private static void catchRegular(SkyBlockPlayer player, Stray stray) {
        boolean hunt = HuntEggSchedule.isHuntActive(SkyBlockCalendar.getElapsed());
        long[] gained = {0};
        boolean[] newRabbit = {false};
        ChocolateFactoryHelper.mutateData(player, data -> {
            if (stray.fish()) {
                if (data.hasFoundRabbit(ChocolateRabbit.FISH_THE_RABBIT)) {
                    gained[0] = HoppityEggService.duplicateReward(player, data, ChocolateRabbit.FISH_THE_RABBIT);
                } else {
                    data.addFoundRabbit(ChocolateRabbit.FISH_THE_RABBIT);
                    newRabbit[0] = true;
                }
                return;
            }
            gained[0] = Math.max(1, (long) (data.getChocolatePerSecond()
                    * StrayRabbits.rewardSeconds(stray.rabbit().getRarity(), hunt)));
            data.addChocolate(gained[0]);
        });
        if (newRabbit[0]) {
            player.sendMessage("<d><l>NEW RABBIT! <7>{} <7>was hiding in the factory!", stray.rabbit().getFormattedName());
        } else {
            player.sendMessage("<d><l>STRAY RABBIT! <7>You caught {} <6>+{} Chocolate", stray.rabbit().getFormattedName(),
                    ChocolateFactoryHelper.formatChocolate(gained[0]));
        }
    }

    private static void catchGolden(SkyBlockPlayer player, StrayRabbits.Golden golden, State state) {
        ThreadLocalRandom random = ThreadLocalRandom.current();
        switch (golden) {
            case JACKPOT -> grantMultiple(player, "JACKPOT", JACKPOT_MULTIPLIER);
            case CHOCOLATE_MOUNTAIN -> grantMultiple(player, "CHOCOLATE MOUNTAIN", MOUNTAIN_MULTIPLIER);
            case SIDE_DISH -> {
                player.sendMessage("<6><l>GOLDEN RABBIT! <7>Side Dish: <6>you found a Chocolate Egg!");
                HoppityEggService.open(player, null, null, random);
            }
            case EL_DORADO -> catchElDorado(player);
            case STAMPEDE -> stampede(player, state, random);
            case GOLDEN_CLICK -> {
                int year = SkyBlockCalendar.getYear();
                ChocolateFactoryHelper.mutateData(player, data -> {
                    if (data.getGoldenClickYear() != year) data.setGoldenClickBonuses(0);
                    data.setGoldenClickYear(year);
                    data.setGoldenClickBonuses(data.getGoldenClickBonuses() + 1);
                });
                player.sendMessage("<6><l>GOLDEN RABBIT! <7>Golden Click: <6>+5 Chocolate per second <7>for the rest of the year!");
            }
        }
    }

    private static void grantMultiple(SkyBlockPlayer player, String name, int multiplier) {
        long[] gained = {0};
        ChocolateFactoryHelper.mutateData(player, data -> {
            gained[0] = Math.max(1, (long) (data.getChocolatePerSecond() * multiplier));
            data.addChocolate(gained[0]);
        });
        player.sendMessage("<6><l>GOLDEN RABBIT! <7>{}: <6>+{} Chocolate", name, ChocolateFactoryHelper.formatChocolate(gained[0]));
    }

    private static void catchElDorado(SkyBlockPlayer player) {
        long[] gained = {0};
        long[] duplicate = {0};
        boolean[] rabbit = {false};
        ChocolateFactoryHelper.mutateData(player, data -> {
            gained[0] = Math.max(1, (long) (data.getChocolatePerSecond() * EL_DORADO_SECONDS));
            data.addChocolate(gained[0]);
            data.setElDoradoCatches(data.getElDoradoCatches() + 1);
            if (data.getElDoradoCatches() % EL_DORADO_CATCHES_FOR_RABBIT != 0) return;
            if (data.hasFoundRabbit(ChocolateRabbit.EL_DORADO)) {
                duplicate[0] = HoppityEggService.duplicateReward(player, data, ChocolateRabbit.EL_DORADO);
            } else {
                data.addFoundRabbit(ChocolateRabbit.EL_DORADO);
                rabbit[0] = true;
            }
        });
        player.sendMessage("<6><l>GOLDEN RABBIT! <7>El Dorado: <6>+{} Chocolate", ChocolateFactoryHelper.formatChocolate(gained[0]));
        if (rabbit[0]) player.sendMessage("<d><l>NEW RABBIT! {}", ChocolateRabbit.EL_DORADO.getFormattedName());
        if (duplicate[0] > 0) {
            player.sendMessage("<d><l>DUPLICATE RABBIT! <7>{} <6>+{} Chocolate", ChocolateRabbit.EL_DORADO.getFormattedName(),
                    ChocolateFactoryHelper.formatChocolate(duplicate[0]));
        }
    }

    private static void stampede(SkyBlockPlayer player, State state, ThreadLocalRandom random) {
        ChocolateFactoryData data = ChocolateFactoryHelper.getData(player);
        long expires = System.currentTimeMillis() + StrayRabbits.VISIBLE_MS;
        for (int i = 0; i < StrayRabbits.STAMPEDE_STRAYS && state.active.size() < StrayRabbitService.STRAY_SLOTS.length; i++) {
            ChocolateRabbit rabbit = pick(player, data, random);
            if (rabbit == null) break;
            state.active.add(new Stray(freeSlot(state, random), rabbit, null, false, expires));
        }
        player.sendMessage("<6><l>GOLDEN RABBIT! <7>Stampede: <6>a herd of stray rabbits appeared!");
    }
}
