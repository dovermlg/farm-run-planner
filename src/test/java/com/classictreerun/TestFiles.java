package com.classictreerun;

import java.nio.file.Path;
import net.runelite.client.util.Filepath;

/** Temporary test directories only. Production obtains Filepath from Plugin.getPluginDirectory(). */
final class TestFiles
{
    private TestFiles() { }
    static Filepath at(Path file)
    { return Filepath.Unchecked.getRooted(file.getParent()).joinSegment(file.getFileName().toString()); }
}
