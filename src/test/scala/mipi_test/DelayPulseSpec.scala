
package mipi_test

import chisel3._
import chisel3.tester._
import org.scalatest.FlatSpec

class DelayPulseSpec extends FlatSpec with ChiselScalatestTester {
  val c = 77
  behavior of "DelayPulse(c)"
  it should "Delay a pulse for c cycles" in {
    test(new DelayPulse(c)) { dut =>
      dut.reset.poke(true.B)
      dut.io.pulse.poke(false.B)
      dut.clock.step(1)
      dut.reset.poke(false.B)

      dut.io.pulse.poke(true.B)
      dut.clock.step(1)
      dut.io.pulse.poke(false.B)

      for(t <- 1 to 2 * c) {

        println(s"cycle: +$t:\t${dut.io.pulseDelayed.peek()}")
        if (t != c) dut.io.pulseDelayed.expect(false.B)
        else if (t == c) dut.io.pulseDelayed.expect(true.B)
        dut.clock.step(1)
      }

    }
  }
}
