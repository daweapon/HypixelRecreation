package dev.daweapon.type.skyblockgeneric.rabbits;

import lombok.Getter;

@Getter
public enum RabbitFaction {
    CITY("City", "b04d59adf2fd6729abf58353de480c25ca3534c47bdf622362224a483b481e78"),
    MOUNTAIN("Mountain", "bbba21f229f94f8d53ca7e6e6e9936aee61a3b9c525a82f6212ff4dbae4109c3"),
    COUNTRY("Country", "222ffb651c5ff25a28fcce15d6f8c958c5f8f983c9dfe9ec083e079dc700435e"),
    BEACH("Beach", "785c52ed4b820711853c5b281c037939934389e7b0eb809ced1ce5026473227c");

    private final String name;
    private final String texture;

    RabbitFaction(String name, String texture) {
        this.name = name;
        this.texture = texture;
    }

    public String getDisplayName() {
        return name + " Rabbits";
    }

    public static RabbitFaction byName(String name) {
        if (name == null || name.isEmpty()) return null;
        try {
            return valueOf(name);
        } catch (IllegalArgumentException exception) {
            return null;
        }
    }
}
