package dev.daweapon.type.skyblockgeneric.data.datapoints;

import dev.daweapon.commons.protocol.Serializer;
import dev.daweapon.type.skyblockgeneric.data.SkyBlockDatapoint;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

public class DatapointHOTF extends SkyBlockDatapoint<DatapointHOTF.PlayerHOTFData> {
	private static final Serializer<PlayerHOTFData> serializer = new Serializer<>() {
		@Override
		public String serialize(PlayerHOTFData value) {
			JSONObject json = new JSONObject();
			json.put("unlocked", value.unlocked);
			json.put("tokensSpent", value.tokensSpent);
			json.put("bonusTokens", value.bonusTokens);
			json.put("coreLevel", value.coreLevel);
			json.put("selectedAbility", value.selectedAbility == null ? "" : value.selectedAbility);
			json.put("perkLevels", new JSONObject(value.perkLevels));
			json.put("disabledPerks", new JSONArray(value.disabledPerks));
			json.put("abilityReadyAt", new JSONObject(value.abilityReadyAt));
			json.put("giftsDay", value.giftsDay);
			json.put("giftsToday", value.giftsToday);
			json.put("treesDay", value.treesDay);
			json.put("treesToday", value.treesToday);
			json.put("wishDay", value.wishDay);
			json.put("lotteryDay", value.lotteryDay);
			json.put("lotteryEffect", value.lotteryEffect == null ? "" : value.lotteryEffect);
			return json.toString();
		}

		@Override
		public PlayerHOTFData deserialize(String json) {
			if (json == null || json.isEmpty()) return new PlayerHOTFData();
			try {
				JSONObject obj = new JSONObject(json);
				PlayerHOTFData data = new PlayerHOTFData();
				data.unlocked = obj.optBoolean("unlocked", false);
				data.tokensSpent = obj.optInt("tokensSpent", 0);
				data.bonusTokens = obj.optInt("bonusTokens", 0);
				data.coreLevel = obj.optInt("coreLevel", 0);
				data.selectedAbility = obj.optString("selectedAbility", "").isEmpty() ? null : obj.getString("selectedAbility");
				JSONObject perks = obj.optJSONObject("perkLevels");
				if (perks != null) perks.keySet().forEach(key -> data.perkLevels.put(key, perks.getInt(key)));
				JSONArray disabled = obj.optJSONArray("disabledPerks");
				if (disabled != null) for (int i = 0; i < disabled.length(); i++) data.disabledPerks.add(disabled.getString(i));
				JSONObject cooldowns = obj.optJSONObject("abilityReadyAt");
				if (cooldowns != null) cooldowns.keySet().forEach(key -> data.abilityReadyAt.put(key, cooldowns.getLong(key)));
				data.giftsDay = obj.optLong("giftsDay", -1L);
				data.giftsToday = obj.optInt("giftsToday", 0);
				data.treesDay = obj.optLong("treesDay", -1L);
				data.treesToday = obj.optInt("treesToday", 0);
				data.wishDay = obj.optLong("wishDay", -1L);
				data.lotteryDay = obj.optLong("lotteryDay", -1L);
				data.lotteryEffect = obj.optString("lotteryEffect", "").isEmpty() ? null : obj.getString("lotteryEffect");
				return data;
			} catch (Exception e) {
				return new PlayerHOTFData();
			}
		}

		@Override
		public PlayerHOTFData clone(PlayerHOTFData value) {
			PlayerHOTFData cloned = new PlayerHOTFData();
			cloned.unlocked = value.unlocked;
			cloned.tokensSpent = value.tokensSpent;
			cloned.bonusTokens = value.bonusTokens;
			cloned.coreLevel = value.coreLevel;
			cloned.selectedAbility = value.selectedAbility;
			cloned.perkLevels.putAll(value.perkLevels);
			cloned.disabledPerks.addAll(value.disabledPerks);
			cloned.abilityReadyAt.putAll(value.abilityReadyAt);
			cloned.giftsDay = value.giftsDay;
			cloned.giftsToday = value.giftsToday;
			cloned.treesDay = value.treesDay;
			cloned.treesToday = value.treesToday;
			cloned.wishDay = value.wishDay;
			cloned.lotteryDay = value.lotteryDay;
			cloned.lotteryEffect = value.lotteryEffect;
			return cloned;
		}
	};

	public DatapointHOTF(String key, PlayerHOTFData value) {
		super(key, value, serializer);
	}

	public DatapointHOTF(String key) {
		super(key, new PlayerHOTFData(), serializer);
	}

	@Getter
	@Setter
	@NoArgsConstructor
	public static class PlayerHOTFData {
		private boolean unlocked;
		private int tokensSpent;
		private int bonusTokens;
		private int coreLevel;
		private String selectedAbility;
		private final Map<String, Integer> perkLevels = new LinkedHashMap<>();
		private final Set<String> disabledPerks = new LinkedHashSet<>();
		private final Map<String, Long> abilityReadyAt = new LinkedHashMap<>();
		private long giftsDay = -1L;
		private int giftsToday;
		private long treesDay = -1L;
		private int treesToday;
		private long wishDay = -1L;
		private long lotteryDay = -1L;
		private String lotteryEffect;

		public int perkLevel(String perkId) {
			return perkLevels.getOrDefault(perkId, 0);
		}

		public boolean isPerkActive(String perkId) {
			return perkLevel(perkId) > 0 && !disabledPerks.contains(perkId);
		}

		public int activeLevel(String perkId) {
			return isPerkActive(perkId) ? perkLevel(perkId) : 0;
		}
	}
}
