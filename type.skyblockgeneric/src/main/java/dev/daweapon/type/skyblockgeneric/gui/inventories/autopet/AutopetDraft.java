package dev.daweapon.type.skyblockgeneric.gui.inventories.autopet;

import dev.daweapon.type.skyblockgeneric.user.pets.autopet.AutopetRule;
import dev.daweapon.type.skyblockgeneric.user.pets.autopet.AutopetTrigger;
import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
class AutopetDraft {
    private final int slot;
    private @Nullable AutopetTrigger trigger;
    private @Nullable String param;
    private @Nullable String petType;
    private @Nullable String petRarity;
    private final List<AutopetRule.Condition> exceptions = new ArrayList<>();

    AutopetDraft(int slot) {
        this.slot = slot;
    }

    static AutopetDraft from(int slot, AutopetRule rule) {
        AutopetDraft draft = new AutopetDraft(slot);
        draft.trigger = rule.trigger();
        draft.param = rule.param();
        draft.petType = rule.petType();
        draft.petRarity = rule.petRarity();
        draft.exceptions.addAll(rule.exceptions());
        return draft;
    }

    boolean needsParam() {
        return trigger != null && trigger.param() != dev.daweapon.type.skyblockgeneric.user.pets.autopet.AutopetParam.NONE;
    }

    boolean isComplete() {
        return trigger != null && petType != null && (!needsParam() || param != null);
    }

    AutopetRule build() {
        return new AutopetRule(trigger, needsParam() ? param : null, petType, petRarity, List.copyOf(exceptions));
    }
}
