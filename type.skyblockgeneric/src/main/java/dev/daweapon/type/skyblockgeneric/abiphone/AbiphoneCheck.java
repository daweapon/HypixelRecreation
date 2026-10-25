package dev.daweapon.type.skyblockgeneric.abiphone;

import dev.daweapon.commons.skyblock.item.ItemType;
import dev.daweapon.commons.skyblock.item.Rarity;
import dev.daweapon.type.skyblockgeneric.spidersden.ArachneFight;
import dev.daweapon.type.skyblockgeneric.user.pets.PetLevels;
import dev.daweapon.type.skyblockgeneric.essence.EssenceType;
import dev.daweapon.type.skyblockgeneric.essence.Essences;
import dev.daweapon.type.skyblockgeneric.farmingcontest.ContestBracket;
import dev.daweapon.type.skyblockgeneric.farmingcontest.FarmingContests;
import dev.daweapon.type.skyblockgeneric.item.SkyBlockItem;
import dev.daweapon.type.skyblockgeneric.item.components.AbiphoneComponent;
import dev.daweapon.type.skyblockgeneric.slayer.SlayerRequirement;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;
import net.minestom.server.inventory.PlayerInventory;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;

import java.util.List;

public enum AbiphoneCheck {
    JACOB_AND_GOLD_MEDAL {
        @Override
        public boolean test(SkyBlockPlayer player) {
            return AbiphoneContactXP.unlocked(player).contains("jacob")
                    && FarmingContests.profile(player).cropsWithAtLeast(ContestBracket.GOLD) > 0;
        }
    },
    SPIDER_SLAYER_9 {
        @Override
        public boolean test(SkyBlockPlayer player) {
            return SlayerRequirement.isMet(player, "Spider", 9);
        }
    },
    BLACKSMITH_DEBT {
        @Override
        public boolean test(SkyBlockPlayer player) {
            return player.getCoins() >= 32_000_000;
        }

        @Override
        public void consume(SkyBlockPlayer player) {
            player.removeCoins(32_000_000);
        }
    },
    JAX_COINS {
        @Override
        public boolean test(SkyBlockPlayer player) {
            return player.getCoins() >= 10_000_000;
        }

        @Override
        public void consume(SkyBlockPlayer player) {
            player.removeCoins(10_000_000);
        }
    },
    UNDEAD_ESSENCE_5000 {
        @Override
        public boolean test(SkyBlockPlayer player) {
            return Essences.get(player, EssenceType.UNDEAD) >= 5000;
        }

        @Override
        public void consume(SkyBlockPlayer player) {
            Essences.take(player, EssenceType.UNDEAD, 5000);
        }
    },
    KILLED_ARACHNE {
        @Override
        public boolean test(SkyBlockPlayer player) {
            return ArachneFight.hasDefeatedEither(player);
        }
    },
    TARANTULA_PET_LEVEL_1 {
        @Override
        public boolean test(SkyBlockPlayer player) {
            return starterTarantula(player) != null;
        }

        @Override
        public void consume(SkyBlockPlayer player) {
            SkyBlockItem pet = starterTarantula(player);
            if (pet != null) player.getPetData().removePet(pet);
        }

        private SkyBlockItem starterTarantula(SkyBlockPlayer player) {
            return player.getPetData().getPetsMap().keySet().stream()
                    .filter(pet -> pet.getAttributeHandler().getPotentialType() == ItemType.TARANTULA_PET)
                    .filter(pet -> pet.getAttributeHandler().getRarity() == Rarity.EPIC)
                    .filter(pet -> PetLevels.level(pet) == 1)
                    .findFirst().orElse(null);
        }
    },
    DONATE_ABIPHONE {
        private static final List<String> ACCEPTED = List.of("ABIPHONE_BASIC", "ABIPHONE_X_RED", "ABIPHONE_X_BLUE", "ABIPHONE_XI_YELLOW");

        @Override
        public boolean test(SkyBlockPlayer player) {
            return spareSlot(player) >= 0;
        }

        @Override
        public void consume(SkyBlockPlayer player) {
            int slot = spareSlot(player);
            if (slot >= 0) player.getInventory().setItemStack(slot, ItemStack.of(Material.AIR));
        }

        private int spareSlot(SkyBlockPlayer player) {
            for (int slot = 0; slot < PlayerInventory.INNER_INVENTORY_SIZE; slot++) {
                if (slot == player.getHeldSlot()) continue;
                ItemStack stack = player.getInventory().getItemStack(slot);
                if (stack.isAir() || !SkyBlockItem.isSkyBlockItem(stack)) continue;
                SkyBlockItem item = new SkyBlockItem(stack);
                if (item.hasComponent(AbiphoneComponent.class) && ACCEPTED.contains(item.getAttributeHandler().getTypeAsString())) return slot;
            }
            return -1;
        }
    };

    public abstract boolean test(SkyBlockPlayer player);

    public void consume(SkyBlockPlayer player) {
    }
}
