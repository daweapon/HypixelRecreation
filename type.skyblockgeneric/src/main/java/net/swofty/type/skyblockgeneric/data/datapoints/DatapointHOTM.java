package net.swofty.type.skyblockgeneric.data.datapoints;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import net.swofty.commons.protocol.Serializer;
import net.swofty.type.skyblockgeneric.data.SkyBlockDatapoint;
import org.json.JSONObject;

import java.util.LinkedHashMap;
import java.util.Map;

// Persists a player's Heart of the Mountain progress: XP/tier, mithril and gemstone powder
// balances, and HOTM skill tokens earned/spent.
public class DatapointHOTM extends SkyBlockDatapoint<DatapointHOTM.PlayerHOTMData> {
	private static final Serializer<PlayerHOTMData> serializer = new Serializer<>() {
		@Override
		public String serialize(PlayerHOTMData value) {
			JSONObject json = new JSONObject();
			json.put("experience", value.experience);
			json.put("mithrilPowder", value.mithrilPowder);
			json.put("gemstonePowder", value.gemstonePowder);
			json.put("tokens", value.tokens);
			json.put("tokensSpent", value.tokensSpent);
			json.put("glacitePowder", value.glacitePowder);
			json.put("coreLevel", value.coreLevel);
			json.put("selectedAbility", value.selectedAbility == null ? "" : value.selectedAbility);
			json.put("skyMallDay", value.skyMallDay);
			json.put("skyMallBuff", value.skyMallBuff == null ? "" : value.skyMallBuff);
			json.put("dailyPowderDay", value.dailyPowderDay);
			json.put("dailyCommissionDay", value.dailyCommissionDay);
			json.put("perkLevels", new JSONObject(value.perkLevels));
			json.put("abilityReadyAt", new JSONObject(value.abilityReadyAt));
			return json.toString();
		}

		@Override
		public PlayerHOTMData deserialize(String json) {
			if (json == null || json.isEmpty()) {
				return PlayerHOTMData.createDefault();
			}

			try {
				JSONObject obj = new JSONObject(json);
				PlayerHOTMData data = new PlayerHOTMData();
				data.experience = obj.optLong("experience", 0L);
				data.mithrilPowder = obj.optLong("mithrilPowder", 0L);
				data.gemstonePowder = obj.optLong("gemstonePowder", 0L);
				data.tokens = obj.optInt("tokens", 0);
				data.tokensSpent = obj.optInt("tokensSpent", 0);
				data.glacitePowder = obj.optLong("glacitePowder", 0L);
				data.coreLevel = obj.optInt("coreLevel", 0);
				data.selectedAbility = obj.optString("selectedAbility", "").isEmpty() ? null : obj.getString("selectedAbility");
				data.skyMallDay = obj.optLong("skyMallDay", -1L);
				data.skyMallBuff = obj.optString("skyMallBuff", "").isEmpty() ? null : obj.getString("skyMallBuff");
				data.dailyPowderDay = obj.optLong("dailyPowderDay", -1L);
				data.dailyCommissionDay = obj.optLong("dailyCommissionDay", -1L);
				JSONObject perks = obj.optJSONObject("perkLevels");
				if (perks != null) perks.keySet().forEach(key -> data.perkLevels.put(key, perks.getInt(key)));
				JSONObject cooldowns = obj.optJSONObject("abilityReadyAt");
				if (cooldowns != null) cooldowns.keySet().forEach(key -> data.abilityReadyAt.put(key, cooldowns.getLong(key)));
				return data;
			} catch (Exception e) {
				return PlayerHOTMData.createDefault();
			}
		}

		@Override
		public PlayerHOTMData clone(PlayerHOTMData value) {
			PlayerHOTMData cloned = new PlayerHOTMData();
			cloned.experience = value.experience;
			cloned.mithrilPowder = value.mithrilPowder;
			cloned.gemstonePowder = value.gemstonePowder;
			cloned.tokens = value.tokens;
			cloned.tokensSpent = value.tokensSpent;
			cloned.glacitePowder = value.glacitePowder;
			cloned.coreLevel = value.coreLevel;
			cloned.selectedAbility = value.selectedAbility;
			cloned.skyMallDay = value.skyMallDay;
			cloned.skyMallBuff = value.skyMallBuff;
			cloned.dailyPowderDay = value.dailyPowderDay;
			cloned.dailyCommissionDay = value.dailyCommissionDay;
			cloned.perkLevels.putAll(value.perkLevels);
			cloned.abilityReadyAt.putAll(value.abilityReadyAt);
			return cloned;
		}
	};

	public DatapointHOTM(String key, PlayerHOTMData value) {
		super(key, value, serializer);
	}

	public DatapointHOTM(String key) {
		super(key, PlayerHOTMData.createDefault(), serializer);
	}

