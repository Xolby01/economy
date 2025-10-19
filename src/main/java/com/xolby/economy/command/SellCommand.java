package com.xolby.economy.command;

import com.xolby.economy.EconomyMod;
import com.xolby.economy.data.PlayerDataManager;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public class SellCommand {

    private static final SuggestionProvider<CommandSourceStack> SUGGEST_ALL = (context, builder) -> {
        return SharedSuggestionProvider.suggest(new String[]{"all"}, builder);
    };

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("sell")
            .then(Commands.literal("all")
                .suggests(SUGGEST_ALL)
                .executes(context -> sellAll(context))
            )
        );
    }

    private static int sellAll(CommandContext<CommandSourceStack> context) {
        ServerPlayer player = context.getSource().getPlayer();
        if (player == null) return 0;

        PlayerDataManager pdm = EconomyMod.getPlayerDataManager();
        double totalEarned = 0;
        int itemsSold = 0;

        for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
            ItemStack stack = player.getInventory().getItem(i);
            if (stack.isEmpty()) continue;

            Item item = stack.getItem();
            ResourceLocation itemId = BuiltInRegistries.ITEM.getKey(item);
            Double price = EconomyMod.getEconomyConfig().getItemPrice(item);

            if (price != null && price > 0) {
                int count = stack.getCount();
                double sellMultiplier = EconomyMod.getEconomyConfig().getSellPriceMultiplier();
                double earned = price * count * sellMultiplier;
                
                totalEarned += earned;
                itemsSold += count;
                
                player.getInventory().setItem(i, ItemStack.EMPTY);
            }
        }

        if (itemsSold == 0) {
            player.sendSystemMessage(Component.literal(
                "§cVous n'avez aucun item vendable dans votre inventaire!"
            ));
            return 0;
        }

        pdm.addMoney(player.getUUID(), totalEarned);
        
        player.sendSystemMessage(Component.literal("§6§l=== VENTE ==="));
        player.sendSystemMessage(Component.literal(
            String.format("§aVous avez vendu %d items pour §f%.2f$", itemsSold, totalEarned)
        ));
        player.sendSystemMessage(Component.literal(
            String.format("§eNouveau solde: §f%.2f$", pdm.getMoney(player.getUUID()))
        ));

        return 1;
    }
}