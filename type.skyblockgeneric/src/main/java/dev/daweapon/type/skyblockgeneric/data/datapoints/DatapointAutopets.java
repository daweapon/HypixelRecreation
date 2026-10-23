package dev.daweapon.type.skyblockgeneric.data.datapoints;

import dev.daweapon.commons.protocol.Serializer;
import dev.daweapon.type.skyblockgeneric.data.SkyBlockDatapoint;
import dev.daweapon.type.skyblockgeneric.user.pets.autopet.AutopetException;
import dev.daweapon.type.skyblockgeneric.user.pets.autopet.AutopetRule;
import dev.daweapon.type.skyblockgeneric.user.pets.autopet.AutopetTrigger;
import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.Nullable;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class DatapointAutopets extends SkyBlockDatapoint<DatapointAutopets.AutopetData> {
    public static final int SLOTS_PER_PACK = 2;
    public static final int MAX_RULES = 28;
    public static final int MAX_PACKS = MAX_RULES / SLOTS_PER_PACK;

    private static final Serializer<AutopetData> serializer = new Serializer<>() {
        @Override
        public String serialize(AutopetData value) {
            JSONArray rules = new JSONArray();
            for (AutopetRule rule : value.rules) {
                if (rule == null) {
                    rules.put(JSONObject.NULL);
                    continue;
                }
                JSONArray exceptions = new JSONArray();
                rule.exceptions().forEach(condition -> exceptions.put(new JSONObject()
                        .put("type", condition.exception().name())
                        .put("param", condition.param() == null ? JSONObject.NULL : condition.param())));
                rules.put(new JSONObject()
                        .put("trigger", rule.trigger().name())
                        .put("param", rule.param() == null ? JSONObject.NULL : rule.param())
                        .put("pet", rule.petType())
                        .put("rarity", rule.petRarity())
                        .put("exceptions", exceptions));
            }
            return new JSONObject().put("packs", value.packs).put("rules", rules).toString();
        }

        @Override
        public AutopetData deserialize(String json) {
            JSONObject root = new JSONObject(json);
            AutopetData data = new AutopetData();
            data.packs = root.optInt("packs", 0);
            JSONArray rules = root.optJSONArray("rules");
            for (int i = 0; rules != null && i < Math.min(rules.length(), MAX_RULES); i++) {
                JSONObject rule = rules.optJSONObject(i);
                if (rule == null) continue;
                List<AutopetRule.Condition> exceptions = new ArrayList<>();
                JSONArray stored = rule.optJSONArray("exceptions");
                for (int j = 0; stored != null && j < stored.length(); j++) {
                    JSONObject condition = stored.getJSONObject(j);
                    exceptions.add(new AutopetRule.Condition(AutopetException.valueOf(condition.getString("type")),
                            condition.isNull("param") ? null : condition.getString("param")));
                }
                data.rules[i] = new AutopetRule(AutopetTrigger.valueOf(rule.getString("trigger")),
                        rule.isNull("param") ? null : rule.getString("param"),
                        rule.getString("pet"), rule.getString("rarity"), exceptions);
            }
            return data;
        }

        @Override
        public AutopetData clone(AutopetData value) {
            return deserialize(serialize(value));
        }
    };

    public DatapointAutopets(String key, AutopetData value) {
        super(key, value, serializer);
    }

    public DatapointAutopets(String key) {
        this(key, new AutopetData());
    }

    @Getter
    public static class AutopetData {
        @Setter
        private int packs;
        private final AutopetRule[] rules = new AutopetRule[MAX_RULES];

        public int unlockedSlots() {
            return Math.min(MAX_RULES, packs * SLOTS_PER_PACK);
        }

        public boolean addPack() {
            if (packs >= MAX_PACKS) return false;
            packs++;
            return true;
        }

        public @Nullable AutopetRule rule(int slot) {
            return slot >= 0 && slot < unlockedSlots() ? rules[slot] : null;
        }

        public void setRule(int slot, @Nullable AutopetRule rule) {
            if (slot >= 0 && slot < unlockedSlots()) rules[slot] = rule;
        }
    }
}
