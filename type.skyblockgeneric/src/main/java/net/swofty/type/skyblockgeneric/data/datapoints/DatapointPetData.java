package net.swofty.type.skyblockgeneric.data.datapoints;

import net.swofty.type.skyblockgeneric.user.pets.PetEquipEffects;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import net.swofty.commons.skyblock.item.ItemType;
import net.swofty.commons.skyblock.item.Rarity;
import net.swofty.commons.skyblock.item.UnderstandableSkyBlockItem;
import net.swofty.commons.protocol.Serializer;
import net.swofty.commons.protocol.serializers.UnderstandableSkyBlockItemSerializer;
import net.swofty.type.skyblockgeneric.data.SkyBlockDatapoint;
import net.swofty.type.skyblockgeneric.entity.PetEntityImpl;
import net.swofty.type.skyblockgeneric.item.SkyBlockItem;
import net.swofty.type.skyblockgeneric.item.components.PetComponent;
import net.swofty.type.skyblockgeneric.item.components.PetItemComponent;
import net.swofty.type.skyblockgeneric.item.components.SkullHeadComponent;
import net.swofty.type.skyblockgeneric.item.handlers.pet.abstr.AbilityRuntime;
import net.swofty.type.skyblockgeneric.item.handlers.pet.abstr.PetAbility;
import net.swofty.type.skyblockgeneric.item.handlers.pet.abstr.PetEvent;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;
import net.swofty.type.skyblockgeneric.user.pets.PetItems;
import net.swofty.type.skyblockgeneric.user.pets.PetLevels;
import org.jetbrains.annotations.Nullable;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

// Persists a player's owned pets and which one is active, and manages the active pet's entity,
// abilities, and ability runtime state.
public class DatapointPetData extends SkyBlockDatapoint<DatapointPetData.UserPetData> {
    public static final int BASE_CAPACITY = 112;
    public static final int EXP_SHARE_SLOTS = 3;
    private static final int FORMAT = 2;

    private static final Serializer serializer = new Serializer<UserPetData>() {
        @Override
        public String serialize(UserPetData value) {
            JSONArray pets = new JSONArray();
            for (Map.Entry<SkyBlockItem, Boolean> entry : value.getPetsMap().entrySet()) {
                SkyBlockItem pet = entry.getKey();
                pets.put(new JSONObject()
                        .put("item", new UnderstandableSkyBlockItemSerializer().serialize(pet.toUnderstandable()))
                        .put("active", entry.getValue())
                        .put("favorite", value.isFavorite(pet))
                        .put("expShare", value.getExpShareSlot(pet)));
            }
            return new JSONObject()
                    .put("format", FORMAT)
                    .put("pets", pets)
                    .put("visibility", value.getVisibility().name())
                    .put("nameTags", value.isNameTags())
                    .toString();
        }

        @Override
        public UserPetData deserialize(String json) {
            JSONObject root = new JSONObject(json);
            UserPetData data = new UserPetData(new HashMap<>());
            if (!root.has("format")) {
                root.keySet().forEach(key -> data.petsMap.put(itemFrom(key), root.getBoolean(key)));
                return data;
            }

            JSONArray pets = root.getJSONArray("pets");
            for (int i = 0; i < pets.length(); i++) {
                JSONObject entry = pets.getJSONObject(i);
                SkyBlockItem pet = itemFrom(entry.getString("item"));
                data.petsMap.put(pet, entry.optBoolean("active"));
                if (entry.optBoolean("favorite")) data.favorites.add(pet);
                int slot = entry.optInt("expShare", -1);
                if (slot >= 0 && slot < EXP_SHARE_SLOTS) data.expShare[slot] = pet;
            }
            data.visibility = PetVisibility.fromName(root.optString("visibility"));
            data.nameTags = root.optBoolean("nameTags", true);
            return data;
        }

        private SkyBlockItem itemFrom(String serialized) {
            return new SkyBlockItem(new UnderstandableSkyBlockItemSerializer().deserialize(serialized));
        }

        @Override
        public UserPetData clone(UserPetData value) {
            return deserialize(serialize(value));
        }
    };

