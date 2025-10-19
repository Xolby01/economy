package com.xolby.economy.gui;

import com.xolby.economy.EconomyMod;
import com.xolby.economy.data.PlayerDataManager;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.SimpleContainer;

import java.util.*;

public class ShopGui {
    
    private static final int ROWS = 6;
    private static final int SLOTS = ROWS * 9;

    public static void open(ServerPlayer player) {
        Map<String, Double> shopItems = EconomyMod.getEconomyConfig().getShopItems();
        List<Map.Entry<String, Double>> itemList = new ArrayList<>(shopItems.entrySet());
        
        openPage(player, 0, itemList);
    }

    private static void openPage(ServerPlayer player, int page, List<Map.Entry<String, Double>> allItems) {
        int itemsPerPage = 45; // 5 rangées de 9 items
        int totalPages = (int) Math.ceil(allItems.size() / (double) itemsPerPage);
        int startIndex = page * itemsPerPage;
        int endIndex = Math.min(startIndex + itemsPerPage, allItems.size());

        SimpleContainer container = new SimpleContainer(SLOTS);
        
        // Remplir avec les items de la boutique
        int slot = 0;
        for (int i = startIndex; i < endIndex; i++) {
            Map.Entry<String, Double> entry = allItems.get(i);
            String itemId = entry.getKey();
            double price = entry.getValue();
            
            ItemStack displayItem = createShopItem(itemId, price);
            if (displayItem != null) {
                container.setItem(slot++, displayItem);
            }
        }

        // Barre de navigation (dernière ligne)
        fillNavigationBar(container, page, totalPages, player);

        MenuProvider provider = new SimpleMenuProvider(
            (id, inv, p) -> createMenu(id, inv, container, player, page, allItems),
            Component.literal("§6§lBoutique - Page " + (page + 1) + "/" + totalPages)
        );

        player.openMenu(provider);
    }

    private static AbstractContainerMenu createMenu(int id, Inventory playerInv, SimpleContainer container, 
                                                    ServerPlayer player, int page, List<Map.Entry<String, Double>> allItems) {
        return new ChestMenu(MenuType.GENERIC_9x6, id, playerInv, container, ROWS) {
            @Override
            public boolean stillValid(Player p) {
                return true;
            }

            @Override
            public ItemStack quickMoveStack(Player p, int slot) {
                return ItemStack.EMPTY; // Désactive le shift-click
            }

            @Override
            public void clicked(int slotId, int button, net.minecraft.world.inventory.ClickType clickType, Player p) {
                if (!(p instanceof ServerPlayer serverPlayer)) return;
                
                // Navigation buttons
                if (slotId == 45) { // Page précédente
                    if (page > 0) {
                        serverPlayer.closeContainer();
                        openPage(serverPlayer, page - 1, allItems);
                    }
                    return;
                }
                
                if (slotId == 49) { // Informations
                    showPlayerInfo(serverPlayer);
                    return;
                }
                
                if (slotId == 53) { // Page suivante
                    int totalPages = (int) Math.ceil(allItems.size() / 45.0);
                    if (page < totalPages - 1) {
                        serverPlayer.closeContainer();
                        openPage(serverPlayer, page + 1, allItems);
                    }
                    return;
                }

                // Items de la boutique
                if (slotId < 45 && slotId >= 0) {
                    ItemStack clickedItem = container.getItem(slotId);
                    if (!clickedItem.isEmpty() && clickedItem.getItem() != Items.GRAY_STAINED_GLASS_PANE) {
                        handlePurchase(serverPlayer, clickedItem, button == 1, page, allItems);
                    }
                }
            }
        };
    }

    private static ItemStack createShopItem(String itemId, double price) {
        try {
            ResourceLocation rl = ResourceLocation.parse(itemId);
            Item item = BuiltInRegistries.ITEM.get(rl);
            
            if (item == Items.AIR) return null;
            
            ItemStack stack = new ItemStack(item);
            
            List<Component> lore = new ArrayList<>();
            lore.add(Component.literal(""));
            lore.add(Component.literal("§6Prix: §f" + String.format("%.2f", price) + EconomyMod.getEconomyConfig().getCurrencySymbol()));
            lore.add(Component.literal(""));
            lore.add(Component.literal("§e▸ §7Clic gauche: §fAcheter 1"));
            lore.add(Component.literal("§e▸ §7Clic droit: §fAcheter 64"));
            lore.add(Component.literal(""));
            
            stack.set(net.minecraft.core.component.DataComponents.LORE, 
                new net.minecraft.world.item.component.ItemLore(lore));
            
            return stack;
        } catch (Exception e) {
            return null;
        }
    }

