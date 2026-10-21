package com.classictreerun;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.*;

/** Pure observation/estimate model. Times are conservative estimates, not remote crop checks. */
final class CropGrowth
{
    private static final DateTimeFormatter READY_TIME = DateTimeFormatter.ofPattern("EEE HH:mm");
    static final class Observation
    {
        final int raw; final long observedAt, readyAt, cycle; final boolean armed, seasonal;
        Observation(int raw,long observedAt,long readyAt,long cycle,boolean armed,boolean seasonal)
        { this.raw=raw;this.observedAt=observedAt;this.readyAt=readyAt;this.cycle=cycle;this.armed=armed;this.seasonal=seasonal; }
        String encode() { return raw+","+observedAt+","+readyAt+","+cycle+","+armed+","+seasonal; }
    }
    static final class Group
    {
        final int total, unknown, blocked, ready; final long readyAt; final boolean armed; final String fingerprint;
        Group(int total,int unknown,int blocked,int ready,long readyAt,boolean armed,String fingerprint)
        { this.total=total;this.unknown=unknown;this.blocked=blocked;this.ready=ready;this.readyAt=readyAt;this.armed=armed;this.fingerprint=fingerprint; }
        boolean ready(long now) { return total>0 && unknown==0 && blocked==0 && readyAt<=now; }
    }
    final Map<Patch,Observation> observations=new EnumMap<>(Patch.class);
    final CropReference reference;
    CropGrowth(CropReference reference) { this.reference=reference; }