    public DatapointPetData(String key, UserPetData value) {
        super(key, value, serializer);
    }

    public DatapointPetData(String key) {
        super(key, new UserPetData(new HashMap<>()), serializer);
    }

    public record ActiveAbility(SkyBlockItem pet, PetAbility ability) {
    }

    private static final Comparator<SkyBlockItem> BEST_COPY = Comparator
            .comparingInt((SkyBlockItem pet) -> pet.getAttributeHandler().getRarity().ordinal())
            .thenComparingInt(PetLevels::level);

    @Getter
    @JsonIgnoreProperties(ignoreUnknown = true) // Due to protocols serializing "enabledPet"
    public static class UserPetData {
        private HashMap<SkyBlockItem, Boolean> petsMap;
        @JsonIgnore
        private final Set<SkyBlockItem> favorites = Collections.newSetFromMap(new IdentityHashMap<>());
        @JsonIgnore
        private final SkyBlockItem[] expShare = new SkyBlockItem[EXP_SHARE_SLOTS];
        @JsonIgnore
        private PetVisibility visibility = PetVisibility.ALL;
        @JsonIgnore
        private boolean nameTags = true;
        private PetEntityImpl enabledPetEntityImpl = null;
        @JsonIgnore
        private transient List<ActiveAbility> activeAbilities;
        private final transient Map<PetAbility, AbilityRuntime> abilityRuntimes = new HashMap<>();

        public UserPetData() {
            this.petsMap = new HashMap<>();
        }

        public UserPetData(HashMap<SkyBlockItem, Boolean> pets) {
            this.petsMap = pets;
        }

        public void addPet(SkyBlockItem pet) {
            petsMap.put(pet, false);
            activeAbilities = null;
        }

        public void setEnabled(ItemType type, boolean enabled) {
            SkyBlockItem pet = getPet(type);
            if (pet == null) {
                deselectCurrent();
                return;
            }
            setEnabled(pet, enabled);
        }

        public void setEnabled(SkyBlockItem pet, boolean enabled) {
            petsMap.keySet().forEach(owned -> petsMap.put(owned, false));
            if (petsMap.containsKey(pet)) petsMap.put(pet, enabled);

            if (enabledPetEntityImpl != null)
                enabledPetEntityImpl.remove();

            refreshCachedAbilities();
        }

        public int capacity() {
            return BASE_CAPACITY;
        }

        public boolean isFull() {
            return petsMap.size() >= capacity();
        }

        public boolean isFavorite(SkyBlockItem pet) {
            return favorites.contains(pet);
        }

        public boolean toggleFavorite(SkyBlockItem pet) {
            if (favorites.remove(pet)) return false;
            favorites.add(pet);
            return true;
        }

        public int getExpShareSlot(SkyBlockItem pet) {
            for (int i = 0; i < expShare.length; i++) {
                if (expShare[i] == pet) return i;
            }
            return -1;
        }

        public @Nullable SkyBlockItem getExpSharePet(int slot) {
            return expShare[slot];
        }

        public void setExpSharePet(int slot, @Nullable SkyBlockItem pet) {
            if (pet != null) {
                int previous = getExpShareSlot(pet);
                if (previous >= 0) expShare[previous] = null;
            }
            expShare[slot] = pet;
        }

        public void setVisibility(PetVisibility visibility) {
            this.visibility = visibility;
        }

        public void setNameTags(boolean nameTags) {
            this.nameTags = nameTags;
        }

