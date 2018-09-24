package de.cnolle.validations
import cats.data._
import cats.implicits._
import de.cnolle.models.EuroMillions.TicketFields

trait TicketFieldBoundsValidation {

  private val NumberFieldsUpperBound: Int = 50
  private val NumberFieldsLowerBound: Int = 1

  private val StarFieldUpperBound: Int = 12
  private val StarFieldLowerBound: Int = 1

  def validateFieldBounds(n: Set[Int], s: Set[Int]): TicketValidationResult[TicketFields] =(
   validateNumberFieldBounds(n),
    validateStarFieldBounds(s)
  ).mapN(TicketFields)



  private def validateNumberFieldBounds(s: Set[Int]): TicketValidationResult[Set[Int]] =
    Validated.condNel(
      s.forall(e => isBetween(e)(NumberFieldsLowerBound, NumberFieldsUpperBound)),
      s,
      NumberFieldOutOfBounds
    )


  private def validateStarFieldBounds(s: Set[Int]): TicketValidationResult[Set[Int]] =
    Validated.condNel(
      s.forall(e => isBetween(e)(StarFieldLowerBound, StarFieldUpperBound)),
      s,
      StarFieldOutOfBounds
    )

  private def isBetween(e: Int)(l: Int, u: Int): Boolean = e >= l && e <= u
}
