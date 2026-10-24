package dev.daweapon.type.spidersden.npcs;

import net.minestom.server.coordinate.Pos;
import dev.daweapon.commons.skyblock.item.Rarity;
import dev.daweapon.type.generic.data.datapoints.DatapointToggles;
import dev.daweapon.type.generic.entity.npc.HypixelNPC;
import dev.daweapon.type.generic.entity.npc.configuration.HumanConfiguration;
import dev.daweapon.type.generic.event.custom.NPCInteractEvent;
import dev.daweapon.type.generic.user.HypixelPlayer;
import dev.daweapon.type.skyblockgeneric.bestiary.BestiaryMilestones;
import dev.daweapon.type.skyblockgeneric.item.SkyBlockItem;
import dev.daweapon.type.skyblockgeneric.spidersden.GrandmaWolfPet;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;

// Grandma Wolf, found in her house in the Spider's Den, gives and upgrades the Grandma Wolf Pet.
public class NPCGrandmaWolf extends HypixelNPC {
    public NPCGrandmaWolf() {
        super(new HumanConfiguration() {
            @Override
            public String[] holograms(HypixelPlayer player) {
                return new String[]{
                        "<c>Grandma Wolf",
                        "<e><l>CLICK"
                };
            }

            @Override
            public String signature(HypixelPlayer player) {
                return "PX74SU2IXWdqcl0HOqT7dsD78wvGTZQxfirgsAw1lF2iN5XhEosX+1QlZL2OimwV7WsQIs3Slnwo6aG7a07OdnkLSnrkQSA6payjVMexS3aSk5QBqIVrVyxDXbcSp7ck4SOPwmFuzOTqtr0+jPmjiJlcrHUa4MnYd+taIyweiE1Vwu/j65Skx1roTbEAf1l3zXdmemVXUVKfXYbT96+do4dp1ycGA2pYGC5xWgFPzxdY+WU2OYVl7KPCnArfGyKvfoA9gpWNlmo/tDQcieHkSADuOZrKWjRNw97w3FOys2BGWgF95Qsf9GJqBBQsLJMIfSdHh5nlvvBiQnhnCaacZ5l5ySm1VQ6beAllMWs0W+UaTfNtwsXYsBnC65JzhC01w3ALt7GjO5sfSlRPjbDkfp14z6U4/hebL0yQrQT3K+ghQ6iEU7acpP+0pclwhT6GGNjqssO+wvIvYOfkgDQ+KJFIPOdEsnYG7DPFgJVmJWG+qMHC3inkCkT9Afb4fLprjDtZVbMgVYo2V8AWsjGE2IDtQ4sjOa0OAEBwqlPB62n8irts6JgNMWddE69f0LLkmwLkcLHiLalygSyxxuNmyGlAyOMkMS7AANvsYalqFbjHn3EyPhRR+Mzz3q0UK048ezFMs8BgStPq4OgPnCSIJggpemXThSdAIULn6BqTnRY=";
            }

            @Override
            public String texture(HypixelPlayer player) {
                return "ewogICJ0aW1lc3RhbXAiIDogMTYxNDg3MTAxNDQ5OCwKICAicHJvZmlsZUlkIiA6ICJmMTA0NzMxZjljYTU0NmI0OTkzNjM4NTlkZWY5N2NjNiIsCiAgInByb2ZpbGVOYW1lIiA6ICJ6aWFkODciLAogICJzaWduYXR1cmVSZXF1aXJlZCIgOiB0cnVlLAogICJ0ZXh0dXJlcyIgOiB7CiAgICAiU0tJTiIgOiB7CiAgICAgICJ1cmwiIDogImh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvODM5MTkzYzA5YmNhZDlkMWY5MDFhZmVmNGUyYzBjYWQ4M2FhZTFhNTcwMzM1YzNlY2JjN2QwNWZkZTE5ODc5NSIKICAgIH0KICB9Cn0=";
            }

            @Override
            public Pos position(HypixelPlayer player) {
                return new Pos(-281.5, 122, -190.5, 0, 0);
            }

            @Override
            public boolean looking(HypixelPlayer player) {
                return true;
            }
        });
    }

