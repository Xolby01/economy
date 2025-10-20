package com.xolby.economy.command;

import com.xolby.economy.EconomyMod;
import com.xolby.economy.config.ConfigGenerator;
import com.xolby.economy.config.EconomyConfig;
import com.xolby.economy.data.PlayerDataManager;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import java.util.List;

public class EpargneCommand {

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("epargne")
            .executes(context -> showSavings(context))
            .then(Commands.literal("deposer")
                .then(Commands.argument("amount", DoubleArgumentType.doubleArg(0.01))
                    .executes(context -> deposit(context,
                        DoubleArgumentType.getDouble(context, "amount")))
                )
            )
            .then(Commands.literal("retirer")
                .then(Commands.argument("amount", DoubleArgumentType.doubleArg(0.01))
                    .executes(context -> withdraw(context,
                        DoubleArgumentType.getDouble(context, "amount")))
                )
            )
            .then(Commands.literal("interets")
                .executes(context -> claimInterest(context))
            )
            .then(Commands.literal("paliers")
                .executes(context -> showTiers(context))
            )
        );
    }

    private static int showSavings(CommandContext<CommandSourceStack> context) {
        ServerPlayer player = context.getSource().getPlayer();
        if (player == null) return 0;

        PlayerDataManager pdm = EconomyMod.getPlayerDataManager();
        EconomyConfig config = EconomyMod.getEconomyConfig();
        
        double savings = pdm.getSavings(player.getUUID());
        double money = pdm.getMoney(player.getUUID());
        
        ConfigGenerator.SavingsTier currentTier = config.getCurrentTier(savings);

        player.sendSystemMessage(Component.literal("§6§l=== ÉPARGNE ==="));
        player.sendSystemMessage(Component.literal(""));
        player.sendSystemMessage(Component.literal(
            String.format("§eÉpargne actuelle: §f%.2f$", savings)
        ));
        player.sendSystemMessage(Component.literal(
            String.format("§aArgent disponible: §f%.2f$", money)
        ));
        player.sendSystemMessage(Component.literal(""));
        player.sendSystemMessage(Component.literal(
            String.format("§bPalier actuel: §f%s", currentTier.name)
        ));
        player.sendSystemMessage(Component.literal(
            String.format("§bTaux d'intérêt: §f%.1f%%", currentTier.interestRate * 100)
        ));
        player.sendSystemMessage(Component.literal(""));
        player.sendSystemMessage(Component.literal(
            "§7Utilisez §f/epargne deposer <montant> §7pour déposer"
        ));
        player.sendSystemMessage(Component.literal(
            "§7Utilisez §f/epargne retirer <montant> §7pour retirer"
        ));
        player.sendSystemMessage(Component.literal(
            "§7Utilisez §f/epargne interets §7pour récupérer vos intérêts"
        ));
        player.sendSystemMessage(Component.literal(
            "§7Utilisez §f/epargne paliers §7pour voir tous les paliers"
        ));

        return 1;
    }

    private static int deposit(CommandContext<CommandSourceStack> context, double amount) {
        ServerPlayer player = context.getSource().getPlayer();
        if (player == null) return 0;

        PlayerDataManager pdm = EconomyMod.getPlayerDataManager();
        double money = pdm.getMoney(player.getUUID());

        if (money < amount) {
            player.sendSystemMessage(Component.literal(
                String.format("§cVous n'avez pas assez d'argent! Vous avez %.2f$", money)
            ));
            return 0;
        }

        pdm.removeMoney(player.getUUID(), amount);
        pdm.addSavings(player.getUUID(), amount);

        double newSavings = pdm.getSavings(player.getUUID());
        ConfigGenerator.SavingsTier tier = EconomyMod.getEconomyConfig().getCurrentTier(newSavings);

        player.sendSystemMessage(Component.literal(
            String.format("§aVous avez déposé %.2f$ dans votre épargne!", amount)
        ));
        player.sendSystemMessage(Component.literal(
            String.format("§eÉpargne totale: %.2f$ (Palier: %s - %.1f%%)", 
                newSavings, tier.name, tier.interestRate * 100)
        ));

        return 1;
    }

    private static int withdraw(CommandContext<CommandSourceStack> context, double amount) {
        ServerPlayer player = context.getSource().getPlayer();
        if (player == null) return 0;

        PlayerDataManager pdm = EconomyMod.getPlayerDataManager();
        double savings = pdm.getSavings(player.getUUID());

        if (savings < amount) {
            player.sendSystemMessage(Component.literal(
                String.format("§cVous n'avez pas assez d'épargne! Vous avez %.2f$", savings)
            ));
            return 0;
        }

        pdm.setSavings(player.getUUID(), savings - amount);
        pdm.addMoney(player.getUUID(), amount);

        player.sendSystemMessage(Component.literal(
            String.format("§aVous avez retiré %.2f$ de votre épargne!", amount)
        ));
        player.sendSystemMessage(Component.literal(
            String.format("§eÉpargne restante: %.2f$", pdm.getSavings(player.getUUID()))
        ));

        return 1;
    }

    private static int claimInterest(CommandContext<CommandSourceStack> context) {
        ServerPlayer player = context.getSource().getPlayer();
        if (player == null) return 0;

        PlayerDataManager pdm = EconomyMod.getPlayerDataManager();
        EconomyConfig config = EconomyMod.getEconomyConfig();
        
        double savings = pdm.getSavings(player.getUUID());
        
        if (savings <= 0) {
            player.sendSystemMessage(Component.literal(
                "§cVous n'avez pas d'épargne pour générer des intérêts!"
            ));
            return 0;
        }

        ConfigGenerator.SavingsTier tier = config.getCurrentTier(savings);
        double interest = savings * tier.interestRate;

        pdm.addSavings(player.getUUID(), interest);
        double newSavings = pdm.getSavings(player.getUUID());

        player.sendSystemMessage(Component.literal("§6§l=== INTÉRÊTS ==="));
        player.sendSystemMessage(Component.literal(
            String.format("§aPalier: §f%s §7(%.1f%%)", tier.name, tier.interestRate * 100)
        ));
        player.sendSystemMessage(Component.literal(
            String.format("§aIntérêts générés: §f+%.2f$", interest)
        ));
        player.sendSystemMessage(Component.literal(
            String.format("§eNouvelle épargne: §f%.2f$", newSavings)
        ));

        return 1;
    }

    private static int showTiers(CommandContext<CommandSourceStack> context) {
        ServerPlayer player = context.getSource().getPlayer();
        if (player == null) return 0;

        EconomyConfig config = EconomyMod.getEconomyConfig();
        List<ConfigGenerator.SavingsTier> tiers = config.getSavingsTiers();

        player.sendSystemMessage(Component.literal("§6§l=== PALIERS D'ÉPARGNE ==="));
        player.sendSystemMessage(Component.literal(""));

        for (ConfigGenerator.SavingsTier tier : tiers) {
            player.sendSystemMessage(Component.literal(
                String.format("§e%s §7- À partir de §f%.2f$", tier.name, tier.minAmount)
            ));
            player.sendSystemMessage(Component.literal(
                String.format("  §7%s §7(§a%.1f%%§7)", tier.description, tier.interestRate * 100)
            ));
        }

        return 1;
    }
}
