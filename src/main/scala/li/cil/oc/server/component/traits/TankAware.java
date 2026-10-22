package li.cil.oc.server.component.traits;

import li.cil.oc.api.internal.MultiTank;
import li.cil.oc.api.machine.Arguments;
import li.cil.oc.util.ExtendedArguments;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.IFluidTank;
import scala.Option;

public interface TankAware {
    MultiTank tank();

    int selectedTank();

    void selectedTank_$eq(int value);

    default int optTank(Arguments args, int index) {
        return args.count() > 0 && args.checkAny(0) != null
            ? new ExtendedArguments.ExtendedArguments(args).checkTank(tank(), 0) : selectedTank();
    }

    default Option<IFluidTank> getTank(int index) {
        return Option.apply(tank().getFluidTank(index));
    }

    default Option<FluidStack> fluidInTank(int index) {
        Option<IFluidTank> selected = getTank(index);
        return selected.isDefined() ? Option.apply(selected.get().getFluid()) : Option.empty();
    }

    default boolean haveSameFluidType(FluidStack first, FluidStack second) {
        return first.isFluidEqual(second);
    }
}
