package dev.daweapon.service.api.control.admin;

import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

// An island is reachable through a profile id, the profile document's island_uuid or the co-op
// link's island_uuid (the same three routes PlayerWipeService.islandIdsOf follows), so it is only
// orphaned when none of them leads to it.
public final class OrphanClassifier {
    public static final String PROFILE = "PROFILE";
    public static final String ISLAND = "ISLAND";
    public static final String COOP = "COOP";

    public record ProfileFacts(String profileId, @Nullable String ownerField, @Nullable String islandRef, double coins) {}

    public record CoopFacts(String coopId, List<String> memberProfiles) {}

    public record Inputs(Map<String, ProfileFacts> profiles, Map<String, String> ownerByIndexedProfile,
                         Set<String> accounts, Set<String> islands, Set<String> linkedIslandRefs,
                         List<CoopFacts> coops) {}

    public record Orphan(String kind, String id, String reason, @Nullable String related) {}

    private OrphanClassifier() {}

    public static List<Orphan> classify(Inputs inputs) {
        List<Orphan> orphans = new ArrayList<>();

        for (ProfileFacts profile : inputs.profiles().values()) {
            String indexedOwner = inputs.ownerByIndexedProfile().get(profile.profileId());
            if (indexedOwner != null) continue;
            String owner = profile.ownerField();
            String reason = owner == null ? "no owner recorded and no account lists it"
                    : inputs.accounts().contains(owner) ? "owner's account does not list this profile"
                    : "owner account is missing";
            orphans.add(new Orphan(PROFILE, profile.profileId(), reason, owner));
        }

        Set<String> reachable = new HashSet<>(inputs.profiles().keySet());
        inputs.profiles().values().forEach(profile -> {
            if (profile.islandRef() != null) reachable.add(profile.islandRef());
        });
        reachable.addAll(inputs.linkedIslandRefs());
        for (String island : inputs.islands()) {
            if (!reachable.contains(island)) {
                orphans.add(new Orphan(ISLAND, island, "no profile or co-op points at this island", null));
            }
        }

        for (CoopFacts coop : inputs.coops()) {
            List<String> missing = coop.memberProfiles().stream()
                    .filter(profileId -> !inputs.profiles().containsKey(profileId))
                    .toList();
            if (!missing.isEmpty()) {
                orphans.add(new Orphan(COOP, coop.coopId(), "points at " + missing.size() + " missing profile(s)",
                        String.join(",", missing)));
            }
        }
        return orphans;
    }

    public static double totalCoins(Inputs inputs) {
        return inputs.profiles().values().stream().mapToDouble(ProfileFacts::coins).sum();
    }
}
