package dev.daweapon.type.skyblockgeneric.bank;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

class PersonalBankItemTest {
    @Test
    void lockedOrCoolingDownBanksCannotBeOpenedRemotely() {
        assertNotNull(PersonalBankItem.problem(false, 0));
        assertNotNull(PersonalBankItem.problem(true, 1));
        assertNotNull(PersonalBankItem.problem(false, 5_000));
    }

    @Test
    void anUnlockedBankWithoutACooldownOpens() {
        assertNull(PersonalBankItem.problem(true, 0));
    }
}
