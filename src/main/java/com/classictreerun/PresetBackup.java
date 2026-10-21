package com.classictreerun;

import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

/** Portable saved runs. Character records and active configuration are deliberately excluded. */
final class PresetBackup
{
    static final int MAX_CHARACTERS = 1024 * 1024;
    private static final int MAX_PRESETS = 256;
    private static final String FORMAT = "farm-run-planner-presets";
    private static final Set<String> BOOLEAN_SETTINGS = Set.of("autoOptimize", "hideCompleted", "showDetails",
        "supplyProtection", "supplyTools", "supplyRake", "supplyDibber", "supplyTeleports", "supplyFairyStaff", "loadPlanning", "loadNotedProtection");
    private static final Set<String> NUMBER_SETTINGS=Set.of("supplyCoins","loadReservedSlots");

    static final class Entry
    {
        final String name;
        final List<Patch> patches;
        final Map<String, String> settings;

        Entry(String name, List<Patch> patches, Map<String, String> settings)
        {
            this.name = PresetLibrary.validName(name);
            this.patches = List.copyOf(patches);
            this.settings = Map.copyOf(settings);
        }

        boolean matches(Entry other)
        {
            return patches.equals(other.patches) && settings.equals(other.settings);
        }

        Properties snapshot(String savedName)
        {
            Properties values = new Properties();
            values.putAll(settings);
            values.setProperty("runName", savedName);
            for (Patch patch : Patch.values())
            {
                values.setProperty("patch_" + patch.name(), Boolean.toString(patches.contains(patch)));
            }
            String order = RouteState.encode(patches);
            values.setProperty("selectionV2", patches.isEmpty() ? "-" : order);
            values.setProperty("order", order);
            values.setProperty("excluded", "");
            for (String gate : new String[]{"trees", "fruit", "hardwood", "calquat", "otherPatches"})
            {
                values.setProperty(gate, "true");
            }
            return values;
        }
    }

    private PresetBackup() { }

    static Entry capture(String name, ClassicTreeRunConfig config)
    {
        return new Entry(name, RouteState.active(config), settings(config));
    }

    private static Map<String, String> settings(ClassicTreeRunConfig config)
    {
        Map<String, String> values = new LinkedHashMap<>();
        values.put("autoOptimize", Boolean.toString(config.autoOptimize()));
        values.put("hideCompleted", Boolean.toString(config.hideCompleted()));
        values.put("showDetails", Boolean.toString(config.showDetails()));
        values.putAll(TravelCosts.settings(config));
        values.putAll(CropChoices.settings(config));
        values.putAll(InventoryLoadPlanner.settings(config));
        return values;
    }

    static String write(List<Entry> presets)
    {
        if (presets.size() > MAX_PRESETS) { throw invalid("A backup can contain at most 256 presets."); }
        StringWriter text = new StringWriter();
        try (JsonWriter writer = new JsonWriter(text))
        {
            writer.setIndent("  ");
            writer.beginObject().name("format").value(FORMAT).name("version").value(7);
            writer.name("presets").beginArray();
            for (Entry preset : presets)
            {
                writer.beginObject().name("name").value(preset.name).name("patches").beginArray();
                for (Patch patch : preset.patches) { writer.value(patch.name()); }
                writer.endArray().name("settings").beginObject();
                // Use a fixed order so exports are easy to compare and edit.
                for (String key : settings(new ClassicTreeRunConfig() { }).keySet())
                {
                    writer.name(key);
                    if (BOOLEAN_SETTINGS.contains(key)) { writer.value(Boolean.parseBoolean(preset.settings.get(key))); }
                    else if (NUMBER_SETTINGS.contains(key)) { writer.value(Integer.parseInt(preset.settings.get(key))); }
                    else { writer.value(preset.settings.get(key)); }
                }
                writer.endObject().endObject();
            }
            writer.endArray().endObject();
        }
        catch (IOException error) { throw new IllegalStateException("Could not prepare the preset backup.", error); }
        String result = text.toString() + "\n";
        if (result.getBytes(StandardCharsets.UTF_8).length > MAX_CHARACTERS)
        { throw invalid("Preset backups must be smaller than 1 MB."); }
        return result;
    }

