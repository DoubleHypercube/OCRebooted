package li.cil.oc.server.command

import com.mojang.brigadier.CommandDispatcher
import net.minecraft.commands.{CommandSourceStack, Commands}
import net.neoforged.neoforge.event.RegisterCommandsEvent

object CommandHandler {
  def canUse(source: CommandSourceStack, permissionLevel: Int): Boolean =
    source.hasPermission(permissionLevel) || source.getServer.isSingleplayer

  def onRegisterCommands(event: RegisterCommandsEvent): Unit = register(event.getDispatcher)

  private def register(dispatcher: CommandDispatcher[CommandSourceStack]): Unit = {
    val root = Commands.literal("oc")
    DebugNanomachinesCommand.register(root)
    LogNanomachinesCommand.register(root)
    NonDisassemblyAgreementCommand.register(root)
    WirelessRenderingCommand.register(root)
    SpawnComputerCommand.register(root)
    DebugWhitelistCommand.register(root)
    SendDebugMessageCommand.register(root)
    dispatcher.register(root)
  }
}
