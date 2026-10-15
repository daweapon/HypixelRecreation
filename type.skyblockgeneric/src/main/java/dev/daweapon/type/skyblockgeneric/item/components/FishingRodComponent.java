package dev.daweapon.type.skyblockgeneric.item.components;

import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;
import dev.daweapon.type.skyblockgeneric.entity.FishingHook;
import dev.daweapon.type.skyblockgeneric.fishing.item.FishingItemSupport;
import dev.daweapon.type.skyblockgeneric.fishing.FishingService;
import dev.daweapon.type.skyblockgeneric.item.SkyBlockItemComponent;

public class FishingRodComponent extends SkyBlockItemComponent {

	public FishingRodComponent() {
		addInheritedComponent(new InteractableComponent(
				(player, item) -> {
					FishingHook hook = FishingHook.getFishingHookForOwner(player);
					if (hook != null) {
						hook.tryRetrieve(item);
						hook.remove();
						FishingService.clearSession(player.getUuid());
						float pitch = 0.8f + (float) (Math.random() * 0.4f);
						player.playSound(
								Sound.sound()
										.type(Key.key("entity.fishing_bobber.retrieve"))
										.source(Sound.Source.NEUTRAL)
										.volume(1f)
										.pitch(pitch)
										.build()
						);
					} else {
						String itemId = item.getAttributeHandler().getPotentialType() == null ? null : item.getAttributeHandler().getPotentialType().name();
                        var metadata = FishingItemSupport.getRodMetadata(itemId);
                        if (metadata != null && metadata.getLegacyConversionTarget() != null) {
							return;
						}
						new FishingHook(player, item).spawn(player.getInstance());
					}
				},
				null,
				null
		));
	}

}
