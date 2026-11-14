package dev.daweapon.type.bedwarsgame.shop;

import lombok.Getter;
import dev.daweapon.type.bedwarsgame.shop.upgrades.CushionedBootsUpgrade;
import dev.daweapon.type.bedwarsgame.shop.upgrades.ForgeUpgrade;
import dev.daweapon.type.bedwarsgame.shop.upgrades.HealPoolUpgrade;
import dev.daweapon.type.bedwarsgame.shop.upgrades.ManiacMinerUpgrade;
import dev.daweapon.type.bedwarsgame.shop.upgrades.ReinforcedArmorUpgrade;
import dev.daweapon.type.bedwarsgame.shop.upgrades.SharpenedSwordUpgrade;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

// Registers and looks up the team-wide upgrades available in the Bedwars team shop.
public class TeamShopManager {

	@Getter
	private final List<TeamUpgrade> upgrades = new ArrayList<>();
	private final Map<TeamUpgradeId, TeamUpgrade> upgradesById = new EnumMap<>(TeamUpgradeId.class);

	public TeamShopManager() {
		register(new SharpenedSwordUpgrade());
		register(new ReinforcedArmorUpgrade());
		register(new ManiacMinerUpgrade());
		register(new HealPoolUpgrade());
		register(new ForgeUpgrade());
		register(new CushionedBootsUpgrade());
	}

	@Nullable
	public TeamUpgrade getUpgrade(TeamUpgradeId id) {
		return upgradesById.get(id);
	}

	private void register(TeamUpgrade upgrade) {
		upgrades.add(upgrade);
		upgradesById.put(upgrade.getId(), upgrade);
	}
}
