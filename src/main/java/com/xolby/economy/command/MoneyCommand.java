package com.xolby.economy.command;

import com.xolby.economy.EconomyMod;
import com.xolby.economy.data.PlayerDataManager;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

public class MoneyCommand {

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("money")
            .executes(context -> showMoney(context))
            .then(Commands.literal("set")
                .requires(source -> source.hasPermission(2))
                .then(Commands.argument("player", EntityArgument.player())
                    .then(Commands.argument("amount", DoubleArgumentType.doubleArg(0))
                        .executes(context -> setMoney(context,
                            EntityArgument.getPlayer(context, "player"),
                            DoubleArgumentType.getDouble(context, "amount")))
                    )
                )
            )
            .then(Commands.literal("add")
                .requires(source -> source.hasPermission(2))
                .then(Commands.argument("player", EntityArgument.player())
                    .then(Commands.argument("amount", DoubleArgumentType.doubleArg(0))
                        .executes(context -> addMoney(context,
                            EntityArgument.getPlayer(context, "player"),
                            DoubleArgumentType.getDouble(context, "amount")))
                    )
                )
            )
            .then(Commands.literal("remove")
                .requires(source -> source.hasPermission(2))
                .then(Commands.argument("player", EntityArgument.player())
                    .then(Commands.argument("amount", DoubleArgumentType.doubleArg(0))
                        .executes(context -> removeMoney(context,
                            EntityArgument.getPlayer(context, "player"),
                            DoubleArgumentType.getDouble(context, "amount")))
                    )
                )
            )
        );
    }

    private static int showMoney(CommandContext<CommandSourceStack> context) {
        ServerPlayer player = context.getSource().getPlayer();
        if (player == null) return 0;

        PlayerDataManager pdm = EconomyMod.getPlayerDataManager();
        double money = pdm.getMoney(player.getUUID());
        double savings = pdm.getSavings(player.getUUID());

        player.sendSystemMessage(Component.literal("§6§l=== VOTRE ARGENT ==="));
        player.sendSystemMessage(Component.literal(String.format("§aArgent disponible: §f%.2f$", money)));
        player.sendSystemMessage(Component.literal(String.format("§eÉpargne: §f%.2f$", savings)));
        player.sendSystemMessage(Component.literal(String.format("§bTotal: §f%.2f$", money + savings)));

        return 1;
    }

    private static int setMoney(CommandContext<CommandSourceStack> context, ServerPlayer target, double amount) {
        PlayerDataManager pdm = EconomyMod.getPlayerDataManager();
        pdm.setMoney(target.getUUID(), amount);

        context.getSource().sendSuccess(
            () -> Component.literal(String.format("§aArgent de %s défini à %.2f$", 
                target.getName().getString(), amount)),
            true
        );

        target.sendSystemMessage(Component.literal(
            String.format("§aVotre argent a été défini à %.2f$", amount)
        ));

        return 1;
    }

    private static int addMoney(CommandContext<CommandSourceStack> context, ServerPlayer target, double amount) {
        PlayerDataManager pdm = EconomyMod.getPlayerDataManager();
        pdm.addMoney(target.getUUID(), amount);
        double newAmount = pdm.getMoney(target.getUUID());

        context.getSource().sendSuccess(
            () -> Component.literal(String.format("§a%.2f$ ajoutés à %s (nouveau solde: %.2f$)", 
                amount, target.getName().getString(), newAmount)),
            true
        );

        target.sendSystemMessage(Component.literal(
            String.format("§aVous avez reçu %.2f$ (nouveau solde: %.2f$)", amount, newAmount)
        ));

        return 1;
    }

    private static int removeMoney(CommandContext<CommandSourceStack> context, ServerPlayer target, double amount) {
        PlayerDataManager pdm = EconomyMod.getPlayerDataManager();
        
        if (!pdm.removeMoney(target.getUUID(), amount)) {
            context.getSource().sendFailure(Component.literal("§cLe joueur n'a pas assez d'argent!"));
            return 0;
        }

        double newAmount = pdm.getMoney(target.getUUID());

        context.getSource().sendSuccess(
            () -> Component.literal(String.format("§a%.2f$ retirés de %s (nouveau solde: %.2f$)", 
                amount, target.getName().getString(), newAmount)),
            true
        );

        target.sendSystemMessage(Component.literal(
            String.format("§c%.2f$ ont été retirés de votre compte (nouveau solde: %.2f$)", 
                amount, newAmount)
        ));

        return 1;
    }
}