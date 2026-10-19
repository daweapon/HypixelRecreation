package dev.daweapon.type.skyblockgeneric.firesale;

public record ActiveFireSale(FireSale sale, long endsAt, long sold) {
    public long remaining() {
        return sale.unlimited() ? Long.MAX_VALUE : Math.max(0, sale.stock() - sold);
    }

    public boolean soldOut() {
        return remaining() == 0;
    }

    public boolean ended(long now) {
        return now >= endsAt || soldOut();
    }
}
