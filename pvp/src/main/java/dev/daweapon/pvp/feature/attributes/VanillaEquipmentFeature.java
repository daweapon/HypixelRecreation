package dev.daweapon.pvp.feature.attributes;

import net.minestom.server.entity.EquipmentSlot;
import net.minestom.server.entity.LivingEntity;
import net.minestom.server.event.EventNode;
import net.minestom.server.event.item.EntityEquipEvent;
import net.minestom.server.event.player.PlayerChangeHeldSlotEvent;
import net.minestom.server.event.trait.EntityInstanceEvent;
import net.minestom.server.item.ItemStack;
import dev.daweapon.pvp.enums.ArmorMaterial;
import dev.daweapon.pvp.enums.Tool;
import dev.daweapon.pvp.feature.FeatureType;
import dev.daweapon.pvp.feature.RegistrableFeature;
import dev.daweapon.pvp.feature.config.DefinedFeature;
import dev.daweapon.pvp.feature.config.FeatureConfiguration;
import dev.daweapon.pvp.utils.CombatVersion;

/**
 * Vanilla implementation of {@link EquipmentFeature}
 */
public class VanillaEquipmentFeature implements EquipmentFeature, RegistrableFeature {
	public static final DefinedFeature<VanillaEquipmentFeature> DEFINED = new DefinedFeature<>(
			FeatureType.EQUIPMENT, VanillaEquipmentFeature::new,
			FeatureType.VERSION
	);

	private final FeatureConfiguration configuration;

	//TODO this probably shouldn't work this way
	// We probably want to store all the tools & armor separately per DataFeature
	private CombatVersion version;

	public VanillaEquipmentFeature(FeatureConfiguration configuration) {
		this.configuration = configuration;
	}

	@Override
	public void initDependencies() {
		this.version = configuration.get(FeatureType.VERSION);
	}

	@Override
	public void init(EventNode<EntityInstanceEvent> node) {
		node.addListener(EntityEquipEvent.class, this::onEquip);
		node.addListener(PlayerChangeHeldSlotEvent.class, event -> {
			LivingEntity entity = event.getPlayer();
			ItemStack newItem = event.getPlayer().getInventory().getItemStack(event.getNewSlot());
			Tool.updateEquipmentAttributes(entity, entity.getEquipment(EquipmentSlot.MAIN_HAND), newItem, EquipmentSlot.MAIN_HAND, version);
		});
	}

	protected void onEquip(EntityEquipEvent event) {
		if (!(event.getEntity() instanceof LivingEntity entity)) return;

		EquipmentSlot slot = event.getSlot();
		if (slot.isArmor()) {
			ArmorMaterial.updateEquipmentAttributes(entity, entity.getEquipment(slot), event.getEquippedItem(), slot, version);
		} else if (slot.isHand()) {
			Tool.updateEquipmentAttributes(entity, entity.getEquipment(slot), event.getEquippedItem(), slot, version);
		}
	}
}
