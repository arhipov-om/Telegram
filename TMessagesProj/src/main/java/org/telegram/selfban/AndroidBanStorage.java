package org.telegram.selfban;

import android.content.Context;
import android.util.AtomicFile;
import java.io.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/** App-private, per-account atomic persistence for channel bans. */
public final class AndroidBanStorage implements StrictChannelBanStore.Storage {
    private final AtomicFile file;

    public AndroidBanStorage(Context context, int account) {
        if (account < 0) throw new IllegalArgumentException("Invalid account");
        file = new AtomicFile(new File(context.getFilesDir(), "selfban_account_" + account + ".properties"));
    }

    @Override public Map<Long, Long> loadBans() throws IOException {
        Map<Long, Long> result = new HashMap<>();
        if (!file.getBaseFile().exists()) return result;
        Properties props = new Properties();
        try (InputStream in = file.openRead()) { props.load(in); }
        for (String key : props.stringPropertyNames()) {
            if (!key.startsWith("ban.")) continue;
            try {
                long id = Long.parseLong(key.substring(4));
                long until = Long.parseLong(props.getProperty(key));
                if (id > 0 && until > 0) result.put(id, until);
            } catch (NumberFormatException e) {
                throw new IOException("Invalid self-ban data", e);
            }
        }
        return result;
    }

    @Override public void saveBans(Map<Long, Long> bans) throws IOException {
        Properties props = new Properties();
        for (Map.Entry<Long, Long> entry : bans.entrySet()) {
            props.setProperty("ban." + entry.getKey(), Long.toString(entry.getValue()));
        }
        FileOutputStream out = file.startWrite();
        try {
            props.store(out, "Strict channel self-bans");
            file.finishWrite(out);
        } catch (IOException | RuntimeException e) {
            file.failWrite(out);
            throw e;
        }
    }
}
