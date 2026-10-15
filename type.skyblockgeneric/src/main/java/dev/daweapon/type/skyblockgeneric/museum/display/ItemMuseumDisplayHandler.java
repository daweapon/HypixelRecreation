package dev.daweapon.type.skyblockgeneric.museum.display;

import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.LivingEntity;
import net.minestom.server.entity.metadata.item.ItemEntityMeta;
import net.minestom.server.entity.metadata.other.InteractionMeta;
import dev.daweapon.commons.text.Text;
import dev.daweapon.type.skyblockgeneric.data.datapoints.DatapointMuseum;
import dev.daweapon.type.generic.entity.hologram.PlayerHolograms;
import dev.daweapon.type.skyblockgeneric.item.SkyBlockItem;
import dev.daweapon.type.skyblockgeneric.museum.MuseumDisplay;
import dev.daweapon.type.skyblockgeneric.museum.MuseumDisplayEntityImpl;
import dev.daweapon.type.skyblockgeneric.museum.MuseumDisplayEntityInformation;
import dev.daweapon.type.skyblockgeneric.museum.MuseumDisplays;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.ArrayList;
import java.util.List;

public class ItemMuseumDisplayHandler extends MuseumDisplay {

    @Override
    public MuseumDisplayEntityInformation display(SkyBlockPlayer player, MuseumDisplays display, boolean empty, int position) {
        DatapointMuseum.MuseumData museumData = player.getMuseumData();
        Pos pos = display.getPositions().get(position);
        PlayerHolograms.ExternalPlayerHologram hologram;
        LivingEntity itemEntity;
        ArrayList<LivingEntity> entities = new ArrayList<>();

        if (empty) {
            hologram = PlayerHolograms.ExternalPlayerHologram.builder()
                    .player(player)
                    .text(new String[]{
                            "<7>" + display + " Slot #" + (position + 1),
                            "<e><l>CLICK TO EDIT"
                    })
                    .pos(pos.add(0, 1, 0))
                    .build();
            PlayerHolograms.addExternalPlayerHologram(hologram);

            itemEntity = new MuseumDisplayEntityImpl(EntityType.ARMOR_STAND, display, position, true);
            itemEntity.setAutoViewable(false);
            itemEntity.setNoGravity(true);
            itemEntity.setInvisible(true);
            itemEntity.setInstance(player.getInstance(), pos);
            itemEntity.addViewer(player);
        } else {
            List<SkyBlockItem> items = museumData.getDisplayHandler().getItemsAtSlot(display, position);

            // Validate that there is only one item at the slot as there should be for this type of display
            if (items.size() > 1) throw new RuntimeException("Invalid museum display state");
            SkyBlockItem item = items.getFirst();

            hologram = PlayerHolograms.ExternalPlayerHologram.builder()
                    .player(player)
                    .text(new String[]{Text.literal(item.getDisplayName()).serialize()})
                    .pos(pos.add(0, 0.6, 0))
                    .build();
            PlayerHolograms.addExternalPlayerHologram(hologram);

            itemEntity = new MuseumDisplayEntityImpl(EntityType.ITEM, display, position, false);
            ItemEntityMeta itemDisplayMeta = (ItemEntityMeta) itemEntity.getEntityMeta();
            itemDisplayMeta.setItem(item.getItemStack());

            itemEntity.setAutoViewable(false);
            itemEntity.setInvisible(false);
            itemEntity.setInstance(player.getInstance(), pos.add(0, 1, 0));
            itemEntity.addViewer(player);

            LivingEntity interactableEntity = new MuseumDisplayEntityImpl(EntityType.INTERACTION, display, position, false);
            InteractionMeta interactionMeta = (InteractionMeta) interactableEntity.getEntityMeta();
            interactionMeta.setWidth(1);
            interactionMeta.setHeight(2);

            interactableEntity.setAutoViewable(false);
            interactableEntity.setInvisible(false);
            interactableEntity.setInstance(player.getInstance(), pos.add(0, 0.5, 0));
            interactableEntity.addViewer(player);
            entities.add(interactableEntity);
        }

        entities.add(itemEntity);
        return new MuseumDisplayEntityInformation(entities, hologram);
    }
}
