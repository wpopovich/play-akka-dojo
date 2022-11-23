import scala.annotation.tailrec


def factorial(n: BigDecimal) = {
  (1 to n.intValue).product
}


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
    case n1 => factorial3(n1-1, acc * n)
  }
}
//factorial2(6)
//factorial(6)
//factorial3(6)
//
//factorial2(0)
//factorial(0)
//factorial3(0)

import scala.util.Try
factorial2(9999999)
//factorial(9999999)
factorial3(9999999)
