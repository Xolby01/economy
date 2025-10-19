package com.xolby.economy.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import java.util.*;

public class EconomyConfig {
    private ConfigGenerator.ConfigData config;

    public void load() {
        config = ConfigGenerator.loadOrCreate();
    }

    public Map<String, Double> getShopItems() {
        return config.shopItems;
    }

    public Double getItemPrice(Item item) {
        ResourceLocation key = BuiltInRegistries.ITEM.getKey(item);
        return config.shopItems.get(key.toString());
    }

    public List<ConfigGenerator.SavingsTier> getSavingsTiers() {
        return config.savingsTiers;
    }

    public ConfigGenerator.SavingsTier getCurrentTier(double savingsAmount) {
        ConfigGenerator.SavingsTier current = null;
        for (ConfigGenerator.SavingsTier tier : config.savingsTiers) {
            if (savingsAmount >= tier.minAmount) {
                current = tier;
            }
        }
        return current != null ? current : config.savingsTiers.get(0);
    }

    public String getCurrencySymbol() {
        return config.settings.currencySymbol;
    }

    public double getSellPriceMultiplier() {
        return config.settings.sellPriceMultiplier;
    }

    public double getStartingMoney() {
        return config.settings.startingMoney;
    }

    public double getMaxTransferAmount() {
        return config.settings.maxTransferAmount;
    }
}