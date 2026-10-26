package li.cil.oc.common.datacomponents

import com.mojang.serialization.Codec
import com.mojang.serialization.codecs.RecordCodecBuilder
import li.cil.oc.common.datacomponents.TextBufferContents.IntArray

import java.nio.{ByteBuffer, IntBuffer}

case class TextBufferContents(width: Int,
                              height: Int,
                              depth: Int,
                              foreground: Int,
                              foregroundIsPalette: Boolean,
                              background: Int,
                              backgroundIsPalette: Boolean,
                              buffer: List[String],
                              colors: IntArray)

object TextBufferContents {
  final class IntArray(values: Array[Int]) {
    private val array = values.clone()

    def this(array: Array[Array[Int]]) = this(array.flatten)

    def this(buf: IntBuffer) = {
      this(Array.fill[Int](buf.remaining()) { 0 })
      buf.get(array)
    }

    def this(buf: ByteBuffer) = {
      this(buf.asIntBuffer())
    }

    def intoByteBuffer(): ByteBuffer = {
      val bytes = ByteBuffer.allocate(array.length * 4)
      bytes.asIntBuffer().put(array)
      bytes
    }

    def sized(width: Int, height: Int): Array[Array[Int]] =
      Array.tabulate[Int](height, width) { (y, x) =>
        val index = y * width + x
        if (index < array.length) array(index) else 0
      }

    override def equals(other: Any): Boolean = other match {
      case that: IntArray => java.util.Arrays.equals(array, that.array)
      case _ => false
    }

    override def hashCode(): Int = java.util.Arrays.hashCode(array)
  }

  object IntArray {
    val CODEC: Codec[IntArray] = Codec.BYTE_BUFFER.xmap(buf => new IntArray(buf), array => array.intoByteBuffer())
  }

  val CODEC: Codec[TextBufferContents] = RecordCodecBuilder.create(inst => inst.group(
    ScalaCodec.INT.fieldOf("width").forGetter(_.width),
    ScalaCodec.INT.fieldOf("height").forGetter(_.height),
    ScalaCodec.INT.fieldOf("depth").forGetter(_.depth),
    ScalaCodec.INT.fieldOf("foreground").forGetter(_.foreground),
    ScalaCodec.BOOL.fieldOf("foregroundIsPalette").forGetter(_.foregroundIsPalette),
    ScalaCodec.INT.fieldOf("background").forGetter(_.background),
    ScalaCodec.BOOL.fieldOf("backgroundIsPalette").forGetter(_.backgroundIsPalette),
    ScalaCodec.list(Codec.STRING).fieldOf("buffer").forGetter(_.buffer),
    IntArray.CODEC.fieldOf("colors").forGetter(_.colors)
  ).apply(inst, TextBufferContents.apply _))
}