    static List<Entry> read(String text)
    {
        if (text.length() > MAX_CHARACTERS) { throw invalid("Preset backups must be smaller than 1 MB."); }
        try (JsonReader reader = new JsonReader(new StringReader(text)))
        {
            reader.setLenient(false);
            Set<String> fields = new HashSet<>();
            List<Entry> presets = new ArrayList<>();
            int version = 0;
            reader.beginObject();
            while (reader.hasNext())
            {
                String key = uniqueField(reader, fields);
                switch (key)
                {
                    case "format":
                        if (!FORMAT.equals(string(reader))) { throw invalid("This file is not a Farm Run Planner preset backup."); }
                        break;
                    case "version":
                        if (reader.peek() != JsonToken.NUMBER) { throw invalid("This preset backup version is not supported."); }
                        String encodedVersion=reader.nextString();
                        if (!("1".equals(encodedVersion) || "2".equals(encodedVersion) || "3".equals(encodedVersion) || "4".equals(encodedVersion) || "5".equals(encodedVersion) || "6".equals(encodedVersion) || "7".equals(encodedVersion)))
                        { throw invalid("This preset backup version is not supported."); }
                        version=Integer.parseInt(encodedVersion);
                        break;
                    case "presets":
                        Set<String> names = new HashSet<>();
                        reader.beginArray();
                        while (reader.hasNext())
                        {
                            if (presets.size() == MAX_PRESETS) { throw invalid("A backup can contain at most 256 presets."); }
                            Entry entry = readEntry(reader);
                            if (!names.add(entry.name)) { throw invalid("The backup contains duplicate preset names."); }
                            presets.add(entry);
                        }
                        reader.endArray();
                        break;
                    default: throw invalid("The backup contains an unrecognized field.");
                }
            }
            reader.endObject();
            if (!fields.equals(Set.of("format", "version", "presets")) || reader.peek() != JsonToken.END_DOCUMENT)
            { throw invalid("The preset backup is incomplete or contains extra data."); }
            List<Entry> normalized=new ArrayList<>();
            for(Entry entry:presets)
            {
                if(!entry.settings.keySet().equals(keysForVersion(version)))throw invalid("The preset settings do not match the backup version.");
                Map<String,String> values=new LinkedHashMap<>(settings(new ClassicTreeRunConfig() { }));
                // These older formats predate bank choices; retain their historical default.
                if(version<=2)values.put("loadBank",BankDestination.GRAND_EXCHANGE.name());
                values.putAll(entry.settings);
                normalized.add(new Entry(entry.name,entry.patches,values));
            }
            return normalized;
        }
        catch (IOException | IllegalStateException error)
        { throw new IllegalArgumentException("The preset backup is not valid JSON in the expected format.", error); }
    }

