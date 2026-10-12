package net.swofty.type.skyblockgeneric.item.events;

import net.swofty.commons.skyblock.item.ItemType;
import net.swofty.type.skyblockgeneric.event.value.SkyBlockValueEvent;
import net.swofty.type.skyblockgeneric.event.value.ValueUpdateEvent;
import net.swofty.type.skyblockgeneric.event.value.events.PlayerDamagedByMobValueUpdateEvent;
import net.swofty.type.skyblockgeneric.item.SkyBlockItem;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class GuardianChestplateEvent extends SkyBlockValueEvent {
	public static final long COOLDOWN_MILLIS = 60_000;
	private static final Map<UUID, Long> LAST_BLOCK = new ConcurrentHashMap<>();

	public static boolean canBlock(boolean atFullHealth, long lastBlockMillis, long nowMillis) {
		return atFullHealth && nowMillis - lastBlockMillis >= COOLDOWN_MILLIS;
	}

	@Override
	public Class<? extends ValueUpdateEvent> getValueEvent() {
		return PlayerDamagedByMobValueUpdateEvent.class;
	}

	@Override
	public void run(ValueUpdateEvent event) {
		SkyBlockPlayer player = event.getPlayer();
		ItemType worn = new SkyBlockItem(player.getChestplate()).getAttributeHandler().getPotentialType();
		if (worn != ItemType.GUARDIAN_CHESTPLATE) return;
		long now = System.currentTimeMillis();
		boolean fullHealth = player.getHealth() >= player.getMaxHealth();
		if (!canBlock(fullHealth, LAST_BLOCK.getOrDefault(player.getUuid(), Long.MIN_VALUE / 2), now)) return;
		LAST_BLOCK.put(player.getUuid(), now);
		event.setValue(event.getValue() instanceof Float ? (Object) 0f : (Object) 0D);
	}
}
