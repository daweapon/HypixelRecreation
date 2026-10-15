package dev.daweapon.anticheat.loader.managers;

// Platform-agnostic scheduler abstraction for delayed/repeating tasks used by the anticheat engine.
public abstract class AnticheatSchedulerManager {
    public abstract int scheduleDelayedTask(Runnable runnable, int delay);
    public abstract int scheduleRepeatingTask(Runnable runnable, int delay, int period);
    public abstract void cancelTask(int taskId);
    public abstract void cancelAllTasks();
}
