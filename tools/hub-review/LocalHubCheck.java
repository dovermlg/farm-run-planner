package net.runelite.pluginhub.packager;

import java.nio.file.*;
import java.io.*;
import net.runelite.pluginhub.apirecorder.API;

/** Local fixture only: no repository download, upload, credentials or game client. */
public class LocalHubCheck
{
    public static void main(String[] args) throws Exception
    {
        Path source=Path.of(args[0]).toAbsolutePath(),reports=Path.of(args[1]).toAbsolutePath();
        Files.createDirectories(reports);
        try(Plugin plugin=new Plugin(new File(args[2])))
        {
            try
            {
                Files.createDirectories(plugin.repositoryDirectory.toPath());
                for(String name:new String[]{"src","docs","tools",".github","gradle","README.md","CHANGELOG.md","icon.png","LICENSE",
                    "THIRD-PARTY-NOTICES.md","build.gradle","settings.gradle","runelite-plugin.properties",".gitignore","gradlew","gradlew.bat"})
                {
                    Path from=source.resolve(name);if(!Files.exists(from))continue;
                    if(Files.isDirectory(from))
                    {
                        try(var paths=Files.walk(from))
                        {
                            for(Path p:(Iterable<Path>)paths::iterator)
                            {
                                Path to=plugin.repositoryDirectory.toPath().resolve(source.relativize(p));
                                if(Files.isDirectory(p))Files.createDirectories(to);else Files.copy(p,to);
                            }
                        }
                    }
                    else Files.copy(from,plugin.repositoryDirectory.toPath().resolve(name));
                }
                plugin.build("1.13.2",true);
                java.util.Properties metadata=new java.util.Properties();
                try(InputStream stream=Files.newInputStream(source.resolve("runelite-plugin.properties"))){metadata.load(stream);}
                String version=metadata.getProperty("version");
                if(!version.equals(plugin.getDisplayData().getVersion()))throw new IllegalStateException("Plugin Hub manifest version does not match source metadata");
                Files.writeString(reports.resolve("version.txt"),version+"\n");
                try(InputStream stream=Files.newInputStream(plugin.getApiFile().toPath()))
                {
                    String missing=API.decode(stream).missingFrom(Plugin.CURRENT_API).sorted().collect(java.util.stream.Collectors.joining("\n"));
                    if(!missing.isEmpty())throw new IllegalStateException("APIs missing from RuneLite 1.13.2:\n"+missing);
                }
                Path built=plugin.getLogFile().toPath().getParent();
                for(String name:new String[]{"plugin.jar","api","source.zip"})Files.copy(built.resolve(name),reports.resolve(name),StandardCopyOption.REPLACE_EXISTING);
                System.out.println("OFFICIAL LOCAL PACKAGER CHECK PASSED; API compatibility matched RuneLite 1.13.2.");
                System.out.println("Repository URL is a local test fixture. No GitHub publication, submission or remote-source validation was performed.");
            }
            finally
            {
                Files.copy(plugin.getLogFile().toPath(),reports.resolve("packager.log"),StandardCopyOption.REPLACE_EXISTING);
            }
        }
    }
}
