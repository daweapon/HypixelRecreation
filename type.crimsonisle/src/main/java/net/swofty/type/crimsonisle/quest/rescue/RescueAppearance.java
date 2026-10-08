package net.swofty.type.crimsonisle.quest.rescue;

import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.EntityType;
import net.swofty.commons.text.Text;
import net.swofty.type.generic.entity.npc.configuration.AnimalConfiguration;
import net.swofty.type.generic.entity.npc.configuration.HumanConfiguration;
import net.swofty.type.generic.entity.npc.configuration.NPCConfiguration;
import net.swofty.type.generic.user.HypixelPlayer;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;
import org.jetbrains.annotations.Nullable;

import java.util.function.Function;
import java.util.function.Predicate;

/**
 * How a Rescue Mission NPC looks: a Taika skin for Mage-side characters, a Piglin for Barbarian ones. None of
 * these NPCs have NEU skins, so the Mage skins are borrowed from Scarleton NPCs (Grelius for guards).
 */
public record RescueAppearance(@Nullable String texture, @Nullable EntityType entityType) {
    public static final RescueAppearance MAGE_GUARD = human("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHBzOi8vdGV4dHVyZXMubWluZWNyYWZ0Lm5ldC90ZXh0dXJlL2UyMWFiNGFjMmZlNWI0NDlmMzJkYjRlYTU2MDlhZWU3OTY3OTRlMTE1Nzc2NGM1NzkyOGY4ODYxMGNhMzE4ODQifX19");
    public static final RescueAppearance MAGE_RECRUITER = human("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHBzOi8vdGV4dHVyZXMubWluZWNyYWZ0Lm5ldC90ZXh0dXJlLzQwNWRiNDFhMzU1NTU2MGJlMGQ0NDdmMWMyZWRiYzU3ZGFlNWFkZGNjYWZiZjkwYjNkMGE4MDI3NjZkYjAxYmIifX19");
    public static final RescueAppearance MAGE_AGENT = human("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHBzOi8vdGV4dHVyZXMubWluZWNyYWZ0Lm5ldC90ZXh0dXJlLzUyNDM5YjcxMTBlOGExZmI5YTYzZTM5MzkxZWVmZTU4YTJlN2QwNDgxMTdmNDA4MWViZWEzMjY2NDQ2MzdhMCJ9fX0=");
    public static final RescueAppearance MAGE_HOSTAGE = human("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHBzOi8vdGV4dHVyZXMubWluZWNyYWZ0Lm5ldC90ZXh0dXJlLzViNWEzYjk2MTgyM2JlYzQ3ZTRlOGFjMjNlN2EzYjJhZTczNmM5YzgxMzQ3ZmEyYjFiZTAzYzY4YzQxZmFhMWUifX19");
    public static final RescueAppearance PIGLIN = new RescueAppearance(null, EntityType.PIGLIN);

    public static RescueAppearance human(String texture) {
        return new RescueAppearance(texture, null);
    }

    public static RescueAppearance of(EntityType type) {
        return new RescueAppearance(null, type);
    }

    public NPCConfiguration configure(String[] holograms, String chatName, Function<SkyBlockPlayer, Pos> position,
                                      Predicate<SkyBlockPlayer> visible, boolean looking, boolean dynamic) {
        if (texture != null) {
            return new HumanConfiguration() {
                @Override
                public String texture(HypixelPlayer player) {
                    return texture;
                }

                @Override
                public String signature(HypixelPlayer player) {
                    return null;
                }

                @Override
                public String[] holograms(HypixelPlayer player) {
                    return holograms;
                }

                @Override
                public Text chatNameText(HypixelPlayer player) {
                    return Text.of(chatName);
                }

                @Override
                public Pos position(HypixelPlayer player) {
                    return position.apply((SkyBlockPlayer) player);
                }

                @Override
                public boolean dynamicPosition() {
                    return dynamic;
                }

                @Override
                public boolean looking(HypixelPlayer player) {
                    return looking;
                }

                @Override
                public boolean visible(HypixelPlayer player) {
                    return visible.test((SkyBlockPlayer) player);
                }
            };
        }
        return new AnimalConfiguration() {
            @Override
            public EntityType entityType() {
                return entityType;
            }

            @Override
            public String[] holograms(HypixelPlayer player) {
                return holograms;
            }

            @Override
            public Text chatNameText(HypixelPlayer player) {
                return Text.of(chatName);
            }

            @Override
            public Pos position(HypixelPlayer player) {
                return position.apply((SkyBlockPlayer) player);
            }

            @Override
            public boolean dynamicPosition() {
                return dynamic;
            }

            @Override
            public boolean looking(HypixelPlayer player) {
                return looking;
            }

            @Override
            public boolean visible(HypixelPlayer player) {
                return visible.test((SkyBlockPlayer) player);
            }
        };
    }
}
