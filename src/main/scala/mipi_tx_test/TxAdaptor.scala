
package mipi_tx_test

import chisel3._
import chisel3.util._

// 1920 * 1080 60Hz
// 25MHz pixel clock
// RAW10 6 * 10 bits per DATA
// H    24   320    15   7
// V    38   1080   8    12
//     ___._______.____. blanking
//        |display|    |____

class TxAdaptor extends Module {
  val io = IO(new Bundle {
    val h       = Input(UInt(log2Ceil(24 + 320 + 15 + 7).W))
    val v       = Input(UInt(log2Ceil(38 + 1080 + 8 + 12).W))
    val dataIn  = Flipped(DecoupledIO(UInt(64.W)))
    val dataOut = ValidIO(UInt(64.W))
    val hSync   = Output(Bool())
    val vSync   = Output(Bool())
  })

  val data = io.dataIn.bits


  val vSync = Mux(io.v < (38 + 1080 + 8).U, true.B, false.B)
  val hSync = Mux(io.h < (24 + 320 + 12).U, true.B, false.B) && vSync
  val hDisplay =
    Mux(io.h >= (24).U && io.h < (320 + 24).U, true.B, false.B)
  val vDisplay =
    Mux(io.v >= (38).U && io.v < (38 + 1080).U, true.B, false.B)
  val display = vDisplay && hDisplay

  io.dataIn.ready  := display
  io.dataOut.bits  := Mux(io.dataIn.valid, data, 0.U)
  io.dataOut.valid := display
  io.hSync := hSync
  io.vSync := vSync
}
