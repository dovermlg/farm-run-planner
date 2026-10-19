package com.classictreerun;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import net.runelite.client.util.Filepath;

/** Reads and writes preset-only backups inside the plugin's local backup directory. */
final class PresetTransfer
{
    private final Filepath directory;
    private final ClassicTreeRunPanel.Preferences preferences;

    PresetTransfer(Filepath directory, ClassicTreeRunPanel.Preferences preferences)
    {
        this.directory = directory;
        this.preferences = preferences;
    }

    String exportPresets(String filename)
    {
        Filepath file = file(filename);
        PresetLibrary library = new PresetLibrary(preferences);
        List<PresetBackup.Entry> entries = new ArrayList<>();
        for (String name : library.names()) { entries.add(PresetBackup.capture(name, library.config(name))); }
        String backup = PresetBackup.write(entries);
        Filepath temporary = null;
        try
        {
            directory.createDirectories();
            temporary = directory.createTempFile("preset-backup-", ".tmp");
            try (Writer writer = temporary.openBufferedWriter()) { writer.write(backup); }
            try { temporary.moveTo(file, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING); }
            catch (AtomicMoveNotSupportedException error) { temporary.moveTo(file, StandardCopyOption.REPLACE_EXISTING); }
        }
        catch (IOException error) { throw new IllegalStateException("Could not export presets. Check folder access and disk space.", error); }
        finally
        {
            if (temporary != null)
            {
                try { temporary.deleteIfExists(); } catch (IOException ignored) { /* Keep the existing backup. */ }
            }
        }
        return "Exported " + entries.size() + " presets to preset-backups/" + file.getFileName() + ".";
    }

    String importPresets(String filename, PresetSlots slots)
    {
        Filepath file = file(filename);
        if (!file.isFile()) { throw new IllegalArgumentException("Put the JSON file in the plugin's preset-backups folder, then try Import presets again."); }
        StringBuilder text = new StringBuilder();
        try
        {
            if (file.size() > PresetBackup.MAX_CHARACTERS) { throw new IllegalArgumentException("Preset backups must be smaller than 1 MB."); }
            try (Reader reader = file.openBufferedReader())
            {
                char[] buffer = new char[4096];
                int count;
                while ((count = reader.read(buffer)) != -1)
                {
                    if (text.length() + count > PresetBackup.MAX_CHARACTERS)
                    { throw new IllegalArgumentException("Preset backups must be smaller than 1 MB."); }
                    text.append(buffer, 0, count);
                }
            }
        }
        catch (IOException error) { throw new IllegalStateException("Could not read the preset backup. Existing presets were kept.", error); }
        return slots.importPresets(PresetBackup.read(text.toString()));
    }

    private Filepath file(String filename)
    {
        String clean = filename.trim();
        if (!clean.matches("[A-Za-z0-9][A-Za-z0-9._-]{0,75}\\.json") || clean.contains(".."))
        { throw new IllegalArgumentException("Use a simple .json filename with letters, numbers, hyphens or underscores; no folder path."); }
        return directory.joinSegment(clean);
    }
}
