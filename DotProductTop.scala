package DP

import Chisel._
import Templates._
import BRam._
import ArithOperations._
import DataPaths._


class DotProductTop(w : Int) extends Module {

	val io = new Bundle {

		val cmd = Bool(INPUT)
		val status = UInt(OUTPUT)

    val result = UInt(OUTPUT, w)
	}

   val topInit :: topRun :: topIntermediate :: topDone :: Nil = Enum(UInt(), 4)
   val state = Reg(init = topInit)

   val dpBlock = Module(new DotProductPipe(w))
   
   val dp_ram1 = Module(new BRAM(w, 5))
   val dp_ram2 = Module(new BRAM(w, 5))

   val dp_arith = Module(new DotProductArith(w))



   /* Counter */
   val counter = Module(new ItrCounter(w))

   switch (state) {

       /* topInit -> 0x0 */
   	 is (topInit) {
   	 	when (io.cmd) { state := topRun }
   	 }

       /* topRun -> 0x1 */
   	 is (topRun) {
   	 	when (dpBlock.io.done) { state := topIntermediate }
   	 }

       /* topIntermediate -> 0x2 */
   	 is (topIntermediate) {
   	 	state := topDone
   	 }

       /* topDone -> 0x3 */
   	 is (topDone) {
   	 	state := topInit 
   	 }

   }
   
   counter.io.max := UInt(5)

   counter.io.reset := (state === topInit) || dpBlock.io.pipe_reset
   /* run Counter block when in pipeRun state from pipe FSM */
   counter.io.en_count := (state === topRun) && dpBlock.io.run


   dpBlock.io.en := (state === topRun)
   dpBlock.io.reset := (state === topInit)
   dpBlock.io.block_done := counter.io.done

   dp_ram1.io.read_addr := counter.io.cout
   dp_ram2.io.read_addr := counter.io.cout

   /* delay */
   dp_arith.io.v1 := dp_ram1.io.read_out
   dp_arith.io.v2 := dp_ram2.io.read_out

   val delayReg = Reg(init = Bool(false))
   delayReg := dpBlock.io.en 

   dp_arith.io.en := delayReg  /* delaying this one cycle just in case */
   dp_arith.io.reset := Bool(false)

   io.status := state
   io.result := dp_arith.io.dotproduct
}


class DotProductTopTests (c: DotProductTop) extends Tester(c) {

	poke(c.io.cmd, 1)
	expect(c.io.status, 0)
	step(1)
  expect(c.io.status, 1)
  expect(c.io.result, 0)
  step(1)
	expect(c.io.status, 1)
  expect(c.io.result, 0)
  step(1)
  expect(c.io.status, 1)
  expect(c.io.result, 0)
  step(1)
  poke(c.io.cmd, 0)
  expect(c.io.status, 1)
  expect(c.io.result, 0)
  step(1)
  poke(c.io.cmd, 1) 
  expect(c.io.status, 1)
  expect(c.io.result, 1)
  step(1)
  expect(c.io.status, 1)
  expect(c.io.result, 5)   
  step(1)
  expect(c.io.status, 2)
  expect(c.io.result, 14) 
  step(1)
  expect(c.io.status, 3)
  expect(c.io.result, 30) 
  step(1)
  poke(c.io.cmd, 0)
  expect(c.io.status, 0)
  expect(c.io.result, 30) 
  step(1)
  expect(c.io.status, 0)
  expect(c.io.result, 30) 
  step(1)
  expect(c.io.status, 0)
  expect(c.io.result, 30)   
}