package f2b4.Death_Swap;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class SwapCommand implements CommandExecutor {

    private final F2B4_DeathSwap plugin;

    public SwapCommand(F2B4_DeathSwap plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("f2b4.admin")) {
            sender.sendMessage("§cนายไม่มีสิทธิ์ใช้คำสั่งนี้นะ!");
            return true;
        }

        plugin.getSwapTask().forceStart(61);
        
        sender.sendMessage("§7[System] เริ่มกิจกรรม Death Swap เรียบร้อย (โหมดเนียน)");
        return true;
    }
}