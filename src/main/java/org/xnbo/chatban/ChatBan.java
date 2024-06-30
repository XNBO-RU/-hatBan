package org.xnbo.chatban;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.List;

public class ChatBan extends JavaPlugin implements Listener, CommandExecutor {
    private List<String> bannedWords;
    private String banCommand;
    private boolean additionalCommandEnabled;
    private String additionalCommand;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        loadConfigValues();
        getServer().getPluginManager().registerEvents(this, this);
        getCommand("chatbanreload").setExecutor(this);
    }

    private void loadConfigValues() {
        FileConfiguration config = getConfig();
        bannedWords = config.getStringList("banned_words");
        banCommand = config.getString("ban_command");
        additionalCommandEnabled = config.getBoolean("additional_command_enabled");
        additionalCommand = config.getString("additional_command");
        getLogger().info("Config values loaded");
        getLogger().info("Banned words: " + bannedWords);
        getLogger().info("Ban command: " + banCommand);
        getLogger().info("Additional command enabled: " + additionalCommandEnabled);
        getLogger().info("Additional command: " + additionalCommand);
    }

    @Override
    public void onDisable() {
        // Any necessary cleanup here
    }

    @EventHandler
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        String message = event.getMessage();
        String playerName = event.getPlayer().getName();

        for (String word : bannedWords) {
            if (message.toLowerCase().contains(word.toLowerCase())) { // Игнорирование регистра
                event.setCancelled(true);
                getLogger().info("Detected banned word in message: " + message);

                Bukkit.getScheduler().runTask(this, () -> {
                    String banCmd = banCommand.replace("{player}", playerName);
                    getLogger().info("Executing ban command: " + banCmd);
                    boolean banSuccess = Bukkit.dispatchCommand(Bukkit.getConsoleSender(), banCmd);
                    getLogger().info("Ban command executed: " + banSuccess);

                    if (additionalCommandEnabled) {
                        String addMessage = additionalCommand.replace("{player}", playerName);
                        getLogger().info("Broadcasting additional message: " + addMessage);
                        Bukkit.broadcastMessage(addMessage);
                    }
                });

                break;
            }
        }
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (command.getName().equalsIgnoreCase("chatbanreload")) {
            reloadConfig();
            loadConfigValues();
            sender.sendMessage("ChatBan configuration reloaded.");
            getLogger().info("ChatBan configuration reloaded by " + sender.getName());
            return true;
        }
        return false;
    }
}