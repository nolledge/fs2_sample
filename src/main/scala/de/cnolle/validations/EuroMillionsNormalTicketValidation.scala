package de.cnolle.validations

import cats.implicits._
import de.cnolle.models.EuroMillions.{ EuroMillionsNormalTicket, TicketFields }

/**
 * Validations for the 'normal' EuroMillion ticket
 */
trait EuroMillionsNormalTicketValidation extends TicketFieldBoundsValidation {

  /**
   * The maximum of number fields to check on a ticket
   */
  private val SizeNumberHits: Int = 5

  /**
   * Max size of star fields to hit on a ticket
   */
  private val SizeStarHits: Int = 2

  def validateEuroMillionNormalTicket(
      n: Set[Int],
      s: Set[Int]
  ): TicketValidationResult[EuroMillionsNormalTicket] =
    (
      validateNumberHitsUpper(n),
      validateStarHitsUpper(s),
      validateNumberHitsLower(n),
      validateStarHitsLower(s),
      validateFieldBounds(n, s)
    ).mapN {
      case _ => EuroMillionsNormalTicket(TicketFields(n, s))
    }

  private def validateNumberHitsUpper(n: Set[Int]): TicketValidationResult[Set[Int]] =
    if (n.size > SizeNumberHits) { TooManyNumberHits.invalidNel } else { n.validNel }

  private def validateNumberHitsLower(n: Set[Int]): TicketValidationResult[Set[Int]] =
    if (n.size < SizeNumberHits) { NotEnoughNumberHits.invalidNel } else { n.validNel }

  private def validateStarHitsUpper(s: Set[Int]): TicketValidationResult[Set[Int]] =
    if (s.size > SizeStarHits) { TooManyStarHits.invalidNel } else { s.validNel }

  private def validateStarHitsLower(s: Set[Int]): TicketValidationResult[Set[Int]] =
    if (s.size < SizeStarHits) { NotEnoughStarHits.invalidNel } else { s.validNel }

}
