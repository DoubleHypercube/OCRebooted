package li.cil.oc.server.command

import com.mojang.brigadier.builder.LiteralArgumentBuilder
import com.mojang.brigadier.arguments.StringArgumentType
import li.cil.oc.api.Network
import li.cil.oc.server.network.DebugNetwork
import net.minecraft.commands.{CommandSourceStack, Commands}

object SendDebugMessageCommand {
  def register(root: LiteralArgumentBuilder[CommandSourceStack]): Unit = {
    root.then(Commands.literal("sendDebugMessage")
      .requires(CommandHandler.canUse(_, 2))
      .then(Commands.argument("destination", StringArgumentType.word())
        .executes(context => execute(context.getSource, StringArgumentType.getString(context, "destination"), Array.empty))
        .then(Commands.argument("message", StringArgumentType.greedyString())
          .executes(context => execute(
            context.getSource,
            StringArgumentType.getString(context, "destination"),
            StringArgumentType.getString(context, "message").split(" ").map(_.asInstanceOf[AnyRef]))))))
  }

  private def execute(source: CommandSourceStack, destination: String, message: Array[AnyRef]): Int = {
    DebugNetwork.getEndpoint(destination).foreach { endpoint =>
      val packet = Network.newPacket(source.getTextName, destination, 0, message)
      endpoint.receivePacket(packet)
    }
    1
  }
}