    private static Entry readEntry(JsonReader reader) throws IOException
    {
        Set<String> fields = new HashSet<>();
        String name = null;
        List<Patch> patches = new ArrayList<>();
        Map<String, String> values = new LinkedHashMap<>();
        reader.beginObject();
        while (reader.hasNext())
        {
            switch (uniqueField(reader, fields))
            {
                case "name": name = PresetLibrary.validName(string(reader)); break;
                case "patches":
                    Set<Patch> unique = new HashSet<>();
                    reader.beginArray();
                    while (reader.hasNext())
                    {
                        Patch patch;
                        try { patch = Patch.valueOf(string(reader)); }
                        catch (IllegalArgumentException error) { throw invalid("The backup contains an unknown patch."); }
                        if (!unique.add(patch)) { throw invalid("A preset contains a duplicate patch."); }
                        patches.add(patch);
                    }
                    reader.endArray();
                    break;
                case "settings":
                    Set<String> keys = new HashSet<>();
                    Map<String, String> allowed = settings(new ClassicTreeRunConfig() { });
                    reader.beginObject();
                    while (reader.hasNext())
                    {
                        String key = uniqueField(reader, keys);
                        if (!allowed.containsKey(key)) { throw invalid("The backup contains an unsupported preset setting."); }
                        if (BOOLEAN_SETTINGS.contains(key))
                        {
                            if (reader.peek() != JsonToken.BOOLEAN) { throw invalid("A preset setting must be true or false."); }
                            values.put(key, Boolean.toString(reader.nextBoolean()));
                        }
                        else if (NUMBER_SETTINGS.contains(key))
                        {
                            if(reader.peek()!=JsonToken.NUMBER)throw invalid("The inventory setting must be a whole number.");
                            String value=reader.nextString();
                            if(key.startsWith("load"))InventoryLoadPlanner.validate(key,value);else CropChoices.validate(key,value);
                            values.put(key,value);
                        }
                        else
                        {
                            String value = string(reader);
                            if(key.startsWith("crop") || key.startsWith("supply"))CropChoices.validate(key,value);
                            else if(key.startsWith("load"))InventoryLoadPlanner.validate(key,value);
                            else validateTravel(key, value);
                            values.put(key, value);
                        }
                    }
                    reader.endObject();
                    if (!keys.equals(keysForVersion(7)) && !keys.equals(keysForVersion(6)) && !keys.equals(keysForVersion(5)) && !keys.equals(keysForVersion(4)) && !keys.equals(keysForVersion(3)) && !keys.equals(keysForVersion(2)) && !keys.equals(keysForVersion(1)))
                    { throw invalid("A preset is missing required settings."); }
                    break;
                default: throw invalid("A preset contains an unrecognized field.");
            }
        }
        reader.endObject();
        if (!fields.equals(Set.of("name", "patches", "settings"))) { throw invalid("A preset is missing required fields."); }
        return new Entry(name, patches, values);
    }

    private static Set<String> keysForVersion(int version)
    {
        Set<String> keys=new HashSet<>(settings(new ClassicTreeRunConfig() { }).keySet());
        if(version<7)keys.remove("supplyInventory");
        if(version<6)keys.remove("supplyBankView");
        if(version<5)keys.remove("supplyEquipment");
        if(version<4)keys.removeAll(Set.of("supplyTeleports","supplyTeleportMode","supplyFairyStaff"));
        if(version<3)keys.removeIf(key->key.startsWith("load"));
        if(version<2)keys.removeIf(key->key.startsWith("crop") || key.startsWith("supply"));
        return keys;
    }

    private static void validateTravel(String key, String value)
    {
        try
        {
            switch (key)
            {
                case "faladorTravel": ClassicTreeRunConfig.FaladorTravel.valueOf(value); break;
                case "taverleyTravel": ClassicTreeRunConfig.TaverleyTravel.valueOf(value); break;
                case "strongholdTravel": ClassicTreeRunConfig.StrongholdTravel.valueOf(value); break;
                case "catherbyTravel": ClassicTreeRunConfig.CatherbyTravel.valueOf(value); break;
                case "brimhavenTravel": ClassicTreeRunConfig.BrimhavenTravel.valueOf(value); break;
                case "calquatTravel": ClassicTreeRunConfig.CalquatTravel.valueOf(value); break;
                case "fossilTravel": ClassicTreeRunConfig.FossilTravel.valueOf(value); break;
                case "guildTravel": ClassicTreeRunConfig.GuildTravel.valueOf(value); break;
                default: throw invalid("The backup contains an unsupported travel setting.");
            }
        }
        catch (IllegalArgumentException error) { throw invalid("The backup contains an unknown travel option."); }
    }

    private static String uniqueField(JsonReader reader, Set<String> fields) throws IOException
    {
        String key = reader.nextName();
        if (!fields.add(key)) { throw invalid("The backup contains a duplicate field."); }
        return key;
    }

    private static String string(JsonReader reader) throws IOException
    {
        if (reader.peek() != JsonToken.STRING) { throw invalid("A backup field must contain text."); }
        return reader.nextString();
    }

    private static IllegalArgumentException invalid(String message) { return new IllegalArgumentException(message); }
}
