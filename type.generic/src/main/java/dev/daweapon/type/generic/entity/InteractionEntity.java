package dev.daweapon.type.generic.entity;

import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.LivingEntity;
import net.minestom.server.entity.metadata.other.InteractionMeta;
import net.minestom.server.event.player.PlayerEntityInteractEvent;
import dev.daweapon.type.generic.user.HypixelPlayer;

import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class InteractionEntity extends LivingEntity {

	public final BiConsumer<HypixelPlayer, PlayerEntityInteractEvent> onClick;
	private Consumer<HypixelPlayer> attackHandler;

	public InteractionEntity(float width, float height, BiConsumer<HypixelPlayer, PlayerEntityInteractEvent> onClick) {
		super(EntityType.INTERACTION);
		this.onClick = onClick;

        setAutoViewable(true);
		editEntityMeta(InteractionMeta.class, meta -> {
			meta.setHeight(height);
			meta.setWidth(width);
			meta.setHasNoGravity(true);
			meta.setResponse(true);
		});
	}

	public InteractionEntity onAttack(Consumer<HypixelPlayer> handler) {
		this.attackHandler = handler;
		return this;
	}

	public void attacked(HypixelPlayer player) {
		if (attackHandler != null) attackHandler.accept(player);
	}
}
