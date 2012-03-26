package me.minercraftguy.webauctionaphelion.listeners;

import me.minercraftguy.webauctionaphelion.WebAuction;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.server.PluginEnableEvent;

public class WebAuctionServerListener implements Listener {
	private WebAuction plugin;

	public WebAuctionServerListener(WebAuction webAuction) {
		this.plugin = webAuction;
	}

	@EventHandler(priority= EventPriority.MONITOR)
        public void onPluginEnable(PluginEnableEvent event) {
		if (plugin.economy != null) {
			if (plugin.economy.isEnabled()) {
				//System.out.println("[" + plugin.getDescription().getName() + "] Payment method enabled: " + plugin.economy.getName() + ".");
			}
		}
		if (plugin.permission != null) {
			if (plugin.permission.isEnabled()) {
				//System.out.println("[" + plugin.getDescription().getName() + "] Permission method enabled: " + plugin.permission.getName() + ".");
			}
		}
	}
}