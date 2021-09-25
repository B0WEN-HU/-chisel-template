
package mipi_test

import chisel3._
import chisel3.util._

class DelayPulse(n: Int) extends Module {
  val io = IO(new Bundle {
    val pulse        = Input(Bool())
    val pulseDelayed = Output(Bool())
  })

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

  io.pulseDelayed := delayPulse(io.pulse, n)
}
