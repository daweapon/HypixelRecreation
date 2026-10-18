package dev.daweapon.type.skyblockgeneric.tab.widgets;

import dev.daweapon.commons.skyblock.item.Rarity;
import dev.daweapon.commons.skyblock.item.attribute.attributes.ItemAttributePetData;
import dev.daweapon.commons.StringUtility;
import dev.daweapon.commons.text.Text;
import dev.daweapon.type.skyblockgeneric.data.datapoints.DatapointKat;
import dev.daweapon.type.skyblockgeneric.item.SkyBlockItem;
import dev.daweapon.type.skyblockgeneric.item.components.PetComponent;
import dev.daweapon.type.skyblockgeneric.tab.TabWidget;
import dev.daweapon.type.skyblockgeneric.tab.TabWidgetContext;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;
import net.minestom.server.item.Material;

import java.util.ArrayList;
import java.util.List;

public final class PetWidget implements TabWidget {

    @Override
    public String id() {
        return "pet";
    }

    @Override
    public String displayName() {
        return "Pet";
    }

    @Override
    public Material icon() {
        return Material.BONE;
    }

    @Override
    public int defaultPriority() {
        return 10;
    }

    @Override
    public List<Text> lines(SkyBlockPlayer player, TabWidgetContext context) {
        List<Text> lines = new ArrayList<>();
        lines.add(Text.of("<a><l>Pet:"));
        SkyBlockItem pet = player.getPetData().getEnabledPet();
        if (pet == null) {
            lines.add(Text.of(" <7>None"));
        } else {
            lines.add(Text.of(" ").append(pet.getDisplayNameText()));
            lines.add(experienceLine(pet));
        }
        DatapointKat.PlayerKat kat = player.getKatData();
        SkyBlockItem sitting = kat.getPet();
        if (sitting != null) {
            long remaining = kat.getFinishTime() - System.currentTimeMillis();
            lines.add(Text.of("<e><l>Kat:"));
            lines.add(Text.of(" ").append(sitting.getDisplayNameText())
                    .append(remaining <= 0 ? " <a>READY" : " <e>" + TabDurations.spans(remaining)));
        }
        return lines;
    }

    private Text experienceLine(SkyBlockItem pet) {
        PetComponent component = pet.getComponent(PetComponent.class);
        Rarity rarity = pet.getAttributeHandler().getRarity();
        ItemAttributePetData.PetData data = pet.getAttributeHandler().getPetData();
        int offset = component.getFixedXpOffset() != null
                ? component.getFixedXpOffset()
                : ItemAttributePetData.rarityOffset(rarity);
        int maxLevel = component.getMaxLevel();
        int level = data.getAsLevel(offset, maxLevel);
        if (level >= maxLevel) return Text.of(" <b><l>MAX LEVEL");
        long needed = ItemAttributePetData.totalExperienceForLevel(level + 1, offset)
                - ItemAttributePetData.totalExperienceForLevel(level, offset);
        double current = data.getExperienceInCurrentLevel(offset, maxLevel);
        return Text.of(" <e>{}<6>/<e>{} <6>XP <7>({}%)", StringUtility.shortenNumber(current), StringUtility.shortenNumber(needed),
                needed <= 0 ? "0.0" : String.format(java.util.Locale.ROOT, "%.1f", current * 100.0 / needed));
    }
}
