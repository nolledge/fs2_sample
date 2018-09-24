package de.cnolle.validations

import cats.implicits._
import de.cnolle.models.EuroMillions.{ DrawResult, TicketFields }

trait DrawValidation extends TicketFieldBoundsValidation {

  val NumbersInDraw = 5
  val StarsInDraw = 2

  def validateDraw(n: Set[Int], s: Set[Int]): TicketValidationResult[DrawResult] =
    (validNumbers(n), validStars(s), validateFieldBounds(n, s))
      .mapN {
        case (numbers, stars, _) => TicketFields(numbers, stars)
      }

  private def validNumbers(n: Set[Int]): TicketValidationResult[Set[Int]] =
    if (n.size == NumbersInDraw) {
      n.validNel
    } else {
      InvalidDraw.invalidNel
    }

  private def validStars(s: Set[Int]): TicketValidationResult[Set[Int]] =
    if (s.size == StarsInDraw) {
      s.validNel
    } else {
      InvalidDraw.invalidNel
    }

}
