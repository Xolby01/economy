package com.xolby.economy;

import com.xolby.economy.command.*;
import com.xolby.economy.config.EconomyConfig;
import com.xolby.economy.data.PlayerDataManager;
import com.mojang.logging.LogUtils;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.event.server.ServerStartingEvent;
import net.neoforged.neoforge.event.server.ServerStoppingEvent;
import org.slf4j.Logger;

@Mod("economy")
public class EconomyMod {
    public static final String MODID = "economy";
    private static final Logger LOGGER = LogUtils.getLogger();
    
    private static PlayerDataManager playerDataManager;
    private static EconomyConfig economyConfig;

    public EconomyMod(IEventBus modEventBus, ModContainer modContainer) {
        modEventBus.addListener(this::commonSetup);
        
        NeoForge.EVENT_BUS.addListener(this::onServerStarting);
        NeoForge.EVENT_BUS.addListener(this::onServerStopping);
        NeoForge.EVENT_BUS.addListener(this::onRegisterCommands);
    }

    private void commonSetup(final FMLCommonSetupEvent event) {
        LOGGER.info("Economy Mod Setup");
    }

    private void onServerStarting(ServerStartingEvent event) {
        LOGGER.info("Economy Mod - Server Starting");
        economyConfig = new EconomyConfig();
        economyConfig.load();
        playerDataManager = new PlayerDataManager(event.getServer());
        playerDataManager.load();
    }

    private void onServerStopping(ServerStoppingEvent event) {
        LOGGER.info("Economy Mod - Server Stopping");
        if (playerDataManager != null) {
            playerDataManager.save();
        }
    }

    private void onRegisterCommands(RegisterCommandsEvent event) {
        LOGGER.info("Registering Economy Commands");
        ShopCommand.register(event.getDispatcher());
        MoneyCommand.register(event.getDispatcher());
        SellCommand.register(event.getDispatcher());
        PayCommand.register(event.getDispatcher());
        TopMoneyCommand.register(event.getDispatcher());
        EpargneCommand.register(event.getDispatcher());
    }

    public static PlayerDataManager getPlayerDataManager() {
        return playerDataManager;
    }

    public static EconomyConfig getEconomyConfig() {
        return economyConfig;
    }
}
