package dev.daweapon.type.skyblockgeneric.century.raffle;

import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public final class RaffleProfile {
    static final String TEAM = "team";
    static final String TICKETS = "tickets";
    static final String ONLINE = "online";
    static final String SIMON = "simon";
    static final String MEMENTO = "memento";
    static final String MILESTONE_PREFIX = "milestone:";
    static final String TASK_PREFIX = "task:";
    static final String FED_PREFIX = "fed:";

    private final String eventId;
    private volatile @Nullable CenturyTeam team;
    private volatile long tickets;
    private volatile long onlineMinutes;
    private final Set<Integer> milestones = ConcurrentHashMap.newKeySet();
    private final Set<String> tasks = ConcurrentHashMap.newKeySet();
    private final Set<String> flags = ConcurrentHashMap.newKeySet();

    RaffleProfile(String eventId) {
        this.eventId = eventId;
    }

    static RaffleProfile of(String eventId, Map<String, String> fields) {
        RaffleProfile profile = new RaffleProfile(eventId);
        profile.team = CenturyTeam.parse(fields.get(TEAM));
        profile.tickets = parse(fields.get(TICKETS));
        profile.onlineMinutes = parse(fields.get(ONLINE));
        if (fields.containsKey(SIMON)) profile.flags.add(SIMON);
        if (fields.containsKey(MEMENTO)) profile.flags.add(MEMENTO);
        fields.keySet().forEach(key -> {
            if (key.startsWith(MILESTONE_PREFIX)) profile.milestones.add((int) parse(key.substring(MILESTONE_PREFIX.length())));
            else if (key.startsWith(TASK_PREFIX)) profile.tasks.add(key.substring(TASK_PREFIX.length()));
        });
        return profile;
    }

    static long parse(@Nullable String raw) {
        if (raw == null) return 0;
        try {
            return Long.parseLong(raw.trim());
        } catch (NumberFormatException exception) {
            return 0;
        }
    }

    public String eventId() {
        return eventId;
    }

    public @Nullable CenturyTeam team() {
        return team;
    }

    void team(CenturyTeam team) {
        this.team = team;
    }

    public long tickets() {
        return tickets;
    }

    void tickets(long tickets) {
        this.tickets = tickets;
    }

    public long onlineMinutes() {
        return onlineMinutes;
    }

    void onlineMinutes(long onlineMinutes) {
        this.onlineMinutes = onlineMinutes;
    }

    public boolean claimedMilestone(int milestone) {
        return milestones.contains(milestone);
    }

    void claimMilestone(int milestone) {
        milestones.add(milestone);
    }

    public boolean taskDone(int period, RaffleTask task) {
        return tasks.contains(taskKey(period, task));
    }

    void completeTask(int period, RaffleTask task) {
        tasks.add(taskKey(period, task));
    }

    public boolean flagged(String flag) {
        return flags.contains(flag);
    }

    void flag(String flag) {
        flags.add(flag);
    }

    static String taskKey(int period, RaffleTask task) {
        return period + ":" + task.name();
    }
}
