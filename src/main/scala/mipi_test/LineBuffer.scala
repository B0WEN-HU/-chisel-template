//Sync FIFO store a line of Data

package mipi_test

import chisel3.util._
import chisel3._

class LineBuffer(size: Int = 1920) extends Module {
  val io = IO(new Bundle {
    val dataIn  = Flipped(Decoupled(UInt(64.W)))
    val dataOut = Decoupled(UInt(64.W))
    val clear   = Input(Bool())
  })

  //val q = Module(new Queue(UInt(64.W), size))
  withReset(io.clear) {
    val q      = Queue(io.dataIn, size)
    io.dataOut <> q
  }
}
