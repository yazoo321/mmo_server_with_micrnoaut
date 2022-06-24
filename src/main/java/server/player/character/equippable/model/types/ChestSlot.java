package server.player.character.equippable.model.types;

import com.fasterxml.jackson.annotation.JsonTypeName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import server.items.types.ItemType;
import server.player.character.equippable.model.EquippedItems;

@Data
@JsonTypeName("HELM")
@EqualsAndHashCode(callSuper=false)
public class ChestSlot extends EquippedItems {

    public ChestSlot(String characterName, String characterItemId) {
        super(characterName, characterItemId, ItemType.CHEST.getType());
    }
}