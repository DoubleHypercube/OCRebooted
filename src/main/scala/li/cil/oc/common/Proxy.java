package li.cil.oc.common;

import li.cil.oc.CreativeTab$;
import li.cil.oc.OpenComputers;
import li.cil.oc.Settings;
import li.cil.oc.api.API;
import li.cil.oc.integration.Mods;
import li.cil.oc.server.PacketHandler$;
import li.cil.oc.server.driver.Registry;
import li.cil.oc.server.fs.FileSystem$;
import li.cil.oc.server.machine.Machine;
import li.cil.oc.server.machine.luac.LuaStateFactory$;
import li.cil.oc.server.machine.luac.NativeLua52Architecture;
import li.cil.oc.server.machine.luac.NativeLua53Architecture;
import li.cil.oc.server.machine.luac.NativeLua54Architecture;
import li.cil.oc.server.machine.luaj.LuaJLuaArchitecture;
import li.cil.oc.server.network.Network$;
import li.cil.oc.common.init.OCItems$;
import li.cil.oc.common.nanomachines.Nanomachines$;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.util.zip.InflaterInputStream;

public class Proxy {
    private final IEventBus modBus;

    public Proxy(IEventBus modBus) {
        this.modBus = modBus;
    }

    public IEventBus modBus() {
        return modBus;
    }

    public void preInit() {
        OpenComputers.log().info("Initializing OpenComputers API.");

        li.cil.oc.api.CreativeTab.instance = CreativeTab$.MODULE$.MAIN().getKey();
        API.driver = Registry.INSTANCE;
        API.fileSystem = FileSystem$.MODULE$;
        API.items = OCItems$.MODULE$;
        API.machine = Machine.FACTORY;
        API.nanomachines = Nanomachines$.MODULE$;
        API.network = Network$.MODULE$;
        API.config = Settings.get().config();

        LuaStateFactory$ lua = LuaStateFactory$.MODULE$;
        if (lua.isAvailable()) {
            if (lua.include53()) li.cil.oc.api.Machine.add(NativeLua53Architecture.class);
            if (lua.include54()) li.cil.oc.api.Machine.add(NativeLua54Architecture.class);
            if (lua.include52()) li.cil.oc.api.Machine.add(NativeLua52Architecture.class);
        }
        if (lua.includeLuaJ()) li.cil.oc.api.Machine.add(LuaJLuaArchitecture.class);

        li.cil.oc.api.Machine.LuaArchitecture = Settings.get().forceLuaJ()
            ? LuaJLuaArchitecture.class
            : li.cil.oc.api.Machine.architectures().iterator().next();
    }

    public void init(FMLCommonSetupEvent event) {
        event.enqueueWork(() -> {
            PacketHandler$.MODULE$.serverHandler_$eq(li.cil.oc.server.PacketHandler$.MODULE$);
            OpenComputers.log().debug("Initializing mod integration.");
            Mods.init();
            API.isPowerEnabled = !Settings.get().ignorePower();
        });
    }

    public void registerPacket(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar(OpenComputers.ID()).versioned("1");
        registrar.playBidirectional(PacketPayload.TYPE, PacketPayload.STREAM_CODEC,
            (payload, context) -> {
                boolean clientbound = context.flow() == PacketFlow.CLIENTBOUND;
                byte[] data = payload.data;
                context.enqueueWork(() -> handlePacket(clientbound, data, context.player()));
            });
    }

    private static void handlePacket(boolean clientbound, byte[] data, Player player) {
        InputStream stream = null;
        try {
            PacketHandler handler = clientbound
                ? PacketHandler$.MODULE$.clientHandler()
                : PacketHandler$.MODULE$.serverHandler();
            if (handler != null) {
                stream = new ByteArrayInputStream(data);
                if (stream.read() != 0) stream = new InflaterInputStream(stream);
                handler.dispatch(handler.createParser(stream, player));
            }
        }
        catch (Throwable error) {
            OpenComputers.log().warn("Received a badly formatted packet.", error);
        }
        finally {
            if (stream != null) {
                try {
                    stream.close();
                }
                catch (IOException error) {
                    throw new UncheckedIOException(error);
                }
            }
        }
        if (player instanceof ServerPlayer serverPlayer) serverPlayer.resetLastActionTime();
    }
}
