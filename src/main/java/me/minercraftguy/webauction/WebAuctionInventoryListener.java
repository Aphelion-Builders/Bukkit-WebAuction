package me.minercraftguy.webauction;

import java.sql.ResultSet;

import org.bukkit.Location;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.spout.api.event.Listener;
import org.spout.api.event.inventory.PlayerInventoryCloseEvent;
import org.spout.api.event.inventory.PlayerInventoryEvent;
import org.spout.api.event.inventory.PlayerInventoryOpenEvent;

public class WebAuctionInventoryListener implements Listener {

    private final WebAuction plugin;

    public WebAuctionInventoryListener(final WebAuction plugin) {
        this.plugin = plugin;
    }

    public void onInventoryOpen(PlayerInventoryOpenEvent event) {
    }

    public void onInventoryClose(PlayerInventoryCloseEvent event) {
        plugin.manageMySQL.initialize();
        if (event.getPlayer() != null) {

            Location closedInv = event.getLocation();
            if (WebAuction.chestUse.containsKey(closedInv)) {
                event.getPlayer().sendMessage(WebAuction.logPrefix + "Deposit chest closed, Items deposited.");
                Inventory inv = event.getInventory();
                ItemStack[] stack = inv.getContents();
                for (int i = 0; i < stack.length; i++) {
                    try {
                        if (stack[i] != null) {
                            String playerName = event.getPlayer().getName();
                            String itemName = Integer.toString(stack[i].getTypeId());
                            String itemDamage = "";
                            if (stack[i].getDurability() >= 0) {
                                itemDamage = Integer.toString(stack[i].getDurability());
                            } else {
                                itemDamage = "0";
                            }
                            int quantityInt = stack[i].getAmount();

                            String querySelect = "SELECT * FROM WA_Items WHERE player='" + playerName + "' AND name='" + itemName + "' AND damage='" + itemDamage + "';";
                            ResultSet result = null;
                            result = WebAuction.manageMySQL.sqlQuery(querySelect);
                            if ((result != null) && (result.next())) {
                                int currentQuantity = result.getInt("quantity");
                                currentQuantity += quantityInt;
                                String queryUpdate = "UPDATE WA_Items SET quantity='" + currentQuantity + "' WHERE name='" + itemName + "' AND player='" + playerName + "' AND damage='" + itemDamage + "';";
                                //event.getPlayer().sendMessage(queryUpdate);
                                WebAuction.manageMySQL.updateQuery(queryUpdate);
                            } else {
                                String queryInsert = "INSERT INTO WA_Items (name, damage, player, quantity) VALUES ('" + itemName + "', '" + itemDamage + "', '" + playerName + "', '" + quantityInt + "');";
                                //event.getPlayer().sendMessage(queryInsert);
                                WebAuction.manageMySQL.insertQuery(queryInsert);
                            }
                        }
                    } catch (Exception e) {
                        // TODO Auto-generated catch block
                    }
                }
                inv.clear();
                WebAuction.chestUse.remove(event.getLocation());

            }
        }
        plugin.manageMySQL.close();
    }
}
