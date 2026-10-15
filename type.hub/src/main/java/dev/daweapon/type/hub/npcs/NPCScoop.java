package dev.daweapon.type.hub.npcs;

import net.minestom.server.coordinate.Pos;
import dev.daweapon.type.generic.entity.npc.HypixelNPC;
import dev.daweapon.type.generic.entity.npc.configuration.HumanConfiguration;
import dev.daweapon.type.generic.event.custom.NPCInteractEvent;
import dev.daweapon.type.generic.user.HypixelPlayer;
import dev.daweapon.type.skyblockgeneric.shop.catalogue.CatalogueShopView;

public class NPCScoop extends HypixelNPC {

    public NPCScoop() {
        super(new HumanConfiguration() {
            @Override
            public String[] holograms(HypixelPlayer player) {
                return new String[]{"Scoop", "<e><l>CLICK"};
            }

            @Override
            public String signature(HypixelPlayer player) {
                return "PS+Pk0FRc+9Klz9VLvuDKpzgA6MK6JCKPTLQKtRKPtZGxkUR8IU0HMhzLt9hRjvIrPsgFtNPz4pezviDBdTFyNm934B8UyslZWzsQJjeqqNYsDeSbDFQxQ1KSVIYurKbZpvMF7hS69uscQLlRG5ee3dvVva3XhgCryTllXTJjhaqLjLC8w+ujHzs1PU0WBrtFiArLmtMnwOIhr4CIK2T4jnmb9PAkzsCcI/TIYY39R0PvANnmN5tTl7BohHVXQgZsDZLOqDvam8kLg9iXh0HY6dOx35q7YaKudN8uk/FwusMlJLnvDaEJK1e47oNmSWykJ3TCGpO5U1gCX45e/3gO4VTPSKKZUHNJwO7T3K9lwM4HG8yfS2lKFk5z0y5tJtQBHQ1HirJS0OpQbqFu4ccvXgGeqlWUCByDbgiwMAm0K40daQNM1kzLaHMffNqR3Q+k16lbZnmICGHDJqBzkf/nlKscGjeQ5vB+3O/82k+3CYEoNLeKpBQwP9T5lhwZqCk685TjG7fqyFtbMG154nubl5FKH05d1JL91Ir+Zsn101DypoUY7f5KKkaWgjf8B66aKA9Mkh6yiSDZmaczuj3hlWUM/fUC6LNAAXaYqlDoboyACJrswV+Teiis6h9vuq2ZefU5kmVlSnifrvkCIeYFrLkv1LEvex7LOClbJ3V8YY=";
            }

            @Override
            public String texture(HypixelPlayer player) {
                return "ewogICJ0aW1lc3RhbXAiIDogMTYxMzQwOTIyMzExMywKICAicHJvZmlsZUlkIiA6ICIyYzEwNjRmY2Q5MTc0MjgyODRlM2JmN2ZhYTdlM2UxYSIsCiAgInByb2ZpbGVOYW1lIiA6ICJOYWVtZSIsCiAgInNpZ25hdHVyZVJlcXVpcmVkIiA6IHRydWUsCiAgInRleHR1cmVzIiA6IHsKICAgICJTS0lOIiA6IHsKICAgICAgInVybCIgOiAiaHR0cDovL3RleHR1cmVzLm1pbmVjcmFmdC5uZXQvdGV4dHVyZS9hODk4YWQ1OGQzNjY0Y2ZhY2YxYmYxMWFhZDlmNTEzZDI1YThiOWJiOTFhNzczODJiMzJjMTBiN2QwYTRiYzliIgogICAgfQogIH0KfQ==";
            }

            @Override
            public Pos position(HypixelPlayer player) {
                return new Pos(4.5, 180, 56.5, 69, 0);
            }

            @Override
            public boolean looking(HypixelPlayer player) {
                return true;
            }
        });
    }

    @Override
    public void onClick(NPCInteractEvent e) {
        HypixelPlayer player = e.player();
        if (!isInDialogue(player)) setDialogue(player, "open-shop");
        player.openView(new CatalogueShopView("scoop"));
    }

    @Override
    protected DialogueSet[] dialogues(HypixelPlayer player) {
        return new DialogueSet[]{
            DialogueSet.ofTranslation("open-shop", "npcs_hub.scoop.dialogue.open_shop")
        };
    }
}
