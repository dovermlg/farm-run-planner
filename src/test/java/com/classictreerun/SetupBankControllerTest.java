package com.classictreerun;

import java.util.List;
import java.util.Map;
import org.junit.Test;
import static org.junit.Assert.*;

public class SetupBankControllerTest
{
    private static BankSetupRequest request(String name,boolean focus)
    {
        ConfigFixture f=InventoryFixtures.light();f.set("runName",name);
        return BankSetupRequest.create(f.config,SupplyPlan.forRoute(f.config,List.of(Patch.LUMBRIDGE_TREE),false,false),InventoryItemData.EMPTY,focus);
    }
    private static final class View implements SetupBankController.View
    {
        boolean open,enabled=true;String tag;int shows;BankSetupRequest last;
        public boolean available(){return enabled;}
        public boolean bankOpen(){return open;}
        public String activeTag(){return tag;}
        public void show(BankSetupRequest request){shows++;last=request;tag=SetupBankController.TAG;}
        public void removeOwnedView(){if(SetupBankController.TAG.equals(tag))tag=null;}
    }
    @Test public void defersUntilThePlayerOpensABankAndRetainsSetupAcrossClosingIt()
    {
        View view=new View();SetupBankController controller=new SetupBankController(view);
        controller.select(request("Herbs",true));assertTrue(controller.ready());assertEquals(0,view.shows);
        controller.bankOpened();assertEquals(0,view.shows);
        view.open=true;controller.bankOpened();assertEquals(1,view.shows);
        view.open=false;controller.suspend();assertTrue(controller.ready());assertNull(view.tag);
        view.open=true;controller.bankOpened();assertEquals(2,view.shows);
    }
    @Test public void updatesAnOwnedViewButDoesNotStealAnotherBankViewDuringProgressUpdates()
    {
        View view=new View();view.open=true;SetupBankController controller=new SetupBankController(view);
        controller.select(request("Herbs",false));assertEquals(1,view.shows);
        controller.select(request("Herbs",false));assertEquals(1,view.shows);
        controller.select(request("Trees",false));assertEquals(2,view.shows);
        view.tag="another-plugin-or-player-tag";controller.select(request("Next load",false));
        assertEquals(2,view.shows);assertEquals("another-plugin-or-player-tag",view.tag);
        controller.select(request("Next load",true));assertEquals(3,view.shows);
        view.tag=null;controller.select(request("Search view",false));assertEquals(3,view.shows);
    }
    @Test public void explicitClearOffAndEmptySetupReleaseSelectionWithoutClosingSomeoneElsesTag()
    {
        View view=new View();view.open=true;SetupBankController controller=new SetupBankController(view);
        controller.select(request("Herbs",true));view.tag="my-other-tag";controller.clear();
        assertFalse(controller.ready());assertEquals("my-other-tag",view.tag);
        controller.bankOpened();assertEquals(1,view.shows);
        controller.select(request("Herbs",true));ConfigFixture f=InventoryFixtures.light();f.set("supplyBankView","OFF");
        controller.select(BankSetupRequest.create(f.config,SupplyPlan.forRoute(f.config,List.of(Patch.LUMBRIDGE_TREE),false,false),InventoryItemData.EMPTY,false));
        assertFalse(controller.ready());assertNull(view.tag);
        controller.select(request("Herbs",true));f.set("supplyBankView","FILTER");
        controller.select(BankSetupRequest.create(f.config,new SupplyPlan(List.of(),List.of()),new InventoryItemData(Map.of()),false));
        assertFalse(controller.ready());assertNull(view.tag);
    }
    @Test public void disabledBankTagsNeverReportsSuccessAndReenablingRestoresTheSelectedSetup()
    {
        View view=new View();view.open=true;view.enabled=false;SetupBankController controller=new SetupBankController(view);
        controller.select(request("My trees",true));assertFalse(controller.ready());assertEquals(0,view.shows);
        controller.bankOpened();assertEquals(0,view.shows);
        view.enabled=true;controller.availabilityChanged();assertTrue(controller.ready());assertEquals(1,view.shows);
        view.enabled=false;controller.availabilityChanged();assertFalse(controller.ready());assertNull(view.tag);
        view.enabled=true;controller.availabilityChanged();assertTrue(controller.ready());assertEquals(2,view.shows);
    }
    @Test public void aDeferredBankCloseCannotRemoveASetupFromABankAlreadyReopened()
    {
        View view=new View();view.open=true;SetupBankController controller=new SetupBankController(view);
        controller.select(request("My trees",true));controller.bankClosed();
        assertEquals(SetupBankController.TAG,view.tag);assertTrue(controller.ready());
        view.open=false;controller.bankClosed();assertNull(view.tag);assertTrue(controller.ready());
        view.open=true;controller.bankOpened();assertEquals(SetupBankController.TAG,view.tag);
    }
}
