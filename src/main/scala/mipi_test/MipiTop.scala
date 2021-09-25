// Test efinix FPGA mipi interface

package mipi_test

import chisel3._
import chisel3.util._
import chisel3.experimental.BundleLiterals._

class MipiTop(mipiHRes: Int = 1920, mipiVRes: Int = 1080) extends MultiIOModule {
//  val io = IO(new Bundle {
//    val mipiRxControl = Output(new MipiRxControlInterface)
//    val mipiRxVideo   = Input(new MipiRxVideoInterface)
//    val mipiTxControl = Output(new MipiTxControlInterface)
//    val mipiTxVideo   = Output(new MipiTxVideoInterface)
//    val key1 = Input(Bool())
//    val key2 = Input(Bool())
//  })

  val mipi = IO(new Bundle {
    // Rx Control
    val inst1C = Output(new MipiRxControlInterface)
    // Rx Video
    val inst1V = Input(new MipiRxVideoInterface)
    // Tx Control
    val inst2C = Output(new MipiTxControlInterface)
    // Tx Video
    val inst2V = Output(new MipiTxVideoInterface)
  })
  val key1 = IO(Input(Bool()))
  val key2 = IO(Input(Bool()))


  val tVFrontPorch  = mipiHRes * 1
  val tVBackPorch   = mipiHRes * 1
  val tSyncPulse    = mipiHRes * 1
  val tHFrontPorch  = 4
  val tHBackPorch   = 4

  def posedge(x: Bool): Bool = {
    // detect positive edge and output for 1 cycle
    val xPos = RegNext(x)
    val xNeg = RegNext(xPos)
    (!xNeg) && xPos
  }

  def negedge(x: Bool): Bool = {
    // detect negative edge and output for 1 cycle
    val xNeg = RegNext(x)
    val xPos = RegNext(xNeg)
    (!xNeg) && xPos
  }

  def timer(cycles: Int, posEdgeSet: Bool): Bool = {
    // 2 cycles of delay from set pull up to start counting
    // minimum counting number is 3
    val target  = cycles - 2 // compensation of delay
    val counter = RegInit(0.U(log2Ceil(cycles).W))
    val valid   = !posEdgeSet && (counter === target.U)
    val counterNext =
      Mux(posEdgeSet, 0.U, Mux(valid, counter, counter + 1.U))

    counter := counterNext
    valid
  }

  def delayPulse(pulse: Bool, cycles: Int): Bool = {
    val counter = Reg(UInt(log2Ceil(cycles).W))
    val enable  = RegInit(false.B)

    val pulseDelayed = RegInit(false.B)

    when(pulse) {
      enable  := true.B
      counter := 0.U

      pulseDelayed := false.B
    }.elsewhen(counter === (cycles - 2).U) {
      enable  := false.B
      counter := 0.U

      pulseDelayed   := true.B
    }.elsewhen(enable) {
      counter := counter + 1.U
      pulseDelayed := false.B
    }.otherwise {
      pulseDelayed := false.B
    }

    pulseDelayed
  }

  def delayPulse(pulse: Bool, cycles: UInt): Bool = {
    val counter = Reg(UInt(log2Ceil(mipiHRes * 2).W))
    val enable  = RegInit(false.B)

    val pulseDelayed = RegInit(false.B)

    when(pulse) {
      enable  := true.B
      counter := 0.U

      pulseDelayed := false.B
    }.elsewhen(counter === (cycles - 2.U)) {
      enable  := false.B
      counter := 0.U

      pulseDelayed   := true.B
    }.elsewhen(enable) {
      counter := counter + 1.U
      pulseDelayed := false.B
    }.otherwise {
      pulseDelayed := false.B
    }

    pulseDelayed
  }

  // Pixel counter
  val rxHCnt = RegInit(0.U(log2Ceil(mipiHRes).W))
  val rxVCnt = RegInit(0.U(log2Ceil(mipiVRes).W))
  val txHCnt = RegInit(0.U(log2Ceil(mipiHRes).W))
  val txVCnt = RegInit(0.U(log2Ceil(mipiVRes).W))

  // RX Control
  mipi.inst1C := (new MipiRxControlInterface).Lit (
    _.RSTN      -> true.B,
    _.DPHY_RSTN -> true.B,
    _.VC_ENA    -> "b0001".U, // with only virtual channel 0
    _.LANES     -> "b01".U,   // 2 lanes
    _.CLEAR     -> false.B    // do not care about clear, set to 0
  )

