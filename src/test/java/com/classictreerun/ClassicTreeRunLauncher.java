package com.classictreerun;

import net.runelite.client.RuneLite;
import net.runelite.client.externalplugins.ExternalPluginManager;

/** Local development client entry point, following RuneLite's official template. */
public final class ClassicTreeRunLauncher
{
    private ClassicTreeRunLauncher() { }
    public static void main(String[] args) throws Exception
    {
        ExternalPluginManager.loadBuiltin(ClassicTreeRunPlugin.class);
        RuneLite.main(args);
    }
}
