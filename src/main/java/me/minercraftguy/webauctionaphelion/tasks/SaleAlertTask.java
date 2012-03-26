package me.minercraftguy.webauctionaphelion.tasks;

import java.util.List;

import me.minercraftguy.webauctionaphelion.WebAuction;
import me.minercraftguy.webauctionaphelion.dao.SaleAlert;

import org.bukkit.entity.Player;

public class SaleAlertTask implements Runnable {

	private final WebAuction plugin;

	public SaleAlertTask(WebAuction plugin) {
		this.plugin = plugin;
	}

	@Override
	public void run() {
		Player[] playerList = plugin.getServer().getOnlinePlayers();
		for (Player player : playerList) {
			List<SaleAlert> newSaleAlerts = plugin.dataQueries.getNewSaleAlertsForSeller(player.getName());
			for (SaleAlert saleAlert : newSaleAlerts) {
				String formattedPrice = plugin.economy.format(saleAlert.getPriceEach());
				player.sendMessage(plugin.logPrefix + "You sold " + saleAlert.getQuantity() + " " + saleAlert.getItem() + " to " + saleAlert.getBuyer()
						+ " for " + formattedPrice + " each.");
				plugin.dataQueries.markSaleAlertSeen(saleAlert.getId());
			}
		}
	}
}