    @Override
    public void onClick(NPCInteractEvent event) {
        SkyBlockPlayer player = (SkyBlockPlayer) event.player();
        DatapointToggles.Toggles toggles = player.getToggles();
        SkyBlockItem pet = GrandmaWolfPet.owned(player);
        if (pet == null) {
            if (toggles.get(DatapointToggles.Toggles.ToggleType.HAS_SPOKEN_TO_GRANDMA_WOLF)) {
                setDialogue(player, "lost-pet");
                return;
            }
            if (!GrandmaWolfPet.give(player)) {
                player.sendMessage("<c>Your Pets menu is full. Make some room, then come back.");
                return;
            }
            setDialogue(player, "initial-hello").thenRun(() ->
                    toggles.set(DatapointToggles.Toggles.ToggleType.HAS_SPOKEN_TO_GRANDMA_WOLF, true));
            return;
        }
        Rarity next = GrandmaWolfPet.next(pet.getAttributeHandler().getRarity());
        if (next == null) {
            setDialogue(player, "maxed");
            return;
        }
        if (BestiaryMilestones.of(player) < GrandmaWolfPet.milestoneFor(next)) {
            setDialogue(player, "requirement-" + next.name().toLowerCase());
            return;
        }
        GrandmaWolfPet.upgrade(player, pet, next);
        setDialogue(player, "upgrade-" + next.name().toLowerCase());
    }

    @Override
    public DialogueSet[] dialogues(HypixelPlayer player) {
        return new DialogueSet[]{
                DialogueSet.builder().key("initial-hello").lines(
                        "Oh, " + player.getUsername() + "! I'm having a conundrum! There's so much to kill, yet so little time.",
                        "I've added the <a>Grandma Wolf Pet<f> to your <a>Pets Menu<f>.",
                        "The Grandma Wolf is particularly adept at combos. The more mobs you kill in quick succession, the better!",
                        "If you reach high enough <6>Bestiary Milestones<f>, I will increase the rarity of this pet!",
                        "The pet does not need to be spawned for combos to work! Happy hunting!").build(),
                DialogueSet.builder().key("upgrade-uncommon").lines(
                        "There you go! I've upgraded your <a>Grandma Wolf Pet<f> to <a>Uncommon<f>!",
                        "Come back when you've reached <6>Bestiary Milestone X<f> to upgrade it to <9>Rare<f>!").build(),
                DialogueSet.builder().key("upgrade-rare").lines(
                        "There you go! I've upgraded your <9>Grandma Wolf Pet<f> to <9>Rare<f>!",
                        "Come back when you've reached <6>Bestiary Milestone XV<f> to upgrade it to <5>Epic<f>!").build(),
                DialogueSet.builder().key("upgrade-epic").lines(
                        "There you go! I've upgraded your <5>Grandma Wolf Pet<f> to <5>Epic<f>!",
                        "Come back when you've reached <6>Bestiary Milestone XX<f> to upgrade it to <6>Legendary<f>!").build(),
                DialogueSet.builder().key("upgrade-legendary").lines(
                        "There you go! I've upgraded your <6>Grandma Wolf Pet<f> to <6>Legendary<f>!",
                        "I'm afraid that's all I can do for you. Thank you for helping this little old Grandma!").build(),
                DialogueSet.builder().key("maxed").line(
                        "I've already upgraded your pet to the maximum tier! Leave this little old Grandma alone!").build(),
                DialogueSet.builder().key("lost-pet").line(
                        "I'm afraid you don't have a <6>Grandma Wolf Pet<f>! Leave this little old Grandma alone!").build(),
                DialogueSet.builder().key("requirement-uncommon").line(
                        "I will upgrade your Grandma Wolf Pet to <a>Uncommon<f> when you reach <6>Bestiary Milestone 5<f>.").build(),
                DialogueSet.builder().key("requirement-rare").line(
                        "I will upgrade your Grandma Wolf Pet to <9>Rare<f> when you reach <6>Bestiary Milestone 10<f>.").build(),
                DialogueSet.builder().key("requirement-epic").line(
                        "I will upgrade your Grandma Wolf Pet to <5>Epic<f> when you reach <6>Bestiary Milestone 15<f>.").build(),
                DialogueSet.builder().key("requirement-legendary").line(
                        "I will upgrade your Grandma Wolf Pet to <6>Legendary<f> when you reach <6>Bestiary Milestone 20<f>.").build(),
        };
    }
}
