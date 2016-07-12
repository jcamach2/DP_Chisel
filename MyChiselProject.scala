package DP

import Chisel._  

object DP {
  def main(args: Array[String]): Unit = { 
    val tutArgs = args.slice(1, args.length) 
    val res = 
    args(0) match {
  	    case "DotProductTop" =>   
 	       chiselMainTest(tutArgs, () => Module(new DotProductTop(16))) {
    	      c => new DotProductTopTests(c)
        }  
    }
  }
}