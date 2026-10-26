import li.cil.oc.util.PackedColor
import org.junit.runner.RunWith
import org.scalatest.funspec.AnyFunSpec
import org.scalatest.matchers.should.Matchers
import org.scalatestplus.junit.JUnitRunner

@RunWith(classOf[JUnitRunner])
class PackedColor16BitTest extends AnyFunSpec with Matchers {
  describe("16-bit screen colors") {
    it("keeps independent foreground and background RGB565 values") {
      val format = new PackedColor.SixteenBitFormat
      val packed = PackedColor.pack(PackedColor.Color(0xFF0000), PackedColor.Color(0x00FF00), format)

      PackedColor.extractForeground(packed) should be > 0xFF
      PackedColor.extractBackground(packed) should be > 0xFF
      PackedColor.unpackForeground(packed, format) shouldBe 0xFF0000
      PackedColor.unpackBackground(packed, format) shouldBe 0x00FF00
    }
  }
}
