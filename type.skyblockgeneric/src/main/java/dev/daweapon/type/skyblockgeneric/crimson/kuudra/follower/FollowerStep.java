package dev.daweapon.type.skyblockgeneric.crimson.kuudra.follower;

import lombok.Getter;

/**
 * Every saved step of the Kuudra Follower questline. {@code knowledge} steps are things the player
 * learns rather than earns, which is what /maxprofile grants.
 */
@Getter
public enum FollowerStep {
    BELIEVER_MET(true),
    WET_NAPKIN(false),
    HELMET_CLAIMED(false),
    HERRING_CLAIMED(false),
    CHESTPLATE_CLAIMED(false),
    ARCHEOLOGIST_CLUE(true),
    BOOKSHELF_READ(true),
    RELIC_FOUND(false),
    LEGGINGS_CLAIMED(false),
    LOREMASTER_TRIGGER(true),
    LOREMASTER_LAIR_LINE(true),
    BOOTS_CLAIMED(false),
    PLAQUE_RECEIVED(false),
    DECAY_ARCHEOLOGIST(false),
    DECAY_ARCH(false),
    DECAY_BELIEVER(false),
    DECAY_LOREMASTER(false),
    RELIC_UPGRADED(false),
    ;

    private final boolean knowledge;

    FollowerStep(boolean knowledge) {
        this.knowledge = knowledge;
    }
}
