package dev.daweapon.type.skyblockgeneric.data.datapoints;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import dev.daweapon.commons.protocol.Serializer;
import dev.daweapon.type.skyblockgeneric.data.SkyBlockDatapoint;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

public class DatapointCrystalHollows extends SkyBlockDatapoint<DatapointCrystalHollows.CrystalHollowsData> {
	private static final Serializer<CrystalHollowsData> serializer = new Serializer<>() {
		@Override
		public String serialize(CrystalHollowsData value) {
			JSONObject json = new JSONObject();
			json.put("passExpiresAt", value.passExpiresAt);
			json.put("permanentAccess", value.permanentAccess);
			json.put("nucleusRuns", value.nucleusRuns);
			json.put("rngXp", value.rngXp);
			json.put("rngSelected", value.rngSelected == null ? "" : value.rngSelected);
			json.put("flags", new JSONArray(value.flags));
			json.put("counters", new JSONObject(value.counters));
			return json.toString();
		}

		@Override
		public CrystalHollowsData deserialize(String json) {
			CrystalHollowsData data = new CrystalHollowsData();
			if (json == null || json.isEmpty()) return data;
			try {
				JSONObject obj = new JSONObject(json);
				data.passExpiresAt = obj.optLong("passExpiresAt", 0L);
				data.permanentAccess = obj.optBoolean("permanentAccess", false);
				data.nucleusRuns = obj.optInt("nucleusRuns", 0);
				data.rngXp = obj.optLong("rngXp", 0L);
				data.rngSelected = obj.optString("rngSelected", "").isEmpty() ? null : obj.getString("rngSelected");
				JSONArray flags = obj.optJSONArray("flags");
				if (flags != null) for (int i = 0; i < flags.length(); i++) data.flags.add(flags.getString(i));
				JSONObject counters = obj.optJSONObject("counters");
				if (counters != null) counters.keySet().forEach(key -> data.counters.put(key, counters.getLong(key)));
			} catch (Exception ignored) {
			}
			return data;
		}

		@Override
		public CrystalHollowsData clone(CrystalHollowsData value) {
			CrystalHollowsData cloned = new CrystalHollowsData();
			cloned.passExpiresAt = value.passExpiresAt;
			cloned.permanentAccess = value.permanentAccess;
			cloned.nucleusRuns = value.nucleusRuns;
			cloned.rngXp = value.rngXp;
			cloned.rngSelected = value.rngSelected;
			cloned.flags.addAll(value.flags);
			cloned.counters.putAll(value.counters);
			return cloned;
		}
	};

	public DatapointCrystalHollows(String key, CrystalHollowsData value) {
		super(key, value, serializer);
	}

	public DatapointCrystalHollows(String key) {
		super(key, new CrystalHollowsData(), serializer);
	}

	@Getter
	@Setter
	@NoArgsConstructor
	public static class CrystalHollowsData {
		private long passExpiresAt;
		private boolean permanentAccess;
		private int nucleusRuns;
		private long rngXp;
		private String rngSelected;
		private final Set<String> flags = new LinkedHashSet<>();
		private final Map<String, Long> counters = new LinkedHashMap<>();

		public boolean hasFlag(String flag) {
			return flags.contains(flag);
		}

		public long counter(String key) {
			return counters.getOrDefault(key, 0L);
		}

		public long addCounter(String key, long amount) {
			return counters.merge(key, amount, Long::sum);
		}
	}
}
