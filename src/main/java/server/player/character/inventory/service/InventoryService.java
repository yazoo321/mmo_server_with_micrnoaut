package server.player.character.inventory.service;

import lombok.extern.slf4j.Slf4j;
import server.common.dto.Location;
import server.common.dto.Location2D;
import server.items.dropped.model.DroppedItem;
import server.items.dto.Item;
import server.items.service.ItemService;
import server.player.character.inventory.model.CharacterItem;
import server.player.character.inventory.model.Inventory;
import server.player.character.inventory.model.exceptions.InventoryException;
import server.player.character.inventory.repository.InventoryRepository;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Slf4j
@Singleton
public class InventoryService {

    @Inject
    InventoryRepository inventoryRepository;

    @Inject
    ItemService itemService;

    public Inventory pickupItem(String characterName, String droppedItemId) throws InventoryException {
        // Could add additional validations.
        // For example add unique ID to player items and match it with dropped ID
        // There can be occasions where when laggy
        // you could add item more than once / to multiple users

        DroppedItem droppedItem = itemService.getDroppedItemById(droppedItemId);
        Item item = droppedItem.getItem();
        Inventory inventory = inventoryRepository.getCharacterInventory(characterName);

        // check for example if inventory is full
        List<CharacterItem> items = inventory.getCharacterItems();
        Location2D position = getNextAvailableSlot(inventory);

        CharacterItem newCharacterItem = new CharacterItem(characterName, item, position);

        items.add(newCharacterItem);

        // delete the dropped item first (this is a blocking get) to prevent duplication
        itemService.deleteDroppedItem(droppedItemId);

        inventoryRepository.updateInventoryItems(characterName, items);

        return inventory;
    }

    public DroppedItem dropItem(String characterName, Location2D inventoryLocation, Location location)
            throws InventoryException {
        Inventory inventory = inventoryRepository.getCharacterInventory(characterName);
        CharacterItem characterItem = getItemAtLocation(inventoryLocation, inventory);

        if (characterItem == null) {
            return null;
        }

        List<CharacterItem> itemsList = inventory.getCharacterItems();
        itemsList.remove(characterItem);
        inventoryRepository.updateInventoryItems(characterName, itemsList);

        // TODO: if dropItem fails, we need to revert the removal of item from inventory.
        return itemService.dropItem(characterItem.getItem().getItemId(), location);
    }

    public Inventory getInventory(String characterName) {
        return inventoryRepository.getCharacterInventory(characterName);
    }

    public void sellItem() {
        // TODO: later
    }

    public void equipItem() {
        // validate item and target
        // TODO: later
    }

    public void pickupGold() {
        // TODO: later
    }

    public Inventory createInventoryForNewCharacter(String characterName) {
        Inventory inventory = new Inventory();

        inventory.setCharacterName(characterName);
        inventory.setCharacterItems(new ArrayList<>());
        inventory.setGold(0);
        inventory.setMaxSize(new Location2D(10, 10));

        return inventoryRepository.insert(inventory);
    }


    private Location2D getNextAvailableSlot(Inventory inventory) {
        // Implement this as per your requirement, based on position for example.
        Location2D maxSize = inventory.getMaxSize();
        List<CharacterItem> items = inventory.getCharacterItems();
        int[][] invArr = new int[maxSize.getX()][maxSize.getY()];

        items.forEach(i -> {
            Location2D loc = i.getLocation();
            invArr[loc.getX()][loc.getY()] = 1;
        });

        for (int x = 0; x < maxSize.getX(); x++) {
            for (int y = 0; y < maxSize.getY(); y++) {
                if (invArr[x][y] != 1) {
                    return new Location2D(x,y);
                }
            }
        }

        return null;
    }

    public void clearAllDataForCharacter(String characterName) {
        inventoryRepository.deleteAllInventoryDataForCharacter(characterName);
    }

    private CharacterItem getItemAtLocation(Location2D location, Inventory inventory) {
        List<CharacterItem> items = inventory.getCharacterItems();

        Optional<CharacterItem> item = items.stream().filter(i -> i.getLocation().equals(location)).findFirst();

        if (item.isPresent()) {
            return item.get();
        } else {
            log.warn("item was not found in the inventory");
            return null;
        }
    }
}