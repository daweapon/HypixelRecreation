package dev.daweapon.type.skyblockgeneric.commission;

import dev.daweapon.type.skyblockgeneric.region.RegionType;

import java.util.Objects;
import java.util.Optional;

// Restricts a Dwarven Mines commission to a specific region, or allows it to be completed anywhere.
public final class LocationSelector {

	private static final LocationSelector ANY = new LocationSelector(null);
	private final RegionType region;

	private LocationSelector(RegionType region) {
		this.region = region;
	}

	public static LocationSelector any() {
		return ANY;
	}

	public static LocationSelector of(RegionType region) {
		Objects.requireNonNull(region);
		return new LocationSelector(region);
	}

	public static LocationSelector glaciteTunnels() {
		return new LocationSelector(RegionType.GLACITE_TUNNELS);
	}

	public boolean allows(RegionType region) {
		if (this.region == RegionType.GLACITE_TUNNELS) {
			return region == RegionType.GLACITE_TUNNELS || region == RegionType.GREAT_GLACITE_LAKE || region == RegionType.GRANDPA_WOLFS_CAVE;
		}
		return this.region == null || this.region == region;
	}

	public boolean isAny() {
		return region == null;
	}

	public Optional<RegionType> getRegion() {
		return Optional.ofNullable(region);
	}

}
