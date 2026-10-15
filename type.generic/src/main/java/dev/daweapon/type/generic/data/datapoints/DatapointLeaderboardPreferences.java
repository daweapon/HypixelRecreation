package dev.daweapon.type.generic.data.datapoints;

import dev.daweapon.commons.bedwars.LeaderboardPreferences;
import dev.daweapon.commons.protocol.JacksonSerializer;
import dev.daweapon.type.generic.data.Datapoint;

// Datapoint storing a player's BedWars leaderboard display preferences.
public class DatapointLeaderboardPreferences extends Datapoint<LeaderboardPreferences> {
	private static final JacksonSerializer<LeaderboardPreferences> serializer =
			new JacksonSerializer<>(LeaderboardPreferences.class);

	public DatapointLeaderboardPreferences(String key, LeaderboardPreferences value) {
		super(key, value, serializer);
	}

	public DatapointLeaderboardPreferences(String key) {
		this(key, LeaderboardPreferences.defaults());
	}
}
