package dev.daweapon.type.skyblockgeneric.hoppity;

import dev.daweapon.type.skyblockgeneric.calendar.SkyBlockCalendar;
import dev.daweapon.type.skyblockgeneric.data.datapoints.DatapointChocolateFactory.ChocolateFactoryData;
import dev.daweapon.type.skyblockgeneric.rabbits.ChocolateFactoryHelper;
import dev.daweapon.type.skyblockgeneric.rabbits.ChocolateRabbit;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public final class HoppityShopService {
    public enum Result {
        BOUGHT, UNKNOWN_SLOT, ALREADY_PURCHASED, CANNOT_AFFORD
    }

    private HoppityShopService() {
    }

    public static List<ChocolateRabbit> stock(SkyBlockPlayer player) {
        int year = SkyBlockCalendar.getYear();
        ChocolateFactoryData current = ChocolateFactoryHelper.getData(player);
        if (current.getHoppityShopYear() != year || current.getHoppityStock().size() != HoppityShopStock.SLOTS) {
            List<String> generated = HoppityShopStock.generate(player.getUuid(), ThreadLocalRandom.current())
                    .stream().map(ChocolateRabbit::name).toList();
            ChocolateFactoryHelper.mutateData(player, data -> {
                if (data.getHoppityShopYear() == year && data.getHoppityStock().size() == HoppityShopStock.SLOTS) return;
                data.setHoppityShopYear(year);
                data.setHoppityStock(new ArrayList<>(generated));
                data.setHoppityPurchases(new java.util.HashSet<>());
            });
        }
        return ChocolateFactoryHelper.getData(player).getHoppityStock().stream().map(ChocolateRabbit::valueOf).toList();
    }

    public static boolean purchased(SkyBlockPlayer player, ChocolateRabbit rabbit) {
        return ChocolateFactoryHelper.getData(player).getHoppityPurchases().contains(rabbit.name());
    }

    public static Result buy(SkyBlockPlayer player, int slot) {
        List<ChocolateRabbit> stock = stock(player);
        if (slot < 0 || slot >= stock.size()) return Result.UNKNOWN_SLOT;
        ChocolateRabbit rabbit = stock.get(slot);
        long price = HoppityShopStock.price(rabbit);
        if (purchased(player, rabbit)) return Result.ALREADY_PURCHASED;
        if (!HoppityPayment.canAfford(player, price)) return Result.CANNOT_AFFORD;
        boolean[] reserved = {false};
        ChocolateFactoryHelper.mutateData(player, data -> reserved[0] = data.getHoppityPurchases().add(rabbit.name()));
        if (!reserved[0]) return Result.ALREADY_PURCHASED;
        if (!HoppityPayment.pay(player, price)) {
            ChocolateFactoryHelper.mutateData(player, data -> data.getHoppityPurchases().remove(rabbit.name()));
            return Result.CANNOT_AFFORD;
        }
        HoppityEggService.award(player, rabbit);
        return Result.BOUGHT;
    }
}
