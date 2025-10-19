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

public class PayCommand {

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("pay")
            .then(Commands.argument("player", EntityArgument.player())
                .then(Commands.argument("amount", DoubleArgumentType.doubleArg(0.01))
                    .executes(context -> payPlayer(context,
                        EntityArgument.getPlayer(context, "player"),
                        DoubleArgumentType.getDouble(context, "amount")))
                )
            )
        );
    }

    private static int payPlayer(CommandContext<CommandSourceStack> context, ServerPlayer target, double amount) {
        ServerPlayer sender = context.getSource().getPlayer();
        if (sender == null) return 0;

        if (sender.getUUID().equals(target.getUUID())) {
            sender.sendSystemMessage(Component.literal("§cVous ne pouvez pas vous envoyer de l'argent à vous-même!"));
            return 0;
        }

        PlayerDataManager pdm = EconomyMod.getPlayerDataManager();
        double senderMoney = pdm.getMoney(sender.getUUID());

        if (senderMoney < amount) {
            sender.sendSystemMessage(Component.literal(
                String.format("§cVous n'avez pas assez d'argent! Vous avez %.2f$, il faut %.2f$", 
                    senderMoney, amount)
            ));
            return 0;
        }

        pdm.removeMoney(sender.getUUID(), amount);
        pdm.addMoney(target.getUUID(), amount);

        sender.sendSystemMessage(Component.literal(
            String.format("§aVous avez envoyé %.2f$ à %s", amount, target.getName().getString())
        ));
        sender.sendSystemMessage(Component.literal(
            String.format("§eNouveau solde: %.2f$", pdm.getMoney(sender.getUUID()))
        ));

        target.sendSystemMessage(Component.literal(
            String.format("§aVous avez reçu %.2f$ de %s", amount, sender.getName().getString())
        ));
        target.sendSystemMessage(Component.literal(
            String.format("§eNouveau solde: %.2f$", pdm.getMoney(target.getUUID()))
        ));

        return 1;
    }
}