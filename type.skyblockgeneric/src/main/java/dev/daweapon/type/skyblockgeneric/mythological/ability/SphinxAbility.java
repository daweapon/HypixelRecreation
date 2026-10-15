package dev.daweapon.type.skyblockgeneric.mythological.ability;

import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.minestom.server.entity.attribute.Attribute;
import dev.daweapon.commons.text.Text;
import dev.daweapon.type.skyblockgeneric.SkyBlockGenericLoader;
import dev.daweapon.type.skyblockgeneric.mythological.MythologicalCreatureMob;
import dev.daweapon.type.skyblockgeneric.mythological.burrow.GriffinBurrowService;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Greets its digger with one of fifteen SkyBlock riddles. A right answer is paid in Griffin Burrow
 * treasure; a wrong one infuriates it, raising its Health by half and doubling its Bleed.
 */
public final class SphinxAbility implements MythologicalAbility {
    private static final Map<UUID, SphinxAbility> AWAITING_ANSWER = new ConcurrentHashMap<>();
    private static final double DAMAGE_PER_BLEED = 75;
    private static final double INFURIATED_HEALTH_MULTIPLIER = 1.5;
    private static final double INFURIATED_BLEED_MULTIPLIER = 2;

    private record Riddle(String question, String answer, List<String> decoys) {
    }

    private static final List<Riddle> RIDDLES = List.of(
            new Riddle("How do you obtain the Dark Purple Dye?", "Dark Auction", List.of("Fishing", "The End")),
            new Riddle("How many floors are there in The Catacombs?", "7", List.of("6", "9")),
            new Riddle("What does Junker Joel collect?", "Junk", List.of("Dyes", "Pests")),
            new Riddle("What is the first type of slayer Maddox offers?", "Zombie", List.of("Enderman", "Spider")),
            new Riddle("What item do you use to kill Pests?", "Vacuum", List.of("Axe", "Hoe")),
            new Riddle("What type of mob is exclusive to the Fishing Festival?", "Shark", List.of("Snake", "Whale")),
            new Riddle("Where is the Titanoboa found?", "Backwater Bayou", List.of("Spider's Den", "The Park")),
            new Riddle("Where is Trevor the Trapper found?", "Mushroom Desert", List.of("The Hub", "The Park")),
            new Riddle("Which item rarity comes after Mythic?", "Divine", List.of("Legendary", "Rare")),
            new Riddle("Which of these is NOT a pet?", "Slime", List.of("Sheep", "Slug")),
            new Riddle("Which of these is NOT a type of Gemstone?", "Prismite", List.of("Amethyst", "Peridot")),
            new Riddle("Which type of Gemstone has the lowest Breaking Power?", "Ruby", List.of("Jade", "Topaz")),
            new Riddle("Who helps you apply Rod Parts?", "Roddy", List.of("Remi", "Rudy")),
            new Riddle("Who owns the Gold Essence Shop?", "Marigold", List.of("Gemma", "King Midas")),
            new Riddle("Who runs the Chocolate Factory?", "Hoppity", List.of("Beppity", "Hipppity")));

    private final Bleed bleed = new Bleed("Sphinx Bleed", DAMAGE_PER_BLEED);
    private MythologicalCreatureMob sphinx;
    private Riddle riddle;
    private List<String> choices;

    public static void answer(SkyBlockPlayer player, UUID sphinxId, int choice) {
        SphinxAbility ability = AWAITING_ANSWER.get(sphinxId);
        if (ability == null || !player.getUuid().equals(ability.sphinx.getDigger())) return;
        if (choice < 0 || choice >= ability.choices.size()) return;
        AWAITING_ANSWER.remove(sphinxId);
        ability.resolve(player, ability.choices.get(choice).equals(ability.riddle.answer()));
    }

    @Override
    public void onSpawn(MythologicalCreatureMob mob) {
        sphinx = mob;
        riddle = RIDDLES.get(ThreadLocalRandom.current().nextInt(RIDDLES.size()));
        choices = new ArrayList<>(riddle.decoys());
        choices.add(riddle.answer());
        Collections.shuffle(choices);
        if (mob.getDigger() == null) return;
        SkyBlockPlayer digger = SkyBlockGenericLoader.getFromUUID(mob.getDigger());
        if (digger == null) return;
        AWAITING_ANSWER.put(mob.getUuid(), this);
        digger.sendMessage("<6><l>Question");
        digger.sendMessage("<f>" + riddle.question());
        for (int i = 0; i < choices.size(); i++) {
            digger.sendMessage(Text.of(" <f>" + (char) ('A' + i) + ") " + choices.get(i)).asComponent()
                    .clickEvent(ClickEvent.runCommand("/sphinxanswer " + mob.getUuid() + " " + i))
                    .hoverEvent(HoverEvent.showText(Text.of("<e>Click to answer!").asComponent())));
        }
        digger.sendMessage(" ");
        digger.sendMessage("<8>Click your response to answer!");
        digger.sendMessage(" ");
    }

    private void resolve(SkyBlockPlayer player, boolean correct) {
        if (correct) {
            player.sendMessage("<a>Right answer! The Sphinx is intrigued by your superior intellect.");
            GriffinBurrowService.INSTANCE.giveTreasure(player);
            return;
        }
        player.sendMessage("<c>Wrong answer! Sphinx is infuriated by your inferior intellect!");
        if (sphinx.isDead() || sphinx.isRemoved()) return;
        float health = sphinx.getHealth();
        sphinx.setMaxHealth((float) (sphinx.getMaxHealth() * INFURIATED_HEALTH_MULTIPLIER));
        sphinx.setHealth((float) (health * INFURIATED_HEALTH_MULTIPLIER));
        bleed.setDamagePerStack(DAMAGE_PER_BLEED * INFURIATED_BLEED_MULTIPLIER);
        MythologicalCombat.sound(sphinx, "entity.ravager.roar", 1f, 0.6f);
        sphinx.refreshName();
    }

    @Override
    public void onMeleeHit(MythologicalCreatureMob mob, SkyBlockPlayer target) {
        bleed.add(target, 1);
        mob.refreshName();
    }

    @Override
    public void tick(MythologicalCreatureMob mob, int ticksAlive) {
        bleed.tick(mob, ticksAlive);
        if (ticksAlive % 20 == 0) mob.refreshName();
    }

    @Override
    public String nameHeader(MythologicalCreatureMob mob) {
        int highest = bleed.highest();
        return highest > 0 ? Bleed.label(highest) : "";
    }

    @Override
    public void cleanup(MythologicalCreatureMob mob) {
        AWAITING_ANSWER.remove(mob.getUuid());
        bleed.clearAll();
    }
}
