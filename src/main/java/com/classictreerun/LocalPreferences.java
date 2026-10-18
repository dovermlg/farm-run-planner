package com.classictreerun;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.StandardCopyOption;
import java.util.Map;
import java.util.Properties;
import net.runelite.client.util.Filepath;

/** A plain local file, outside RuneLite's synced configuration. No network access. */
final class LocalPreferences implements ClassicTreeRunPanel.Preferences
{
    private final Filepath file;
    private final Properties values = new Properties();

    LocalPreferences(Filepath file) throws IOException
    {
        this.file = file;
        if (file.exists())
        {
            try (Reader reader = file.openBufferedReader()) { values.load(reader); }
            catch (IllegalArgumentException error) { throw new IOException("Invalid local settings; original file preserved", error); }
        }
    }

    boolean exists() { return file.exists(); }
    public String get(String key) { return values.getProperty(key); }
    public void set(String key, String value)
    {
        java.util.Map<String, String> change = new java.util.HashMap<>();
        change.put(key, value);
        update(change);
    }

    @Override public void update(Map<String, String> changes)
    {
        Properties next = new Properties();
        next.putAll(values);
        changes.forEach((key, value) -> { if (value == null) { next.remove(key); } else { next.setProperty(key, value); } });
        Filepath temporary = null;
        try
        {
            file.getParent().createDirectories();
            temporary = file.getParent().createTempFile("planner-", ".tmp");
            try (Writer writer = temporary.openBufferedWriter())
            {
                next.store(writer, "Farm Run Planner - local data only");
            }
            try { temporary.moveTo(file, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE); }
            catch (AtomicMoveNotSupportedException error) { temporary.moveTo(file, StandardCopyOption.REPLACE_EXISTING); }
            values.clear();
            values.putAll(next);
        }
        catch (IOException error) { throw new IllegalStateException("Could not save local farming settings. Check folder access and disk space.", error); }
        finally
        {
            if (temporary != null) { try { temporary.deleteIfExists(); } catch (IOException ignored) { /* Old data remains intact. */ } }
        }
    }
}
