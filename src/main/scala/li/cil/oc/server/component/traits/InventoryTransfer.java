package li.cil.oc.server.component.traits;

import li.cil.oc.Settings;
import li.cil.oc.api.machine.Arguments;
import li.cil.oc.api.machine.Callback;
import li.cil.oc.api.machine.Context;
import li.cil.oc.util.BlockPosition;
import li.cil.oc.util.ExtendedArguments;
import li.cil.oc.util.FluidUtils;
import li.cil.oc.util.InventoryUtils;
import li.cil.oc.util.ResultWrapper;
import net.minecraft.core.Direction;
import net.neoforged.neoforge.fluids.FluidType;
import net.neoforged.neoforge.items.IItemHandler;
import scala.Option;

public interface InventoryTransfer extends LevelAware, SideRestricted {
    // Empty on success, otherwise the reason the transfer was denied.
    Option<String> onTransferContents();

    @Callback(doc = "function(sourceSide:number, sinkSide:number[, count:number[, sourceSlot:number[, sinkSlot:number]]]):boolean -- Transfer some items between two inventories.")
    default Object[] transferItem(Context context, Arguments args) {
        Direction sourceSide = checkSideForAction(args, 0);
        BlockPosition sourcePos = position().offset(sourceSide);
        Direction sinkSide = checkSideForAction(args, 1);
        BlockPosition sinkPos = position().offset(sinkSide);
        ExtendedArguments.ExtendedArguments extended = new ExtendedArguments.ExtendedArguments(args);
        int count = extended.optItemCount(2, 64);

        Option<String> blocked = onTransferContents();
        if (blocked.isDefined()) return ResultWrapper.result(null, blocked.get());

        scala.Function0<Object> extractor;
        if (args.count() > 3) {
            Option<IItemHandler> source = InventoryUtils.inventoryAt(sourcePos, sourceSide.getOpposite());
            if (source.isEmpty()) throw new IllegalArgumentException("no inventory");
            int sourceSlot = extended.checkSlot(source.get(), 3);
            Option<IItemHandler> sink = InventoryUtils.inventoryAt(sinkPos, sinkSide.getOpposite());
            if (sink.isEmpty()) throw new IllegalArgumentException("no inventory");
            int sinkSlot = extended.optSlot(sink.get(), 4, -1);
            extractor = InventoryUtils.getTransferBetweenInventoriesSlotsAt(
                sourcePos, sourceSide.getOpposite(), sourceSlot, sinkPos,
                Option.apply(sinkSide.getOpposite()), sinkSlot < 0 ? Option.empty() : Option.apply(sinkSlot), count);
        }
        else {
            extractor = InventoryUtils.getTransferBetweenInventoriesAt(
                sourcePos, sourceSide.getOpposite(), sinkPos, Option.apply(sinkSide.getOpposite()), count);
        }
        return extractor == null ? ResultWrapper.result(null, "no inventory") : ResultWrapper.result(extractor.apply());
    }

    @Callback(doc = "function(sourceSide:number, sinkSide:number[, count:number [, sourceTank:number]]):boolean, number -- Transfer some fluid between two tanks. Returns operation result and filled amount")
    default Object[] transferFluid(Context context, Arguments args) {
        Direction sourceSide = checkSideForAction(args, 0);
        BlockPosition sourcePos = position().offset(sourceSide);
        Direction sinkSide = checkSideForAction(args, 1);
        BlockPosition sinkPos = position().offset(sinkSide);
        ExtendedArguments.ExtendedArguments extended = new ExtendedArguments.ExtendedArguments(args);
        int count = extended.optFluidCount(2, FluidType.BUCKET_VOLUME);
        int sourceTank = args.optInteger(3, -1);

        Option<String> blocked = onTransferContents();
        if (blocked.isDefined()) return ResultWrapper.result(null, blocked.get());

        int moved = FluidUtils.transferBetweenFluidHandlersAt(
            sourcePos, sourceSide.getOpposite(), sinkPos, sinkSide.getOpposite(), count, sourceTank);
        if (moved > 0) context.pause(moved / Settings.get().transposerFluidTransferRate());
        return ResultWrapper.result(moved > 0, moved);
    }
}
