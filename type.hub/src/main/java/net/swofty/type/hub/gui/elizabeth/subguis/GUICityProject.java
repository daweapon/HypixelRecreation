package net.swofty.type.hub.gui.elizabeth.subguis;

import net.minestom.server.event.inventory.InventoryPreClickEvent;
import net.minestom.server.inventory.InventoryType;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.swofty.commons.StringUtility;
import net.swofty.type.generic.gui.inventory.HypixelInventoryGUI;
import net.swofty.type.generic.gui.inventory.ItemStacks;
import net.swofty.type.generic.gui.inventory.item.GUIClickableItem;
import net.swofty.type.generic.gui.inventory.item.GUIItem;
import net.swofty.type.generic.user.HypixelPlayer;
import net.swofty.type.skyblockgeneric.cityprojects.CityProject;
import net.swofty.type.skyblockgeneric.cityprojects.CityProjectContributions;
import net.swofty.type.skyblockgeneric.item.SkyBlockItem;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.stream.Collectors;

// The current City Project, laid out as on Hypixel: the project at the top, its four components
// across the middle with a contribute button under each, and the personal memento bonus below.
public class GUICityProject extends HypixelInventoryGUI {
    private static final int[] COMPONENT_SLOTS = {19, 21, 23, 25};
    private static final int PROGRESS_BAR_LENGTH = 20;

    private final CityProject project;

    public GUICityProject(CityProject project) {
        super("Project - " + project.getDisplayName(), InventoryType.CHEST_6_ROW);
        this.project = project;
    }

    @Override
    public void onOpen(InventoryGUIOpenEvent e) {
        fill(ItemStacks.filler(Material.BLACK_STAINED_GLASS_PANE));
        render((SkyBlockPlayer) e.player());
    }

    private void render(SkyBlockPlayer player) {
        long total = CityProjectContributions.globalTotal(project);
        double progress = Math.min(1, (double) total / project.getRequiredContributions());
        int filled = (int) Math.round(progress * PROGRESS_BAR_LENGTH);

        set(new GUIItem(4) {
            @Override
            public ItemStack.Builder getItem(HypixelPlayer p) {
                return ItemStacks.item(project.getIcon(),
                        "<a>{}\n<8>City Project\n \n<7>{}\n \n<7>Total contributions: <b>{}\n \n<7>Community progress:\n<a>{}<f>{} <e>{}%\n<7>Stage: <b><l>FUNDING",
                        project.getDisplayName(), project.getDescription(), StringUtility.commaify(total),
                        "-".repeat(filled), "-".repeat(PROGRESS_BAR_LENGTH - filled),
                        StringUtility.decimalify(progress * 100, 1));
            }
        });

        for (int i = 0; i < project.getComponents().size(); i++) {
            int index = i;
            CityProject.Component component = project.getComponents().get(i);
            long global = CityProjectContributions.global(project, index);

            set(new GUIItem(COMPONENT_SLOTS[i]) {
                @Override
                public ItemStack.Builder getItem(HypixelPlayer p) {
                    return ItemStacks.item(component.icon(),
                            "<a>{}\n<8>Project Component #{}\n \n<7>{}\n \n<7>Global: <b>{} contributions",
                            component.name(), index + 1, component.description(), StringUtility.commaify(global));
                }
            });

            set(new GUIClickableItem(COMPONENT_SLOTS[i] + 9) {
                @Override
                public void run(InventoryPreClickEvent event, HypixelPlayer p) {
                    SkyBlockPlayer clicker = (SkyBlockPlayer) p;
                    switch (CityProjectContributions.contribute(clicker, project, index)) {
                        case CONTRIBUTED -> clicker.sendMessage("<a>You contributed to <e>{}<a> and earned <e>{} Fame<a>!",
                                component.name(), StringUtility.commaify(component.fame()));
                        case ALREADY_TODAY -> clicker.sendMessage("<c>You have already contributed to this component today!");
                        case CAPPED -> clicker.sendMessage("<c>You have contributed the maximum to this component!");
                        case CANNOT_AFFORD -> clicker.sendMessage("<c>Cannot afford to contribute!");
                    }
                    render(clicker);
                    updateItemStacks(getInventory(), clicker);
                }

                @Override
                public ItemStack.Builder getItem(HypixelPlayer p) {
                    SkyBlockPlayer viewer = (SkyBlockPlayer) p;
                    long remaining = Math.max(0, project.getMementoContributions()
                            - CityProjectContributions.personalTotal(viewer, project));
                    String costs = component.costs().stream()
                            .map(cost -> new SkyBlockItem(cost.item()).getDisplayName() + " <8>x" + cost.amount())
                            .collect(Collectors.joining("\n"));
                    if (component.bits() > 0) costs += "\n<b>" + StringUtility.commaify(component.bits()) + " Bits";
                    String status = CityProjectContributions.contributedToday(viewer, project, index)
                            ? "<c>Already contributed today!"
                            : CityProjectContributions.canAfford(viewer, component)
                            ? "<e>Click to contribute!" : "<c>Cannot afford to contribute!";
                    String memento = remaining > 0
                            ? "<7>You will earn a special\n<a>memento <7>after <a>" + remaining + " <7>more\n<7>contributions!\n \n"
                            : "";
                    return ItemStacks.item(Material.GRAY_DYE,
                            "<e>Contribute this component!\n<7>Adds <b>+1 <7>global contribution to this\n<7>project and earn <e>{}\n<e>Fame<7>.\n \n{}<7>Cost\n{}\n \n{}",
                            StringUtility.commaify(component.fame()), memento, costs, status);
                }
            });
        }

        set(new GUIItem(50) {
            @Override
            public ItemStack.Builder getItem(HypixelPlayer p) {
                SkyBlockPlayer viewer = (SkyBlockPlayer) p;
                boolean earned = CityProjectContributions.personalTotal(viewer, project) >= project.getMementoContributions();
                return ItemStacks.item(Material.PLAYER_HEAD,
                        "<a>Personal Bonus\n<8>{}\n \n<7>Earn a unique <a>memento <7>item if you\n<7>contribute at least <a>{} <7>times to\n<7>the project.\n \n{}",
                        project.getDisplayName(), project.getMementoContributions(),
                        earned ? "<a>Memento earned!" : "<b>Not enough contributions (yet)!");
            }
        });
    }

    @Override
    public boolean allowHotkeying() {
        return false;
    }

    @Override
    public void onBottomClick(InventoryPreClickEvent e) {
    }
}
