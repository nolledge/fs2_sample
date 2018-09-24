package de.cnolle.models
import io.circe.Decoder
import io.circe._
import io.circe.generic.semiauto._

/**
 * Represents unevaluated data.
 * Might represent a DrawingResult, a EuroMillionTicket or a EuroMillionSystemTicket
 *
 * @param numbers
 * @param stars
 */
case class FieldRecord(numbers: Set[Int], stars: Set[Int])

object FieldRecord {
  implicit val jsonDecoder: Decoder[FieldRecord] = deriveDecoder[FieldRecord]
}

