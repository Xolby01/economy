package com.xolby.economy.command;

import com.xolby.economy.EconomyMod;
import com.xolby.economy.data.PlayerDataManager;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.storage.PlayerDataStorage;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class TopMoneyCommand {

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("topmoney")
            .executes(context -> showTopMoney(context))
        );
    }

    private static int showTopMoney(CommandContext<CommandSourceStack> context) {
        ServerPlayer player = context.getSource().getPlayer();
        if (player == null) return 0;

        PlayerDataManager pdm = EconomyMod.getPlayerDataManager();
        List<Map.Entry<UUID, Double>> topPlayers = pdm.getTopMoney(10);

        player.sendSystemMessage(Component.literal("§6§l=== TOP ARGENT ==="));
        player.sendSystemMessage(Component.literal(""));

        int position = 1;
        for (Map.Entry<UUID, Double> entry : topPlayers) {
            UUID uuid = entry.getKey();
            double money = entry.getValue();
            
            String playerName = getPlayerName(context, uuid);
            String medal = getMedal(position);
            
            player.sendSystemMessage(Component.literal(
                String.format("§f%s §7#%d §e%s §7- §a%.2f$", 
                    medal, position, playerName, money)
            ));
            
            position++;
        }

        player.sendSystemMessage(Component.literal(""));
        
        // Afficher la position du joueur actuel
        int playerPosition = getPlayerPosition(pdm, player.getUUID());
        if (playerPosition > 10) {
            double playerMoney = pdm.getMoney(player.getUUID());
            player.sendSystemMessage(Component.literal(
                String.format("§7Votre position: §f#%d §7- §a%.2f$", 
                    playerPosition, playerMoney)
            ));
        }

        return 1;
    }

    private static String getMedal(int position) {
        return switch (position) {
            case 1 -> "🥇";
            case 2 -> "🥈";
            case 3 -> "🥉";
            default -> "  ";
        };
    }

    private static String getPlayerName(CommandContext<CommandSourceStack> context, UUID uuid) {
        ServerPlayer targetPlayer = context.getSource().getServer().getPlayerList().getPlayer(uuid);
        if (targetPlayer != null) {
            return targetPlayer.getName().getString();
        }
        
        try {
            return context.getSource().getServer()
                .getProfileCache()
                .get(uuid)
                .map(profile -> profile.getName())
                .orElse("Joueur Inconnu");
        } catch (Exception e) {
            return "Joueur Inconnu";
        }
    }

    private static int getPlayerPosition(PlayerDataManager pdm, UUID uuid) {
        List<Map.Entry<UUID, Double>> allPlayers = pdm.getTopMoney(Integer.MAX_VALUE);
        for (int i = 0; i < allPlayers.size(); i++) {
            if (allPlayers.get(i).getKey().equals(uuid)) {
                return i + 1;
            }
        }
        return allPlayers.size() + 1;
    }
}