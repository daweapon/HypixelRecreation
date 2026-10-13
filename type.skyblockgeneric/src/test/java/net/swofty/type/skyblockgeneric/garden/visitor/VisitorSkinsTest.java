package net.swofty.type.skyblockgeneric.garden.visitor;

import net.swofty.type.skyblockgeneric.garden.npc.GardenNpcSkins;
import org.json.JSONObject;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class VisitorSkinsTest {
    @Test
    void almostEveryVisitorHasASkinOrAnEntityType() throws Exception {
        VisitorCatalogue.load(Path.of("../configuration/skyblock/garden"));
        JSONObject root = new JSONObject(Files.readString(Path.of("../configuration/skyblock/garden/visitor_skins.json")));
        JSONObject skins = root.getJSONObject("skins");
        JSONObject entities = root.getJSONObject("entities");
        long covered = VisitorCatalogue.get().visitors().stream()
                .filter(visitor -> skins.has(visitor.id()) || entities.has(visitor.id())).count();
        assertTrue(covered >= 135, "covered " + covered);
        for (String id : skins.keySet()) {
            JSONObject skin = skins.getJSONObject(id);
            assertFalse(skin.getString("texture").isBlank(), id);
            assertFalse(skin.getString("signature").isBlank(), id);
        }
    }
}
