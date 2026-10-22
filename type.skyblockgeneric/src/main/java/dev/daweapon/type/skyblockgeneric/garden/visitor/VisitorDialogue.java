package dev.daweapon.type.skyblockgeneric.garden.visitor;

import dev.daweapon.type.skyblockgeneric.farming.lab.BethVisits;
import dev.daweapon.type.skyblockgeneric.farming.lab.LabQuest;
import dev.daweapon.type.skyblockgeneric.garden.Gardens;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.List;

public final class VisitorDialogue {
    private VisitorDialogue() {
    }

    public static List<String> offer(VisitorCatalogue.Visitor visitor, SkyBlockPlayer player) {
        if (LabQuest.BETH.equals(visitor.id())) {
            List<String> staged = BethVisits.offer(Gardens.profile(player).visitors().offersAccepted(visitor.id()) + 1);
            if (!staged.isEmpty()) return staged;
        }
        return visitor.offerLines().isEmpty() ? List.of() : List.of(visitor.offerLines().getFirst());
    }

    public static List<String> accept(VisitorCatalogue.Visitor visitor, SkyBlockPlayer player) {
        if (LabQuest.BETH.equals(visitor.id())) {
            List<String> staged = BethVisits.accept(Gardens.profile(player).visitors().offersAccepted(visitor.id()));
            if (!staged.isEmpty()) return staged;
        }
        return visitor.acceptLines().isEmpty() ? List.of() : List.of(visitor.acceptLines().getFirst());
    }
}
