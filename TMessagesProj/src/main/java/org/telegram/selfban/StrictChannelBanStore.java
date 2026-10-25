package org.telegram.selfban;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Per-account strict channel bans. This is a standalone core, not yet wired
 * into Telegram's navigation, search, notifications or channel menus.
 *
 * IMPORTANT: A device clock jump forward can expire bans early. Data clearing,
 * other clients, and modified APKs also bypass this local restriction.
 */
public final class StrictChannelBanStore {
    public interface Clock { long nowMillis(); }
    public interface Storage {
        Map<Long, Long> loadBans() throws IOException;
        void saveBans(Map<Long, Long> bans) throws IOException;
    }

    private final Clock clock;
    private final Storage storage;
    private final Map<Long, Long> bans;

    public StrictChannelBanStore(Storage storage, Clock clock) throws IOException {
        this.storage = storage;
        this.clock = clock;
        this.bans = new HashMap<>(storage.loadBans());
    }

    /** Use a canonical positive TLRPC.Chat.id after verifying Chat is a channel. */
    public synchronized void ban(long channelId, long durationMillis) throws IOException {
        if (channelId <= 0 || durationMillis <= 0) {
            throw new IllegalArgumentException("Invalid channel ID or duration");
        }
        long until = Math.addExact(clock.nowMillis(), durationMillis);
        long previous = bans.containsKey(channelId) ? bans.get(channelId) : 0L;
        bans.put(channelId, Math.max(previous, until));
        storage.saveBans(new HashMap<>(bans));
    }

    public synchronized boolean isBlocked(long channelId) {
        Long until = bans.get(channelId);
        return until != null && clock.nowMillis() < until;
    }

    public synchronized long blockedUntil(long channelId) {
        Long until = bans.get(channelId);
        return until != null && clock.nowMillis() < until ? until : 0L;
    }

    // Intentionally no unban method.
}
