package f2b4.Death_Swap;

import org.bukkit.plugin.java.JavaPlugin;

public class F2B4_DeathSwap extends JavaPlugin {

    private SwapManager swapManager;
    private SwapTask swapTask;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        
        this.swapManager = new SwapManager(this); 
        this.swapTask = new SwapTask(this, swapManager);
        
        swapTask.runTaskTimer(this, 0L, 20L);
        
        getCommand("dstest").setExecutor(new SwapCommand(this));
        
        getLogger().info("F2B4 Death Swap has been enabled!");
    }

    public SwapTask getSwapTask() {
        return swapTask;
    }
}