package net.swofty.type.skyblockgeneric.item.components;

import net.swofty.commons.StringUtility;
import net.swofty.type.skyblockgeneric.gui.inventories.abiphone.AbiphoneView;
import lombok.Getter;
import net.swofty.type.skyblockgeneric.item.SkyBlockItem;
import net.swofty.type.skyblockgeneric.item.SkyBlockItemComponent;
import net.swofty.type.skyblockgeneric.item.handlers.lore.LoreConfig;

import java.util.ArrayList;
import java.util.List;

public class AbiphoneComponent extends SkyBlockItemComponent {

	public static final int MAX_CONTACT_ADDONS = 75;

	@Getter
	private final int maxContacts;

	public AbiphoneComponent(int maxContacts, int maxDiscs, List<AbiphoneFeature> features) {
		this.maxContacts = maxContacts;
		addInheritedComponent(new LoreUpdateComponent(new LoreConfig(
				(item, player) -> lore(item, maxDiscs, features), null), false));
		addInheritedComponent(new InteractableComponent(
				(player, item) -> player.openView(new AbiphoneView(), new AbiphoneView.State(item)),
				null,
				null
		));
		addInheritedComponent(new TrackedUniqueComponent());
	}

	public int contactLimit(SkyBlockItem abiphone) {
		return maxContacts + abiphone.getAttributeHandler().getAbiphoneExtraContacts();
	}

	private List<String> lore(SkyBlockItem abiphone, int maxDiscs, List<AbiphoneFeature> features) {
		int extra = abiphone.getAttributeHandler().getAbiphoneExtraContacts();
		List<String> lore = new ArrayList<>(List.of(
				"<7>A device that can be used to contact",
				"<7>people! Click NPCs to add them to ",
				"<7>your contacts!",
				" ",
				"<7>Features:",
				" <7>Maximum Contacts: <b>" + contactLimit(abiphone) + (extra > 0 ? " <8>(+" + extra + " from Contacts Trios)" : "")
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
