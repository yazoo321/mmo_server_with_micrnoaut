package server.items.consumable;

import com.fasterxml.jackson.annotation.JsonTypeName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import server.common.dto.Tag;
import server.items.dto.Item;
import server.items.dto.Stacking;
import server.items.types.ItemType;

import java.util.List;

@Data
@NoArgsConstructor
@JsonTypeName("consumable")
@EqualsAndHashCode(callSuper=false)
public class Consumable extends Item {

    public Consumable(String itemId, String itemName, List<Tag> tags, Stacking stacking, Integer value) {
        super(itemId, itemName, ItemType.CONSUMABLE.getType(), tags, stacking, value);
    }
}