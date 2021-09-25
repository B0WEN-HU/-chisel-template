
package mipi_tx_test

import chisel3._
import mipi_test.{
  MipiTxControlInterface,
  MipiTxVideoInterface
}
import chisel3.util._
import chisel3.experimental.BundleLiterals._


class MipiTxTest(mipiHRes: Int = 1920, mipiVRes: Int = 1080) extends Module {
  val io = IO(new Bundle {
    val mipiTxControl = Output(new MipiTxControlInterface)
    val mipiTxVideo   = Output(new MipiTxVideoInterface)
    val mipiRxVSync   = Input(Bool())
    val key1 = Input(Bool())
    val key2 = Input(Bool())
  })

  def posedge(x: Bool): Bool = {
    // detect positive edge and output for 1 cycle
    val xPos = RegNext(x)
    val xNeg = RegNext(xPos)
    (!xNeg) && xPos
  }

  // TX Control
  io.mipiTxControl := (new MipiTxControlInterface).Lit (
    _.RSTN      ->  true.B,
    _.DPHY_RSTN -> true.B,
    _.LANES     -> "b01".U
  )


  // Pixel Counter
  val hMax = 24 + 320 + 15 + 7
  val vMax = 38 + 1080 + 8 + 12
  val h = RegInit(0.U(log2Ceil(hMax).W))
  val v = RegInit(0.U(log2Ceil(vMax).W))


  val enteringNewLine  = h >= hMax.U
  val enteringNewFrame = v >= vMax.U
  // val posVSync = posedge(io.mipiRxVSync)
  //stall := Mux(posVSync, false.B, Mux(enteringNewFrame, true.B, stall))
//  h :=
//    Mux(posVSync, 0.U, Mux(enteringNewLine, 0.U, h + 1.U))
//  v :=
//    Mux(posVSync, 0.U, Mux(enteringNewLine && !enteringNewFrame, v + 1.U, v))
    h :=
      Mux(enteringNewLine, 0.U, h + 1.U)
    v :=
      Mux(enteringNewLine && !enteringNewFrame, v + 1.U, Mux(enteringNewFrame, 0.U, v))

  val source = Module(new DataSource)
  val tx     = Module(new TxAdaptor)
  source.io.h := h
  source.io.v := v
  tx.io.h := h
  tx.io.v := v

  source.io.dataOut <> tx.io.dataIn

  io.mipiTxVideo.DATA  := tx.io.dataOut.bits
  io.mipiTxVideo.HSYNC := tx.io.hSync
  io.mipiTxVideo.VSYNC := tx.io.vSync
  io.mipiTxVideo.VALID := tx.io.dataOut.valid
  io.mipiTxVideo.HRES  := mipiHRes.U(16.W)
  io.mipiTxVideo.TYPE  := "h2B".U // RAW10 data type
  io.mipiTxVideo.FRAME_MODE := false.B
  io.mipiTxVideo.VC    := 0.U
  io.mipiTxVideo.ULPS_CLK_ENTER := false.B
  io.mipiTxVideo.ULPS_CLK_EXIT  := false.B
  io.mipiTxVideo.ULPS_ENTER     := 0.U
  io.mipiTxVideo.ULPS_EXIT      := 0.U

}
