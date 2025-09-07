package org.com.mainpl;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;

public class MorteListener implements Listener {

    private final MortesManager mortesManager;

    public MorteListener(MortesManager mortesManager) {
        this.mortesManager = mortesManager;
    }

    @EventHandler
    public void aoMorrer(PlayerDeathEvent e) {
        e.setDeathMessage(null);
        mortesManager.salvarMorte(e.getEntity(), e.getEntity().getLocation());
    }

}
