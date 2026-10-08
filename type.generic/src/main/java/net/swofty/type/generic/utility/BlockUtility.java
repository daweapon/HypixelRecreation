package net.swofty.type.generic.utility;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import net.minestom.server.coordinate.Pos;
import net.minestom.server.instance.block.Block;
import org.json.JSONObject;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class BlockUtility {


    public static Block applyTexture(Block block, String texture) {
        JSONObject json = new JSONObject();
        json.put("isPublic", true);
        json.put("signatureRequired", false);
        json.put("textures", new JSONObject().put("SKIN",
                new JSONObject().put("url", "http://textures.minecraft.net/texture/" + texture).put("metadata", new JSONObject().put("model", "slim"))));

        /*

        String texturesEncoded = Base64.getEncoder().encodeToString(json.toString().getBytes());
        block = block.withTag(ExtraItemTags.SKULL_OWNER, new ExtraItemTags.SkullOwner(null, "25",
                new PlayerSkin(texturesEncoded, null)));*/
        return block;
    }

    /** The coloured sidebar arrow from where {@code from} is looking toward {@code to}; see {@link DirectionArrow}. */
    public static String getArrow(Pos from, Pos to) {
        return DirectionArrow.markup(from.x(), from.z(), from.yaw(), to.x(), to.z());
    }
}
