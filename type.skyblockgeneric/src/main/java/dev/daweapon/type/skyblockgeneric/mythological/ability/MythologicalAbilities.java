package dev.daweapon.type.skyblockgeneric.mythological.ability;

import dev.daweapon.type.skyblockgeneric.mythological.MythologicalCreature;

public final class MythologicalAbilities {
    private MythologicalAbilities() {
    }

    public static MythologicalAbility create(MythologicalCreature creature) {
        return switch (creature) {
            case MINOS_HUNTER, SIAMESE_LYNXES -> MythologicalAbility.NONE;
            case STRANDED_NYMPH -> new StrandedNymphAbility();
            case CRETAN_BULL -> new CretanBullAbility();
            case HARPY -> new HarpyAbility();
            case GAIA_CONSTRUCT -> new GaiaConstructAbility();
            case MINOTAUR -> new MinotaurAbility();
            case MINOS_CHAMPION -> new MinosChampionAbility();
            case SPHINX -> new SphinxAbility();
            case MINOS_INQUISITOR -> new MinosInquisitorAbility();
            case MANTICORE -> new ManticoreAbility();
            case KING_MINOS -> new KingMinosAbility();
        };
    }
}
