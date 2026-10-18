package com.classictreerun;

import java.util.*;
import javax.swing.SwingUtilities;
import org.junit.Test;
import static org.junit.Assert.*;

public class GrowthMonitorTest
{
    @Test public void failedWritesSurviveACharacterSwitchAndAreRetriedTogether() throws Exception
    {
        SwingUtilities.invokeAndWait(()->{
            ConfigFixture f=new ConfigFixture();boolean[] fail={true};long[] now={1000};
            ClassicTreeRunPanel.Preferences storage=new ClassicTreeRunPanel.Preferences(){
                public String get(String key){return f.get(key);}
                public void set(String key,String value){if(fail[0])throw new IllegalStateException("disk failure");f.set(key,value);}
                public void update(Map<String,String> values){if(fail[0])throw new IllegalStateException("disk failure");f.update(values);}
            };
            GrowthMonitor monitor=new GrowthMonitor(f.config,storage,new ClassicTreeRunPanel(f.config,storage),message->{},new CropReference(),()->now[0],false);
            monitor.accept("a",Collections.singletonList(sample(Patch.TROLL_HERB,4,false)),false);
            monitor.accept("b",Collections.singletonList(sample(Patch.WEISS_HERB,4,false)),false);
            assertNull(f.get("growthDataV1.a"));assertNull(f.get("growthDataV1.b"));
            fail[0]=false;monitor.poll();
            assertTrue(f.get("growthDataV1.a").startsWith("TROLL_HERB="));
            assertTrue(f.get("growthDataV1.b").startsWith("WEISS_HERB="));monitor.dispose();
        });
    }
    @Test public void replantingTheSameFruitAfterAStumpCanCompleteTheSelectedStop() throws Exception
    {
        SwingUtilities.invokeAndWait(()->{
            ConfigFixture f=new ConfigFixture();preset(f,"Fruit","CATHERBY_FRUIT");f.set("autoCompleteReplanted","true");
            GrowthMonitor monitor=new GrowthMonitor(f.config,f,new ClassicTreeRunPanel(f.config,f),message->{},new CropReference(),()->1000,false);
            monitor.accept("a",Collections.singletonList(sample(Patch.CATHERBY_FRUIT,33,false)),false);
            monitor.accept("a",Collections.singletonList(sample(Patch.CATHERBY_FRUIT,8,true)),false);
            assertEquals("CATHERBY_FRUIT",f.get(ClassicTreeRunPanel.PROGRESS_KEY));monitor.dispose();
        });
    }
    @Test public void editingASavedPresetInvalidatesItsReadinessSelection() throws Exception
    {
        SwingUtilities.invokeAndWait(()->{
            ConfigFixture f=new ConfigFixture();preset(f,"Herbs","TROLL_HERB,WEISS_HERB");long[] now={1000};List<String> notices=new ArrayList<>();
            GrowthMonitor monitor=new GrowthMonitor(f.config,f,new ClassicTreeRunPanel(f.config,f),notices::add,new CropReference(),()->now[0],false);
            monitor.accept("a",Collections.singletonList(sample(Patch.TROLL_HERB,4,false)),false);monitor.poll();
            now[0]=6000;monitor.poll();assertTrue(notices.isEmpty());
            preset(f,"Herbs","TROLL_HERB");monitor.poll();assertEquals(1,notices.size());
            assertTrue(notices.get(0).contains("all 1 patches"));monitor.dispose();
        });
    }
    private static GrowthSampler.Sample sample(Patch patch,int raw,boolean transition){return new GrowthSampler.Sample(patch,raw,transition);}
    private static void preset(ConfigFixture f,String name,String patches)
    {f.set("selectionV2",patches);new PresetLibrary(f).save(name,f.config);}

