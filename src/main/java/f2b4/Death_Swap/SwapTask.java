package f2b4.Death_Swap;

import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.DayOfWeek;
import java.time.LocalDateTime;

public class SwapTask extends BukkitRunnable {

    private final F2B4_DeathSwap plugin;
    private final SwapManager manager;
    private int secondsRemaining = 300;
    private boolean isForced = false;
    private boolean wasActive = false;

    public SwapTask(F2B4_DeathSwap plugin, SwapManager manager) {
        this.plugin = plugin;
        this.manager = manager;
    }

    public void forceStart(int seconds) {
        this.secondsRemaining = seconds;
        this.isForced = true;
    }

    @Override
    public void run() {
        LocalDateTime now = LocalDateTime.now();
        DayOfWeek day = now.getDayOfWeek();
        int hour = now.getHour();
        int minute = now.getMinute();

        boolean isWednesday = (day == DayOfWeek.WEDNESDAY);
        boolean isSunday = (day == DayOfWeek.SUNDAY);
        boolean isSlot1 = (hour == 15);
        boolean isSlot2 = (hour == 19);

        boolean isActiveTime = (isWednesday || isSunday) && (isSlot1 || isSlot2);

        
        if ((isActiveTime || isForced) && !wasActive) {
            sendDiscordWebhook(true);
            wasActive = true;
        } else if (!isActiveTime && !isForced && wasActive) {
            sendDiscordWebhook(false);
            wasActive = false;
        }


        if (!isActiveTime && !isForced && secondsRemaining < 300) {
            Bukkit.broadcastMessage("§c[F2B4 Event] ⏳ หมดเวลากิจกรรมแล้ว! ยกเลิกการวาร์ปสลับตำแหน่งรอบนี้");
            secondsRemaining = 300;
            return;
        }


        if (isActiveTime && !isForced && secondsRemaining == 300) {
            int secondsLeftInHour = ((59 - minute) * 60) + (60 - now.getSecond());
            if (secondsLeftInHour < 300) {
                return; 
            }
        }

        if (isActiveTime || isForced) {
            secondsRemaining--;

            if (secondsRemaining == 60) {
                broadcastEventInfo("§6§l[F2B4 Event] §eกิจกรรม §cDeath Swap §eเริ่มขึ้นแล้ว!");
                broadcastWithSound("§7> §fจะสลับตำแหน่งในอีก §c1 นาที §fเตรียมตัวให้พร้อม!", Sound.BLOCK_NOTE_BLOCK_PLING);
                broadcastWithSound("§c⚠ คำแนะนำ: §7อย่าอยู่แต่ในบ้าน! ออกไปที่เสี่ยงๆ เพื่อให้คนอื่นมาตายแทนคุณ", null);
            } else if (secondsRemaining == 40 || secondsRemaining == 30) {
                broadcastWithSound("§6§l[F2B4 Event] §eจะสลับตำแหน่งในอีก §c" + secondsRemaining + " §eวินาที!", Sound.BLOCK_NOTE_BLOCK_PLING);
            } else if (secondsRemaining <= 20 && secondsRemaining > 0) {
                broadcastWithSound("§6§l[F2B4 Event] §eสลับตำแหน่งในอีก §c" + secondsRemaining + " §eวินาที!", Sound.BLOCK_NOTE_BLOCK_BIT);
            }

            if (secondsRemaining <= 0) {
                manager.performSwap();
                secondsRemaining = 300;
                isForced = false;
            }
        } else {
            if (secondsRemaining != 300) secondsRemaining = 300;
        }
    }

    private void broadcastWithSound(String msg, Sound sound) {
        Bukkit.broadcastMessage(msg);
        if (sound != null) {
            Bukkit.getOnlinePlayers().forEach(p -> p.playSound(p.getLocation(), sound, 1f, 1f));
        }
    }

    private void broadcastEventInfo(String header) {
        Bukkit.broadcastMessage("");
        Bukkit.broadcastMessage(header);
        String url = plugin.getConfig().getString("event-info-url", "https://f2b4smp.com");
        TextComponent link = new TextComponent("§b§n[คลิกที่นี่เพื่อดูวิธีเล่นและรายละเอียดกิจกรรม]");
        link.setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, url));
        link.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder("§7กดเพื่อเปิดเว็บไซต์").create()));
        for (Player p : Bukkit.getOnlinePlayers()) { p.spigot().sendMessage(link); }
        Bukkit.broadcastMessage("");
    }

    private void sendDiscordWebhook(boolean isStart) {
        if (!plugin.getConfig().getBoolean("discord-webhook.enabled", false)) return;
        String webhookUrl = plugin.getConfig().getString("discord-webhook.url", "");
        if (webhookUrl.isEmpty() || webhookUrl.contains("YOUR_WEBHOOK")) return;

        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            try {
                String title = isStart ? plugin.getConfig().getString("discord-webhook.start-title", "💀 กิจกรรม Death Swap เริ่มขึ้นแล้ว!") : plugin.getConfig().getString("discord-webhook.stop-title", "✅ จบกิจกรรม Death Swap ประจำรอบนี้");
                String color = isStart ? "16717591" : "65280";
                String desc = isStart ? "ระบบเริ่มทำการสุ่มสลับตำแหน่งผู้เล่นทุกๆ **5 นาที** แล้ว!\\n**คำเตือน:** ไม่มีสถานะอมตะหลังวาร์ป" : "ระบบหยุดทำการสลับตัวแล้ว ขอบคุณทุกคนที่ร่วมสนุก!";
                String eventUrl = plugin.getConfig().getString("event-info-url", "https://f2b4.xyz");
                String avatar = plugin.getConfig().getString("discord-webhook.avatar-url", "https://mc-heads.net/avatar/MHF_Skeleton");
                
                String json = "{\"username\":\"F2B4 Event System\",\"avatar_url\":\"" + avatar + "\",\"embeds\":[{"
                        + "\"title\":\"" + title + "\","
                        + "\"description\":\"" + desc + "\\n\\n[ดูรายละเอียดกิจกรรม](" + eventUrl + ")\","
                        + "\"color\":" + color + ","
                        + "\"footer\":{\"text\":\"F2B4 SMP Automatic Schedule\"}}]}";

                URL url = new URL(webhookUrl);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
                conn.setRequestProperty("User-Agent", "F2B4-DeathSwap-Plugin");
                conn.setDoOutput(true);
                try (OutputStream os = conn.getOutputStream()) { 
                    byte[] input = json.getBytes("utf-8");
                    os.write(input, 0, input.length); 
                }
                conn.getResponseCode();
            } catch (Exception ignored) {}
        });
    }
}