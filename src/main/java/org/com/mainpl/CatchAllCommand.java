package org.com.mainpl;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

public class CatchAllCommand extends Command {

    public CatchAllCommand() {
        super("catchall"); // nome interno
        this.setDescription("Captura comandos inválidos");
    }

    @Override
    public boolean execute(CommandSender sender, String label, String[] args) {
        sender.sendMessage("§cComando inválido! Digite /ajuda para ver os comandos.");
        return true;
    }
}
