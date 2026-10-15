package net.swofty.type.skyblockgeneric.event.actions.custom.skill;

import net.swofty.type.generic.event.EventNodes;
import net.swofty.type.generic.event.HypixelEventClass;
import net.swofty.type.generic.event.phase.EventPhase;
import net.swofty.type.generic.event.phase.PhasedEvent;
import net.swofty.type.skyblockgeneric.data.datapoints.DatapointSkills;
import net.swofty.type.skyblockgeneric.event.custom.CustomBlockBreakEvent;
import net.swofty.type.skyblockgeneric.item.SkyBlockItem;
import net.swofty.type.skyblockgeneric.item.handlers.pet.abilities.gathering.MiningPetBonuses;
import net.swofty.type.skyblockgeneric.item.components.SkillableMineComponent;
import net.swofty.type.skyblockgeneric.item.tool.ToolPassives;
import net.swofty.type.skyblockgeneric.loottable.LootRoll;
import net.swofty.type.skyblockgeneric.reforge.ReforgeLoadout;
import net.swofty.type.skyblockgeneric.skill.SkillCategories;
import net.swofty.type.skyblockgeneric.crystalhollows.CrystalHollowsDrops;
import net.swofty.type.skyblockgeneric.region.SkyBlockRegion;

import java.util.OptionalDouble;

// Grants mining-skill XP for blocks broken with a skillable mining tool
public class ActionSkillMiningHandler implements HypixelEventClass {

    @PhasedEvent(node = EventNodes.CUSTOM, requireDataLoaded = true, phase = EventPhase.GAMEPLAY)
    public void run(CustomBlockBreakEvent event) {
        if (event.getPlayerPlaced()) return;

        SkyBlockRegion region = SkyBlockRegion.getRegionOfPosition(event.getPoint());
        OptionalDouble hollowsXp = CrystalHollowsDrops.miningXp(event.getMaterial(), region == null ? null : region.getType());
        SkillCategories skillCategory;
        double baseXp;
        if (hollowsXp.isPresent()) {
            skillCategory = SkillCategories.MINING;
            baseXp = hollowsXp.getAsDouble();
        } else {
            SkyBlockItem item = new SkyBlockItem(event.getMaterial());
            if (!item.hasComponent(SkillableMineComponent.class)) return;
            SkillableMineComponent skillableMine = item.getComponent(SkillableMineComponent.class);
            skillCategory = skillableMine.getCategory();
            baseXp = skillableMine.getMiningValue();
        }
        DatapointSkills.PlayerSkills skills = event.getPlayer().getSkills();

        double experience = skillCategory == SkillCategories.MINING
                ? MiningPetBonuses.miningSkillExperience(event.getPlayer(), baseXp)
                : baseXp;
        if (skillCategory == SkillCategories.MINING) {
            experience *= ReforgeLoadout.miningExperienceMultiplier(event.getPlayer());
            if (ToolPassives.holding(event.getPlayer(), "LAPIS_PICKAXE")
                    && LootRoll.roll(event.getPlayer().getUuid(), null, ToolPassives.LAPIS_DOUBLE_XP_CHANCE)) experience *= 2;
        }
        if (skillCategory == SkillCategories.FARMING) {
            event.getPlayer().addCoins(ReforgeLoadout.harvestCoins(event.getPlayer()));
        }
        skills.increase(event.getPlayer(), skillCategory, experience);
    }
}
