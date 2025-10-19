package com.xolby.economy.data;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.minecraft.server.MinecraftServer;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

public class PlayerDataManager {
    private final Gson gson = new GsonBuilder().setPrettyPrinting().create();
    private final Path dataPath;
    private Map<UUID, PlayerEconomyData> playerData = new HashMap<>();

    public PlayerDataManager(MinecraftServer server) {
        this.dataPath = server.getServerDirectory().toPath().resolve("economy_data.json");
    }

    public void load() {
        if (Files.exists(dataPath)) {
            try (Reader reader = Files.newBufferedReader(dataPath)) {
                PlayerEconomyData[] data = gson.fromJson(reader, PlayerEconomyData[].class);
                if (data != null) {
                    for (PlayerEconomyData ped : data) {
                        playerData.put(ped.uuid, ped);
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void save() {
        try (Writer writer = Files.newBufferedWriter(dataPath)) {
            gson.toJson(playerData.values().toArray(new PlayerEconomyData[0]), writer);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public PlayerEconomyData getData(UUID uuid) {
        return playerData.computeIfAbsent(uuid, k -> {
            PlayerEconomyData data = new PlayerEconomyData(uuid);
            data.money = com.xolby.economy.EconomyMod.getEconomyConfig().getStartingMoney();
            return data;
        });
    }

    public double getMoney(UUID uuid) {
        return getData(uuid).money;
    }

    public void setMoney(UUID uuid, double amount) {
        getData(uuid).money = Math.max(0, amount);
        save();
    }

    public void addMoney(UUID uuid, double amount) {
        setMoney(uuid, getMoney(uuid) + amount);
    }

    public boolean removeMoney(UUID uuid, double amount) {
        double current = getMoney(uuid);
        if (current >= amount) {
            setMoney(uuid, current - amount);
            return true;
        }
        return false;
    }

    public double getSavings(UUID uuid) {
        return getData(uuid).savings;
    }

    public void setSavings(UUID uuid, double amount) {
        getData(uuid).savings = Math.max(0, amount);
        save();
    }

    public void addSavings(UUID uuid, double amount) {
        setSavings(uuid, getSavings(uuid) + amount);
    }

    public List<Map.Entry<UUID, Double>> getTopMoney(int limit) {
        return playerData.entrySet().stream()
            .sorted((a, b) -> Double.compare(b.getValue().money, a.getValue().money))
            .limit(limit)
            .map(e -> new AbstractMap.SimpleEntry<>(e.getKey(), e.getValue().money))
            .toList();
    }

    public static class PlayerEconomyData {
        public UUID uuid;
        public double money;
        public double savings;

        public PlayerEconomyData(UUID uuid) {
            this.uuid = uuid;
            this.money = 0;
            this.savings = 0;
        }
    }
}