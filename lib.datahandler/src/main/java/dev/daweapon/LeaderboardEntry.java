package dev.daweapon;

import java.util.UUID;

public record LeaderboardEntry<T>(UUID playerId, T value, int rank) {}
