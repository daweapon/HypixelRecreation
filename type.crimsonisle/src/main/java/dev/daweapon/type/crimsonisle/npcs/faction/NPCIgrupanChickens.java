package dev.daweapon.type.crimsonisle.npcs.faction;

import net.minestom.server.coordinate.Pos;
import dev.daweapon.type.crimsonisle.faction.IgrupanChickens;

// Igrupan's runaway chickens at their wiki coordinates.
public final class NPCIgrupanChickens {
    private NPCIgrupanChickens() {
    }

    public static class Henrietta extends IgrupanChickens.Chicken {
        public Henrietta() {
            super("Henrietta", new Pos(-8.5, 92.9, -847.5));
        }
    }

    public static class Chikira extends IgrupanChickens.Chicken {
        public Chikira() {
            super("Chikira", new Pos(-47.5, 112, -856.5));
        }
    }

    public static class Chickovsky extends IgrupanChickens.Chicken {
        public Chickovsky() {
            super("Chickovsky", new Pos(-23.5, 116, -812.5));
        }
    }

    public static class Cluckles extends IgrupanChickens.Chicken {
        public Cluckles() {
            super("Cluckles", new Pos(-48.5, 103, -824.5));
        }
    }

    public static class Hennifer extends IgrupanChickens.Chicken {
        public Hennifer() {
            super("Hennifer", new Pos(-62.5, 121, -817.5));
        }
    }
}
