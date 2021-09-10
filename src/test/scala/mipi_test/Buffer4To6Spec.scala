
package mipi_test

import chisel3._
import chisel3.tester._
import org.scalatest.{FlatSpec, FreeSpec}

class Buffer4To6Spec extends FlatSpec with ChiselScalatestTester {

  behavior of "Buffer4To6"
  it should "convert 4-pixel data to 6-pixel data" in {
    test(new Buffer4To6) { dut =>
      dut.io.clear.poke(true.B)
      dut.clock.step(1)
      dut.io.clear.poke(false.B)
      dut.clock.step(1)

      val rand = scala.util.Random
      val raw10BitWidth = 10
      val numOfInputData = 1920
//      val inputSampleList: List[Long] =
//        List(883, 507, 254, 185, 447, 592, 261, 752, 223, 354, 181, 157, 87, 750, 790, 922, 441, 827, 156, 628)
      val inputSampleList: List[Long] = {
        List.fill(numOfInputData * 4)(rand.nextInt(1 << raw10BitWidth).toLong)
      }
      val inputDataList =
        for (i <- 0 until inputSampleList.size / 4) yield {
          inputSampleList(4 * i) +
            (inputSampleList(4 * i + 1) << raw10BitWidth) +
            (inputSampleList(4 * i + 2) << (2 * raw10BitWidth)) +
            (inputSampleList(4 * i + 3) << (3 * raw10BitWidth))
        }

      println(inputSampleList)
      println(inputDataList)
      val outputDataList: scala.collection.mutable.Buffer[Long] =
        scala.collection.mutable.Buffer()

      for (i <- inputDataList) {

        dut.io.dataIn.poke(i.U)
        dut.io.validIn.poke(true.B)


        dut.clock.step(1)

        println(dut.io.validOut.peek)
        if(dut.io.validOut.peek.litToBoolean) {
          outputDataList += dut.io.dataOut.peek.litValue.toLong
          println(dut.io.dataOut.peek)
        }

        if(rand.nextBoolean) {
          dut.io.dataIn.poke((rand.nextLong.abs/4).U)
          dut.io.validIn.poke(false.B)
          dut.clock.step(rand.nextInt(5) + 1)
        }
        dut.io.validIn.poke(false.B)
      }

      println(outputDataList)

      for (i <- 1 to 10) {
        dut.io.dataIn.poke(0.U)
        dut.io.validIn.poke(false.B)
        dut.clock.step(1)
        if(dut.io.validOut.peek == true.B) {
          outputDataList += dut.io.dataOut.peek.toString.toLong
          println(dut.io.dataOut.peek)
        }
      }

      val outputSampleList: List[Long] = outputDataList.toList flatMap { case x =>
        val mask: Long = (1 << 10) - 1
        for (s <- 0L until 6L) yield {
          (x & (mask << (raw10BitWidth * s))) >> (raw10BitWidth * s)
        }
      }

      println(inputSampleList)
      println(outputSampleList)
      assert(inputSampleList == outputSampleList)
    }
  }

}
