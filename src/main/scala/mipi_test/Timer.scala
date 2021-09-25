// This is a test for timer implementation
package mipi_test

import chisel3._
import chisel3.util._

class Timer(cycles: Int) extends Module {
  val io = IO(new Bundle {
    val set   = Input(Bool())
    val valid = Output(Bool())
  })

  def posedge(x: Bool): Bool = {
    // detect positive edge and output for 1 cycle
    val xPos = RegNext(x)
    val xNeg = RegNext(xPos)
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

  val valid = timer(cycles, posedge(io.set))
  io.valid := valid
}
