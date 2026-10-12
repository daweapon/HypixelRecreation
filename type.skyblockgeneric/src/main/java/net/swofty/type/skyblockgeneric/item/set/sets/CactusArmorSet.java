package net.swofty.type.skyblockgeneric.item.set.sets;

import net.swofty.type.skyblockgeneric.entity.mob.SkyBlockMob;
import net.swofty.type.skyblockgeneric.event.value.SkyBlockValueEvent;
import net.swofty.type.skyblockgeneric.event.value.ValueUpdateEvent;
import net.swofty.type.skyblockgeneric.event.value.events.PlayerDamagedByMobValueUpdateEvent;
import net.swofty.type.skyblockgeneric.item.set.impl.ArmorSet;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;
import net.swofty.type.skyblockgeneric.utility.AttackService;

import java.util.ArrayList;
import java.util.List;

public class CactusArmorSet extends SkyBlockValueEvent implements ArmorSet {
	public static final double REBOUND_PERCENT = 33;

	public static double rebound(double damage) {
		return Math.max(0, damage) * REBOUND_PERCENT / 100D;
	}

	@Override
	public String getName() {
		return "Deflect";
	}

	@Override
	public ArrayList<String> getDescription() {
		return new ArrayList<>(List.of(
				"<7>Rebound <a>33% <7>of the damage you take",
				"<7>back at your enemy."));
	}

	@Override
	public Class<? extends ValueUpdateEvent> getValueEvent() {
		return PlayerDamagedByMobValueUpdateEvent.class;
	}

	@Override
	public void run(ValueUpdateEvent event) {
		SkyBlockPlayer player = event.getPlayer();
		if (!isWearingSet(player)) return;
		if (!(event.getValue() instanceof Number damage)) return;
		SkyBlockMob mob = ((PlayerDamagedByMobValueUpdateEvent) event).getMob();
		AttackService.applyHit(player, mob, (float) rebound(damage.doubleValue()), false);
	}
}
