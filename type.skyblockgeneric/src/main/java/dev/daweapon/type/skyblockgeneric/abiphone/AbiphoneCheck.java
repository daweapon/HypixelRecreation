package dev.daweapon.type.skyblockgeneric.abiphone;

import dev.daweapon.commons.skyblock.item.ItemType;
import dev.daweapon.commons.skyblock.item.Rarity;
import dev.daweapon.type.skyblockgeneric.spidersden.ArachneFight;
import dev.daweapon.type.skyblockgeneric.user.pets.PetLevels;
import dev.daweapon.type.skyblockgeneric.essence.EssenceType;
import dev.daweapon.type.skyblockgeneric.essence.Essences;
import dev.daweapon.type.skyblockgeneric.farmingcontest.ContestBracket;
import dev.daweapon.type.skyblockgeneric.farmingcontest.FarmingContests;
import dev.daweapon.type.skyblockgeneric.chapters.ChapterTriggers;
import dev.daweapon.type.skyblockgeneric.item.SkyBlockItem;
import dev.daweapon.type.skyblockgeneric.item.set.ArmorSetRegistry;
import dev.daweapon.type.skyblockgeneric.item.components.AbiphoneComponent;
import dev.daweapon.type.skyblockgeneric.miningevent.CultOfTheFallenStar;
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
    CULT_MEMBER {
        @Override
        public boolean test(SkyBlockPlayer player) {
            return CultOfTheFallenStar.wearingHelmet(player) && CultOfTheFallenStar.attended(player) >= CultOfTheFallenStar.MEETINGS_FOR_CONTACT;
        }

        @Override
        public List<String> failureLines(SkyBlockPlayer player) {
            if (!CultOfTheFallenStar.wearingHelmet(player)) {
                return List.of(
                        "I'm not just gonna give my contact out to anybody...",
                        "Huh? Are you a member of, y'know, our <o>thing<f>?",
                        "Where's your <9>Fallen Star Helmet<f>? Put it on!");
            }
            return List.of(
                    "You want my <3>Abiphone Contact<f>? Respectfully, we don't see you at our meetings very often.",
                    "Here's the deal.",
                    "Attend <6>3<f> meetings of the <5>Cult of the Fallen Star<f>, and you can have my contact.",
                    "Confirm your attendance at a meeting by speaking to me with your <1>Fallen Star Helmet<f> equipped.");
        }
    },
    GEORGE_PETS {
        @Override
        public boolean test(SkyBlockPlayer player) {
            return GeorgePets.findAll(player) != null;
        }

        @Override
        public void consume(SkyBlockPlayer player) {
            List<SkyBlockItem> pets = GeorgePets.findAll(player);
            if (pets != null) pets.forEach(pet -> player.getPetData().removePet(pet));
        }

        @Override
        public String requested(SkyBlockPlayer player) {
            return GeorgePets.describe(GeorgePets.requested(player));
        }
    },
    ZOG_PET_LOVER {
        @Override
        public boolean test(SkyBlockPlayer player) {
            return player.getPetData().getPetsMap().keySet().stream()
                    .filter(pet -> pet.getAttributeHandler().getRarity().isAtLeast(Rarity.LEGENDARY))
                    .filter(PetLevels::isMaxLevel)
                    .count() >= ZOG_PETS;
        }
    },
    RUNIC_ENDERMAN {
        @Override
        public boolean test(SkyBlockPlayer player) {
            return ChapterTriggers.done(player, RUNIC_ENDERMAN_TRIGGER);
        }
    },
    FAIRY_ARMOR {
        @Override
        public boolean test(SkyBlockPlayer player) {
            return player.getArmorSet() == ArmorSetRegistry.FAIRY_OUTFIT;
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

    public static final String RUNIC_ENDERMAN_TRIGGER = "abiphone.dusk.runic_enderman";
    private static final int ZOG_PETS = 5;

    public abstract boolean test(SkyBlockPlayer player);

    public String requested(SkyBlockPlayer player) {
        return "";
    }

    public void consume(SkyBlockPlayer player) {
    }

    public List<String> failureLines(SkyBlockPlayer player) {
        return List.of();
    }
}
