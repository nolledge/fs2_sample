package de.cnolle.repositories

import cats.data.Validated.Valid
import cats.effect.Sync
import de.cnolle.models.EuroMillions.Ticket
import de.cnolle.models.{ EuroMillions, FieldRecord }

class MixedTicketsRepository[F[_]: Sync]
    extends StreamedJsonFileSource
    with algebra.MixedTicketRepository[F] {

  def all(): fs2.Stream[F, Ticket] =
    allFromFile[F, FieldRecord]("testdata/mixed_tickets.txt")
      .map(r => EuroMillions.ticket(r.numbers, r.stars))
      .collect {
        case Valid(ticket) => ticket
      }
}