    @Test public void mixedRunNotifiesOnlyWhenLatestCropIsReadyAndOnlyOnce() throws Exception
    {
        SwingUtilities.invokeAndWait(()->{
            ConfigFixture f=new ConfigFixture();preset(f,"Herbs and magic","TROLL_HERB,LUMBRIDGE_TREE");
            long[] now={1000};List<String> notices=new ArrayList<>();
            GrowthMonitor monitor=new GrowthMonitor(f.config,f,new ClassicTreeRunPanel(f.config,f),notices::add,new CropReference(),()->now[0],false);
            monitor.accept("character-a",Arrays.asList(sample(Patch.TROLL_HERB,4,false),sample(Patch.LUMBRIDGE_TREE,48,false)),false);
            now[0]+=80*60;monitor.poll();assertTrue(notices.isEmpty());
            now[0]=1000+480*60;monitor.poll();assertEquals(1,notices.size());assertTrue(notices.get(0).contains("all 2 patches"));
            monitor.poll();assertEquals(1,notices.size());monitor.dispose();
            GrowthMonitor reopened=new GrowthMonitor(f.config,f,new ClassicTreeRunPanel(f.config,f),notices::add,new CropReference(),()->now[0],false);
            reopened.accept("character-a",Collections.emptyList(),false);reopened.poll();assertEquals(1,notices.size());reopened.dispose();
        });
    }
    @Test public void aGrowingTimerRestoredAfterRestartCanNotifyWhenYouReturn() throws Exception
    {
        SwingUtilities.invokeAndWait(()->{
            ConfigFixture f=new ConfigFixture();preset(f,"Herbs","TROLL_HERB");long[] now={1000};List<String> notices=new ArrayList<>();
            GrowthMonitor first=new GrowthMonitor(f.config,f,new ClassicTreeRunPanel(f.config,f),notices::add,new CropReference(),()->now[0],false);
            first.accept("a",Collections.singletonList(sample(Patch.TROLL_HERB,4,false)),false);first.dispose();now[0]=6000;
            GrowthMonitor next=new GrowthMonitor(f.config,f,new ClassicTreeRunPanel(f.config,f),notices::add,new CropReference(),()->now[0],false);
            next.poll();assertTrue(notices.isEmpty());next.accept("a",Collections.emptyList(),false);next.poll();assertEquals(1,notices.size());next.dispose();
        });
    }
    @Test public void unknownOrUnhealthyMembersPreventFullRunNotification() throws Exception
    {
        SwingUtilities.invokeAndWait(()->{
            ConfigFixture f=new ConfigFixture();preset(f,"Herbs","TROLL_HERB,WEISS_HERB");long[] now={1000};List<String> notices=new ArrayList<>();
            GrowthMonitor monitor=new GrowthMonitor(f.config,f,new ClassicTreeRunPanel(f.config,f),notices::add,new CropReference(),()->now[0],false);
            monitor.accept("a",Collections.singletonList(sample(Patch.TROLL_HERB,4,false)),false);now[0]=6000;monitor.poll();assertTrue(notices.isEmpty());
            monitor.accept("a",Collections.singletonList(sample(Patch.WEISS_HERB,128,false)),false);monitor.poll();assertTrue(notices.isEmpty());
            monitor.accept("a",Collections.singletonList(sample(Patch.WEISS_HERB,0,true)),false);monitor.poll();assertTrue(notices.isEmpty());monitor.dispose();
        });
    }
    @Test public void merelySeeingMatureCropsDoesNotSpamButReplantingArmsNewCycles() throws Exception
    {
        SwingUtilities.invokeAndWait(()->{
            ConfigFixture f=new ConfigFixture();preset(f,"Herbs","TROLL_HERB");long[] now={1000};List<String> notices=new ArrayList<>();
            GrowthMonitor monitor=new GrowthMonitor(f.config,f,new ClassicTreeRunPanel(f.config,f),notices::add,new CropReference(),()->now[0],false);
            monitor.accept("a",Collections.singletonList(sample(Patch.TROLL_HERB,8,false)),false);monitor.poll();assertTrue(notices.isEmpty());
            now[0]=1100;monitor.accept("a",Collections.singletonList(sample(Patch.TROLL_HERB,4,true)),false);now[0]=6000;monitor.poll();assertEquals(1,notices.size());
            monitor.accept("a",Collections.singletonList(sample(Patch.TROLL_HERB,8,true)),false);monitor.poll();assertEquals(1,notices.size());
            now[0]=6100;monitor.accept("a",Collections.singletonList(sample(Patch.TROLL_HERB,4,true)),false);now[0]=11000;monitor.poll();assertEquals(2,notices.size());monitor.dispose();
        });
    }
    @Test public void localCharacterContextsDoNotMixTheirCropsOrNotificationMarkers() throws Exception
    {
        SwingUtilities.invokeAndWait(()->{
            ConfigFixture f=new ConfigFixture();preset(f,"Herbs","TROLL_HERB,WEISS_HERB");long[] now={1000};List<String> notices=new ArrayList<>();
            GrowthMonitor monitor=new GrowthMonitor(f.config,f,new ClassicTreeRunPanel(f.config,f),notices::add,new CropReference(),()->now[0],false);
            monitor.accept("a",Collections.singletonList(sample(Patch.TROLL_HERB,4,false)),false);
            monitor.accept("b",Collections.singletonList(sample(Patch.WEISS_HERB,4,false)),false);now[0]=6000;monitor.poll();assertTrue(notices.isEmpty());
            monitor.accept("a",Collections.singletonList(sample(Patch.WEISS_HERB,8,false)),false);monitor.poll();assertEquals(1,notices.size());
            monitor.accept("b",Collections.emptyList(),false);monitor.poll();assertEquals(1,notices.size());monitor.dispose();
            assertNotEquals(GrowthMonitor.characterKey(123),GrowthMonitor.characterKey(124));assertEquals(64,GrowthMonitor.characterKey(123).length());
            assertNotEquals(GrowthMonitor.characterKey(123),GrowthMonitor.characterKey(123,true,false));
            assertNotEquals(GrowthMonitor.characterKey(123,true,false),GrowthMonitor.characterKey(123,false,true));
        });
    }
    @Test public void disabledNotificationsAndLoadingSessionDoNotSendAlerts() throws Exception
    {
        SwingUtilities.invokeAndWait(()->{
            ConfigFixture f=new ConfigFixture();preset(f,"Herbs","TROLL_HERB");long[] now={1000};List<String> notices=new ArrayList<>();
            GrowthMonitor monitor=new GrowthMonitor(f.config,f,new ClassicTreeRunPanel(f.config,f),notices::add,new CropReference(),()->now[0],false);
            monitor.accept("a",Collections.singletonList(sample(Patch.TROLL_HERB,4,false)),false);now[0]=6000;
            monitor.allowNotifications(false);monitor.poll();assertTrue(notices.isEmpty());
            monitor.allowNotifications(true);f.set("notifyWhenReady","false");monitor.poll();assertTrue(notices.isEmpty());
            f.set("notifyWhenReady","true");f.set("trackGrowth","false");monitor.poll();assertTrue(notices.isEmpty());
            f.set("trackGrowth","true");monitor.poll();assertEquals(1,notices.size());monitor.dispose();
        });
    }
    @Test public void deletedSavedRunsNoLongerNotify() throws Exception
    {
        SwingUtilities.invokeAndWait(()->{
            ConfigFixture f=new ConfigFixture();preset(f,"Herbs","TROLL_HERB");long[] now={1000};List<String> notices=new ArrayList<>();
            GrowthMonitor monitor=new GrowthMonitor(f.config,f,new ClassicTreeRunPanel(f.config,f),notices::add,new CropReference(),()->now[0],false);
            monitor.accept("a",Collections.singletonList(sample(Patch.TROLL_HERB,4,false)),false);new PresetLibrary(f).delete("Herbs");
            now[0]=6000;monitor.poll();assertTrue(notices.isEmpty());monitor.dispose();
        });
    }
    @Test public void autoCompletionNeedsConfirmedReplantOfSelectedPatchAndOptIn() throws Exception
    {
        SwingUtilities.invokeAndWait(()->{
            ConfigFixture f=new ConfigFixture();preset(f,"Herbs","TROLL_HERB");long[] now={1000};
            GrowthMonitor monitor=new GrowthMonitor(f.config,f,new ClassicTreeRunPanel(f.config,f),message->{},new CropReference(),()->now[0],false);
            monitor.accept("a",Collections.singletonList(sample(Patch.TROLL_HERB,0,false)),false);
            monitor.accept("a",Collections.singletonList(sample(Patch.TROLL_HERB,4,true)),false);assertNull(f.get(ClassicTreeRunPanel.PROGRESS_KEY));
            f.set("autoCompleteReplanted","true");monitor.accept("a",Arrays.asList(sample(Patch.TROLL_HERB,8,true),sample(Patch.WEISS_HERB,0,false)),false);
            now[0]=1100;monitor.accept("a",Arrays.asList(sample(Patch.TROLL_HERB,4,true),sample(Patch.WEISS_HERB,4,true)),false);
            assertEquals("TROLL_HERB",f.get(ClassicTreeRunPanel.PROGRESS_KEY));monitor.dispose();
        });
    }
    @Test public void replantLikeStateOnArrivalNeverCompletesAStop() throws Exception
    {
        SwingUtilities.invokeAndWait(()->{
            ConfigFixture f=new ConfigFixture();preset(f,"Herbs","TROLL_HERB");f.set("autoCompleteReplanted","true");
            GrowthMonitor monitor=new GrowthMonitor(f.config,f,new ClassicTreeRunPanel(f.config,f),message->{},new CropReference(),()->1000,false);
            monitor.accept("a",Collections.singletonList(sample(Patch.TROLL_HERB,8,false)),false);
            monitor.accept("a",Collections.singletonList(sample(Patch.TROLL_HERB,4,false)),false);assertNull(f.get(ClassicTreeRunPanel.PROGRESS_KEY));monitor.dispose();
        });
    }
    @Test public void localWriteFailureIsRetriedAndCannotDuplicateNotification() throws Exception
    {
        SwingUtilities.invokeAndWait(()->{
            ConfigFixture f=new ConfigFixture();preset(f,"Herbs","TROLL_HERB");boolean[] fail={true};long[] now={1000};List<String> notices=new ArrayList<>();
            ClassicTreeRunPanel.Preferences storage=new ClassicTreeRunPanel.Preferences(){
                public String get(String key){return f.get(key);}
                public void set(String key,String value){if(fail[0])throw new IllegalStateException("disk failure");f.set(key,value);}
                public void update(Map<String,String> values){if(fail[0])throw new IllegalStateException("disk failure");f.update(values);}
            };
            GrowthMonitor monitor=new GrowthMonitor(f.config,storage,new ClassicTreeRunPanel(f.config,storage),notices::add,new CropReference(),()->now[0],false);
            monitor.accept("a",Collections.singletonList(sample(Patch.TROLL_HERB,4,false)),false);assertNull(f.get("growthDataV1.a"));
            fail[0]=false;monitor.poll();assertNotNull(f.get("growthDataV1.a"));
            now[0]=6000;monitor.poll();monitor.poll();assertEquals(1,notices.size());monitor.dispose();
        });
    }
}
