package dev.daweapon.type.skyblockgeneric.abiphone;

import java.util.List;

public record AbiphoneContact(String id, String name, Difficulty difficulty, String location, String description,
                              String requirement, String usage, List<String> aliases) {

    public enum Difficulty {
        EASY("<a>Easy"),
        MEDIUM("<e>Medium"),
        HARD("<c>Hard");

        private final String markup;

        Difficulty(String markup) {
            this.markup = markup;
        }

        public String markup() {
            return markup;
        }
    }

    public boolean available() {
        return AbiphoneRegistry.getFromId(id) != null;
    }
}
