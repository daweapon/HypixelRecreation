package dev.daweapon.type.bedwarsgame.shop;

import lombok.Getter;
import dev.daweapon.type.bedwarsgame.shop.traps.BlindnessTrap;
import dev.daweapon.type.bedwarsgame.shop.traps.CounterOffensiveTrap;
import dev.daweapon.type.bedwarsgame.shop.traps.MinerFatigueTrap;
import dev.daweapon.type.bedwarsgame.shop.traps.RevealTrap;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

// Registers and looks up the trap types available in the Bedwars team shop.
public class TrapManager {

	@Getter
	private final List<Trap> traps = new ArrayList<>();
	private final Map<TrapId, Trap> trapsById = new EnumMap<>(TrapId.class);

	public TrapManager() {
		register(new BlindnessTrap());
		register(new CounterOffensiveTrap());
		register(new RevealTrap());
		register(new MinerFatigueTrap());
	}

	public Trap getTrap(TrapId id) {
		return trapsById.get(id);
	}

	private void register(Trap trap) {
		traps.add(trap);
		trapsById.put(trap.getId(), trap);
	}

}
