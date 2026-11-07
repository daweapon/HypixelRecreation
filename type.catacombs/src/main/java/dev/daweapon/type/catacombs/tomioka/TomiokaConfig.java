package dev.daweapon.type.catacombs.tomioka;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import dev.daweapon.type.catacombs.engine.look.Skin;
import dev.daweapon.type.skyblockgeneric.enchantment.EnchantmentType;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/** Tomioka's look and stock ({@code configuration/skyblock/dungeons/tomioka.json}), from the M7 run 1 capture. */
public record TomiokaConfig(Skin skin, List<String> nameLines, List<Book> stock) {
    public static final Path FILE = Path.of("./configuration/skyblock/dungeons/tomioka.json");

    public record Book(EnchantmentType enchantment, int level, long coins) {
    }

    public static TomiokaConfig load(Path file) throws IOException {
        return parse(Files.readString(file));
    }

    static TomiokaConfig parse(String json) {
        JsonObject root = JsonParser.parseString(json).getAsJsonObject();
        List<String> lines = new ArrayList<>();
        root.getAsJsonArray("nameLines").forEach(line -> lines.add(line.getAsString()));
        List<Book> stock = new ArrayList<>();
        for (JsonElement element : root.getAsJsonArray("stock")) {
            JsonObject book = element.getAsJsonObject();
            stock.add(new Book(EnchantmentType.valueOf(book.get("enchantment").getAsString()), book.get("level").getAsInt(),
                    book.get("coins").getAsLong()));
        }
        return new TomiokaConfig(Skin.parse(root.getAsJsonObject("skin")), List.copyOf(lines), List.copyOf(stock));
    }
}
