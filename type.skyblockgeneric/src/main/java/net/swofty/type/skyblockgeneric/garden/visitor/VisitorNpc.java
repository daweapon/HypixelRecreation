package net.swofty.type.skyblockgeneric.garden.visitor;

import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.VillagerProfession;
import net.minestom.server.instance.Instance;
import net.swofty.type.generic.entity.npc.HypixelNPC;
import net.swofty.type.generic.entity.npc.configuration.AnimalConfiguration;
import net.swofty.type.generic.entity.npc.configuration.HumanConfiguration;
import net.swofty.type.generic.entity.npc.configuration.NPCConfiguration;
import net.swofty.type.generic.entity.npc.configuration.VillagerConfiguration;
import net.swofty.type.generic.event.custom.NPCInteractEvent;
import net.swofty.type.generic.user.HypixelPlayer;
import net.swofty.type.skyblockgeneric.garden.Gardens;
import net.swofty.type.skyblockgeneric.garden.npc.GardenGround;
import net.swofty.type.skyblockgeneric.garden.npc.GardenNpcSkins;
import net.swofty.type.skyblockgeneric.gui.inventories.garden.GUIVisitor;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;
import net.swofty.type.skyblockgeneric.user.garden.SkyBlockGarden;

public class VisitorNpc extends HypixelNPC {
    private final VisitorCatalogue.Visitor visitor;

    public VisitorNpc(VisitorCatalogue.Visitor visitor) {
        super(configuration(visitor));
        this.visitor = visitor;
    }

    private static NPCConfiguration configuration(VisitorCatalogue.Visitor visitor) {
        GardenNpcSkins.Entity entity = GardenNpcSkins.entity(visitor.id());
        if (entity == null) {
            return new HumanConfiguration() {
                @Override
                public String signature(HypixelPlayer player) {
                    return GardenNpcSkins.of(visitor.id()).signature();
                }

                @Override
                public String texture(HypixelPlayer player) {
                    return GardenNpcSkins.of(visitor.id()).texture();
                }

                @Override
                public String[] holograms(HypixelPlayer player) {
                    return Shared.holograms(visitor);
                }

                @Override
                public Pos position(HypixelPlayer player) {
                    return Shared.position(visitor, player);
                }

                @Override
                public Instance instance(HypixelPlayer player) {
                    return player.getInstance();
                }

                @Override
                public boolean visible(HypixelPlayer player) {
                    return Shared.visible(visitor, player);
                }

                @Override
                public boolean dynamicPosition() {
                    return true;
                }

                @Override
                public Pos spawnPosition(HypixelPlayer player) {
                    return Shared.spawnPosition(visitor, player);
                }

                @Override
                public boolean walksToNewPosition() {
                    return true;
                }

                @Override
                public boolean looking(HypixelPlayer player) {
                    return true;
                }

                @Override
                public String chatName(HypixelPlayer player) {
                    return visitor.coloured();
                }
            };
        }
        if (entity.type().equals("VILLAGER")) {
            VillagerProfession profession = profession(entity.profession());
            return new VillagerConfiguration() {
                @Override
                public VillagerProfession profession() {
                    return profession;
                }

                @Override
                public String[] holograms(HypixelPlayer player) {
                    return Shared.holograms(visitor);
                }

                @Override
                public Pos position(HypixelPlayer player) {
                    return Shared.position(visitor, player);
                }

                @Override
                public Instance instance(HypixelPlayer player) {
                    return player.getInstance();
                }

                @Override
                public boolean visible(HypixelPlayer player) {
                    return Shared.visible(visitor, player);
                }

                @Override
                public boolean dynamicPosition() {
                    return true;
                }

                @Override
                public Pos spawnPosition(HypixelPlayer player) {
                    return Shared.spawnPosition(visitor, player);
                }

                @Override
                public boolean walksToNewPosition() {
                    return true;
                }

                @Override
                public boolean looking(HypixelPlayer player) {
                    return true;
                }

                @Override
                public String chatName(HypixelPlayer player) {
                    return visitor.coloured();
                }
            };
        }
        EntityType type = switch (entity.type()) {
            case "WITCH" -> EntityType.WITCH;
            case "ZOMBIE" -> EntityType.ZOMBIE;
            default -> EntityType.SNOW_GOLEM;
        };
        return new AnimalConfiguration() {
            @Override
            public EntityType entityType() {
                return type;
            }

            @Override
            public String[] holograms(HypixelPlayer player) {
                return Shared.holograms(visitor);
            }

            @Override
            public Pos position(HypixelPlayer player) {
                return Shared.position(visitor, player);
            }

            @Override
            public Instance instance(HypixelPlayer player) {
                return player.getInstance();
            }

            @Override
            public boolean visible(HypixelPlayer player) {
                return Shared.visible(visitor, player);
            }

            @Override
            public boolean dynamicPosition() {
                return true;
            }

            @Override
            public Pos spawnPosition(HypixelPlayer player) {
                return Shared.spawnPosition(visitor, player);
            }

            @Override
            public boolean walksToNewPosition() {
                return true;
            }

            @Override
            public boolean looking(HypixelPlayer player) {
                return true;
            }

            @Override
            public String chatName(HypixelPlayer player) {
                return visitor.coloured();
            }
        };
    }

    private static VillagerProfession profession(String name) {
        return switch (name == null ? "FARMER" : name) {
            case "CLERIC" -> VillagerProfession.CLERIC;
            case "LIBRARIAN" -> VillagerProfession.LIBRARIAN;
            case "BUTCHER" -> VillagerProfession.BUTCHER;
            case "TOOLSMITH" -> VillagerProfession.TOOLSMITH;
            default -> VillagerProfession.FARMER;
        };
    }

    private static final class Shared {
        private static String[] holograms(VisitorCatalogue.Visitor visitor) {
            return new String[]{visitor.coloured(), "<e><l>CLICK"};
        }

        private static Pos position(VisitorCatalogue.Visitor visitor, HypixelPlayer player) {
            int slot = VisitorSlots.slotOf(player.getUuid(), visitor.id());
            Pos wanted = VisitorSlots.POSITIONS[Math.max(0, slot) % VisitorSlots.POSITIONS.length];
            return GardenGround.onGround(player.getInstance(), wanted);
        }

        private static Pos spawnPosition(VisitorCatalogue.Visitor visitor, HypixelPlayer player) {
            Pos slot = position(visitor, player);
            VisitorData.Present present = Gardens.profile((SkyBlockPlayer) player).visitors().find(visitor.id());
            if (present == null || System.currentTimeMillis() - present.getArrivedAt() > VisitorSlots.ARRIVAL_WINDOW_MILLIS) {
                return slot;
            }
            return GardenGround.onGround(player.getInstance(), VisitorSlots.ENTRANCE);
        }

        private static boolean visible(VisitorCatalogue.Visitor visitor, HypixelPlayer player) {
            return VisitorSlots.slotOf(player.getUuid(), visitor.id()) >= 0
                    && player.getInstance() != null
                    && player.getInstance() == SkyBlockGarden.of((SkyBlockPlayer) player).getGardenInstance();
        }
    }

    @Override
    public String getName() {
        return visitor.name();
    }

    @Override
    public void onClick(NPCInteractEvent event) {
        SkyBlockPlayer player = (SkyBlockPlayer) event.player();
        if (!visitor.offerLines().isEmpty()) {
            sendNPCMessage(player, VisitorText.fill(visitor.offerLines().getFirst(), player));
        }
        player.openView(new GUIVisitor(visitor.id()));
    }
}
