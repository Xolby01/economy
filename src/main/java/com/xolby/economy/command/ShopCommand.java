package com.xolby.economy.command;

import com.xolby.economy.EconomyMod;
import com.xolby.economy.data.PlayerDataManager;
import com.xolby.economy.gui.ShopGui;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.server.level.ServerPlayer;

public class ShopCommand {

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("shop")
            .executes(context -> openShop(context))
        );
    }

    private static int openShop(CommandContext<CommandSourceStack> context) {
        ServerPlayer player = context.getSource().getPlayer();
        if (player == null) return 0;

        ShopGui.open(player);
        return 1;
    }
}