    private static void fillNavigationBar(SimpleContainer container, int page, int totalPages, ServerPlayer player) {
        // Remplir avec des vitres grises
        for (int i = 45; i < 54; i++) {
            ItemStack glass = new ItemStack(Items.GRAY_STAINED_GLASS_PANE);
            glass.set(net.minecraft.core.component.DataComponents.CUSTOM_NAME, Component.literal(" "));
            container.setItem(i, glass);
        }

        // Bouton page précédente
        if (page > 0) {
            ItemStack prevPage = new ItemStack(Items.ARROW);
            prevPage.set(net.minecraft.core.component.DataComponents.CUSTOM_NAME, 
                Component.literal("§e◀ Page précédente"));
            container.setItem(45, prevPage);
        }

        // Informations du joueur
        ItemStack info = new ItemStack(Items.EMERALD);
        PlayerDataManager pdm = EconomyMod.getPlayerDataManager();
        double money = pdm.getMoney(player.getUUID());
        
        info.set(net.minecraft.core.component.DataComponents.CUSTOM_NAME, 
            Component.literal("§a§lVotre argent"));
        
        List<Component> lore = new ArrayList<>();
        lore.add(Component.literal(""));
        lore.add(Component.literal("§fSolde: §a" + String.format("%.2f", money) + EconomyMod.getEconomyConfig().getCurrencySymbol()));
        lore.add(Component.literal(""));
        
        info.set(net.minecraft.core.component.DataComponents.LORE, 
            new net.minecraft.world.item.component.ItemLore(lore));
        
        container.setItem(49, info);

        // Bouton page suivante
        if (page < totalPages - 1) {
            ItemStack nextPage = new ItemStack(Items.ARROW);
            nextPage.set(net.minecraft.core.component.DataComponents.CUSTOM_NAME, 
                Component.literal("§ePage suivante ▶"));
            container.setItem(53, nextPage);
        }
    }

    private static void handlePurchase(ServerPlayer player, ItemStack displayItem, boolean buyStack, 
                                      int page, List<Map.Entry<String, Double>> allItems) {
        Item item = displayItem.getItem();
        ResourceLocation itemId = BuiltInRegistries.ITEM.getKey(item);
        Double price = EconomyMod.getEconomyConfig().getShopItems().get(itemId.toString());
        
        if (price == null) {
            player.sendSystemMessage(Component.literal("§cErreur: Prix non trouvé!"));
            return;
        }

        int amount = buyStack ? 64 : 1;
        double totalCost = price * amount;
        
        PlayerDataManager pdm = EconomyMod.getPlayerDataManager();
        double playerMoney = pdm.getMoney(player.getUUID());

        if (playerMoney < totalCost) {
            player.sendSystemMessage(Component.literal(
                String.format("§cVous n'avez pas assez d'argent! Coût: §f%.2f%s§c, Vous avez: §f%.2f%s", 
                    totalCost, EconomyMod.getEconomyConfig().getCurrencySymbol(),
                    playerMoney, EconomyMod.getEconomyConfig().getCurrencySymbol())
            ));
            player.playSound(net.minecraft.sounds.SoundEvents.VILLAGER_NO, 1.0f, 1.0f);
            return;
        }

        ItemStack purchasedItem = new ItemStack(item, amount);
        
        if (!player.getInventory().add(purchasedItem)) {
            player.sendSystemMessage(Component.literal("§cVotre inventaire est plein!"));
            player.playSound(net.minecraft.sounds.SoundEvents.VILLAGER_NO, 1.0f, 1.0f);
            return;
        }

        pdm.removeMoney(player.getUUID(), totalCost);
        player.sendSystemMessage(Component.literal(
            String.format("§aVous avez acheté §f%dx %s §apour §f%.2f%s", 
                amount, item.getName(purchasedItem).getString(), 
                totalCost, EconomyMod.getEconomyConfig().getCurrencySymbol())
        ));
        player.playSound(net.minecraft.sounds.SoundEvents.EXPERIENCE_ORB_PICKUP, 1.0f, 1.0f);
        
        // Rafraîchir la GUI
        player.closeContainer();
        openPage(player, page, allItems);
    }

    private static void showPlayerInfo(ServerPlayer player) {
        PlayerDataManager pdm = EconomyMod.getPlayerDataManager();
        double money = pdm.getMoney(player.getUUID());
        double savings = pdm.getSavings(player.getUUID());
        
        player.sendSystemMessage(Component.literal(""));
        player.sendSystemMessage(Component.literal("§6§l▬▬▬▬▬▬▬▬▬▬ INFORMATIONS ▬▬▬▬▬▬▬▬▬▬"));
        player.sendSystemMessage(Component.literal(""));
        player.sendSystemMessage(Component.literal("§fArgent: §a" + String.format("%.2f", money) + EconomyMod.getEconomyConfig().getCurrencySymbol()));
        player.sendSystemMessage(Component.literal("§fÉpargne: §e" + String.format("%.2f", savings) + EconomyMod.getEconomyConfig().getCurrencySymbol()));
        player.sendSystemMessage(Component.literal("§fTotal: §b" + String.format("%.2f", money + savings) + EconomyMod.getEconomyConfig().getCurrencySymbol()));
        player.sendSystemMessage(Component.literal(""));
        player.sendSystemMessage(Component.literal("§6§l▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬"));
        player.sendSystemMessage(Component.literal(""));
    }
}