package net.swofty.type.skyblockgeneric.gui.inventories.admin;

import net.minestom.server.MinecraftServer;
import net.minestom.server.event.inventory.InventoryPreClickEvent;
import net.minestom.server.inventory.InventoryType;
import net.minestom.server.item.*;
import net.swofty.commons.text.Text;
import net.swofty.type.generic.gui.inventory.*;
import net.swofty.type.generic.gui.inventory.item.*;
import net.swofty.type.generic.user.HypixelPlayer;
import net.swofty.type.skyblockgeneric.gui.inventories.sbmenu.GUISkyBlockMenu;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

public class GUIAdmin extends HypixelInventoryGUI {
    private static final int[] SLOTS={10,11,12,13,14,15,16,19,20,21,22,23,24,25,28,29,30,31,32,33,34,37,38,39,40,41,42,43};
    private final AdminCatalog.Category category;
    private final int page;
    private final String search;
    public GUIAdmin() { this(null,0,""); }
    private GUIAdmin(AdminCatalog.Category category,int page,String search) {
        super(Text.literal(category==null ? "Admin Menu" : category.title),InventoryType.CHEST_6_ROW);
        this.category=category;this.page=page;this.search=search;
    }
    private static boolean allowed(HypixelPlayer player) {
        if(player.getRank().isStaff()) return true;
        player.sendMessage("<c>This menu requires staff rank.");player.closeInventory();return false;
    }
    @Override public void setItems(InventoryGUIOpenEvent event) {
        if(!allowed(event.player())) return;
        fill(Material.BLACK_STAINED_GLASS_PANE, "");
        button(49,Material.ARROW,"Back",List.of(),player -> {
            if(category==null) player.openView(new GUISkyBlockMenu()); else new GUIAdmin().open(player);
        });
        var all=AdminCatalog.actions(event.player());
        if(category==null) {
            int[] slots={20,22,24,31};int i=0;
            for(var value:AdminCatalog.Category.values()) {
                long count=all.stream().filter(a -> a.details().category()==value).count();
                button(slots[i++],value.icon,value.title,List.of(count+" available commands","Click to browse"),player -> new GUIAdmin(value,0,"").open(player));
            }
            return;
        }
        var actions=all.stream().filter(a -> a.details().category()==category)
                .filter(a -> (String.join(" ",a.command().labels())+" "+a.details().description()).toLowerCase(Locale.ROOT).contains(search.toLowerCase(Locale.ROOT))).toList();
        int last=Math.max(0,(actions.size()-1)/SLOTS.length), current=Math.min(page,last);
        for(int i=0;i<SLOTS.length && current*SLOTS.length+i<actions.size();i++) {
            var action=actions.get(current*SLOTS.length+i);
            button(SLOTS[i],category.icon,"/"+action.command().name(),List.of(action.details().usage(),action.details().description(),"Click to configure and review"),
                    player -> new ActionMenu(action,this,"").open(player));
        }
        if(actions.isEmpty()) set(22,ItemStacks.item(Material.BARRIER,1,"<e>No matching commands"));
        if(current>0) button(45,Material.ARROW,"Previous page",List.of(),p -> new GUIAdmin(category,current-1,search).open(p));
        if(current<last) button(53,Material.ARROW,"Next page",List.of(),p -> new GUIAdmin(category,current+1,search).open(p));
        set(48,ItemStacks.item(Material.PAPER,1,Text.literal("Page "+(current+1)+" / "+(last+1)),List.of()));
        set(new GUIQueryItem(50) {
            @Override public ItemStack.Builder getItem(HypixelPlayer p) {return ItemStacks.item(Material.OAK_SIGN,1,"<a>Search commands");}
            @Override public HypixelInventoryGUI onQueryFinish(String query,HypixelPlayer p) {return allowed(p) ? new GUIAdmin(category,0,query==null ? "" : query.trim()) : null;}
        });
    }
    private static List<Text> wrap(List<String> lines) {
        var result=new ArrayList<Text>();
        for(String line:lines) {
            StringBuilder current=new StringBuilder();
            for(String word:line.split(" ")) {
                if(current.length()+word.length()>48 && !current.isEmpty()) { result.add(Text.literal(current.toString()));current.setLength(0); }
                if(!current.isEmpty())current.append(' ');
                current.append(word);
            }
            result.add(Text.literal(current.toString()));
        }
        return result;
    }

