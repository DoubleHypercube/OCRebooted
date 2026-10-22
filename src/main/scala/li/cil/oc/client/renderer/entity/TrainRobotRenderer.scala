package li.cil.oc.client.renderer.entity

import com.mojang.blaze3d.vertex.PoseStack
import li.cil.oc.client.Textures
import li.cil.oc.client.renderer.tileentity.RobotRenderer
import li.cil.oc.common.entity.TrainRobot
import net.minecraft.client.renderer.MultiBufferSource
import net.minecraft.client.renderer.entity.{EntityRenderer, EntityRendererProvider}
import net.minecraft.util.Mth
import com.mojang.math.Axis

import java.lang.reflect.Method

class TrainRobotRenderer(ctx: EntityRendererProvider.Context) extends EntityRenderer[TrainRobot](ctx) {
  shadowRadius = 0f

  private val createHatRenderer: Option[Method] = try {
    val rendererClass = Class.forName("li.cil.oc.client.renderer.entity.CreateTrainRobotHatRenderer")
    val method = rendererClass.getMethod(
      "render",
      classOf[TrainRobot],
      classOf[PoseStack],
      classOf[MultiBufferSource],
      java.lang.Integer.TYPE
    )
    Some(method)
  }
  catch {
    case _: Throwable => None
  }

  override def render(entity: TrainRobot,
                      yaw: Float,
                      partialTick: Float,
                      pose: PoseStack,
                      buffer: MultiBufferSource,
                      light: Int): Unit = {
    pose.pushPose()
    val renderYaw = Mth.rotLerp(partialTick, entity.yRotO, entity.getYRot)
    pose.mulPose(Axis.YP.rotationDegrees(renderYaw))
    pose.translate(-0.5, 0, -0.5)
    RobotRenderer.renderChassis(
      pose,
      buffer,
      light,
      offset = entity.tickCount + partialTick,
      isRunningOverride = true,
      flag = entity.flag
    )
    createHatRenderer.foreach { method =>
      try method.invoke(null, entity, pose, buffer, Int.box(light))
      catch { case _: Throwable => () }
    }
    pose.popPose()
    super.render(entity, yaw, partialTick, pose, buffer, light)
  }

  override def getTextureLocation(entity: TrainRobot) = Textures.Model.Robot
}
