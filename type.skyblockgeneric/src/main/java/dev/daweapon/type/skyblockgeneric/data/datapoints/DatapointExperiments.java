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

public class DatapointExperiments extends SkyBlockDatapoint<DatapointExperiments.ExperimentsData> {
	private static final Serializer<ExperimentsData> serializer = new Serializer<>() {
		@Override
		public String serialize(ExperimentsData value) {
			JSONObject json = new JSONObject();
			json.put("rngXp", value.rngXp);
			json.put("rngSelected", value.rngSelected == null ? "" : value.rngSelected);
			json.put("flags", new JSONArray(value.flags));
			json.put("counters", new JSONObject(value.counters));
			return json.toString();
		}

		@Override
		public ExperimentsData deserialize(String json) {
			ExperimentsData data = new ExperimentsData();
			if (json == null || json.isEmpty()) return data;
			try {
				JSONObject obj = new JSONObject(json);
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
		public ExperimentsData clone(ExperimentsData value) {
			ExperimentsData cloned = new ExperimentsData();
			cloned.rngXp = value.rngXp;
			cloned.rngSelected = value.rngSelected;
			cloned.flags.addAll(value.flags);
			cloned.counters.putAll(value.counters);
			return cloned;
		}
	};

	public DatapointExperiments(String key, ExperimentsData value) {
		super(key, value, serializer);
	}

	public DatapointExperiments(String key) {
		super(key, new ExperimentsData(), serializer);
	}

	@Getter
	@Setter
	@NoArgsConstructor
	public static class ExperimentsData {
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
