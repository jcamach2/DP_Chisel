package DataPaths

import Chisel._

class ItrCounter(w : Int) extends Module {

	val io = new Bundle {
    	val en_count = Bool(INPUT)
    	val reset = Bool(INPUT)
    	val done = Bool(OUTPUT)

	  	val max = UInt(INPUT, w)
    	val cout = UInt(OUTPUT, w)
   }
 
   val currCount = Reg(init = UInt(0, width = w))

   /* counter should go up to max - 1 */
   val hitMax = (currCount >= io.max - UInt(1))
  
   when (io.en_count) {
   		currCount := Mux(hitMax, io.max - UInt(1), currCount + UInt(1))
   }
   .elsewhen (io.reset) {
   		currCount := UInt(0)
   }

   io.cout := currCount
   io.done := hitMax
}

class ItrCounterTestings (c: ItrCounter) extends Tester(c) {

}