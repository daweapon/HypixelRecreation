package dev.daweapon.type.crystalhollows.layout;

import net.minestom.server.coordinate.Pos;

import java.util.Map;

public record StructurePlacement(StructureSpec spec, Pos origin) {

    public Pos anchor(String name) {
        Pos offset = spec.anchors().get(name);
        if (offset == null) return null;
        return new Pos(origin.x() + offset.x(), origin.y() + offset.y(), origin.z() + offset.z(), offset.yaw(), offset.pitch());
    }

    public Map<String, Pos> anchors() {
        Map<String, Pos> resolved = new java.util.LinkedHashMap<>();
        for (String name : spec.anchors().keySet()) resolved.put(name, anchor(name));
        return resolved;
    }
}
