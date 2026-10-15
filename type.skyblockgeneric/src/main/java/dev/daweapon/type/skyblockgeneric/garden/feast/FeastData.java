package dev.daweapon.type.skyblockgeneric.garden.feast;

import lombok.Getter;
import lombok.Setter;
import org.json.JSONObject;

@Getter
@Setter
public final class FeastData {
    private int year;
    private int seasoning;
    private int claimed;
    private int kernels;

    public void rollOver(int currentYear) {
        if (year != currentYear) {
            year = currentYear;
            seasoning = 0;
            claimed = 0;
        }
    }

    public JSONObject toJson() {
        return new JSONObject().put("year", year).put("seasoning", seasoning).put("claimed", claimed).put("kernels", kernels);
    }

    public static FeastData fromJson(JSONObject json) {
        FeastData data = new FeastData();
        if (json == null) return data;
        data.year = json.optInt("year", 0);
        data.seasoning = json.optInt("seasoning", 0);
        data.claimed = json.optInt("claimed", 0);
        data.kernels = json.optInt("kernels", 0);
        return data;
    }
}
