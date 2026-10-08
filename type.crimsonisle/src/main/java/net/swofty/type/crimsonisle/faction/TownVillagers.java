package net.swofty.type.crimsonisle.faction;

import net.minestom.server.coordinate.Pos;
import net.swofty.type.generic.entity.npc.HypixelNPC;
import net.swofty.type.skyblockgeneric.crimson.faction.CrimsonFaction;

import java.util.List;

import static net.swofty.type.skyblockgeneric.crimson.faction.CrimsonFaction.BARBARIAN;
import static net.swofty.type.skyblockgeneric.crimson.faction.CrimsonFaction.MAGE;

/**
 * The talk-only Scarleton and Dragontail villagers who take part in the "talk to 4 villagers" step,
 * with their wiki positions and dialogue. Kutral and Nall's eavesdropped keypad code belongs to
 * Nuvian's secret shop and is not here.
 */
public final class TownVillagers {
    private TownVillagers() {
    }

    public static List<HypixelNPC> all() {
        return List.of(
                villager("<d>Arba", CrimsonSkins.ARBA, new Pos(-3.5, 93, -820.5), MAGE,
                        lines("This is absurd!", "Everyone knows time travel is a myth!",
                                "Excuse me, can't you see we are trying to have a conversation here?"),
                        lines("Please leave us alone!")),
                villager("<d>Arbadak", CrimsonSkins.ARBADAK, new Pos(-0.5, 93, -824.5), MAGE,
                        lines("Just because you don't understand something doesn't mean it's not possible!",
                                "Not even a century ago teleportation was thought to be impossible!",
                                "And you yourself are capable of telepathy and telekinesis, which nobody knew were even possible recently!",
                                "Oh, who are you?", "Can you not disturb our conversation?"),
                        lines("Don't disrespect your elders."), lines("Stop eavesdropping!!!"),
                        lines("Please leave us alone."), lines("You are being very rude.")),
                villager("<d>Mazakala", CrimsonSkins.MAZAKALA, new Pos(-2.5, 93, -824.5), MAGE,
                        lines("Nonsense...",
                                "The appearance of that strange disturbance on the nearby island might have nothing to do with time travel.",
                                "That doesn't mean we should ignore it, someone needs to...", "Can't you see we are busy?"),
                        lines("Don't disrespect your elders."), lines("Stop eavesdropping!!!"),
                        lines("Please leave us alone."), lines("You are being very rude.")),
                villager("<5>Colore", CrimsonSkins.COLORE, new Pos(-131, 89, -711), MAGE,
                        lines("Welcome to the Tourist Office!", "This town was built four centuries ago, or more!",
                                "Visitors come from all around the world to visit our <5>Cathedral<f>.",
                                "But maybe you are more interested in our famous floating water fountain?",
                                "Or the <9>Statue of the Hero<f>?", "There is so much to see!"),
                        lines("If you have any tourist-related questions, I'll be right here!"),
                        lines("Enjoy your time in <5>Scarleton<f>!")),
                villager("<5>Kheharad", CrimsonSkins.KHEHARAD, new Pos(-111.5, 99, -827.5), MAGE,
                        lines("I don't have anything to sell you right now.",
                                "Come back later and I might be able to sell you something!")),
                villager("<5>Offea", CrimsonSkins.OFFEA, new Pos(-87.5, 93, -743.625), MAGE,
                        lines("Inhale...exhale...", "I'm trying to gather enough courage to go upstairs.",
                                "This apartment is very cozy but it triggers something in me.", "I think I might be claustrophobic."),
                        lines("Alwin says we don't need more space, I wish I could agree..."), lines("My husband works all the time.")),
                villager("<5>Rhanora", CrimsonSkins.RHANORA, new Pos(-143.5, 100, -828.5), MAGE, lines(),
                        lines("Do you like my cats?"), lines("My favorite cat? I love them all."),
                        lines("I like to study cats, and I think they sometimes study me."),
                        lines("I think all cats teleport to the same spiritual world when they dream."),
                        lines("Cats are the only known animals that are gifted with magic, except they are too lazy to use their powers."),
                        lines("My dear Leo is on his 9th life, I give him all the care in the world.")),
                villager("<5>Scholar Alluin", CrimsonSkins.SCHOLAR_ALLUIN, new Pos(-15.5, 114, -915.5), MAGE,
                        lines("We store a great deal of knowledge about the history of this island in this <5>Cathedral<f>.",
                                "Every single book is imbued so that only true mages can pull them out.",
                                "Even if you possessed that power, I doubt someone like you could read our texts.")),
                villager("<5>Seffea", CrimsonSkins.SEFFEA, new Pos(-139.125, 99, -816.3125), MAGE,
                        lines("I love <5>Rhanora's <f>cats, but she wont let me play with any of them!",
                                "I wonder if she will notice if one goes missing...")),
                villager("<d>Chak", CrimsonSkins.CHIHAI_CHILD, new Pos(-602.5, 107, -868.5), BARBARIAN,
                        lines("I use the top bed, because I'm the strongest.")),
                villager("<d>Chuk", CrimsonSkins.CHIHAI_CHILD, new Pos(-604.5, 107, -869.5), BARBARIAN,
                        lines("When I'm older, I'll travel the world.")),
                villager("<d>Etc", CrimsonSkins.CHIHAI_CHILD, new Pos(-606.5, 107, -868.5), BARBARIAN,
                        lines("I was born last.")),
                villager("<d>Chihai", CrimsonSkins.CHIHAI, new Pos(-604.5, 107, -861.5), BARBARIAN,
                        lines("You are not from around here."),
                        lines("My kids are a handful, but I love them so."), lines("Please don't play with my kids.")),
                villager("<d>Jine", CrimsonSkins.JINE, new Pos(-545.5, 119, -854.5), BARBARIAN,
                        lines("The view from my window is amazing.", "That's the main reason why I bought this place."),
                        lines("<o>Sigh... <f>What a beautiful view...")),
                villager("<d>Kutral", CrimsonSkins.KUTRAL, new Pos(-658.5, 107, -741.5), BARBARIAN,
                        lines("Are you selling something?", "No, forget what I just said.")),
                villager("<d>Nall", CrimsonSkins.NALL, new Pos(-657.5, 107, -738.5), BARBARIAN,
                        lines("What? I didn't say anything!", "Stop spying on us...", "You are intruding.")),
                villager("<d>Plenk", CrimsonSkins.PLENK, new Pos(-572.5, 118, -739.5), BARBARIAN,
                        lines("Hello!", "I'm a young and ambitious architect.", "Let me know if you need a house, I'll quote you a price!"),
                        lines("Clients? No, I never had any. But soon surely!"), lines("I designed my own house. You like?")),
                villager("<d>Porc", CrimsonSkins.PORC, new Pos(-577, 118, -857), BARBARIAN,
                        lines("Humm, the sound of fire...", "Music to my ears.", "Just listen to the fire with me, what a delightful melody.",
                                "I could stay here for hours just listening to this exquisite harmony."),
                        lines("Shikshrishrikts...you know?"), lines("This is...magnificent..."),
                        lines("Close your eyes and listen, it's so subtle and clean...")),
                villager("<d>Strux", CrimsonSkins.STRUX, new Pos(-596.5, 123, -845.5), BARBARIAN,
                        lines("How did you manage to get in here???", "Keep the trapdoors closed!",
                                "Magma Cube pets are forbidden in <2>Dragontail<f>!", "Please don't tell anyone!!!"),
                        lines("He's so cute when he sleeps. Or she? I don't even know."),
                        lines("I don't know what I would do without <c>Maggie<f>."),
                        lines("<c>Maggie <f>was so tiny when I got them, they grow up so fast."),
                        lines("If anything happened to <c>Maggie <f>I would go nuts.")),
                villager("<d>Truu", CrimsonSkins.TRUU, new Pos(-613.5, 122, -765.5), BARBARIAN,
                        lines("Why can't there be peace in the world?",
                                "I feel like people in power are so disconnected from reality.",
                                "The regular population just wants to have a calm and happy life.", "Literally, who cares?"),
                        lines("I'm sorry for venting, but is that even a hot take?"),
                        lines("If the principal objective of a leader isn't the well being of their population, there is a problem.")),
                villager("<d>Yoink", CrimsonSkins.YOINK, new Pos(-613.5, 123, -786.5), BARBARIAN,
                        lines("I grow my own Nether Warts!", "That way I can save a lot of money on food!"),
                        lines("I like watching Nether Warts grow. If you look closely you can see it grow, I think."),
                        lines("The house on the other side is also mine, that's what saving money does.")),
                new VelynaNPC());
    }

    private static HypixelNPC villager(String name, String texture, Pos home, CrimsonFaction faction,
                                       String[] introduction, String[]... idle) {
        return new TownVillagerNPC(name, texture, home, faction, introduction, idle);
    }

    private static String[] lines(String... lines) {
        return lines;
    }
}
