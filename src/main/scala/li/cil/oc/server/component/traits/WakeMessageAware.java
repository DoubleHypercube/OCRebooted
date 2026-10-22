package li.cil.oc.server.component.traits;

import li.cil.oc.api.machine.Arguments;
import li.cil.oc.api.machine.Callback;
import li.cil.oc.api.machine.Context;
import li.cil.oc.api.network.EnvironmentHost;
import li.cil.oc.api.network.Packet;
import li.cil.oc.common.datacomponents.OCComponents$;
import li.cil.oc.common.datacomponents.WakeMessage;
import li.cil.oc.util.ExtendedDataComponentHolder;
import li.cil.oc.util.ExtendedMutableDataComponentHolder;
import li.cil.oc.util.ResultWrapper;
import net.minecraft.core.component.DataComponentHolder;
import net.neoforged.neoforge.common.MutableDataComponentHolder;
import scala.Option;

import java.nio.charset.StandardCharsets;
import java.util.Objects;

public interface WakeMessageAware extends NetworkAware {
    Option<String> wakeMessage();

    void wakeMessage_$eq(Option<String> value);

    boolean wakeMessageFuzzy();

    void wakeMessageFuzzy_$eq(boolean value);

    @Callback(direct = true, doc = "function():string, boolean -- Get the current wake-up message.")
    default Object[] getWakeMessage(Context context, Arguments args) {
        return ResultWrapper.result(wakeMessage().isDefined() ? wakeMessage().get() : null, wakeMessageFuzzy());
    }

    @Callback(doc = "function(message:string[, fuzzy:boolean]):string -- Set the wake-up message and whether to ignore additional data/parameters.")
    default Object[] setWakeMessage(Context context, Arguments args) {
        Option<String> oldMessage = wakeMessage();
        boolean oldFuzzy = wakeMessageFuzzy();
        wakeMessage_$eq(args.optAny(0, null) == null ? Option.empty() : Option.apply(args.checkString(0)));
        wakeMessageFuzzy_$eq(args.optBoolean(1, wakeMessageFuzzy()));
        return ResultWrapper.result(oldMessage.isDefined() ? oldMessage.get() : null, oldFuzzy);
    }

    default boolean isPacketAccepted(Packet packet, double distance) {
        return true;
    }

    default void receivePacket(Packet packet, double distance, EnvironmentHost host) {
        if (Objects.equals(packet.source(), node().address())
            || packet.destination() != null && !Objects.equals(packet.destination(), node().address())) return;

        if (isPacketAccepted(packet, distance)) {
            Object[] data = packet.data();
            Object[] signal = new Object[4 + data.length];
            signal[0] = "modem_message";
            signal[1] = packet.source();
            signal[2] = packet.port();
            signal[3] = distance;
            System.arraycopy(data, 0, signal, 4, data.length);
            node().sendToReachable("computer.signal", signal);
        }

        // Closed ports do not prevent a saved wake message from starting a computer.
        Object[] data = packet.data();
        if (data != null && data.length > 0 && (wakeMessageFuzzy() || data.length == 1)) {
            String message = data[0] instanceof byte[] bytes ? new String(bytes, StandardCharsets.UTF_8)
                : data[0] instanceof String text ? text : null;
            if (message != null && wakeMessage().isDefined() && wakeMessage().get().equals(message)) {
                if (host instanceof Context context) context.start();
                else node().sendToNeighbors("computer.start");
            }
        }
    }

    default void loadWakeMessage(DataComponentHolder holder) {
        Option<WakeMessage> saved = new ExtendedDataComponentHolder(holder)
            .getComponent(OCComponents$.MODULE$.WAKE_MESSAGE().get());
        wakeMessage_$eq(saved.isDefined() ? Option.apply(saved.get().message()) : Option.empty());
        wakeMessageFuzzy_$eq(saved.isDefined() && saved.get().fuzzy());
    }

    default void saveWakeMessage(MutableDataComponentHolder holder) {
        Option<WakeMessage> saved = wakeMessage().isDefined()
            ? Option.apply(new WakeMessage(wakeMessage().get(), wakeMessageFuzzy())) : Option.empty();
        new ExtendedMutableDataComponentHolder(holder)
            .setComponent(OCComponents$.MODULE$.WAKE_MESSAGE().get(), saved);
    }
}
