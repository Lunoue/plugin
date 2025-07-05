package com.anya.spawncooldown;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginCommand;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;
import java.util.UUID;

public class SpawnCooldownPlugin extends JavaPlugin implements Listener, CommandExecutor {

    private final HashMap<UUID, Long> damageTimestamps = new HashMap<>();
    private final HashMap<UUID, Long> cooldowns = new HashMap<>();
    private static final long DAMAGE_DELAY = 15 * 1000;
    private static final long SPAWN_COOLDOWN = 1800 * 1000;

    @Override
    public void onEnable() {
        Bukkit.getPluginManager().registerEvents(this, this);
        PluginCommand spawnCommand = getCommand("spawn");
        if (spawnCommand != null) {
            spawnCommand.setExecutor(this);
        }
        getLogger().info("SpawnCooldownPlugin enabled.");
    }

    @EventHandler
    public void onDamage(EntityDamageEvent event) {
        if (event.getEntity() instanceof Player player) {
            damageTimestamps.put(player.getUniqueId(), System.currentTimeMillis());
        }
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Only players can use this command.");
            return true;
        }

        long now = System.currentTimeMillis();
        UUID uuid = player.getUniqueId();

        if (damageTimestamps.containsKey(uuid)) {
            long lastDamage = damageTimestamps.get(uuid);
            if (now - lastDamage < DAMAGE_DELAY) {
                long wait = (DAMAGE_DELAY - (now - lastDamage)) / 1000;
                player.sendMessage(ChatColor.RED + "Ты недавно получил урон. Подожди ещё " + wait + " секунд перед использованием /spawn.");
                return true;
            }
        }

        if (cooldowns.containsKey(uuid)) {
            long lastUsed = cooldowns.get(uuid);
            if (now - lastUsed < SPAWN_COOLDOWN) {
                long wait = (SPAWN_COOLDOWN - (now - lastUsed)) / 1000;
                player.sendMessage(ChatColor.RED + "Ты можешь использовать /spawn только раз в 30 минут. Осталось: " + wait + " сек.");
                return true;
            }
        }

        cooldowns.put(uuid, now);
        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "minecraft:tp " + player.getName() + " world 0 100 0");
        return true;
    }
}