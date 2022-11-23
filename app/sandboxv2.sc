//
//
//
//
//
//def x() = println("Hola Ema")
//
//x()

//5! 5 * 4 * 3 * 2 * 1 =
//20 * 3 * 2 * 1 =
//60 * 2 * 1 = 120 * 1
//0! = 1

/*
$n = 5;
$total = 1;
for ($i=1; $i<=$n; $i++) {
    $total *= $i;
}
*/

import scala.annotation.tailrec
val n = 6
(1 to n).product

@tailrec
def factorial2(n: BigDecimal): BigDecimal = {
  n match {
    case n1 if n1 == 1 => n
    case n1 if n1 == 0 => 1
    case n1 => n1 * factorial2(n1-1)
  }
}

@tailrec
def factorial3(n: BigDecimal, acc: BigDecimal = 1): BigDecimal = {
  n match {
    case n1 if n1 == 1 => acc
    //    case acc if acc > 100 => throw new Exception("100")
    case n1 => factorial3(n1-1, acc * n)
  }
}

factorial3(9999999)
factorial2(9999999)