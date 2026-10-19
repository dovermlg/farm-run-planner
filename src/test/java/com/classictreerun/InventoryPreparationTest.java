package com.classictreerun;

import java.util.List;
import java.util.ArrayList;
import javax.swing.SwingUtilities;
import org.junit.Test;
import static org.junit.Assert.*;

public class InventoryPreparationTest
{
    @Test public void lateItemDataFailureCannotClearAPlanThatAlreadyHasItsData() throws Exception
    {
        SwingUtilities.invokeAndWait(()->{
            ConfigFixture f=InventoryFixtures.light();
            List<Patch> route=List.of(Patch.LUMBRIDGE_TREE);
            InventoryPreparation preparation=new InventoryPreparation(f.config,()->{ });
            List<InventoryPreparation.ItemRequest> requests=new ArrayList<>();
            preparation.setRequest(requests::add);
            try
            {
                preparation.update(route);
                f.set("cropTree","MAGIC");
                preparation.update(route);
                assertEquals(2,requests.size());
                preparation.accept(requests.get(1),InventoryFixtures.data(f.config,route));
                preparation.update(route);
                preparation.failed(requests.get(0));
                assertNull("An obsolete response must not clear the current plan",preparation.error());
            }
            finally { preparation.dispose(); }
        });
    }

    @Test public void changingCropsIgnoresOldResponsesAndClearsThePreviousError() throws Exception
    {
        SwingUtilities.invokeAndWait(()->{
            ConfigFixture f=InventoryFixtures.light();List<Patch> route=List.of(Patch.LUMBRIDGE_TREE);
            int[] changed={0};InventoryPreparation preparation=new InventoryPreparation(f.config,()->changed[0]++);
            List<InventoryPreparation.ItemRequest> requests=new ArrayList<>();preparation.setRequest(requests::add);
            try
            {
                preparation.update(route);InventoryItemData oak=InventoryFixtures.data(f.config,route);
                f.set("cropTree","MAGIC");preparation.update(route);
                preparation.accept(requests.get(0),oak);preparation.failed(requests.get(0));
                assertEquals(0,changed[0]);assertFalse(preparation.data().covers(requests.get(0).items));
                assertNull(preparation.error());
                preparation.failed(requests.get(1));assertNotNull(preparation.error());
                preparation.update(route);assertEquals("A failure must not cause a retry loop",2,requests.size());
                f.set("cropTree","YEW");preparation.update(route);
                assertEquals(3,requests.size());assertNull("A new request clears the old error",preparation.error());
                preparation.failed(requests.get(1));assertNull(preparation.error());
                preparation.accept(requests.get(2),InventoryFixtures.data(f.config,route));
                assertTrue(preparation.data().covers(requests.get(2).items));assertEquals(2,changed[0]);
            }
            finally { preparation.dispose(); }
        });
    }

    @Test public void identicalItemRetryAndDisposalRejectOlderCallbacks() throws Exception
    {
        SwingUtilities.invokeAndWait(()->{
            ConfigFixture f=InventoryFixtures.light();List<Patch> route=List.of(Patch.LUMBRIDGE_TREE);
            int[] changed={0};InventoryPreparation preparation=new InventoryPreparation(f.config,()->changed[0]++);
            List<InventoryPreparation.ItemRequest> requests=new ArrayList<>();preparation.setRequest(requests::add);
            try
            {
                preparation.update(route);InventoryItemData data=InventoryFixtures.data(f.config,route);
                f.set("loadPlanning","false");preparation.update(route);
                preparation.failed(requests.get(0));assertNull(preparation.error());
                f.set("loadPlanning","true");preparation.update(route);
                assertEquals(requests.get(0).items,requests.get(1).items);
                assertNotSame(requests.get(0),requests.get(1));
                preparation.failed(requests.get(0));preparation.accept(requests.get(0),data);
                assertEquals(0,changed[0]);assertNull(preparation.error());
                preparation.accept(requests.get(1),data);assertEquals(1,changed[0]);
                preparation.failed(requests.get(1));assertNull(preparation.error());
                preparation.dispose();preparation.accept(requests.get(1),data);preparation.failed(requests.get(1));
                assertEquals(1,changed[0]);assertNull(preparation.error());
            }
            finally { preparation.dispose(); }
        });
    }

    @Test public void returningToCachedCropDataInvalidatesAnOutstandingRequest() throws Exception
    {
        SwingUtilities.invokeAndWait(()->{
            ConfigFixture f=InventoryFixtures.light();List<Patch> route=List.of(Patch.LUMBRIDGE_TREE);
            int[] changed={0};InventoryPreparation preparation=new InventoryPreparation(f.config,()->changed[0]++);
            List<InventoryPreparation.ItemRequest> requests=new ArrayList<>();preparation.setRequest(requests::add);
            try
            {
                preparation.update(route);preparation.accept(requests.get(0),InventoryFixtures.data(f.config,route));
                f.set("cropTree","MAGIC");preparation.update(route);
                InventoryItemData magic=InventoryFixtures.data(f.config,route);
                f.set("cropTree","OAK");preparation.update(route);
                preparation.failed(requests.get(1));preparation.accept(requests.get(1),magic);
                assertEquals(2,requests.size());assertEquals(1,changed[0]);assertNull(preparation.error());
                assertFalse(preparation.data().covers(requests.get(1).items));
            }
            finally { preparation.dispose(); }
        });
    }
}
