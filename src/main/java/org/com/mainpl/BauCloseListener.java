package org.com.mainpl;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.entity.Player;

public class BauCloseListener implements Listener {

    private final SbauCommand sbauCommand;

    public BauCloseListener(SbauCommand sbauCommand) {
        this.sbauCommand = sbauCommand;
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (!(event.getPlayer() instanceof Player player)) return;

        sbauCommand.onCloseInventory(player, event.getView());
    }


}
