
package mipi_test

import chisel3._
import chisel3.tester._
import org.scalatest.FlatSpec

class LineBufferSpec extends FlatSpec with ChiselScalatestTester {

  behavior of "LineBuffer"
  it should "buffer one line of pixels" in {
    test(new LineBuffer) { dut =>
      dut.io.dataIn.initSource()
      dut.io.dataIn.setSourceClock(dut.clock)
      dut.io.dataOut.initSink()
      dut.io.dataOut.setSinkClock(dut.clock)

      val rand = scala.util.Random
      val testValues    = List.fill(100)(rand.nextLong.abs >> 2)
      val inputSeq  = testValues.map(_.U(64.W))
      val resultSeq = inputSeq

      fork {
        // push inputs into dut
        // split at 1/3 of input and stall 11 cycles
        val (seq1, seq2) = inputSeq.splitAt(resultSeq.length / 3)
        dut.io.dataIn.enqueueSeq(seq1)
        dut.clock.step(11)
        dut.io.dataIn.enqueueSeq(seq2)
      }.fork {
        val (seq1, seq2) = resultSeq.splitAt(resultSeq.length / 2)
        dut.io.dataOut.expectDequeueSeq(seq1)
        dut.clock.step(10)
        dut.io.dataOut.expectDequeueSeq(seq2)
      }.join()

    }
  }

}
