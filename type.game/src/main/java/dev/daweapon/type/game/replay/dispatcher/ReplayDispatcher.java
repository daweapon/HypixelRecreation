package dev.daweapon.type.game.replay.dispatcher;

import dev.daweapon.type.game.replay.ReplayRecorder;

public interface ReplayDispatcher {
	void initialize(ReplayRecorder recorder);

	default void tick() {
	}

	void cleanup();

	String getName();
}