  // TX Control
  mipi.inst2C := (new MipiTxControlInterface).Lit (
    _.RSTN      ->  true.B,
    _.DPHY_RSTN -> true.B,
    _.LANES     -> "b01".U
  )


  // RX Sync
  val rxHSync0 = mipi.inst1V.HSYNC(0).asBool
  val rxVSync0 = mipi.inst1V.VSYNC(0).asBool


  val rxHSync  = RegNext(rxHSync0)
  val rxVSync  = RegNext(rxVSync0)

  val rxVSyncPosedge = posedge(rxVSync0) // 2 lanes
  val rxVSyncNegedge = negedge(rxVSync0)
  val rxHSyncPosedge = posedge(rxHSync0) // 2 lanes
  val rxHSyncNegedge = negedge(rxHSync0)

  val rxVideoValid   = RegNext(mipi.inst1V.VALID)
  val rxVideoData    = RegNext(mipi.inst1V.DATA) // type should be RAW10



  // line buffer
  val adaptor = Module(new Buffer4To6)
  val buffer  = Module(new LineBuffer(mipiHRes * 2))

  adaptor.io.clear := rxHSyncPosedge
  adaptor.io.dataIn.valid := rxVideoValid
  adaptor.io.dataIn.bits  := rxVideoData

  buffer.io.clear := rxHSyncPosedge
  buffer.io.dataIn.bits  := adaptor.io.dataOut.bits
  buffer.io.dataIn.valid := adaptor.io.dataOut.valid



  // RX Video
  rxHCnt :=
    Mux(rxHSyncPosedge,
      0.U,
      Mux(rxVideoValid, rxHCnt + 4.U, rxHCnt))  // RAW10 Input 4 pixels per DATA
  rxVCnt :=
    Mux(rxVSyncPosedge,
      0.U,
      Mux(rxHSyncNegedge, rxVCnt + 1.U, rxVCnt))




  // TX Video
  val txHStart = rxHSyncNegedge
  val rxHMax   = RegEnable(Mux(mipiHRes.U > rxHCnt, mipiHRes.U, rxHCnt), txHStart)
  val txHEnd   = txHCnt >= rxHMax //|| !buffer.io.dataOut.valid
  val txVStart = rxVSyncPosedge
  val txHSync  = RegInit(false.B)
  val txVSync  = RegInit(false.B)
  val txVideoValid = RegInit(false.B)

  txHCnt := //Mux(txHStart, 0.U, Mux(txVideoValid, txHCnt + 6.U, txHCnt))
    Mux(txVideoValid, txHCnt + 6.U, 0.U)
  txVCnt :=
    Mux(rxVSyncPosedge, 0.U, Mux(txHStart, txVCnt + 1.U, txVCnt))

  txHSync :=
    Mux(txHStart, true.B, Mux(delayPulse(txHEnd, 3), false.B, txHSync))
  txVideoValid :=
    Mux(delayPulse(txHStart, 3), true.B, Mux(txHEnd, false.B, txVideoValid))

  txVSync :=
    Mux(
      delayPulse(rxVSyncPosedge, rxHMax + 4.U), true.B,
      Mux(
        delayPulse(rxVSyncNegedge, rxHMax + 4.U), false.B,
        txVSync))

  val txVideoData = buffer.io.dataOut.bits
  buffer.io.dataOut.ready := txVideoValid

  /////////////////////////// txVSync

  mipi.inst2V.DATA  := txVideoData
  mipi.inst2V.HSYNC := txHSync
  mipi.inst2V.VSYNC := txVSync
  mipi.inst2V.VALID := txVideoValid
  mipi.inst2V.HRES  := mipiHRes.U(16.W)
  mipi.inst2V.TYPE  := "h2B".U // RAW10 data type
  mipi.inst2V.FRAME_MODE := false.B
  mipi.inst2V.VC    := 0.U
  mipi.inst2V.ULPS_CLK_ENTER := false.B
  mipi.inst2V.ULPS_CLK_EXIT  := false.B
  mipi.inst2V.ULPS_ENTER     := 0.U
  mipi.inst2V.ULPS_EXIT      := 0.U

}
