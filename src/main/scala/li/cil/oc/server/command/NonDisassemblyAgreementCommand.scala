package li.cil.oc.server.command

import com.mojang.brigadier.builder.LiteralArgumentBuilder
import com.mojang.brigadier.arguments.BoolArgumentType
import li.cil.oc.Settings
import net.minecraft.commands.{CommandSourceStack, Commands}
import net.minecraft.core.component.DataComponents
import net.minecraft.world.item.component.CustomData

object NonDisassemblyAgreementCommand {
  private val DataKey = Settings.namespace + "undisassemblable"

  def register(root: LiteralArgumentBuilder[CommandSourceStack]): Unit = {
    root.then(Commands.literal("preventDisassembling")
      .requires(CommandHandler.canUse(_, 2))
      .executes(context => execute(context.getSource, None))
      .then(Commands.argument("value", BoolArgumentType.bool())
        .executes(context => execute(context.getSource, Some(BoolArgumentType.getBool(context, "value"))))))
  }

  private def execute(source: CommandSourceStack, requestedValue: Option[Boolean]): Int = {
    val stack = source.getPlayerOrException.getMainHandItem
    if (stack.isEmpty) return 0

    val currentValue = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag().getBoolean(DataKey)
    val preventDisassembly = requestedValue.getOrElse(!currentValue)
    CustomData.update(DataComponents.CUSTOM_DATA, stack, tag => {
      if (preventDisassembly) tag.putBoolean(DataKey, true)
      else tag.remove(DataKey)
    })
    if (stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).isEmpty)
      stack.remove(DataComponents.CUSTOM_DATA)
    1
  }
}