	@Getter
	@Setter
	@NoArgsConstructor
	public static class PlayerHOTMData {
		private long experience;
		private long mithrilPowder;
		private long gemstonePowder;
		private int tokens;
		private int tokensSpent;
		private long glacitePowder;
		private int coreLevel;
		private String selectedAbility;
		private long skyMallDay = -1L;
		private String skyMallBuff;
		private long dailyPowderDay = -1L;
		private long dailyCommissionDay = -1L;
		private final Map<String, Integer> perkLevels = new LinkedHashMap<>();
		private final Map<String, Long> abilityReadyAt = new LinkedHashMap<>();

		public int perkLevel(String perkId) {
			return perkLevels.getOrDefault(perkId, 0);
		}

		public void addGlacitePowder(long amount) {
			glacitePowder += amount;
		}

		public long powder(net.swofty.type.skyblockgeneric.hotm.HotmPowder powder) {
			return switch (powder) {
				case MITHRIL -> mithrilPowder;
				case GEMSTONE -> gemstonePowder;
				case GLACITE -> glacitePowder;
			};
		}

		public void setPowderBalance(net.swofty.type.skyblockgeneric.hotm.HotmPowder powder, long amount) {
			switch (powder) {
				case MITHRIL -> mithrilPowder = amount;
				case GEMSTONE -> gemstonePowder = amount;
				case GLACITE -> glacitePowder = amount;
			}
		}

		public void addPowder(net.swofty.type.skyblockgeneric.hotm.HotmPowder powder, long amount) {
			switch (powder) {
				case MITHRIL -> mithrilPowder += amount;
				case GEMSTONE -> gemstonePowder += amount;
				case GLACITE -> glacitePowder += amount;
			}
		}

		public static PlayerHOTMData createDefault() {
			PlayerHOTMData data = new PlayerHOTMData();
			data.experience = 0L;
			data.mithrilPowder = 0L;
			data.gemstonePowder = 0L;
			data.tokens = 1;
			data.tokensSpent = 0;
			return data;
		}

		public int getTier() {
			if (experience >= 1_247_000) return 10;
			if (experience >= 847_000) return 9;
			if (experience >= 557_000) return 8;
			if (experience >= 347_000) return 7;
			if (experience >= 197_000) return 6;
			if (experience >= 97_000) return 5;
			if (experience >= 37_000) return 4;
			if (experience >= 12_000) return 3;
			if (experience >= 3_000) return 2;
			return 1;
		}

		public long getNextTierXp() {
			int currentTier = getTier();
			return switch (currentTier) {
				case 1 -> 3_000;
				case 2 -> 12_000;
				case 3 -> 37_000;
				case 4 -> 97_000;
				case 5 -> 197_000;
				case 6 -> 347_000;
				case 7 -> 557_000;
				case 8 -> 847_000;
				case 9 -> 1_247_000;
				default -> -1; // maxed out
			};
		}

		public boolean isMaxed() {
			return getTier() >= 10;
		}

		public int addExperience(long amount) {
			int previousTier = getTier();
			experience += amount;
			int newTier = getTier();

			int levelUps = newTier - previousTier;
			if (levelUps > 0) {
				for (int tier = previousTier + 1; tier <= newTier; tier++) {
					tokens += getTokensForTier(tier);
				}
			}
			return levelUps;
		}

		private int getTokensForTier(int tier) {
			return switch (tier) {
				case 1 -> 1;
				case 2, 3, 4, 5, 6, 8, 9, 10 -> 2;
				case 7 -> 3;
				default -> 0;
			};
		}

		public void addMithrilPowder(long amount) {
			mithrilPowder += amount;
		}

		public void addGemstonePowder(long amount) {
			gemstonePowder += amount;
		}

		public int getAvailableTokens() {
			return tokens - tokensSpent;
		}

		public boolean spendToken() {
			if (getAvailableTokens() <= 0) return false;
			tokensSpent++;
			return true;
		}

		public int getProgressPercent() {
			if (isMaxed()) return 100;

			int currentTier = getTier();
			long currentTierXp = switch (currentTier) {
				case 1 -> 0;
				case 2 -> 3_000;
				case 3 -> 12_000;
				case 4 -> 37_000;
				case 5 -> 97_000;
				case 6 -> 197_000;
				case 7 -> 347_000;
				case 8 -> 557_000;
				case 9 -> 847_000;
				default -> 1_247_000;
			};
			long nextTierXp = getNextTierXp();
			if (nextTierXp == -1) return 100;

			long progressInTier = experience - currentTierXp;
			long xpNeededForTier = nextTierXp - currentTierXp;

			return (int) ((progressInTier * 100) / xpNeededForTier);
		}
	}
}

