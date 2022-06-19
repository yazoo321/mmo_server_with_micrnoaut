package server.items.armour;

import com.fasterxml.jackson.annotation.JsonTypeName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import server.common.dto.Tag;
import server.items.model.Item;
import server.items.model.ItemConfig;
import server.items.model.Stacking;
import server.items.types.ItemType;
import server.player.character.equippable.SlotType;
import server.player.character.equippable.model.EquippableSlots;
import server.player.character.equippable.model.types.ChestSlot;
import server.player.character.equippable.model.types.HelmSlot;

import java.util.List;

@Data
@NoArgsConstructor
@JsonTypeName("HELM")
@EqualsAndHashCode(callSuper=false)
public class Helm extends Item {

    public Helm(String itemId, String itemName, List<Tag> tags, Stacking stacking, Integer value, ItemConfig config) {
        super(itemId, itemName, ItemType.HELM.getType(), tags, stacking, value, config);
    }

    public List<SlotType> getValidSlotTypes() {
        return List.of(SlotType.CHEST);
    }

    @Override
    public EquippableSlots createEquippableSlot(String characterName) {
        return new HelmSlot(characterName);
    }
}