    boolean observe(Patch patch,int raw,long now,boolean seasonal)
    {
        Observation old=observations.get(patch); CropReference.Crop crop=reference.decode(patch,raw);
        if (old!=null && old.raw==raw && old.seasonal==seasonal && now-old.observedAt<300
            && (old.readyAt==0 || old.readyAt>now || crop!=null && crop.ready())) { return false; }
        CropReference.Crop previous=old==null?null:reference.decode(patch,old.raw);
        boolean sameCycle=old!=null && previous!=null && crop!=null && previous.produce.equals(crop.produce);
        boolean regrowing=sameCycle && "HARVESTABLE".equals(crop.state) && "HARVESTABLE".equals(previous.state) && crop.stage<previous.stage && crop.tickMinutes>0;
        boolean plantedAgain=sameCycle && "GROWING".equals(crop.state) && (crop.stage<previous.stage || "HARVESTABLE".equals(previous.state) && crop.stage==0);
        long cycle=old!=null && sameCycle && !regrowing && !plantedAgain ? old.cycle : now;
        boolean stump=reference.isStump(patch,raw);
        boolean growing=crop!=null && crop.healthy() && !crop.ready() && !stump;
        boolean armed=growing || sameCycle && old!=null && old.armed && !plantedAgain;
        long readyAt=0;
        if (crop!=null && crop.healthy() && !stump)
        {
            if (crop.ready()) { readyAt=now; }
            else if (crop.tickMinutes>0)
            {
                long seconds=(long)crop.tickMinutes*60*(seasonal?1:5)/5;
                readyAt=now+crop.remaining()*seconds;
                if (sameCycle && !regrowing && !plantedAgain && old.readyAt>now) { readyAt=Math.min(readyAt,old.readyAt); }
            }
        }
        observations.put(patch,new Observation(raw,now,readyAt,cycle,armed,seasonal)); return true;
    }
    boolean isKnownReady(Patch patch,long now)
    {
        Observation value=observations.get(patch); if(value==null)return false;
        CropReference.Crop crop=reference.decode(patch,value.raw);
        return crop!=null && crop.healthy() && !reference.isStump(patch,value.raw) && value.readyAt>0 && value.readyAt<=now;
    }
    boolean isKnownGrowing(Patch patch,long now)
    {
        Observation value=observations.get(patch);if(value==null || value.observedAt>now || value.readyAt<=now)return false;
        CropReference.Crop crop=reference.decode(patch,value.raw);
        return crop!=null && crop.healthy() && "GROWING".equals(crop.state) && !crop.ready()
            && !reference.isStump(patch,value.raw);
    }
    Group group(Collection<Patch> patches,long now)
    {
        int unknown=0,blocked=0,ready=0;long end=0;boolean armed=false;StringBuilder fingerprint=new StringBuilder();
        Set<Patch> selected=EnumSet.noneOf(Patch.class);selected.addAll(patches);
        for(Patch patch:selected)
        {
            Observation value=observations.get(patch);
            if(value==null){unknown++;continue;}
            CropReference.Crop crop=reference.decode(patch,value.raw);
            if(crop==null){unknown++;continue;}
            if(!crop.healthy() || reference.isStump(patch,value.raw) || value.readyAt<=0){blocked++;continue;}
            if(value.readyAt<=now){ready++;}
            end=Math.max(end,value.readyAt);armed|=value.armed;
            fingerprint.append(patch.name()).append(':').append(value.cycle).append(';');
        }
        return new Group(selected.size(),unknown,blocked,ready,end,armed,fingerprint.toString());
    }
    String summary(Collection<Patch> patches,long now)
    {
        Group group=group(patches,now);
        if(group.total==0)return "Next run: no patches selected.";
        if(group.unknown>0 || group.blocked>0)return "Next full run: "+group.ready+"/"+group.total+" ready; "+group.unknown+" unknown, "+group.blocked+" need planting or care.";
        return group.ready(now)?"All "+group.total+" patches estimated ready for the next full run.":"Next full run estimated in "+duration(group.readyAt-now)+" ("+at(group.readyAt)+").";
    }
    String describe(Patch patch,long now)
    {
        Observation value=observations.get(patch);if(value==null)return "Growth unknown - visit this patch to track it.";
        CropReference.Crop crop=reference.decode(patch,value.raw);if(crop==null)return "Unknown crop state - no reliable timer.";
        if(!crop.planted())return "Empty / no crop observed. No growth timer.";
        if(reference.isStump(patch,value.raw))return crop.label()+": stump when last observed - clear and replant. No growth timer.";
        if(!crop.healthy())return crop.label()+": "+crop.state.toLowerCase(Locale.ROOT)+" - needs attention.";
        if(value.readyAt<=0)return crop.label()+": no reliable ready estimate.";
        if(crop.anima())
        {
            if(value.readyAt<=now)return crop.label()+": "+(crop.ready()?"expired when last observed; replace":"estimated expired; revisit to replace")+".";
            return crop.label()+": "+(crop.stage==crop.stages-2?"withering; ":"active when observed; ")+"replacement est. in "+duration(value.readyAt-now)+" ("+at(value.readyAt)+").";
        }
        if(value.readyAt<=now)return crop.label()+": "+(crop.ready()?"ready when last observed":"estimated ready; revisit to confirm health")+".";
        String prefix="HARVESTABLE".equals(crop.state)?"full regrowth est.":"est. ready";
        return crop.label()+": "+prefix+" in "+duration(value.readyAt-now)+" ("+at(value.readyAt)+").";
    }
    String save()
    {
        StringJoiner encoded=new StringJoiner(";"); observations.forEach((patch,value)->encoded.add(patch.name()+"="+value.encode())); return encoded.toString();
    }
    void load(String text)
    {
        observations.clear();if(text==null)return;
        for(String entry:text.split(";"))
        {
            try
            {
                String[] pair=entry.split("=",2),v=pair[1].split(",");if(v.length!=6)continue;
                long at=Long.parseLong(v[1]),ready=Long.parseLong(v[2]),cycle=Long.parseLong(v[3]);
                int raw=Integer.parseInt(v[0]);
                if(at<=0 || at>Instant.now().getEpochSecond()+86400 || ready<0 || ready>at+366L*86400 || cycle<=0 || cycle>at || raw<0 || raw>65535
                    || !("true".equals(v[4]) || "false".equals(v[4])) || !("true".equals(v[5]) || "false".equals(v[5])))continue;
                observations.put(Patch.valueOf(pair[0]),new Observation(raw,at,ready,cycle,Boolean.parseBoolean(v[4]),Boolean.parseBoolean(v[5])));
            }
            catch(IllegalArgumentException | ArrayIndexOutOfBoundsException ignored){/* Preserve other valid observations. */}
        }
    }
    static String duration(long seconds)
    {
        long minutes=Math.max(1,(seconds+59)/60);long days=minutes/1440,hours=minutes%1440/60,mins=minutes%60;
        return (days>0?days+"d ":"")+(hours>0?hours+"h ":"")+(mins>0?mins+"m":hours==0&&days==0?"1m":"");
    }
    static String at(long seconds){return READY_TIME.format(Instant.ofEpochSecond(seconds).atZone(ZoneId.systemDefault()));}
}
