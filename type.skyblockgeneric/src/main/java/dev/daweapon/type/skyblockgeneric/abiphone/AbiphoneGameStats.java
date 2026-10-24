package dev.daweapon.type.skyblockgeneric.abiphone;

public record AbiphoneGameStats(int snakeBest, int wins, int losses, int draws) {
    public static AbiphoneGameStats parse(String raw) {
        String[] parts = raw.split(",");
        if (parts.length != 4) return new AbiphoneGameStats(0, 0, 0, 0);
        try {
            return new AbiphoneGameStats(Integer.parseInt(parts[0]), Integer.parseInt(parts[1]), Integer.parseInt(parts[2]), Integer.parseInt(parts[3]));
        } catch (NumberFormatException exception) {
            return new AbiphoneGameStats(0, 0, 0, 0);
        }
    }

    public String serialize() {
        return snakeBest + "," + wins + "," + losses + "," + draws;
    }

    public AbiphoneGameStats withSnakeScore(int score) {
        return new AbiphoneGameStats(Math.max(snakeBest, score), wins, losses, draws);
    }

    public AbiphoneGameStats withWin() {
        return new AbiphoneGameStats(snakeBest, wins + 1, losses, draws);
    }

    public AbiphoneGameStats withLoss() {
        return new AbiphoneGameStats(snakeBest, wins, losses + 1, draws);
    }

    public AbiphoneGameStats withDraw() {
        return new AbiphoneGameStats(snakeBest, wins, losses, draws + 1);
    }
}
