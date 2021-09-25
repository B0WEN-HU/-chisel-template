
package mipi_tx_test

import chisel3._
import chisel3.util._

class DataSource extends Module {
  val io = IO(new Bundle {
    val h = Input(UInt(log2Ceil(24 + 320 + 15 + 7).W))
    val v = Input(UInt(log2Ceil(38 + 1080 + 8 + 12).W))
    val dataOut = DecoupledIO(UInt(64.W))
  })

  val evenLine = io.v(0) === 0.U(1.W)

  val displayH   = io.h - 24.U
  val colorIndex = displayH(7, 5)

  val maxColor = 1023.U(10.W)
  val minColor = 0.U(10.W)
  val r = Mux(colorIndex(2).asBool(), maxColor, minColor)
  val g = Mux(colorIndex(1).asBool(), maxColor, minColor)
  val b = Mux(colorIndex(0).asBool(), maxColor, minColor)

  val data =
    Mux(evenLine,
      Cat(b, g, b, g, b, g),
      Cat(g, r, g, r, g, r))

  io.dataOut.bits := data
  io.dataOut.valid := true.B
}
