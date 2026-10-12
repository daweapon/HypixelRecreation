package net.swofty.type.skyblockgeneric.item.set.sets;

import net.minestom.server.potion.Potion;
import net.minestom.server.potion.PotionEffect;
import net.minestom.server.timer.Scheduler;
import net.minestom.server.timer.Task;
import net.minestom.server.timer.TaskSchedule;
import net.swofty.type.skyblockgeneric.item.set.impl.ArmorSet;
import net.swofty.type.skyblockgeneric.item.set.impl.SetRepeatable;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.ArrayList;
import java.util.List;

public class RabbitArmorSet implements ArmorSet, SetRepeatable {
	public static final byte JUMP_BOOST_AMPLIFIER = 1;
	public static final int REFRESH_TICKS = 5;
	public static final int EFFECT_TICKS = 15;

	@Override
	public String getName() {
		return "Springsneak";
	}

	@Override
	public ArrayList<String> getDescription() {
		return new ArrayList<>(List.of(
				"<7>While sneaking, you have permanent",
				"<a>Jump Boost II<7>."));
	}

	@Override
	public Task getTask(Scheduler scheduler) {
		return scheduler.buildTask(() -> {
			for (SkyBlockPlayer player : getWearingSet()) {
				if (player.isSneaking()) {
					player.addEffect(new Potion(PotionEffect.JUMP_BOOST, JUMP_BOOST_AMPLIFIER, EFFECT_TICKS));
				}
			}
		}).repeat(TaskSchedule.tick(REFRESH_TICKS)).schedule();
	}
}