        public void updatePetEntityImpl(SkyBlockPlayer player) {
            if (enabledPetEntityImpl != null) {
                enabledPetEntityImpl.kill();
                enabledPetEntityImpl.remove();
            }
            enabledPetEntityImpl = null;
            if (player == null) return;
            PetEquipEffects.apply(player);
            if (player.getInstance() == null) return;

            SkyBlockItem enabledPet = getEnabledPet();
            if (enabledPet != null) {
                enabledPetEntityImpl = new PetEntityImpl(player, enabledPet.getComponent(SkullHeadComponent.class).getSkullTexture(enabledPet), enabledPet);
                enabledPetEntityImpl.setInstance(player.getInstance(), player.getPosition());
            }
        }

        public void isEnabled(ItemType type) {
            petsMap.keySet().stream().filter(pet -> pet.getAttributeHandler().getPotentialType() == type).findFirst().ifPresent(petsMap::get);
        }

        public void deselectCurrent() {
            petsMap.keySet().forEach(pet -> petsMap.put(pet, false));
            this.activeAbilities = null;
        }

        public @Nullable SkyBlockItem getEnabledPet() {
            return petsMap.keySet().stream().filter(petsMap::get).findFirst().orElse(null);
        }

        @JsonIgnore
        public List<ActiveAbility> getActiveAbilities() {
            List<ActiveAbility> cached = activeAbilities;
            if (cached == null) {
                cached = computeActiveAbilities();
                activeAbilities = cached;
            }
            return cached;
        }

        private List<ActiveAbility> computeActiveAbilities() {
            List<ActiveAbility> active = new ArrayList<>();
            SkyBlockItem summoned = getEnabledPet();
            ItemType summonedType = summoned == null ? null : summoned.getAttributeHandler().getPotentialType();
            if (summoned != null) {
                addAbilities(active, summoned);
                PetItemComponent held = PetItems.held(summoned);
                if (held != null && held.getAbility() != null) active.add(new ActiveAbility(summoned, held.getAbility()));
            }

            Map<ItemType, SkyBlockItem> passive = new HashMap<>();
            for (SkyBlockItem pet : petsMap.keySet()) {
                if (!pet.hasComponent(PetComponent.class)) continue;
                ItemType type = pet.getAttributeHandler().getPotentialType();
                if (!pet.getComponent(PetComponent.class).isPassive() || type == summonedType) continue;
                passive.merge(type, pet, (a, b) -> BEST_COPY.compare(a, b) >= 0 ? a : b);
            }
            passive.values().forEach(pet -> addAbilities(active, pet));
            return List.copyOf(active);
        }

        private static void addAbilities(List<ActiveAbility> active, SkyBlockItem pet) {
            if (!pet.hasComponent(PetComponent.class)) return;
            for (PetAbility ability : pet.getComponent(PetComponent.class).abilitiesFor(pet)) active.add(new ActiveAbility(pet, ability));
        }

        public AbilityRuntime getAbilityRuntime(PetAbility ability) {
            return abilityRuntimes.computeIfAbsent(ability, _ -> new AbilityRuntime());
        }

        public <E extends PetEvent> E dispatch(E event) {
            for (ActiveAbility active : getActiveAbilities()) {
                active.ability().onEvent(event, active.pet());
            }
            return event;
        }

        public void refreshCachedAbilities() {
            this.activeAbilities = null;
        }

        public @Nullable SkyBlockItem getPet(ItemType type) {
            return petsMap.keySet().stream().filter(pet -> pet.getAttributeHandler().getPotentialType() == type).findFirst().orElse(null);
        }

        public void removePet(SkyBlockItem pet) {
            boolean wasActive = Boolean.TRUE.equals(petsMap.remove(pet));
            activeAbilities = null;
            favorites.remove(pet);
            int slot = getExpShareSlot(pet);
            if (slot >= 0) expShare[slot] = null;
            if (wasActive) {
                if (enabledPetEntityImpl != null) {
                    enabledPetEntityImpl.kill();
                    enabledPetEntityImpl.remove();
                    enabledPetEntityImpl = null;
                }
            }
        }
    }
}
