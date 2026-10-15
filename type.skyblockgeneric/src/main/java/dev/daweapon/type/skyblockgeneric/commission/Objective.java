package dev.daweapon.type.skyblockgeneric.commission;

// Describes what a commission requires: the action type, target, amount, location, and event.
public class Objective {
	public final ObjectiveType type;
	public final BlockTarget target;
	public final int amount;
	public final LocationSelector location;
	public final EventType event;

	public Objective(
			ObjectiveType type,
			BlockTarget target,
			int amount,
			LocationSelector location,
			EventType event
	) {
		this.type = type;
		this.target = target;
		this.amount = amount;
		this.location = location;
		this.event = event;
	}

	public enum BlockTarget {
		NONE,
		GOBLIN,
		GLACITE_WALKER,
		TREASURE_HOARDER,
		GOLDEN_GOBLIN,
		STAR_SENTRY,
		MITHRIL,
		TITANIUM,
		HOLLOWS_AUTOMATON,
		HOLLOWS_SLUDGE,
		HOLLOWS_TREASURITE_MEMBER,
		HOLLOWS_GOBLIN,
		HOLLOWS_YOG,
		HOLLOWS_THYST,
		HOLLOWS_BOSS_CORLEONE,
		HOLLOWS_CRYSTAL,
		HARD_STONE,
		CHEST,
		GEM_AMBER,
		GEM_AMETHYST,
		GEM_JADE,
		GEM_RUBY,
		GEM_SAPPHIRE,
		GEM_TOPAZ,
		UMBER,
		TUNGSTEN,
		GLACITE,
		AQUAMARINE,
		CITRINE,
		ONYX,
		PERIDOT;

		public boolean isGlacite() {
			return ordinal() >= UMBER.ordinal();
		}

		public boolean isGemstone() {
			return ordinal() >= GEM_AMBER.ordinal() && ordinal() <= GEM_TOPAZ.ordinal();
		}
	}
}
