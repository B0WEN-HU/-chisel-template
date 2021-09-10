// Test efinix FPGA mipi interface

package mipi_test

import chisel3._
import chisel3.util._
import chisel3.experimental.BundleLiterals._

class MipiTop(mipiHRes: Int = 1920, mipiVRes: Int = 1080) extends Module {
  val io = IO(new Bundle {
    val mipiRxControl = Output(new MipiRxControlInterface)
    val mipiRxVideo   = Input(new MipiRxVideoInterface)
    val mipiTxControl = Output(new MipiTxControlInterface)
    val mipiTXVideo   = Output(new MipiTxVideoInterface)
    val key1 = Input(Bool())
    val key2 = Input(Bool())
  })

  val hPixelCnt = RegInit(0.U(log2Ceil(mipiHRes).W))
  val vPixelCnt = RegInit(0.U(log2Ceil(mipiVRes).W))



}
