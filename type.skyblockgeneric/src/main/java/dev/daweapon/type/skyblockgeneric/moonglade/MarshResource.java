package dev.daweapon.type.skyblockgeneric.moonglade;

import dev.daweapon.commons.ServerType;
import dev.daweapon.commons.skyblock.item.ItemType;
import dev.daweapon.type.skyblockgeneric.hotf.HotfCurrency;
import dev.daweapon.type.skyblockgeneric.starlyn.StarlynResource;
import lombok.Getter;
import net.minestom.server.instance.block.Block;
import net.minestom.server.item.Material;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.ThreadLocalRandom;

@Getter
public enum MarshResource {
    LUSHLILAC(ItemType.LUSHLILAC, StarlynResource.LUSHLILAC, Material.FLOWERING_AZALEA, "berries.collect", 1, 1, 25, 100, 30 * 20, 0, HotfCurrency.FOREST, ServerType.SKYBLOCK_GALATEA),
    SEA_LUMIES(ItemType.SEA_LUMIES, StarlynResource.SEA_LUMIES, Material.SEA_PICKLE, "sealumies.collect", 1, 4, 3.75, 0, 10 * 20, 0, HotfCurrency.FOREST, ServerType.SKYBLOCK_GALATEA),
    RUBY_VEILSHROOM(ItemType.RUBY_VEILSHROOM, StarlynResource.RUBY_VEILSHROOM, Material.CRIMSON_FUNGUS, "veilshroom.collect", 4, 4, 16, 200, 50 * 20, 5 * 20, HotfCurrency.DESERT, ServerType.SKYBLOCK_TORRHUS_CANYON);

    private final ItemType item;
    private final StarlynResource contestResource;
    private final Material source;
    private final String chapterTrigger;
    private final int minimum;
    private final int maximum;
    private final double foragingXpPerItem;
    private final long forestWhispers;
    private final int respawnTicks;
    private final int respawnJitterTicks;
    private final HotfCurrency currency;
    private final ServerType island;

    MarshResource(ItemType item, StarlynResource contestResource, Material source, String chapterTrigger, int minimum, int maximum,
                  double foragingXpPerItem, long forestWhispers, int respawnTicks, int respawnJitterTicks, HotfCurrency currency, ServerType island) {
        this.item = item;
        this.contestResource = contestResource;
        this.source = source;
        this.chapterTrigger = chapterTrigger;
        this.minimum = minimum;
        this.maximum = maximum;
        this.foragingXpPerItem = foragingXpPerItem;
        this.forestWhispers = forestWhispers;
        this.respawnTicks = respawnTicks;
        this.respawnJitterTicks = respawnJitterTicks;
        this.currency = currency;
        this.island = island;
    }

    public int rollRespawnTicks() {
        return respawnTicks + (respawnJitterTicks == 0 ? 0 : ThreadLocalRandom.current().nextInt(respawnJitterTicks + 1));
    }

    public int rollAmount() {
        return ThreadLocalRandom.current().nextInt(minimum, maximum + 1);
    }

    public Block depletedBlock(Block original) {
        if (this == LUSHLILAC) return Block.AZALEA;
        return "true".equals(original.getProperty("waterlogged")) ? Block.WATER : Block.AIR;
    }

    public static @Nullable MarshResource ofBlock(Block block) {
        for (MarshResource resource : values()) {
            if (block.compare(resource.source.block())) return resource;
        }
        return null;
    }
}
