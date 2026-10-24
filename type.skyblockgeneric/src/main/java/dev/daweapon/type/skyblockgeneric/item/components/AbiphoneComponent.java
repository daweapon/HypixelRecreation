package dev.daweapon.type.skyblockgeneric.item.components;

import dev.daweapon.commons.StringUtility;
import dev.daweapon.commons.skyblock.item.ItemType;
import dev.daweapon.type.skyblockgeneric.abiphone.AbiphoneCalls;
import dev.daweapon.type.skyblockgeneric.accessory.AccessoryOwnership;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;
import dev.daweapon.type.skyblockgeneric.gui.inventories.abiphone.AbiphoneView;
import lombok.Getter;
import dev.daweapon.type.skyblockgeneric.item.SkyBlockItem;
import dev.daweapon.type.skyblockgeneric.item.SkyBlockItemComponent;
import dev.daweapon.type.skyblockgeneric.item.handlers.lore.LoreConfig;

import java.util.ArrayList;
import java.util.List;

public class AbiphoneComponent extends SkyBlockItemComponent {

	public static final int MAX_CONTACT_ADDONS = 75;

	@Getter
	private final int maxContacts;

	@Getter
	private final List<AbiphoneFeature> features;

	public AbiphoneComponent(int maxContacts, int maxDiscs, List<AbiphoneFeature> features) {
		this.maxContacts = maxContacts;
		this.features = List.copyOf(features);
		addInheritedComponent(new LoreUpdateComponent(new LoreConfig(
				(item, player) -> lore(player, item, maxDiscs, features), null), false));
		addInheritedComponent(new InteractableComponent(
				(player, item) -> player.openView(new AbiphoneView(), new AbiphoneView.State(item)),
				(player, item) -> AbiphoneCalls.speedDial(player, item),
				null
		));
		addInheritedComponent(new TrackedUniqueComponent());
	}

	public int contactLimit(SkyBlockItem abiphone) {
		return maxContacts + abiphone.getAttributeHandler().getAbiphoneExtraContacts();
	}

	public int contactLimit(SkyBlockPlayer player, SkyBlockItem abiphone) {
		return contactLimit(abiphone) + caseSlots(player);
	}

	public static int caseSlots(SkyBlockPlayer player) {
		if (player == null) return 0;
		if (AccessoryOwnership.owns(player, ItemType.SUMSUNG_GG_ABICASE)) return 2;
		return AccessoryOwnership.owns(player, ItemType.SUMSUNG_G3_ABICASE) ? 1 : 0;
	}

	private List<String> lore(SkyBlockPlayer player, SkyBlockItem abiphone, int maxDiscs, List<AbiphoneFeature> features) {
		int extra = abiphone.getAttributeHandler().getAbiphoneExtraContacts();
		List<String> lore = new ArrayList<>(List.of(
				"<7>A device that can be used to contact",
				"<7>people! Click NPCs to add them to ",
				"<7>your contacts!",
				" ",
				"<7>Features:",
				" <7>Maximum Contacts: <b>" + contactLimit(player, abiphone) + (extra > 0 ? " <8>(+" + extra + " from Contacts Trios)" : "")
		));

		if (maxDiscs > 0) {
			lore.add(" <7>Maximum Music Discs: <b>" + maxDiscs);
		}

		for (AbiphoneFeature feature : features) {
			lore.add(" " + feature.colorCode + "<l>" + StringUtility.toNormalCase(feature.name()));
		}

		lore.add(" ");
		lore.add("<e>Right-click to open!");
		return lore;
	}

	public enum AbiphoneFeature {
		CONTACTS_DIRECTORY("<a>"),
		DO_NOT_DISTURB_MODE("<c>"),
		RINGTONES("<6>"),
		TIC_TAC_TOE("<5>"),
		SNAKE("<5>"),
		SPEED_DIAL("<b>");

		public final String colorCode;

		AbiphoneFeature(String colorCode) {
			this.colorCode = colorCode;
		}
	}

}
