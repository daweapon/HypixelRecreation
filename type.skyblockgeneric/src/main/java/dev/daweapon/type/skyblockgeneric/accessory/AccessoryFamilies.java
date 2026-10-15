package dev.daweapon.type.skyblockgeneric.accessory;

import dev.daweapon.commons.YamlFileUtils;
import org.jetbrains.annotations.Nullable;
import org.tinylog.Logger;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Every accessory family from {@code configuration/skyblock/accessories/families.yml}. Within a family
 * only one member counts; an accessory in no family stands alone.
 */
public final class AccessoryFamilies {
    private static final List<File> LOCATIONS = List.of(
            new File("./configuration/skyblock/accessories/families.yml"),
            new File("../configuration/skyblock/accessories/families.yml"));
    private static volatile AccessoryFamilies instance;

    private final Map<String, AccessoryFamily> byId;
    private final Map<String, AccessoryFamily> byMember;

    AccessoryFamilies(List<AccessoryFamily> families) {
        Map<String, AccessoryFamily> ids = new LinkedHashMap<>();
        Map<String, AccessoryFamily> members = new HashMap<>();
        for (AccessoryFamily family : families) {
            if (ids.put(family.id(), family) != null)
                throw new IllegalStateException("Accessory family " + family.id() + " is listed twice");
            for (String member : family.members()) {
                AccessoryFamily previous = members.put(member, family);
                if (previous != null)
                    throw new IllegalStateException(member + " is in both " + previous.id() + " and " + family.id());
            }
        }
        this.byId = ids;
        this.byMember = members;
    }

    public static AccessoryFamilies get() {
        if (instance == null) {
            synchronized (AccessoryFamilies.class) {
                if (instance == null) instance = load();
            }
        }
        return instance;
    }

    public @Nullable AccessoryFamily familyOf(String itemId) {
        return byMember.get(itemId);
    }

    public @Nullable AccessoryFamily byId(String familyId) {
        return byId.get(familyId);
    }

    public Collection<AccessoryFamily> all() {
        return byId.values();
    }

    /** Whether another member of the item's family is the one that counts at best. */
    public boolean isSuperseded(String itemId) {
        AccessoryFamily family = familyOf(itemId);
        return family != null && !family.best().equals(itemId);
    }

    static AccessoryFamilies fromFile(File file) throws IOException {
        return new AccessoryFamilies(parse(YamlFileUtils.loadYaml(file)));
    }

    private static AccessoryFamilies load() {
        for (File file : LOCATIONS) {
            if (!file.exists()) continue;
            try {
                return fromFile(file);
            } catch (Exception exception) {
                Logger.error(exception, "Failed to load accessory families from {}", file.getPath());
                return new AccessoryFamilies(List.of());
            }
        }
        Logger.error("No accessory families file found; every accessory counts on its own");
        return new AccessoryFamilies(List.of());
    }

    @SuppressWarnings("unchecked")
    private static List<AccessoryFamily> parse(Map<String, Object> root) {
        List<AccessoryFamily> families = new ArrayList<>();
        Object list = root == null ? null : root.get("families");
        if (!(list instanceof List<?> entries)) return families;
        for (Object entry : entries) {
            Map<String, Object> raw = (Map<String, Object>) entry;
            String id = String.valueOf(raw.get("id"));
            List<String> members = ((List<Object>) raw.get("members")).stream().map(String::valueOf).toList();
            if (members.isEmpty()) throw new IllegalStateException("Accessory family " + id + " has no members");
            AccessoryFamily.Type type = AccessoryFamily.Type.valueOf(String.valueOf(raw.getOrDefault("type", "UPGRADE_LINE")));
            String pick = raw.get("pick") == null ? null : String.valueOf(raw.get("pick"));
            if (pick != null && !members.contains(pick))
                throw new IllegalStateException("Accessory family " + id + " picks " + pick + ", which is not a member");
            families.add(new AccessoryFamily(id, String.valueOf(raw.getOrDefault("name", id)), type, members, pick));
        }
        return families;
    }
}
