package li.cil.oc.server.component.traits;

import li.cil.oc.api.machine.Arguments;
import li.cil.oc.util.ExtendedArguments;
import li.cil.oc.util.StackOption;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import scala.collection.immutable.IndexedSeq;
import scala.collection.immutable.Vector;
import scala.collection.immutable.Vector$;

public interface ContainerAware {
    Player fakePlayer();

    Container inventory();

    int selectedSlot();

    void selectedSlot_$eq(int value);

    default IndexedSeq<Object> insertionSlots() {
        scala.collection.mutable.Builder<Object, Vector<Object>> slots = Vector$.MODULE$.newBuilder();
        for (int index = selectedSlot(); index < inventory().getContainerSize(); index++) slots.addOne(index);
        for (int index = 0; index < selectedSlot(); index++) slots.addOne(index);
        return slots.result();
    }

    default int optSlot(Arguments args, int index) {
        return args.count() > 0 && args.checkAny(0) != null
            ? new ExtendedArguments.ExtendedArguments(args).checkSlot(inventory(), 0) : selectedSlot();
    }

    default StackOption stackInSlot(int slot) {
        return StackOption.apply(inventory().getItem(slot));
    }
}
