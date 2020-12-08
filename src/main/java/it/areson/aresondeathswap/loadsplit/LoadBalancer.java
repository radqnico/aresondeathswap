package it.areson.aresondeathswap.loadsplit;

import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayDeque;
import java.util.concurrent.Semaphore;

public class LoadBalancer extends BukkitRunnable {

    private static final int MAX_MILLIS_PER_TICK = 5;
    public static long LAST_TICK_START_TIME = 0;

    private String id;
    private final ArrayDeque<Job> jobs;
    private final Semaphore mutex;

    public LoadBalancer(String id) {
        this.id = id;
        this.mutex = new Semaphore(1);
        jobs = new ArrayDeque<>();
    }

    public synchronized void addJob(Job job) {
        try {
            mutex.acquire();
            jobs.add(job);
            mutex.release();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public synchronized boolean isDone() {
        try {
            mutex.acquire();
            boolean empty = jobs.isEmpty();
            mutex.release();
            return empty;
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return true;
    }

    public synchronized void start(JavaPlugin plugin){
        System.out.println("Load balancer '" + id + "' started");
        runTaskTimer(plugin,0L, 1L);
    }

    @Override
    public synchronized void run() {
        try {
            if (isDone()) {
                System.out.println("Load balancer '" + id + "' finished");
                this.cancel();
            }
            long stopTime = System.currentTimeMillis() + MAX_MILLIS_PER_TICK;
            mutex.acquire();
            while (!jobs.isEmpty() && System.currentTimeMillis() <= stopTime) {
                Job poll = jobs.poll();
                if (poll != null) {
                    poll.compute();
                }
            }
            mutex.release();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}