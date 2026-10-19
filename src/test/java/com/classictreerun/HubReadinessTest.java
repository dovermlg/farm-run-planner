package com.classictreerun;

import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import net.runelite.client.config.ConfigItem;
import net.runelite.client.plugins.PluginDescriptor;
import org.junit.Test;
import static org.junit.Assert.*;

/** Offline development checks. No RuneScape account, game client or server connection. */
public class HubReadinessTest
{
    @Test public void customPresetNamesAreRenderedAsTextInsteadOfSwingHtml() throws Exception
    {
        javax.swing.SwingUtilities.invokeAndWait(()->{
            ConfigFixture fixture=new ConfigFixture();String name="<html><b>My herbs</b>";
            new PresetLibrary(fixture).save(name,fixture.config);
            PresetControls controls=new PresetControls(fixture.config,fixture,()->{},name,value->{},value->{});
            javax.swing.JComboBox<?> combo=(javax.swing.JComboBox<?>)controls.getComponent(1);
            javax.swing.DefaultListCellRenderer renderer=(javax.swing.DefaultListCellRenderer)combo.getRenderer();
            renderer.getListCellRendererComponent(new javax.swing.JList<>(),name,0,false,false);
            assertEquals(name,renderer.getText());
            assertEquals(Boolean.TRUE,renderer.getClientProperty("html.disable"));
            assertNull(renderer.getClientProperty(javax.swing.plaf.basic.BasicHTML.propertyKey));
        });
    }
    @Test public void directConfigRegistryMatchesEveryNativeFieldAndItsValue() throws Exception
    {
        ClassicTreeRunConfig config=new ClassicTreeRunConfig() { };
        Map<String,String> expected=new HashMap<>();
        for(Method method:ClassicTreeRunConfig.class.getDeclaredMethods())
        {
            ConfigItem item=method.getAnnotation(ConfigItem.class);if(item==null)continue;
            Object value=method.invoke(config);
            assertNull("Duplicate config key",expected.put(item.keyName(),value instanceof Enum?((Enum<?>)value).name():String.valueOf(value)));
        }
        assertEquals(expected.keySet(),ConfigValues.KEYS);
        assertEquals(expected,ConfigValues.read(config));
    }

    @Test public void descriptorPreservesDirectoryMigrationAndEnablesInstalledPlugin()
    {
        PluginDescriptor descriptor=ClassicTreeRunPlugin.class.getAnnotation(PluginDescriptor.class);
        assertEquals("farm-run-planner",descriptor.internalName());
        assertEquals("farm-run-planner",descriptor.legacyDataDirectory());
        assertTrue(descriptor.enabledByDefault());
    }

    @Test public void licenseNoticesAreNormalResourcesWithNoCustomBuildStep() throws Exception
    {
        for(String name:new String[]{"LICENSE","THIRD-PARTY-NOTICES.md"})
        {
            try(var resource=ClassicTreeRunPlugin.class.getResourceAsStream("/META-INF/"+name))
            {
                assertNotNull(resource);
                assertArrayEquals(Files.readAllBytes(Path.of(name)),resource.readAllBytes());
            }
        }
    }

    @Test public void productionSourcesAvoidKnownForbiddenFacilities() throws Exception
    {
        String[] forbidden={"java.lang.reflect", "getDeclaredMethods(", "getDeclaredField(",
            "java.nio.file.Files", "java.nio.file.Paths", "java.io.File;", "java.io.FileInputStream",
            "java.io.FileOutputStream", "Filepath.Unchecked", "Runtime.getRuntime(", "ProcessBuilder",
            "Thread.sleep(", "Thread.interrupt(", "KeyboardFocusManager", "java.awt.Robot",
            "java.net.", "client.menuAction(", "client.runScript(", "client.hopToWorld(",
            "new Gson(", "new GsonBuilder(", "new OkHttpClient("};
        try(var paths=Files.walk(Path.of("src/main/java")))
        {
            for(Path file:(Iterable<Path>)paths.filter(p->p.toString().endsWith(".java"))::iterator)
            {
                String source=Files.readString(file);
                for(String facility:forbidden)assertFalse(file+" contains "+facility,source.contains(facility));
            }
        }
    }
}
