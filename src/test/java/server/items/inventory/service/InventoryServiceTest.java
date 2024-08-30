package server.items.inventory.service;

import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import io.reactivex.rxjava3.core.Single;
import jakarta.inject.Inject;

import java.util.Arrays;
import java.util.List;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import server.common.dto.Location;
import server.common.dto.Location2D;
import server.items.helper.ItemTestHelper;
import server.items.inventory.model.CharacterItem;
import server.items.inventory.model.Inventory;
import server.items.inventory.model.exceptions.InventoryException;
import server.items.inventory.model.response.GenericInventoryData;
import server.items.inventory.repository.InventoryRepository;
import server.items.model.DroppedItem;
import server.items.model.ItemInstance;
import server.items.service.ItemService;
import server.items.types.ItemType;
import server.items.types.weapons.Weapon;

@MicronautTest
public class InventoryServiceTest {

    @Inject InventoryService inventoryService;

    @Inject ItemService itemService;

    @Inject ItemTestHelper itemTestHelper;

    InventoryRepository inventoryRepository = Mockito.mock(InventoryRepository.class);


    private static final String ACTOR_ID = "test_character";

    @BeforeEach
    void cleanDb() {
        itemTestHelper.deleteAllItemData();
        itemTestHelper.prepareInventory(ACTOR_ID);
    }

    @Test
    void testPickupItemWillPickUpItemAndSetToInventory() {
        // Given
        Location location = new Location("map", 1, 1, 1);
        Weapon weapon = (Weapon) itemTestHelper.createAndInsertItem(ItemType.WEAPON.getType());
        DroppedItem droppedItem = itemTestHelper.createAndInsertDroppedItem(location, weapon);
        GenericInventoryData request = new GenericInventoryData();
        request.setActorId(ACTOR_ID);
        request.setItemInstanceId(droppedItem.getItemInstanceId());

        // When
        inventoryService.pickupItem(request).blockingGet();

        // Then
        // TODO: Make test not rely on service call
        Inventory inventory = inventoryService.getInventory(ACTOR_ID).blockingGet();
        List<CharacterItem> items = inventory.getCharacterItems();
        Assertions.assertThat(items.size()).isEqualTo(1);

        String actualInstanceId = items.get(0).getItemInstance().getItemInstanceId();

        Assertions.assertThat(actualInstanceId)
                .isEqualTo(droppedItem.getItemInstance().getItemInstanceId());
    }

    @Test
    void dropItemWillRemoveItFromInventoryAndAddDroppedItemObject() {
        // Given
        Weapon weapon = (Weapon) itemTestHelper.createAndInsertItem(ItemType.WEAPON.getType());
        Location location = new Location("map", 1, 1, 1);
        DroppedItem droppedItem = itemTestHelper.createAndInsertDroppedItem(location, weapon);
        GenericInventoryData request = new GenericInventoryData();
        request.setActorId(ACTOR_ID);
        request.setItemInstanceId(droppedItem.getItemInstanceId());
        // TODO: make test not rely on service call
        inventoryService.pickupItem(request).blockingGet();

        // When
        inventoryService
                .dropItem(ACTOR_ID, droppedItem.getItemInstanceId(), location)
                .blockingGet();

        // Then
        List<DroppedItem> itemList = itemService.getItemsInMap(location).blockingGet();

        Assertions.assertThat(itemList.size()).isEqualTo(1);
        Assertions.assertThat(itemList.get(0).getItemInstance())
                .isEqualTo(droppedItem.getItemInstance());
    }

    @Test
    void getAvailableSlotWillReturnCorrectValuesForInventorySize() {
        // Given
        Inventory inventory = inventoryService.getInventory(ACTOR_ID).blockingGet();
        // max size of 2x2 would allow 4 slots. ensure we can add 4 items and anything else will
        // throw.
        inventory.setMaxSize(new Location2D(2, 2));
        inventoryService.updateInventoryMaxSize(inventory).blockingGet();

        Weapon weapon = (Weapon) itemTestHelper.createAndInsertItem(ItemType.WEAPON.getType());

        // When
        for (int i = 0; i < 4; i++) {
            Location location = new Location("map", 1, 1, 1);
            DroppedItem droppedItem = itemTestHelper.createAndInsertDroppedItem(location, weapon);
            GenericInventoryData request = new GenericInventoryData();
            request.setActorId(ACTOR_ID);
            request.setItemInstanceId(droppedItem.getItemInstanceId());
            inventoryService.pickupItem(request).blockingGet();
        }

        // Then
        Location location = new Location("map", 1, 1, 1);
        DroppedItem droppedItem = itemTestHelper.createAndInsertDroppedItem(location, weapon);
        // next `pickup` will error out
        GenericInventoryData request = new GenericInventoryData();
        request.setActorId(ACTOR_ID);
        request.setItemInstanceId(droppedItem.getItemInstanceId());
        org.junit.jupiter.api.Assertions.assertThrows(
                InventoryException.class, () -> inventoryService.pickupItem(request).blockingGet());
    }

    @Test
    public void moveItemToEmptyLocation() {
        ItemService itemService = Mockito.mock(ItemService.class);
        InventoryService inventoryService = new InventoryService();
        inventoryService.inventoryRepository = inventoryRepository;
        inventoryService.itemService = itemService;

        String itemInstanceId = "item123";
        Location2D to = new Location2D(1, 1);

        Inventory inventory = new Inventory();
        inventory.setActorId(ACTOR_ID);
        CharacterItem item = new CharacterItem(ACTOR_ID, new Location2D(0, 0),
                new ItemInstance("itemId", itemInstanceId, null));
        inventory.setCharacterItems(List.of(item));

        Mockito.when(inventoryRepository.getCharacterInventory(ACTOR_ID)).thenReturn(Single.just(inventory));
        Mockito.when(inventoryRepository.updateInventoryItems(Mockito.anyString(), Mockito.anyList()))
                .thenReturn(Single.just(inventory.getCharacterItems()));

        inventoryService.moveItem(ACTOR_ID, itemInstanceId, to)
                .test()
                .assertValue(inv ->
                        inv.getItemByInstanceId(itemInstanceId).getLocation().equals(to));
    }
    @Test
    public void moveNonExistentItem() {
        InventoryRepository inventoryRepository = Mockito.mock(InventoryRepository.class);
        ItemService itemService = Mockito.mock(ItemService.class);
        InventoryService inventoryService = new InventoryService();
        inventoryService.inventoryRepository = inventoryRepository;
        inventoryService.itemService = itemService;

        String itemInstanceId = "nonexistentItem";
        Location2D to = new Location2D(1, 1);

        Inventory inventory = new Inventory();
        inventory.setActorId(ACTOR_ID);
        CharacterItem item = new CharacterItem(ACTOR_ID, new Location2D(0, 0), new ItemInstance());
        inventory.setCharacterItems(Arrays.asList(item));

        Mockito.when(inventoryRepository.getCharacterInventory(ACTOR_ID)).thenReturn(Single.just(inventory));

        inventoryService.moveItem(ACTOR_ID, itemInstanceId, to)
                .test()
                .assertError(InventoryException.class);
    }
}
