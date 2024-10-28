package hu.kxtsoo.fungun.manager;

import hu.kxtsoo.fungun.FunGun;
import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitTask;

import io.papermc.paper.threadedregions.scheduler.ScheduledTask;

public final class SchedulerManager {

    private static boolean isFolia;

    static {
        try {
            Class.forName("io.papermc.paper.threadedregions.RegionizedServer");
            isFolia = true;

        } catch (final ClassNotFoundException e) {
            isFolia = false;
        }
    }

    public static void run(Runnable runnable) {
        if (isFolia)
            Bukkit.getGlobalRegionScheduler().execute(FunGun.getInstance(), runnable);

        else
            Bukkit.getScheduler().runTask(FunGun.getInstance(), runnable);
    }

    public static void nrun(Runnable runnable) {
        if (isFolia)
            Bukkit.getAsyncScheduler().runNow(FunGun.getInstance(), task -> {
                Bukkit.getScheduler().runTask(FunGun.getInstance(), runnable);
            });
        else
            Bukkit.getScheduler().runTask(FunGun.getInstance(), runnable);
    }

    public static void runAsync(Runnable runnable) {
        if (isFolia)
            Bukkit.getGlobalRegionScheduler().execute(FunGun.getInstance(), runnable);
        else
            Bukkit.getScheduler().runTaskAsynchronously(FunGun.getInstance(), runnable);
    }

    public static Task runLater(Runnable runnable, long delayTicks) {
        if (isFolia)
            return new Task(Bukkit.getGlobalRegionScheduler()
                    .runDelayed(FunGun.getInstance(), t -> runnable.run(), delayTicks));

        else
            return new Task(Bukkit.getScheduler().runTaskLater(FunGun.getInstance(), runnable, delayTicks));
    }

    public static Task runAsyncLater(Runnable runnable, long delayTicks) {
        if (isFolia)
            return new Task(Bukkit.getGlobalRegionScheduler()
                    .runDelayed(FunGun.getInstance(), t -> runnable.run(), delayTicks));

        else
            return new Task(Bukkit.getScheduler().runTaskLaterAsynchronously(FunGun.getInstance(), runnable, delayTicks));
    }

    public static Task runTimer(Runnable runnable, long delayTicks, long periodTicks) {
        if (isFolia)
            return new Task(Bukkit.getGlobalRegionScheduler()
                    .runAtFixedRate(FunGun.getInstance(), t -> runnable.run(), delayTicks < 1 ? 1 : delayTicks, periodTicks));

        else
            return new Task(Bukkit.getScheduler().runTaskTimer(FunGun.getInstance(), runnable, delayTicks, periodTicks));
    }

    public static Task runAsyncTimer(Runnable runnable, long delayTicks, long periodTicks) {
        if (isFolia)
            return new Task(Bukkit.getGlobalRegionScheduler()
                    .runAtFixedRate(FunGun.getInstance(), t -> runnable.run(), delayTicks < 1 ? 1 : delayTicks, periodTicks));

        else
            return new Task(Bukkit.getScheduler().runTaskTimerAsynchronously(FunGun.getInstance(), runnable, delayTicks, periodTicks));
    }

    public static boolean isFolia() {
        return isFolia;
    }

    public static class Task {
        private ScheduledTask foliaTask;
        private BukkitTask bukkitTask;

        Task(ScheduledTask foliaTask) {
            this.foliaTask = foliaTask;
        }

        Task(BukkitTask bukkitTask) {
            this.bukkitTask = bukkitTask;
        }

        public void cancel() {
            if (foliaTask != null) {
                foliaTask.cancel();
            } else if (bukkitTask != null) {
                bukkitTask.cancel();
            }
        }
    }
}
