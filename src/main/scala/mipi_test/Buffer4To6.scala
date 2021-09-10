//Buffer from 4 to 6 pixels a data
package mipi_test

import chisel3.util._
import chisel3._

class Buffer4To6 extends Module {
  val io = IO(new Bundle{
    val clear     = Input(Bool())
    val dataIn    = Input(UInt(64.W))
    val validIn   = Input(Bool())
    val dataOut   = Output(UInt(64.W))
    val validOut  = Output(Bool())
  })

  val raw10BitWidth = 10

  val inputSampleReg1 = RegInit(0.U((4 * raw10BitWidth).W))
  val inputSampleReg2 = RegInit(0.U((4 * raw10BitWidth).W))
  val inputSampleReg3 = RegInit(0.U((4 * raw10BitWidth).W))

  val outputSample1 =
    Cat(inputSampleReg2(2 * raw10BitWidth - 1, 0),
      inputSampleReg1(4 * raw10BitWidth - 1, 0))
  val outputSample2 =
    Cat(inputSampleReg3(4 * raw10BitWidth - 1, 0),
      inputSampleReg2(4 * raw10BitWidth - 1, 2 * raw10BitWidth))

  val inputPtr      = RegInit(0.U(2.W))
  val nextInputPtr  = Mux(inputPtr === 3.U, 1.U, inputPtr + 1.U)
  val outputPtr     = RegInit(0.U(2.W))
  val nextOutputPtr = Mux(outputPtr === 2.U, 3.U, 2.U)

  val outputValid =
    (outputPtr =/= inputPtr) &&
      ((inputPtr === 2.U) || (inputPtr === 3.U))

  when(io.clear) {
    inputPtr  := 0.U
    outputPtr := 0.U
  } .otherwise {
    when(io.validIn) {
      inputPtr := nextInputPtr
      when(nextInputPtr === 1.U) {
        inputSampleReg1 := io.dataIn(4 * raw10BitWidth - 1, 0)
      }
      when(nextInputPtr === 2.U) {
        inputSampleReg2 := io.dataIn(4 * raw10BitWidth - 1, 0)
      }
      when(nextInputPtr === 3.U) {
        inputSampleReg3 := io.dataIn(4 * raw10BitWidth - 1, 0)
      }
    }
    when(outputValid) {
      outputPtr := nextOutputPtr
    }
  }

  val outputSample =
    Mux(inputPtr === 2.U, outputSample1, outputSample2)

  io.dataOut  := outputSample
  io.validOut := outputValid
}
