package com.gmail.llmdlio.MonitorDeniedJoins;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.util.Date;
import java.text.SimpleDateFormat;

import org.bukkit.BanList;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.entity.Player;


public class MonitorDeniedJoins extends JavaPlugin implements Listener {

	private static String pluginPrefix;
	private static String whitelistmsg;
	private static String kickothermsg;
	private static String banmsg;
	
    @Override
    public void onEnable() {   	
    	getServer().getPluginManager().registerEvents(this, this);
    	getLogger().info("MonitorDeniedJoins " + this.getDescription().getVersion() + " by LlmDl Enabled.");
    	
    	this.saveDefaultConfig();
    	pluginPrefix = this.getConfig().getString("prefix");
    	whitelistmsg = this.getConfig().getString("whitelistmsg");
    	kickothermsg = this.getConfig().getString("kickothermessage");
    	banmsg = this.getConfig().getString("banmsg");
    	
    	pluginPrefix= ChatColor.translateAlternateColorCodes('&', pluginPrefix );
    }
 
    @Override
    public void onDisable() {
    	getLogger().info("MonitorDeniedJoins Disabled.");
    }
    
    
       
    @EventHandler(priority = EventPriority.MONITOR) 
    public void onPlayerLogin(PlayerLoginEvent event) {
    	Player player = event.getPlayer();
  	
        if(event.getResult() == PlayerLoginEvent.Result.KICK_BANNED) {
        	if (!Bukkit.getBannedPlayers().contains(event.getPlayer())){        		
        		String message = player.getName().toString() + " attempted to login from a banned IP address. Alt Account Banned.";
        		BanList bl = Bukkit.getBanList(BanList.Type.NAME);
        		bl.addBan(
        				event.getPlayer().getName(),
        				"Attempted login from Banned IP", 
        				null, 
        				"MonitorDeniedJoins Alt Account Auto-Ban.").save();
        		String reason = bl.getBanEntry(event.getPlayer().getName()).getReason();
        		event.setKickMessage(banmsg + reason);
        		
        		for (Player ops : Bukkit.getOnlinePlayers()) {      	 
            		if (ops.isOp() || ops.hasPermission("monitordeniedjoins.announce"))
            			ops.sendMessage(pluginPrefix + message);        		
            	}
            	logToFile(message);        		
        	} else {        		
	        	String message = player.getName().toString() + " (banned) attempted to login.";
	        	BanList bl = Bukkit.getBanList(BanList.Type.NAME);
	        	String reason = bl.getBanEntry(event.getPlayer().getName()).getReason();
	    		event.setKickMessage(banmsg + reason);
	        	for (Player ops : Bukkit.getOnlinePlayers()) {      	 
	        		if (ops.isOp() || ops.hasPermission("monitordeniedjoins.announce"))
	        			ops.sendMessage(pluginPrefix + message);        		
	        	}
	        	logToFile(message);
        	}        
    	}
        
        if(event.getResult() == PlayerLoginEvent.Result.KICK_WHITELIST) {        	
        	String message = player.getName().toString() + " (not whitelisted) attempted to login.";
        	event.setKickMessage(whitelistmsg);
        	for (Player ops : Bukkit.getOnlinePlayers()) {      	 
        		if (ops.isOp() || ops.hasPermission("monitordeniedjoins.announce"))
        			ops.sendMessage(pluginPrefix + message);
        	}
        	logToFile(message);
        }
        
        if(event.getResult() == PlayerLoginEvent.Result.KICK_OTHER)
        	event.setKickMessage(kickothermsg);
    }
     
    public void logToFile (String message) {
    	Date now = new Date();
    	    	
    	SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    	try {
    		File dataFolder = getDataFolder();
        	if (!dataFolder.exists())
        		dataFolder.mkdir();
        	
        	File saveTo = new File(getDataFolder(), "log.txt");
        	if (!saveTo.exists())
        		saveTo.createNewFile();
    		
            FileWriter fw = new FileWriter(saveTo, true);
            PrintWriter pw = new PrintWriter(fw);
            pw.println("[" + format.format(now) + "] " + message);
            pw.flush();
            pw.close();
        	
    	} catch (IOException e) {
            e.printStackTrace();
        }
    }
}
