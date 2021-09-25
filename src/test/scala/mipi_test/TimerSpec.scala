
package mipi_test

import chisel3._
import chisel3.tester._
import org.scalatest.FlatSpec

class TimerSpec extends FlatSpec with ChiselScalatestTester {

  val c = 3

  behavior of "Timer(c)"
  it should "count for c cycles after set before valid is true" in {
    test(new Timer(c)) { dut =>
      dut.reset.poke(true.B)
      dut.io.set.poke(false.B)
      dut.clock.step(1)
      dut.reset.poke(false.B)
      //dut.clock.step(1)

      dut.io.set.poke(true.B)
      //dut.clock.step(1)
      //println(s"cycle: +0:\t${dut.io.valid.peek()}")

      for(t <- 1 to 2 * c) {
        dut.clock.step(1)
        dut.io.set.poke(false.B)
        println(s"cycle: +$t:\t${dut.io.valid.peek()}")
        if (t < c) dut.io.valid.expect(false.B)
        else dut.io.valid.expect(true.B)
      }

    }
  }

}
