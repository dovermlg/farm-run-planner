package com.classictreerun;

import com.google.common.collect.Multimap;
import java.lang.reflect.*;
import java.util.*;
import net.runelite.api.coords.WorldPoint;

/** Offline compatibility audit only. Never packaged in the production plugin. */
public final class VerifyFarmingReference
{
    public static void main(String[] args) throws Exception
    {
        CropReference ours=new CropReference();
        String prefix="net.runelite.client.plugins.timetracking.farming.";
        Class<?> implementationType=Class.forName(prefix+"PatchImplementation");
        Method decode=accessible(implementationType,"forVarbitValue",int.class);
        Class<?> stateType=Class.forName(prefix+"PatchState");
        Method[] getters={accessible(stateType,"getProduce"),accessible(stateType,"getCropState"),
            accessible(stateType,"getStage"),accessible(stateType,"getStages"),accessible(stateType,"getTickRate")};
        Map<String,Patch> representatives=new TreeMap<>();
        for(CropReference.Binding binding:ours.bindings.values())representatives.putIfAbsent(binding.implementation,binding.patch);
        long states=0;
        for(Object implementation:implementationType.getEnumConstants())
        {
            String kind=((Enum<?>)implementation).name();Patch patch=representatives.get(kind);
            if(patch==null)continue;
            for(int raw=0;raw<=65535;raw++)
            {
                Object expected=decode.invoke(implementation,raw);CropReference.Crop actual=ours.decode(patch,raw);
                if(expected==null){require(actual==null,kind+" unexpected decoded value "+raw);}
                else
                {
                    require(actual!=null,kind+" missing raw value "+raw);
                    require(getters[0].invoke(expected).toString().equals(actual.produce)
                        && getters[1].invoke(expected).toString().equals(actual.state)
                        && (Integer)getters[2].invoke(expected)==actual.stage
                        && (Integer)getters[3].invoke(expected)==actual.stages
                        && (Integer)getters[4].invoke(expected)==actual.tickMinutes,kind+" differs at "+raw);
                }
                states++;
            }
        }
        Class<?> worldType=Class.forName(prefix+"FarmingWorld");
        Constructor<?> constructor=worldType.getDeclaredConstructor();constructor.setAccessible(true);
        Object world=constructor.newInstance();Field field=worldType.getDeclaredField("regions");field.setAccessible(true);
        Multimap<?,?> regions=(Multimap<?,?>)field.get(world);
        Class<?> regionType=Class.forName(prefix+"FarmingRegion"),patchType=Class.forName(prefix+"FarmingPatch");
        Method getId=regionType.getMethod("getRegionID"),getPatches=regionType.getMethod("getPatches"),bounds=regionType.getMethod("isInBounds",WorldPoint.class);
        Method getVarbit=accessible(patchType,"getVarbit"),getImplementation=accessible(patchType,"getImplementation");
        Map<String,Object> source=new HashMap<>();
        for(Object region:regions.values())
        {
            for(Object patch:(Object[])getPatches.invoke(region))
            {
                String kind=((Enum<?>)getImplementation.invoke(patch)).name();
                if(kind.contains("COMPOST"))continue;
                require(representatives.containsKey(kind),"New untracked crop family: "+kind);
                source.put(getId.invoke(region)+":"+getVarbit.invoke(patch)+":"+kind,region);
            }
        }
        Set<String> mapped=new HashSet<>();long points=0;
        for(CropReference.Binding binding:ours.bindings.values())
        {
            String key=binding.sourceRegion+":"+binding.varbit+":"+binding.implementation;
            Object region=source.get(key);require(region!=null,"Invalid binding: "+binding.patch);
            require(mapped.add(key),"Duplicate patch binding: "+binding.patch);
            Set<Integer> aliases=new HashSet<>();
            for(Map.Entry<?,?> entry:regions.entries())if(entry.getValue()==region)aliases.add((Integer)entry.getKey());
            require(aliases.equals(binding.regions),"Region aliases changed: "+binding.patch);
            for(int id:aliases)
            {
                int originX=(id>>8)*64,originY=(id&255)*64;
                for(int plane=0;plane<4;plane++)for(int x=originX;x<originX+64;x++)for(int y=originY;y<originY+64;y++)
                {
                    boolean expected=(Boolean)bounds.invoke(region,new WorldPoint(x,y,plane));
                    require(expected==binding.applies(x,y,plane),"Bounds differ: "+binding.patch+" at "+x+","+y+","+plane);
                    points++;
                }
            }
        }
        require(mapped.equals(source.keySet()),"Catalogue differs: RuneLite="+source.size()+", plugin="+mapped.size());
        System.out.println("Verified "+states+" raw decoder values across "+representatives.size()+" families, "+mapped.size()+" unique patch bindings, and "+points+" tile/plane boundary checks.");
    }
    private static Method accessible(Class<?> type,String name,Class<?>... parameters) throws Exception
    {Method method=type.getDeclaredMethod(name,parameters);method.setAccessible(true);return method;}
    private static void require(boolean passed,String message)
    {if(!passed)throw new IllegalStateException(message);}
}
