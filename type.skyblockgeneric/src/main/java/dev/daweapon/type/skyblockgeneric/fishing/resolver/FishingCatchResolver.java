package dev.daweapon.type.skyblockgeneric.fishing.resolver;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import dev.daweapon.type.skyblockgeneric.fishing.FishingContext;
import dev.daweapon.type.skyblockgeneric.fishing.catches.CatchPayload;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class FishingCatchResolver {

    public static CatchPayload resolve(FishingContext context) {
        return FishingLootResolver.resolve(context);
    }
}
