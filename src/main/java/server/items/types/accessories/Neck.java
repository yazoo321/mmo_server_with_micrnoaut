package server.items.types.accessories;

import com.fasterxml.jackson.annotation.JsonTypeName;
import java.util.List;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import server.common.dto.Tag;
import server.items.equippable.model.EquippedItems;
import server.items.equippable.model.types.NeckSlot;
import server.items.model.Item;
import server.items.model.ItemConfig;
import server.items.model.ItemInstance;
import server.items.model.Stacking;
import server.items.types.ItemType;

@Data
@NoArgsConstructor
@JsonTypeName("NECK")
@EqualsAndHashCode(callSuper = false)
public class Neck extends Item {

    public Neck(
            String itemId,
            String itemName,
            List<Tag> tags,
            Stacking stacking,
            Integer value,
            ItemConfig config) {
        super(itemId, itemName, ItemType.NECK.getType(), tags, stacking, value, config);
    }

    @Override
    public EquippedItems createEquippedItem(String characterName, ItemInstance itemInstance) {
        return new NeckSlot(characterName, itemInstance);
    }
}