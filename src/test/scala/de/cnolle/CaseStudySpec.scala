package de.cnolle
import org.scalatest.{ MustMatchers, WordSpec }

import scala.annotation.tailrec

trait CaseStudySpec extends WordSpec with MustMatchers {

  /**
   *
   * Implementation from
   * https://de.wikipedia.org/wiki/Binomialkoeffizient
   *
   * @param n
   * @param k
   * @return
   */
  def binomialCoefficient(n: Int, k: Int): Long = {
    @tailrec
    def binomialCoefficientR(n: Int, k: Int, i: Int, acc: Long): Long = {
      if (i == k + 1) acc
      else binomialCoefficientR(n, k, i + 1, (acc * (n - k + i)) / i)
    }
    if (2 * k > n) binomialCoefficientR(n, n - k, 1, 1)
    else binomialCoefficientR(n, k, 1, 1)
  }

  binomialCoefficient(5, 2) mustBe 10
}
