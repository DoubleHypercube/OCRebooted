package li.cil.oc.server.component;

import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.commands.CommandSource;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import net.minecraft.server.commands.SayCommand;
import net.minecraft.server.commands.SeedCommand;
import net.minecraft.server.commands.TimeCommand;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;
import org.junit.Test;

import static org.junit.Assert.*;

/** Checks the vanilla command predicates behind the debug card's position-zero error. */
public class DebugCardCommandPermissionTest {
    private static CommandSourceStack source(int permission) {
        return new CommandSourceStack(CommandSource.NULL, Vec3.ZERO, Vec2.ZERO,
            null, permission, "[OpenComputers]", Component.literal("[OpenComputers]"), null, null);
    }

    @Test
    public void integratedSeedDoesNotProveOperatorPermissions() {
        var dispatcher = new CommandDispatcher<CommandSourceStack>();
        SeedCommand.register(dispatcher, false);
        SayCommand.register(dispatcher);
        TimeCommand.register(dispatcher);

        var unprivileged = source(0);
        assertFalse(dispatcher.parse("seed", unprivileged).getContext().getNodes().isEmpty());
        assertTrue(dispatcher.parse("say test", unprivileged).getContext().getNodes().isEmpty());
        assertTrue(dispatcher.parse("time query daytime", unprivileged).getContext().getNodes().isEmpty());

        var operator = source(2);
        var say = dispatcher.parse("say test", operator);
        assertFalse(say.getContext().getNodes().isEmpty());
        assertFalse(say.getReader().canRead());
        assertTrue(say.getExceptions().isEmpty());
        var time = dispatcher.parse("time query daytime", operator);
        assertFalse(time.getContext().getNodes().isEmpty());
        assertFalse(time.getReader().canRead());
        assertTrue(time.getExceptions().isEmpty());
    }

    @Test
    public void dedicatedSeedRequiresOperatorPermissions() {
        var dispatcher = new CommandDispatcher<CommandSourceStack>();
        SeedCommand.register(dispatcher, true);
        assertTrue(dispatcher.parse("seed", source(0)).getContext().getNodes().isEmpty());
        assertFalse(dispatcher.parse("seed", source(2)).getContext().getNodes().isEmpty());
    }

    @Test
    public void legacySinglePlayerAccessDoesNotGrantDedicatedFakePlayerCommands() {
        var dispatcher = new CommandDispatcher<CommandSourceStack>();
        SayCommand.register(dispatcher);

        int singlePlayerPermission = DebugCardCommands.commandPermission(true, 0);
        assertFalse(dispatcher.parse("say test", source(singlePlayerPermission)).getContext().getNodes().isEmpty());

        int dedicatedPermission = DebugCardCommands.commandPermission(false, 0);
        assertTrue(dispatcher.parse("say test", source(dedicatedPermission)).getContext().getNodes().isEmpty());
    }
}
