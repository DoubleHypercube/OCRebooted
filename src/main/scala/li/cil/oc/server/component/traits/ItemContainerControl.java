package li.cil.oc.server.component.traits;

import li.cil.oc.api.Driver;
import li.cil.oc.api.machine.Arguments;
import li.cil.oc.api.machine.Callback;
import li.cil.oc.api.machine.Context;
import li.cil.oc.util.ExtendedArguments;
import li.cil.oc.util.InventoryUtils;
import li.cil.oc.util.ResultWrapper;
import net.neoforged.neoforge.items.IItemHandler;
import scala.Option;
import scala.runtime.BoxedUnit;

import java.util.function.Function;

public interface ItemContainerControl extends ContainerAware {
    @Callback(doc = "function(slot:number):number -- The size of an item inventory in the specified slot.")
    default Object[] getItemInventorySize(Context context, Arguments args) {
        int slot = new ExtendedArguments.ExtendedArguments(args).checkSlot(inventory(), 0);
        return withItemInventory(slot, itemInventory -> ResultWrapper.result(itemInventory.getSlots()));
    }

    @Callback(doc = "function(inventorySlot:number, slot:number[, count:number=64]):number -- Drops an item from the selected slot into the specified slot in the item inventory.")
    default Object[] dropIntoItemInventory(Context context, Arguments args) {
        var extended = new ExtendedArguments.ExtendedArguments(args);
        int inventorySlot = extended.checkSlot(inventory(), 0);
        return withItemInventory(inventorySlot, itemInventory -> {
            int slot = extended.checkSlot(itemInventory, 1);
            int count = extended.optItemCount(2, extended.optItemCount$default$2());
            int extracted = InventoryUtils.extractFromInventorySlot((stack, simulate) -> {
                InventoryUtils.insertIntoInventorySlot(stack, itemInventory, slot, 64, (Boolean) simulate);
                return BoxedUnit.UNIT;
            }, inventory(), null, selectedSlot(), count);
            return ResultWrapper.result(extracted);
        });
    }

    @Callback(doc = "function(inventorySlot:number, slot:number[, count:number=64]):number -- Sucks an item out of the specified slot in the item inventory.")
    default Object[] suckFromItemInventory(Context context, Arguments args) {
        var extended = new ExtendedArguments.ExtendedArguments(args);
        int inventorySlot = extended.checkSlot(inventory(), 0);
        return withItemInventory(inventorySlot, itemInventory -> {
            int slot = extended.checkSlot(itemInventory, 1);
            int count = extended.optItemCount(2, extended.optItemCount$default$2());
            int extracted = InventoryUtils.extractFromInventorySlot((stack, simulate) -> {
                InventoryUtils.insertIntoInventory(stack, InventoryUtils.asItemHandler(inventory()), 64,
                    (Boolean) simulate, Option.apply(insertionSlots()));
                return BoxedUnit.UNIT;
            }, itemInventory, slot, count);
            return ResultWrapper.result(extracted);
        });
    }

    private Object[] withItemInventory(int slot, Function<IItemHandler, Object[]> action) {
        IItemHandler itemInventory = Driver.itemHandlerFor(inventory().getItem(slot), fakePlayer());
        return itemInventory != null ? action.apply(itemInventory) : ResultWrapper.result(0, "no item inventory");
    }
}
