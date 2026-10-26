package li.cil.oc.server.command

import com.mojang.brigadier.builder.LiteralArgumentBuilder
import com.mojang.brigadier.arguments.BoolArgumentType
import li.cil.oc.Settings
import net.minecraft.commands.{CommandSourceStack, Commands}

object WirelessRenderingCommand {
  def register(root: LiteralArgumentBuilder[CommandSourceStack]): Unit = Seq("renderWirelessNetwork", "wlan").foreach { name =>
    root.then(Commands.literal(name)
      .requires(CommandHandler.canUse(_, 2))
      .executes(_ => execute(None))
      .then(Commands.argument("value", BoolArgumentType.bool())
        .executes(context => execute(Some(BoolArgumentType.getBool(context, "value"))))))
  }

  private def execute(requestedValue: Option[Boolean]): Int = {
    Settings.rTreeDebugRenderer = requestedValue.getOrElse(!Settings.rTreeDebugRenderer)
    1
  }
}
