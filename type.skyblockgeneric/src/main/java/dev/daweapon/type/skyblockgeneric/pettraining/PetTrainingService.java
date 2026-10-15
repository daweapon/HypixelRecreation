package dev.daweapon.type.skyblockgeneric.pettraining;

import dev.daweapon.type.skyblockgeneric.item.handlers.pet.abilities.combat.PetPerkMath;
import dev.daweapon.type.skyblockgeneric.item.handlers.pet.abilities.misc.MiscPetHooks;
import dev.daweapon.type.skyblockgeneric.data.SkyBlockDataHandler;
import dev.daweapon.type.skyblockgeneric.data.datapoints.DatapointPetTraining;
import dev.daweapon.type.skyblockgeneric.data.datapoints.DatapointPetTraining.PetTrainingData;
import dev.daweapon.type.skyblockgeneric.data.datapoints.DatapointPetTraining.Session;
import dev.daweapon.type.skyblockgeneric.item.SkyBlockItem;
import dev.daweapon.type.skyblockgeneric.skill.SkillCategories;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;
import dev.daweapon.type.skyblockgeneric.user.pets.PetLevels;
import org.jetbrains.annotations.Nullable;

public final class PetTrainingService {
    public static final int SLOT_COUNT = 3;
    public static final int MIN_TAMING_LEVEL = 10;
    public static final int GREAT_CARROT_CANDIES_FOR_SLOT_TWO = 5;
    public static final long DAY_MILLIS = 86_400_000L;

    private PetTrainingService() {
    }

    public static DatapointPetTraining datapoint(SkyBlockPlayer player) {
        return player.getSkyblockDataHandler().get(SkyBlockDataHandler.Data.PET_TRAINING, DatapointPetTraining.class);
    }

    public static PetTrainingData data(SkyBlockPlayer player) {
        return datapoint(player).getValue();
    }

    public static int tamingLevel(SkyBlockPlayer player) {
        return player.getSkills().getCurrentLevel(SkillCategories.TAMING);
    }

    public static boolean isSlotUnlocked(PetTrainingData data, int slot) {
        return switch (slot) {
            case 0 -> true;
            case 1 -> data.getGreatCarrotCandiesFed() >= GREAT_CARROT_CANDIES_FOR_SLOT_TWO;
            case 2 -> data.isUltimateCarrotCandyFed();
            default -> false;
        };
    }

    public static @Nullable Session session(SkyBlockPlayer player, int slot) {
        return data(player).getSessions().get(slot);
    }

    public static double xpToMaxLevel(SkyBlockItem pet) {
        return Math.max(0, PetLevels.totalExperienceForMax(pet) - pet.getAttributeHandler().getPetData().getExperience());
    }

    public static int days(SkyBlockItem pet, double xpPerDay, PetTrainingDuration duration) {
        if (!duration.isTargetLevel()) return duration.getFixedDays();
        return Math.max(1, (int) Math.ceil(xpToMaxLevel(pet) / xpPerDay));
    }

    public static double xpEarned(Session session, long now) {
        long elapsed = Math.max(0, Math.min(now, session.endsAt()) - session.startedAt());
        return session.xpPerDay() * elapsed / DAY_MILLIS;
    }

    public static boolean isFinished(Session session, long now) {
        return now >= session.endsAt();
    }

    public static long coinCost(SkyBlockPlayer player, PetTrainingType type, int days) {
        double full = type.getCoinsPerDay() * days;
        return PetPerkMath.discountedTrainingCost(full, MiscPetHooks.trainingRefundPercent(player, full));
    }

    public static void start(SkyBlockPlayer player, int slot, SkyBlockItem pet, PetTrainingType type, int days) {
        double xpPerDay = type.xpPerDay(tamingLevel(player)) * MiscPetHooks.trainingXpMultiplier(pet);
        long now = System.currentTimeMillis();
        long coins = coinCost(player, type, days);
        player.removeCoins(coins);
        if (type.getBitsPerDay() > 0) player.removeBits(type.getBitsPerDay() * days);

        PetTrainingData data = data(player);
        data.getSessions().put(slot, new Session(pet, type, now, now + days * DAY_MILLIS,
                xpPerDay, (double) coins / days, type.getBitsPerDay()));
        datapoint(player).setValue(data);
    }

    public static SkyBlockItem finish(SkyBlockPlayer player, int slot) {
        PetTrainingData data = data(player);
        Session session = data.getSessions().remove(slot);
        long now = System.currentTimeMillis();

        if (!isFinished(session, now)) {
            double halfRemainingDays = (session.endsAt() - now) / 2.0 / DAY_MILLIS;
            player.addCoins(session.coinsPerDay() * halfRemainingDays);
            if (session.bitsPerDay() > 0) player.addBits((int) (session.bitsPerDay() * halfRemainingDays));
        }

        SkyBlockItem pet = session.pet();
        PetLevels.addExperience(pet, xpEarned(session, now));
        datapoint(player).setValue(data);
        return pet;
    }
}
