package me.minercraftguy.webauctionaphelion;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import me.minercraftguy.webauctionaphelion.listeners.WebAuctionBlockListener;
import me.minercraftguy.webauctionaphelion.listeners.WebAuctionPlayerListener;
import me.minercraftguy.webauctionaphelion.listeners.WebAuctionServerListener;
import me.minercraftguy.webauctionaphelion.tasks.SaleAlertTask;
import me.minercraftguy.webauctionaphelion.tasks.ShoutSignTask;

import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.permission.Permission;

import org.bukkit.Location;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

public class WebAuction extends JavaPlugin implements Listener{

	public String logPrefix = "[WebAuction] ";
	public static final Logger log = Logger.getLogger("Minecraft");

	private final WebAuctionPlayerListener playerListener = new WebAuctionPlayerListener(this);
	private final WebAuctionBlockListener blockListener = new WebAuctionBlockListener(this);
	private final WebAuctionServerListener serverListener = new WebAuctionServerListener(this);

	public MySQLDataQueries dataQueries;

	public Map<String, Long> lastSignUse = new HashMap<String , Long>();
	public Map<Location, Integer> recentSigns = new HashMap<Location, Integer>();
	public Map<Location, Integer> shoutSigns = new HashMap<Location, Integer>();

	public int signDelay = 0;
	public int numberOfRecentLink = 0;
	
	public Boolean useSignLink = false;
	public Boolean useOriginalRecent = true;
	
	public Boolean showSalesOnJoin = false;

	public Permission permission = null;
	public Economy economy = null;

	public long getCurrentMilli() {
		return System.currentTimeMillis();
	}

	@Override
	public void onEnable() {

		log.info(logPrefix + "WebAuction is initializing.");

		initConfig();
		String dbHost = getConfig().getString("MySQL.Host");
		String dbUser = getConfig().getString("MySQL.Username");
		String dbPass = getConfig().getString("MySQL.Password");
		String dbPort = getConfig().getString("MySQL.Port");
		String dbDatabase = getConfig().getString("MySQL.Database");
		long saleAlertFrequency = getConfig().getLong("Updates.SaleAlertFrequency");
		long shoutSignUpdateFrequency = getConfig().getLong("Updates.ShoutSignUpdateFrequency");
		long recentSignUpdateFrequency = getConfig().getLong("Updates.RecentSignUpdateFrequency");
		boolean getMessages = getConfig().getBoolean("Misc.ReportSales");
		useOriginalRecent = getConfig().getBoolean("Misc.UseOriginalRecentSigns");
		showSalesOnJoin = getConfig().getBoolean("Misc.ShowSalesOnJoin");
		boolean useMultithreads = getConfig().getBoolean("Development.UseMultithreads");
		signDelay = getConfig().getInt("Misc.SignDelay");
		useSignLink = getConfig().getBoolean("SignLink.UseSignLink");
		numberOfRecentLink = getConfig().getInt("SignLink.NumberOfLatestAuctionsToTrack");

		getCommand("wa").setExecutor(new WebAuctionCommands(this));

		setupEconomy();
		setupPermissions();

		// Set up DataQueries
		dataQueries = new MySQLDataQueries(this, dbHost, dbPort, dbUser, dbPass, dbDatabase);

		// Init tables
		log.info(logPrefix + "MySQL Initializing.");
		dataQueries.initTables();

		// Build shoutSigns map
		shoutSigns.putAll(dataQueries.getShoutSignLocations());

		// Build recentSigns map
		recentSigns.putAll(dataQueries.getRecentSignLocations());

		// If reporting sales in game, schedule sales alert task
		if (useMultithreads){
			log.log(Level.INFO, "{0} Using Multiple Threads.", logPrefix);
			if (getMessages) {
				getServer().getScheduler().scheduleAsyncRepeatingTask(this, new SaleAlertTask(this), saleAlertFrequency, saleAlertFrequency);
			}
			getServer().getScheduler().scheduleAsyncRepeatingTask(this, new ShoutSignTask(this), shoutSignUpdateFrequency, shoutSignUpdateFrequency);
		}else{
			log.log(Level.INFO, "{0} Using Single Thread.", logPrefix);
			if (getMessages) {
				getServer().getScheduler().scheduleSyncRepeatingTask(this, new SaleAlertTask(this), saleAlertFrequency, saleAlertFrequency);
			}
			getServer().getScheduler().scheduleSyncRepeatingTask(this, new ShoutSignTask(this), shoutSignUpdateFrequency, shoutSignUpdateFrequency);
		}

		PluginManager pm = getServer().getPluginManager();
                pm.registerEvents(this.playerListener, this);
                pm.registerEvents(this.blockListener, this);
                pm.registerEvents(this.serverListener, this);
	}

	@Override
	public void onDisable() {
		getServer().getScheduler().cancelTasks(this);
		log.log(Level.INFO, "{0} Disabled. Bye :D", logPrefix);
	}

	private void initConfig() {
		getConfig().addDefault("MySQL.Host", "localhost");
		getConfig().addDefault("MySQL.Username", "root");
		getConfig().addDefault("MySQL.Password", "password123");
		getConfig().addDefault("MySQL.Port", "3306");
		getConfig().addDefault("MySQL.Database", "minecraft");
		getConfig().addDefault("Misc.ReportSales", false);
		getConfig().addDefault("Misc.UseOriginalRecentSigns", false);
		getConfig().addDefault("Misc.ShowSalesOnJoin", false);
		getConfig().addDefault("Development.UseMultithreads", false);
		getConfig().addDefault("Misc.SignDelay", 1000);
		getConfig().addDefault("SignLink.UseSignLink", false);
		getConfig().addDefault("SignLink.NumberOfLatestAuctionsToTrack", 10);
		getConfig().addDefault("Updates.SaleAlertFrequency", 30L);
		getConfig().addDefault("Updates.ShoutSignUpdateFrequency", 90L);
		getConfig().addDefault("Updates.RecentSignUpdateFrequency", 160L);
		getConfig().options().copyDefaults(true);
		saveConfig();
	}

	private Boolean setupPermissions() {
		RegisteredServiceProvider<Permission> permissionProvider = getServer().getServicesManager().getRegistration(Permission.class);
		if (permissionProvider != null) {
			permission = permissionProvider.getProvider();
		}
		return (permission != null);
	}

	private Boolean setupEconomy() {
		RegisteredServiceProvider<Economy> economyProvider = getServer().getServicesManager().getRegistration(Economy.class);
		if (economyProvider != null) {
			economy = economyProvider.getProvider();
		}
		return (economy != null);
	}
}