    private void button(int slot,Material material,String title,List<String> lines,java.util.function.Consumer<HypixelPlayer> action) {
        set(new GUIClickableItem(slot) {
            @Override public ItemStack.Builder getItem(HypixelPlayer p) {return ItemStacks.item(material,1,Text.literal(title),wrap(lines));}
            @Override public void run(InventoryPreClickEvent e,HypixelPlayer p) {if(allowed(p)) action.accept(p);}
        });
    }
    @Override public boolean allowHotkeying() {return false;}
    @Override public void onBottomClick(InventoryPreClickEvent e) {e.setCancelled(true);}

    private static final class ActionMenu extends HypixelInventoryGUI {
        private final AdminCatalog.Action action;
        private final GUIAdmin previous;
        private final String arguments;
        private final AtomicBoolean used=new AtomicBoolean();
        ActionMenu(AdminCatalog.Action action,GUIAdmin previous,String arguments) {
            super(Text.literal("Admin: /"+action.command().name()),InventoryType.CHEST_3_ROW);
            this.action=action;this.previous=previous;this.arguments=arguments;
        }
        @Override public void setItems(InventoryGUIOpenEvent event) {
            if(!allowed(event.player())) return;
            fill(Material.BLACK_STAINED_GLASS_PANE,"");
            set(GUIClickableItem.getGoBackItem(22,previous));
            var lore=new ArrayList<Text>();
            lore.addAll(wrap(List.of(action.details().usage(),action.details().description())));
            if(action.command().labels().contains("forceadmin")) lore.add(Text.literal("Developer allowlist required; staff rank alone is insufficient."));
            lore.add(Text.literal("All existing command and service checks apply."));
            set(4,ItemStacks.item(Material.BOOK,1,Text.literal("Command details"),lore));
            set(new GUIQueryItem(11) {
                @Override public ItemStack.Builder getItem(HypixelPlayer p) {return ItemStacks.item(Material.OAK_SIGN,1,Text.literal("Edit arguments"),List.of(Text.literal("Enter only the arguments, without /"+action.command().name()),Text.literal("Current: "+(arguments.isBlank()?"(none)":arguments))));}
                @Override public String[] lines() {return new String[]{"Command arguments","No /command prefix"};}
                @Override public HypixelInventoryGUI onQueryFinish(String query,HypixelPlayer p) {
                    if(!allowed(p)) return null;
                    try {AdminCatalog.commandLine(action.command().name(),query);return new ActionMenu(action,previous,query==null?"":query.trim());}
                    catch(IllegalArgumentException error) {p.sendMessage(Text.literal(error.getMessage()));return ActionMenu.this;}
                }
            });
            set(new GUIClickableItem(15) {
                @Override public ItemStack.Builder getItem(HypixelPlayer p) {return ItemStacks.item(Material.REDSTONE_BLOCK,1,Text.literal("Run command"),List.of(Text.literal("/"+AdminCatalog.commandLine(action.command().name(),arguments)),Text.literal("Review the target and arguments above."),Text.literal("Click to execute as yourself.")));}
                @Override public void run(InventoryPreClickEvent e,HypixelPlayer p) {
                    if(!allowed(p) || used.get()) return;
                    boolean stillAvailable=AdminCatalog.actions(p).stream().anyMatch(a -> a.command().name().equals(action.command().name()));
                    if(!stillAvailable) {p.sendMessage("<c>This command is no longer available.");return;}
                    if(!used.compareAndSet(false,true))return;
                    p.closeInventory();
                    MinecraftServer.getCommandManager().execute(p,AdminCatalog.commandLine(action.command().name(),arguments));
                }
            });
        }
        @Override public boolean allowHotkeying() {return false;}
        @Override public void onBottomClick(InventoryPreClickEvent e) {e.setCancelled(true);}
    }
}
