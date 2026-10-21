package dev.daweapon.type.skyblockgeneric.hunting.creature;

import dev.daweapon.commons.skyblock.item.ItemType;
import dev.daweapon.type.skyblockgeneric.hunting.AttributeRegistry;
import dev.daweapon.type.skyblockgeneric.hunting.creature.special.CreatureSpecials;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CreatureCatalogTest {
    @BeforeAll
    static void load() {
        CreatureCatalog.load(new File("../configuration/skyblock/hunting"));
    }

    @Test
    void bothIslandsAreCatalogued() {
        assertEquals(27, CreatureCatalog.forIsland("TORRHUS_CANYON").size());
        assertEquals(28, CreatureCatalog.forIsland("GALATEA").size());
    }

    @Test
    void everyMessageParses() {
        List<String> problems = new ArrayList<>();
        for (CreatureSpec spec : CreatureCatalog.all()) {
            for (CreatureSpec.Message message : spec.messages()) {
                try {
                    dev.daweapon.commons.text.Text.parse(message.text().replace("{location}", "the Springs"));
                } catch (RuntimeException exception) {
                    problems.add(spec.id() + " " + message.when() + ": " + exception.getMessage());
                }
            }
        }
        assertTrue(problems.isEmpty(), String.join("\n", problems));
    }

    @Test
    void everyReferenceResolves() {
        List<String> problems = new ArrayList<>();
        for (CreatureSpec spec : CreatureCatalog.all()) {
            if (spec.shard() != null && AttributeRegistry.findByShard(spec.shard()).isEmpty()) problems.add(spec.id() + " shard " + spec.shard());
            for (CreatureSpec.Drop drop : spec.drops()) {
                if (ItemType.get(drop.item()) == null) problems.add(spec.id() + " item " + drop.item());
            }
            if (spec.special() != null && !CreatureSpecials.registered(spec.special())) problems.add(spec.id() + " special " + spec.special());
            if (spec.spawn().regions().isEmpty() && spec.spawn().points().isEmpty() && spec.spawn().trigger() == null) {
                problems.add(spec.id() + " has no spawn rule");
            }
        }
        assertTrue(problems.isEmpty(), String.join("\n", problems));
    }
}
