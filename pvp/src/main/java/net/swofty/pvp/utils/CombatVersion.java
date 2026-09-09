package net.swofty.pvp.utils;

import net.swofty.pvp.feature.CombatFeature;

// Marker feature selecting whether combat mechanics behave as modern (1.9+) or legacy (pre-1.9) vanilla.
public final class CombatVersion implements CombatFeature {
	public static CombatVersion MODERN = new CombatVersion(false);
	public static CombatVersion LEGACY = new CombatVersion(true);
	
	private final boolean legacy;
	
	CombatVersion(boolean legacy) {
		this.legacy = legacy;
	}
	
	public boolean modern() {
		return !legacy;
	}
	
	public boolean legacy() {
		return legacy;
	}
	
	public static CombatVersion fromLegacy(boolean legacy) {
		return legacy ? LEGACY : MODERN;
	}
	
	@Override
	public String toString() {
		return "CombatVersion[legacy=" + legacy + "]";
	}
	
}
