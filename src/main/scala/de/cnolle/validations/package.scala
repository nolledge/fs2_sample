package de.cnolle
import cats.data.ValidatedNel

package object validations {

  sealed trait TicketValidation
  case object NumberFieldOutOfBounds extends TicketValidation
  case object StarFieldOutOfBounds extends TicketValidation

  case object TooManyNumberHits extends TicketValidation
  case object TooManyStarHits extends TicketValidation

  case object NotEnoughNumberHits extends TicketValidation
  case object NotEnoughStarHits extends TicketValidation

  case object InvalidDraw extends TicketValidation

  type TicketValidationResult[A] = ValidatedNel[TicketValidation, A]

}
