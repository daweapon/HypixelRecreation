package dev.daweapon.type.skyblockgeneric.abiphone;

import dev.daweapon.commons.text.Text;
import dev.daweapon.type.skyblockgeneric.data.datapoints.DatapointPetTraining;
import dev.daweapon.type.skyblockgeneric.item.SkyBlockItem;
import dev.daweapon.type.skyblockgeneric.pettraining.PetTrainingService;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;
import dev.daweapon.type.skyblockgeneric.user.pets.PetLevels;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public final class FannTrainingCalls {
    private static final Set<String> ANNOUNCED = ConcurrentHashMap.newKeySet();

    private FannTrainingCalls() {
    }

    public static void register() {
        AbiphoneIncomingHooks.register("fann", new AbiphoneIncomingHooks.Hook(FannTrainingCalls::hasNews, FannTrainingCalls::report));
    }

    private static List<Map.Entry<Integer, DatapointPetTraining.Session>> unannounced(SkyBlockPlayer player) {
        long now = System.currentTimeMillis();
        List<Map.Entry<Integer, DatapointPetTraining.Session>> finished = new ArrayList<>();
        PetTrainingService.data(player).getSessions().forEach((slot, session) -> {
            if (PetTrainingService.isFinished(session, now) && !ANNOUNCED.contains(key(player, slot, session))) {
                finished.add(Map.entry(slot, session));
            }
        });
        return finished;
    }

    private static String key(SkyBlockPlayer player, int slot, DatapointPetTraining.Session session) {
        return player.getUuid() + ":" + slot + ":" + session.endsAt();
    }

    private static boolean hasNews(SkyBlockPlayer player) {
        return !unannounced(player).isEmpty();
    }

    private static void report(SkyBlockPlayer player) {
        AbiphoneNPC fann = AbiphoneRegistry.getFromId("fann");
        if (fann == null) return;
        fann.sendNPCMessage(player, Text.of("<f>Hey <b>{}<f>!", player.getUsername()));
        for (Map.Entry<Integer, DatapointPetTraining.Session> entry : unannounced(player)) {
            DatapointPetTraining.Session session = entry.getValue();
            SkyBlockItem trained = session.pet().clone();
            PetLevels.addExperience(trained, PetTrainingService.xpEarned(session, System.currentTimeMillis()));
            ANNOUNCED.add(key(player, entry.getKey(), session));
            fann.sendNPCMessage(player, Text.of("<e>[#{}] <f>{} {} has concluded and is now <7>Lvl {}",
                    entry.getKey() + 1, session.type().getDisplayName(), trained.getDisplayName(), PetLevels.level(trained)));
        }
    }
}
