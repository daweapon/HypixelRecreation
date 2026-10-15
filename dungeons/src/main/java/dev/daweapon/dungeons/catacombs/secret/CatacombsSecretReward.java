package dev.daweapon.dungeons.catacombs.secret;

import dev.daweapon.dungeons.catacombs.CatacombsFloor;

import java.util.Set;

public record CatacombsSecretReward(
        SecretRewardType type,
        CatacombsFloor minimumFloor,
        Set<CatacombsSecretType> secretTypes
) {
    public CatacombsSecretReward {
        secretTypes = Set.copyOf(secretTypes);
    }

    public boolean canDrop(CatacombsFloor floor, CatacombsSecretType secretType) {
        return floor.ordinal() >= minimumFloor.ordinal() && secretTypes.contains(secretType);
    }
}
