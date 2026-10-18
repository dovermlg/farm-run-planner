package com.classictreerun;

import java.lang.invoke.MethodHandles;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import net.runelite.client.config.ConfigItem;

/** In-memory RuneLite-style configuration proxy; no account or client connection is used. */
final class ConfigFixture implements ClassicTreeRunPanel.Preferences
{
    final Map<String,String> values = new HashMap<>();
    CountDownLatch orderSaved;
    final ClassicTreeRunConfig config = (ClassicTreeRunConfig) Proxy.newProxyInstance(
        ClassicTreeRunConfig.class.getClassLoader(), new Class<?>[]{ClassicTreeRunConfig.class}, this::invoke);

    private Object invoke(Object proxy, Method method, Object[] args) throws Throwable
    {
        ConfigItem item = method.getAnnotation(ConfigItem.class);
        if (item != null && values.containsKey(item.keyName()))
        {
            String value = values.get(item.keyName());
            if (method.getReturnType() == boolean.class) { return Boolean.parseBoolean(value); }
            if (method.getReturnType() == String.class) { return value; }
            if (method.getReturnType().isEnum())
            {
                for (Object option : method.getReturnType().getEnumConstants())
                {
                    if (((Enum<?>) option).name().equals(value)) { return option; }
                }
            }
        }
        return MethodHandles.privateLookupIn(method.getDeclaringClass(), MethodHandles.lookup())
            .unreflectSpecial(method, method.getDeclaringClass()).bindTo(proxy)
            .invokeWithArguments(args == null ? new Object[0] : args);
    }

    public String get(String key) { return values.get(key); }
    public void set(String key, String value)
    {
        values.put(key, value);
        if ("order".equals(key) && orderSaved != null) { orderSaved.countDown(); }
    }
    void nativeSelection(Set<Patch> selected)
    {
        values.put(RunSettings.MIGRATED, "true");
        for (Patch p : Patch.values()) { set("patch_" + p.name(), Boolean.toString(selected.contains(p))); }
    }
}
