package me.swipez.terminatornpc.loadout;

import me.swipez.terminatornpc.TerminatorNPC;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.enchantments.EnchantmentTarget;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.LinkedList;
import java.util.List;

public class TerminatorLoadout implements Listener {

    private final Inventory displayInventory;
    private Material HELMET_ITEM = null;
    private Material CHESTPLATE_ITEM = null;
    private Material LEGGINGS_ITEM = null;
    private Material BOOTS_ITEM = null;

    private Material SWORD_ITEM = Material.IRON_SWORD;
    private Material BLOCK_BUILDING_MATERIAL = Material.COBBLESTONE;

    public TerminatorLoadout(JavaPlugin plugin) {
        this.displayInventory = Bukkit.createInventory(null, 54, ChatColor.YELLOW+"Terminator Equipment");
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    public TerminatorLoadout(Material helmet, Material chest, Material legs, Material boots ,Material sword, Material block){
        this.displayInventory = Bukkit.createInventory(null, 54, ChatColor.YELLOW+"Terminator Equipment");
        HELMET_ITEM = helmet;
        CHESTPLATE_ITEM = chest;
        LEGGINGS_ITEM = legs;
        BOOTS_ITEM = boots;

        SWORD_ITEM = sword;
        BLOCK_BUILDING_MATERIAL = block;
    }

    public void display(Player player){
        displayInventory.clear();

        displayInventory.setItem(11, displayOrNull(HELMET_ITEM, "Helmet"));
        displayInventory.setItem(20, displayOrNull(CHESTPLATE_ITEM, "Chestplate"));
        displayInventory.setItem(29, displayOrNull(LEGGINGS_ITEM, "Leggings"));
        displayInventory.setItem(38, displayOrNull(BOOTS_ITEM, "Boots"));

        displayInventory.setItem(23, displayOrNull(SWORD_ITEM, "Attack Item"));
        displayInventory.setItem(24, displayOrNull(BLOCK_BUILDING_MATERIAL, "Building Block"));

        for (int i = 0; i < displayInventory.getSize(); i++){
            if (displayInventory.getItem(i) == null){
                displayInventory.setItem(i, generateDummyItem(Material.BLACK_STAINED_GLASS_PANE, " "));
            }
        }

        player.openInventory(displayInventory);
    }

    private static ItemStack generateDummyItem(Material material, String name){
        ItemStack itemStack = new ItemStack(material);
        ItemMeta meta = itemStack.getItemMeta();
        meta.setDisplayName(name);

        itemStack.setItemMeta(meta);

        return itemStack;
    }

    public TerminatorLoadout clone(){
        return new TerminatorLoadout(HELMET_ITEM, CHESTPLATE_ITEM, LEGGINGS_ITEM, BOOTS_ITEM, SWORD_ITEM, BLOCK_BUILDING_MATERIAL);
    }

    public Material getHelmetMaterial(){
        return HELMET_ITEM;
    }
    public Material getChestplateMaterial(){
        return CHESTPLATE_ITEM;
    }
    public Material getLeggingsMaterial(){
        return LEGGINGS_ITEM;
    }
    public Material getBootsMaterial(){
        return BOOTS_ITEM;
    }
    public Material getSwordMaterial(){
        return SWORD_ITEM;
    }
    public Material getBlockMaterial(){
        return BLOCK_BUILDING_MATERIAL;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event){
        if (event.getView().getTitle().toLowerCase().contains("terminator equipment")){
            Player player = (Player) event.getWhoClicked();
            ItemStack itemStack = player.getItemOnCursor();
            if (event.isLeftClick()){
                if (event.getClickedInventory().equals(displayInventory)){
                    if (attemptReplaceItemAtIndex(itemStack, event.getSlot())){
                        player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1, 1);
                        display(player);
                    }
                    else {
                        event.setCancelled(true);
                        player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1,1);
                    }
                }
            }
            if (event.isRightClick()){
                if (attemptClear(event.getSlot())){
                    player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1, 0.5F);
                    player.closeInventory();
                    new BukkitRunnable() {
                        @Override
                        public void run() {
                            display(player);
                        }
                    }.runTaskLater(TerminatorNPC.getPlugin(), 1);

                }
                event.setCancelled(true);
            }
        }
    }

    private boolean attemptClear(int index){
        if (index == 11){
            if (HELMET_ITEM != null){
                HELMET_ITEM = null;
                return true;
            }
        }
        if (index == 20){
            if (CHESTPLATE_ITEM != null){
                CHESTPLATE_ITEM = null;
                return true;
            }
        }
        if (index == 29){
            if (LEGGINGS_ITEM != null){
                LEGGINGS_ITEM = null;
                return true;
            }
        }
        if (index == 38){
            if (BOOTS_ITEM != null){
                BOOTS_ITEM = null;
                return true;
            }
        }
        if (index == 23){
            if (SWORD_ITEM != null){
                SWORD_ITEM = null;
                return true;
            }
        }
        return false;
    }

    private boolean attemptReplaceItemAtIndex(ItemStack itemStack, int index){
        if (itemStack != null){
            ItemMeta meta = itemStack.getItemMeta();
            if (meta != null){
                if (index == 11){
                    if (EnchantmentTarget.ARMOR_HEAD.includes(itemStack)){
                        HELMET_ITEM = itemStack.getType();
                        return true;
                    }
                }
                if (index == 20){
                    if (EnchantmentTarget.ARMOR_TORSO.includes(itemStack)){
                        CHESTPLATE_ITEM = itemStack.getType();
                        return true;
                    }
                }
                if (index == 29){
                    if (EnchantmentTarget.ARMOR_LEGS.includes(itemStack)){
                        LEGGINGS_ITEM = itemStack.getType();
                        return true;
                    }
                }
                if (index == 38){
                    if (EnchantmentTarget.ARMOR_FEET.includes(itemStack)){
                        BOOTS_ITEM = itemStack.getType();
                        return true;
                    }
                }
                if (index == 23){
                    if (itemStack.getType().toString().toLowerCase().contains("_axe") || itemStack.getType().toString().toLowerCase().contains("_sword")){
                        SWORD_ITEM = itemStack.getType();
                        return true;
                    }
                }
                if (index == 24){
                    if (itemStack.getType().isBlock()){
                        BLOCK_BUILDING_MATERIAL = itemStack.getType();
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private ItemStack displayOrNull(Material material, String title){
        ItemStack itemStack;
        if (material != null){
            itemStack = new ItemStack(material);
            ItemMeta meta = itemStack.getItemMeta();
            meta.setDisplayName(ChatColor.GOLD+title);
            List<String> lore = new LinkedList<>();
            if (!material.isBlock()){
                lore.add(ChatColor.GRAY.toString()+ChatColor.ITALIC+"Right click to clear me!");
            }
            meta.setLore(lore);
            itemStack.setItemMeta(meta);
        }
        else {
            itemStack = new ItemStack(Material.RED_STAINED_GLASS_PANE);
            ItemMeta meta = itemStack.getItemMeta();
            meta.setDisplayName(ChatColor.RED+title);
            List<String> lore = new LinkedList<>();
            lore.add(ChatColor.GRAY.toString()+ChatColor.ITALIC+"Left click on this slot with an item!");
            meta.setLore(lore);
            itemStack.setItemMeta(meta);
        }
        return itemStack;
    }
}