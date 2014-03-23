package io.github.itachi1706.SpeedChallenge;

import io.github.itachi1706.SpeedChallenge.Utilities.ScoreboardHelper;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collection;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;

import com.onarandombox.MultiverseCore.MultiverseCore;
import com.onarandombox.MultiverseCore.api.MultiverseWorld;

import ru.tehkode.permissions.PermissionManager;
import ru.tehkode.permissions.PermissionUser;
import ru.tehkode.permissions.bukkit.PermissionsEx;

public class Main extends JavaPlugin implements Listener{
	
	public static int players = 0;	//Number of players
	public static boolean gameStart = false;	//Whether game started alr
	public static boolean initGame = false;		//Whether game initialization is done
	public static int gamemode = 0;	//What type of speed challenge to do (0 = random, 1,2... = specific gamemodes)
	public static int pvp = 0;	//PVP enabled or not (0 is random, 1 is on, 2 is off)
	public static int respawn = 0; //Whether or not to respawn players if dead (0 random, 1 yes, 2 no)
	public static int countdown = 90;	//Countdown timer
	public static ArrayList<Player> playerList = new ArrayList<Player>();			//Players
	public static ArrayList<Player> spectators = new ArrayList<Player>();		//Spectators
	
	public static int countDownTimer = 1;
	public static int countDownTimer2 = 2;
	public static int countDownTimer3 = 3;
	
