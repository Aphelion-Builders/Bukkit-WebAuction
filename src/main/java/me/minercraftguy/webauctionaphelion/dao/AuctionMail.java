package me.minercraftguy.webauctionaphelion.dao;

import org.bukkit.inventory.ItemStack;

public class AuctionMail {

	private int id;
	private ItemStack itemStack;
	private String playerName;

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public ItemStack getItemStack() {
		return itemStack;
	}

	public void setItemStack(ItemStack itemStack) {
		this.itemStack = itemStack;
	}

	public String getPlayerName() {
		return playerName;
	}

	public void setPlayerName(String playerName) {
		this.playerName = playerName;
	}
}
