package com.classictreerun;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

/** Versioned preference format. Names are encoded so punctuation cannot corrupt records. */
final class SavedRuns
{
    static final String KEY = "savedRunsV1";

    private SavedRuns() { }

    static Map<String, Set<Patch>> decode(String value)
    {
        Map<String, Set<Patch>> runs = new LinkedHashMap<>();
        if (value == null) { return runs; }
        for (String record : value.split(";"))
        {
            int split = record.indexOf(':');
            if (split < 0) { continue; }
            try
            {
                String name = new String(Base64.getUrlDecoder().decode(record.substring(0, split)), StandardCharsets.UTF_8);
                if (!name.trim().isEmpty()) { runs.put(name, RouteState.parse(record.substring(split + 1))); }
            }
            catch (IllegalArgumentException ignored) { /* Keep valid records if one is damaged. */ }
        }
        return runs;
    }

    static String encode(Map<String, Set<Patch>> runs)
    {
        StringBuilder result = new StringBuilder();
        runs.forEach((name, patches) ->
        {
            if (result.length() > 0) { result.append(';'); }
            result.append(Base64.getUrlEncoder().withoutPadding().encodeToString(name.getBytes(StandardCharsets.UTF_8)))
                .append(':').append(RouteState.encode(patches));
        });
        return result.toString();
    }
}