	@Override
	public void onEnable(){
		getLogger().info("Enabling Plugin...");
		getServer().getPluginManager().registerEvents(this, this);
		this.saveDefaultConfig(); 
		getCommand("scconfig").setExecutor(new ConfigCmd(this));
		getCommand("spectate").setExecutor(new SpecCmd(this));
		Bukkit.getServer().getPluginManager().registerEvents(new PreGameListener(), this);
		
	}
	
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args){
		//Main Command
    	if(cmd.getName().equalsIgnoreCase("sc")){
    		if (args.length < 1 || args.length > 1){
    			displayHelp(sender);
				return true;
    		}
    		if (args[0].equalsIgnoreCase("reset")){
    			//Reloads Plugin
    			sender.sendMessage("Resetting game...");
    			resetGame();
    			return true;
    		} else {
    			//Error
    			displayHelp(sender);
    			return true;
    		}
		}
    	return false;
	}
	
	public void displayHelp(CommandSender s){
		s.sendMessage(ChatColor.GOLD + "-----------CheesecakeMinigameLobby Commands-----------");
    	s.sendMessage(ChatColor.GOLD + "/sc reset: " + ChatColor.WHITE +  "Check current plugin version");
	}
	
	@Override
	public void onDisable(){
		//Logic when plugin gets disabled
		getLogger().info("Disabling Plugin...");
	}
	
	public void startCountDown(){
		String finalCountDown2 = "&b[SpeedChallenge] &6&lGame is starting in 1 and a half minute!";
		Bukkit.getServer().broadcastMessage(ChatColor.translateAlternateColorCodes('&', finalCountDown2));
		countDownTimer = Bukkit.getScheduler().scheduleSyncRepeatingTask(this, new InitGame(this), 20L, 20L);
		MultiverseCore mc = (MultiverseCore) Bukkit.getServer().getPluginManager().getPlugin("Multiverse-Core");
		Collection<MultiverseWorld> collate = mc.getMVWorldManager().getMVWorlds();
		ArrayList<MultiverseWorld> obj = new ArrayList<MultiverseWorld>(collate);
		for (int i = 0; i < obj.size(); i++){
			MultiverseWorld world = obj.get(i);
			if (world.getName().equals("SC")){
				mc.getMVWorldManager().deleteWorld("SC");
			}
			if (world.getName().equals("SC_nether")){
				mc.getMVWorldManager().deleteWorld("SC_nether");
			}
			if (world.getName().equals("SC_the_end")){
				mc.getMVWorldManager().deleteWorld("SC_the_end");
			}
		}
	}
	
	public void resetGame(){
		Player[] p = Bukkit.getServer().getOnlinePlayers();
		World w = Bukkit.getWorld("world");
		Location l = new Location(Bukkit.getServer().getWorld("world"), Bukkit.getServer().getWorld("world").getSpawnLocation().getX(), Bukkit.getServer().getWorld("world").getSpawnLocation().getY(), Bukkit.getServer().getWorld("world").getSpawnLocation().getZ());
		for (int i = 0; i < p.length; i++){
			if (!p[i].getWorld().equals(w)){
				p[i].teleport(l);
				p[i].sendMessage("You were teleported back to the main world!");
			}
		}
		MultiverseCore mc = (MultiverseCore) Bukkit.getServer().getPluginManager().getPlugin("Multiverse-Core");
		Collection<MultiverseWorld> collate = mc.getMVWorldManager().getMVWorlds();
		ArrayList<MultiverseWorld> obj = new ArrayList<MultiverseWorld>(collate);
		for (int i = 0; i < obj.size(); i++){
			MultiverseWorld world = obj.get(i);
			if (world.getName().equals("SC")){
				mc.getMVWorldManager().deleteWorld("SC");
			}
			if (world.getName().equals("SC_nether")){
				mc.getMVWorldManager().deleteWorld("SC_nether");
			}
			if (world.getName().equals("SC_the_end")){
				mc.getMVWorldManager().deleteWorld("SC_the_end");
			}
		}
		if (initGame == false){
			getLogger().info("Resetting countdown timer!");
			Bukkit.getServer().getScheduler().cancelTask(countDownTimer);
		}
		if (gameStart == false && initGame == true){
			getLogger().info("Resetting countdown timer 2!");
			Bukkit.getServer().getScheduler().cancelTask(countDownTimer2);
		}
		if (gameStart == true){
			getLogger().info("Resetting countdown timer 3!");
			Bukkit.getServer().getScheduler().cancelTask(countDownTimer3);
		}
		
		players = 0;
		gameStart = false;
		countdown = 90;
		initGame = false;
		gamemode = 0;
		pvp = 0;
		respawn = 0;
		playerList.clear();
		spectators.clear();
		
		Bukkit.getServer().broadcastMessage("WORLDS DELETED!");
		ScoreboardHelper.resetScoreboard();
		for (int i = 0; i < p.length; i++){
			p[i].kickPlayer("Game is resetting");
		}
	}
	
	@SuppressWarnings("unused")
	private void copy(InputStream in, File file){
		try{
			OutputStream out = new FileOutputStream(file);
			byte[] buf = new byte[1024];
			int len;
			while ((len=in.read(buf))>0){
				out.write(buf,0,len);
			}
			out.close();
			in.close();
		} catch (Exception e){
			e.printStackTrace();
		}
	}
	
	@EventHandler
	public void onJoin(PlayerJoinEvent e){
		String prefix = "";
		if (Bukkit.getServer().getPluginManager().getPlugin("PermissionsEx") != null) {
			PermissionManager pex = PermissionsEx.getPermissionManager();
			PermissionUser target = pex.getUser(e.getPlayer());
			String result = target.getPrefix();
			prefix = ChatColor.translateAlternateColorCodes('&', result);
		}
		World w = Bukkit.getWorld("world");
		Location l = new Location(Bukkit.getServer().getWorld("world"), Bukkit.getServer().getWorld("world").getSpawnLocation().getX(), Bukkit.getServer().getWorld("world").getSpawnLocation().getY(), Bukkit.getServer().getWorld("world").getSpawnLocation().getZ());
		if (!e.getPlayer().getWorld().equals(w)){
			e.getPlayer().teleport(l);
			e.getPlayer().sendMessage("You were teleported back to the main world!");
		}
		Bukkit.getServer().broadcastMessage(prefix + " " + e.getPlayer().getDisplayName() + " joined the game!");
		if (!initGame){
			String[] welMsg = {ChatColor.GOLD + "==================================================" ,
					ChatColor.DARK_GREEN + "Select a gamemode with " + ChatColor.GOLD + "/scconfig gamemode <number>",
					ChatColor.DARK_GREEN + "Then change the other configuration (respawn, pvp, spreadplayers) with" + ChatColor.GOLD + " /scconfig <config> <true/false>",
					ChatColor.DARK_GREEN + "Do " + ChatColor.GOLD + "/scconfig" + ChatColor.DARK_GREEN + " for more details",
					ChatColor.GOLD + "==================================================" ,};
			e.getPlayer().sendMessage(welMsg);
			e.getPlayer().setScoreboard(ScoreboardHelper.sb);
			players++;
			playerList.add(e.getPlayer());
			getLogger().info(players + " player(s)");
			if (players == 1){
				ScoreboardHelper.initPlayersCounter();
			}
			if (countdown == 90){
				startCountDown();
			}
			ScoreboardHelper.updatePlayers();
		} else {
			String[] welMsg = {ChatColor.GOLD + "==================================================" ,
					ChatColor.DARK_GREEN + "Unfortunately, the game has already started. You can spectate the match if you want with" +
					ChatColor.GOLD + "/spectate",
					ChatColor.GOLD + "==================================================" ,};
			e.getPlayer().sendMessage(welMsg);
		}
	}
	
	@EventHandler
	public void onQuit(PlayerQuitEvent e){
		String prefix = "";
		if (Bukkit.getServer().getPluginManager().getPlugin("PermissionsEx") != null) {
			PermissionManager pex = PermissionsEx.getPermissionManager();
			PermissionUser target = pex.getUser(e.getPlayer());
			String result = target.getPrefix();
			prefix = ChatColor.translateAlternateColorCodes('&', result);
		}
		Bukkit.getServer().broadcastMessage(prefix + " " + e.getPlayer().getDisplayName() + " left the game!");
		for (int i = 0; i < playerList.size(); i++){
			Player pla = playerList.get(i);
			if (pla.equals(e.getPlayer())){
				playerList.remove(i);
				players--;
				break;
			}
		}
		getLogger().info(players + " player(s)");
		if (players <= 0){
			getLogger().info("Last Player Left. Resetting game...");
			resetGame();
		} else {
			ScoreboardHelper.updatePlayers();
		}
	}

}
