package com.classictreerun;

import java.util.*;
import java.util.function.*;
import javax.swing.Timer;

/** EDT-owned readiness records and one notification per saved preset/crop cycle. */
final class GrowthMonitor
{
    private final ClassicTreeRunConfig config;
    private final ClassicTreeRunPanel.Preferences preferences;
    private final ClassicTreeRunPanel panel;
    private final Consumer<String> notify;
    private final LongSupplier clock;
    private final CropReference reference;
    private final Timer timer;
    private CropGrowth growth;
    private String characterKey;
    private boolean allowNotifications, disposed, dirty;
    private String pendingProgress;
    private final Map<String,String> pendingWrites=new LinkedHashMap<>();
    private String presetSnapshot;
    private final Map<String,List<Patch>> presetPatches=new LinkedHashMap<>();

    GrowthMonitor(ClassicTreeRunConfig config,ClassicTreeRunPanel.Preferences preferences,ClassicTreeRunPanel panel,
        Consumer<String> notify,CropReference reference,LongSupplier clock,boolean startTimer)
    {
        this.config=config;this.preferences=preferences;this.panel=panel;this.notify=notify;this.reference=reference;this.clock=clock;
        panel.setProgressEdited(this::discardPendingCompletion);
        timer=new Timer(10000,event->poll());if(startTimer)timer.start();
    }
    private void discardPendingCompletion()
    {
        // A successful manual change supersedes failed automatic progress, but keeps crop observations.
        pendingProgress=null;pendingWrites.remove(ClassicTreeRunPanel.PROGRESS_KEY);
    }
    void allowNotifications(boolean allow){allowNotifications=allow;}
    void accept(String character,List<GrowthSampler.Sample> samples,boolean seasonal)
    {
        if(disposed || !config.trackGrowth())return;
        if(!character.equals(characterKey))
        {
            flush(); // Queue failed writes under the old character before changing context.
            characterKey=character;growth=new CropGrowth(reference);
            growth.load(pendingWrites.containsKey(dataKey())?pendingWrites.get(dataKey()):preferences.get(dataKey()));panel.setGrowth(growth);
        }
        allowNotifications=true;long now=clock.getAsLong();boolean changed=false,completionChanged=false;
        RouteState progress=null;
        Set<Patch> active=config.autoCompleteReplanted() && !samples.isEmpty()?new HashSet<>(RouteState.active(config)):Collections.emptySet();
        for(GrowthSampler.Sample sample:samples)
        {
            CropGrowth.Observation old=growth.observations.get(sample.patch);
            CropReference.Crop before=old==null?null:reference.decode(sample.patch,old.raw),after=reference.decode(sample.patch,sample.raw);
            if(config.autoCompleteReplanted() && sample.transition && active.contains(sample.patch) && before!=null && after!=null
                && after.healthy() && "GROWING".equals(after.state) && after.stage==0
                && (!before.planted() || !before.healthy() || before.ready() || reference.isStump(sample.patch,old.raw) || !before.produce.equals(after.produce)))
            {
                if(progress==null)
                {
                    progress=new RouteState();
                    progress.load(pendingWrites.containsKey(ClassicTreeRunPanel.PROGRESS_KEY)?pendingWrites.get(ClassicTreeRunPanel.PROGRESS_KEY):preferences.get(ClassicTreeRunPanel.PROGRESS_KEY));
                }
                if(!progress.isDone(sample.patch)){progress.setDone(sample.patch,true);completionChanged=true;}
            }
            changed|=growth.observe(sample.patch,sample.raw,now,seasonal);
        }
        dirty|=changed;
        if(completionChanged)pendingProgress=progress.save();
        flush();
        if(completionChanged)panel.reload();else if(changed)panel.refreshGrowth(now);
    }
    private boolean flush()
    {
        if(dirty){pendingWrites.put(dataKey(),growth.save());dirty=false;}
        if(pendingProgress!=null){pendingWrites.put(ClassicTreeRunPanel.PROGRESS_KEY,pendingProgress);pendingProgress=null;}
        if(pendingWrites.isEmpty())return true;
        try{preferences.update(pendingWrites);pendingWrites.clear();return true;}
        catch(IllegalStateException error){panel.setStatus(error.getMessage());return false;}
    }
    void poll()
    {
        if(disposed || !config.trackGrowth() || growth==null)return;
        boolean saved=flush();long now=clock.getAsLong();panel.refreshGrowth(now);
        if(!saved || !allowNotifications || !config.notifyWhenReady())return;
        try
        {
            String snapshot=preferences.get(PresetLibrary.KEY);
            if(!Objects.equals(snapshot,presetSnapshot))
            {
                PresetLibrary library=new PresetLibrary(preferences);presetPatches.clear();
                for(String name:library.names())presetPatches.put(name,library.patches(name));
                presetSnapshot=snapshot;
            }
            for(Map.Entry<String,List<Patch>> preset:presetPatches.entrySet())
            {
                String name=preset.getKey();CropGrowth.Group group=growth.group(preset.getValue(),now);
                if(!group.ready(now) || !group.armed)continue;
                String marker="growthNoticeV1."+characterKey+"."+digest(name),fingerprint=digest(group.fingerprint);
                if(fingerprint.equals(preferences.get(marker)))continue;
                // Persist first: restarting or an OS notification failure must not cause repeated alerts.
                preferences.set(marker,fingerprint);
                notify.accept("Farm Run Planner: "+name+" - all "+group.total+" patches are estimated ready for your next full run. Check crop health when you visit.");
            }
        }
        catch(IllegalStateException error){panel.setStatus("Couldn't update local readiness reminders: "+error.getMessage());}
    }
    private String dataKey(){return "growthDataV1."+characterKey;}
    void dispose(){flush();disposed=true;timer.stop();}
    static String characterKey(long accountHash){return characterKey(accountHash,false,false);}
    static String characterKey(long accountHash,boolean seasonal,boolean deadman)
    {return digest(Long.toUnsignedString(accountHash)+":"+(deadman?"deadman":seasonal?"seasonal":"normal"));}
    static String digest(String value) { return Digests.sha256(value); }
}
