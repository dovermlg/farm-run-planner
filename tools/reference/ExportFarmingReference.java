package com.classictreerun;
import java.lang.reflect.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;
public class ExportFarmingReference {
 public static void main(String[] args) throws Exception {
  Class<?> type=Class.forName("net.runelite.client.plugins.timetracking.farming.PatchImplementation");
  Method decode=type.getDeclaredMethod("forVarbitValue",int.class); decode.setAccessible(true);
  Class<?> state=Class.forName("net.runelite.client.plugins.timetracking.farming.PatchState");
  String[] methods={"getProduce","getCropState","getStage","getStages","getTickRate"};
  Method[] getters=new Method[methods.length]; for(int i=0;i<methods.length;i++){getters[i]=state.getDeclaredMethod(methods[i]);getters[i].setAccessible(true);}
  List<String> lines=new ArrayList<>(); lines.add("# RuneLite 1.13.2 crop state reference. See THIRD-PARTY-NOTICES.md.");
  for(Object implementation:type.getEnumConstants()) {
   String name=((Enum<?>)implementation).name(); if(name.contains("COMPOST"))continue;
   int last=-1;String lastValue=null;
   for(int raw=0;raw<65536;raw++) {
    Object current=decode.invoke(implementation,raw); String value=null;
    if(current!=null){List<String> fields=new ArrayList<>(); for(Method get:getters){Object v=get.invoke(current); fields.add(v instanceof Enum?((Enum<?>)v).name():String.valueOf(v));} value=String.join(",",fields);}
    if(!Objects.equals(lastValue,value)){if(lastValue!=null)lines.add(name+","+last+","+(raw-1)+","+lastValue);last=raw;lastValue=value;}
   }
   if(lastValue!=null)lines.add(name+","+last+",65535,"+lastValue);
  }
  Path file=Paths.get("build/reference/crop-states.csv");Files.createDirectories(file.getParent());Files.write(file,lines,StandardCharsets.UTF_8);
  Class<?> worldType=Class.forName("net.runelite.client.plugins.timetracking.farming.FarmingWorld");
  Constructor<?> constructor=worldType.getDeclaredConstructor();constructor.setAccessible(true);Object world=constructor.newInstance();
  Field regionField=worldType.getDeclaredField("regions");regionField.setAccessible(true);
  com.google.common.collect.Multimap<?,?> regions=(com.google.common.collect.Multimap<?,?>)regionField.get(world);
  Class<?> regionType=Class.forName("net.runelite.client.plugins.timetracking.farming.FarmingRegion");
  Method getId=regionType.getMethod("getRegionID"),getName=regionType.getMethod("getName"),getPatches=regionType.getMethod("getPatches");
  Class<?> patchType=Class.forName("net.runelite.client.plugins.timetracking.farming.FarmingPatch");
  Method patchName=patchType.getDeclaredMethod("getName"),varbit=patchType.getDeclaredMethod("getVarbit"),impl=patchType.getDeclaredMethod("getImplementation");
  for(Method m:new Method[]{patchName,varbit,impl})m.setAccessible(true);
  SortedSet<String> regionLines=new TreeSet<>();
  for(Map.Entry<?,?> entry:regions.entries()){
   Object region=entry.getValue();
   for(Object patch:(Object[])getPatches.invoke(region)){
    Object kind=impl.invoke(patch);String kindName=((Enum<?>)kind).name();if(kindName.contains("COMPOST"))continue;
    regionLines.add(entry.getKey()+","+getId.invoke(region)+","+getName.invoke(region)+","+patchName.invoke(patch)+","+varbit.invoke(patch)+","+kindName);
   }
  }
  Files.write(Paths.get("build/reference/regions.csv"),regionLines,StandardCharsets.UTF_8);
  Method getRegions=worldType.getDeclaredMethod("getRegionsForLocation",net.runelite.api.coords.WorldPoint.class);getRegions.setAccessible(true);
  List<String> mapped=new ArrayList<>();mapped.add("# patch,source-region,varbit,implementation,valid-regions");
  for(Patch ours:Patch.values()) {
   PatchSite site=ours.getSite();int x=site.x,y=site.y;
   if(ours==Patch.GUILD_HESPORI){x=1248;y=10060;}
   String kind=ours.getType().name();
   if(kind.equals("FRUIT"))kind="FRUIT_TREE";else if(kind.equals("HARDWOOD"))kind="HARDWOOD_TREE";else if(kind.equals("GRAPE"))kind="GRAPES";else if(kind.equals("CRYSTAL"))kind="CRYSTAL_TREE";else if(kind.equals("SPIRIT"))kind="SPIRIT_TREE";
   Collection<?> matching=(Collection<?>)getRegions.invoke(world,new net.runelite.api.coords.WorldPoint(x,y,0));
   List<Object[]> candidates=new ArrayList<>();
   for(Object region:matching)for(Object patch:(Object[])getPatches.invoke(region))if(((Enum<?>)impl.invoke(patch)).name().equals(kind))candidates.add(new Object[]{region,patch});
   String target="";String id=ours.name();
   for(String suffix:new String[]{"NW","NE","SW","SE","N","S","EAST","MIDDLE","WEST"})if(id.endsWith("_"+suffix)){target=suffix.replace("NW","North West").replace("NE","North East").replace("SW","South West").replace("SE","South East");if(suffix.equals("N"))target="North";if(suffix.equals("S"))target="South";if(suffix.equals("EAST"))target="East";if(suffix.equals("WEST"))target="West";if(suffix.equals("MIDDLE"))target="Middle";}
   if(id.startsWith("VINERY_"))target=(id.contains("_E")?"East ":"West ")+id.substring(id.length()-1);
   if(id.equals("CORAL_E"))target="East";if(id.equals("CORAL_W"))target="West";
   if(candidates.size()>1){final String desired=target;candidates.removeIf(pair->{try{return !patchName.invoke(pair[1]).equals(desired);}catch(Exception error){throw new RuntimeException(error);}});}
   if(candidates.size()!=1){throw new IllegalStateException("MISSING "+ours+" region="+new net.runelite.api.coords.WorldPoint(x,y,0).getRegionID()+" candidates="+candidates.size()+" target="+target);}
   Object region=candidates.get(0)[0],patch=candidates.get(0)[1];SortedSet<Integer> ids=new TreeSet<>();
   for(Map.Entry<?,?> entry:regions.entries())if(entry.getValue()==region)ids.add((Integer)entry.getKey());
   List<String> idText=new ArrayList<>();ids.forEach(v->idText.add(v.toString()));
   mapped.add(ours.name()+","+getId.invoke(region)+","+varbit.invoke(patch)+","+kind+","+String.join(";",idText));
  }
  Files.write(Paths.get("build/reference/patch-bindings.csv"),mapped,StandardCharsets.UTF_8);
  System.out.println("Exported "+(lines.size()-1)+" state ranges");
 }
}
