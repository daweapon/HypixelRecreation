package net.swofty.type.bedwarsgame.shop;

import lombok.Getter;

// A single purchasable level of a team upgrade: its price, currency, and description.
@Getter
public class TeamUpgradeTier {
	private final int level;
	private final String description;
	private final int price;
	private final Currency currency;

	public TeamUpgradeTier(int level, String description, int price, Currency currency) {
		this.level = level;
		this.description = description;
		this.price = price;
		this.currency = currency;
	}
}

