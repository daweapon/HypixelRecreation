package dev.daweapon.type.jerrysworkshop.npcs;

import net.minestom.server.coordinate.Pos;
import dev.daweapon.type.jerrysworkshop.dialogue.IdleLineNPC;

import java.util.List;

// Hendrik, the penguin who left the Glacial Cave to fish at the Hot Springs.
public class NPCHendrik extends IdleLineNPC {
    public NPCHendrik() {
        super("Hendrik", new Pos(16.5, 62, -30.5), "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZjA1ZmIxYmYyMjI1NDE2ODdmMjZkMDBjZGI2NDI4NWVkZDUzOTZjNWUyOWI2NTc2ZTRlYjExZGVhMjgwMjY3ZiIsIm1ldGFkYXRhIjp7fX19LCJwcm9maWxlSWQiOiIwMmFiZjlhODA4M2YyNTg0OGI2ZjdmM2MwZmFkNjczMCIsInByb2ZpbGVOYW1lIjoibmVhODkiLCJpc1B1YmxpYyI6dHJ1ZSwidGltZXN0YW1wIjoxNzgxNjc4NDg2MTE2fQ==",
                null, List.of(
                "Y'know when I'm fishing here it always feels just as fast as up top.",
                "I'm used to fishing underground being so slow! This is great!",
                "Oh man it's so much warmer down here! I don't miss that awful ice cave at all.",
                "All these fish down here are really tough, they are very heat resistant.",
                "I do feel a bit bad for my boys but I'm sure they are fine now.",
                "I hope they get a promotion in the end."));
    }
}
