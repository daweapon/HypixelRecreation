package dev.daweapon.type.skyblockgeneric.greatspook.mob;

import dev.daweapon.commons.text.Text;
import dev.daweapon.type.skyblockgeneric.greatspook.Manifestation;
import dev.daweapon.type.skyblockgeneric.greatspook.PrimalFearChat;
import dev.daweapon.type.skyblockgeneric.greatspook.PrimalFearDialogue;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class PublicSpeakingGroup {
    private final List<PublicSpeakingDemonMob> members = new ArrayList<>();
    private boolean challenge;

    void add(PublicSpeakingDemonMob member) {
        members.add(member);
    }

    void sync(PublicSpeakingDemonMob source) {
        for (PublicSpeakingDemonMob member : members) {
            if (member == source || member.isDead() || member.isRemoved()) continue;
            member.setHealth(source.getHealth());
            member.refreshName();
        }
    }

    void beginChallenge(PublicSpeakingDemonMob speaker, SkyBlockPlayer player) {
        if (challenge) return;
        challenge = true;
        members.forEach(PublicSpeakingDemonMob::markChallengeStarted);
        String line = PrimalFearDialogue.SPEAK.get(ThreadLocalRandom.current().nextInt(PrimalFearDialogue.SPEAK.size()))
                .replace("{}", player.getUsername());
        player.sendMessage(Text.parseLenient(PrimalFearDialogue.line(Manifestation.PUBLIC_SPEAKING_DEMON, line)));
        PrimalFearChat.listen(player, this, message -> {
            if (message.length() < PrimalFearDialogue.MINIMUM_SPEECH_CHARACTERS) {
                player.sendMessage(Text.parseLenient(PrimalFearDialogue.line(Manifestation.PUBLIC_SPEAKING_DEMON, PrimalFearDialogue.SPEAK_TOO_SHORT)));
                return false;
            }
            player.sendMessage(Text.parseLenient(PrimalFearDialogue.line(Manifestation.PUBLIC_SPEAKING_DEMON, PrimalFearDialogue.SPEAK_DONE)));
            members.forEach(PublicSpeakingDemonMob::release);
            return true;
        });
    }

    void finished(PublicSpeakingDemonMob killed, SkyBlockPlayer player) {
        PrimalFearChat.stop(player.getUuid(), this);
        for (PublicSpeakingDemonMob member : List.copyOf(members)) {
            if (member != killed && !member.isRemoved()) member.remove();
        }
    }

    void gone(PublicSpeakingDemonMob member, java.util.UUID owner) {
        if (members.stream().allMatch(other -> other.isDead() || other.isRemoved())) PrimalFearChat.stop(owner, this);
    }
}
