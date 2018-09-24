package de.cnolle.repositories
import cats.data.Validated.Valid
import cats.effect.Sync
import de.cnolle.models.EuroMillions.EuroMillionsSystemTicket
import de.cnolle.models.{EuroMillions, FieldRecord}

class SystemTicketRepository[F[_]: Sync] extends StreamedJsonFileSource with algebra.SystemTicketRepository[F] {

  def all():fs2.Stream[F, EuroMillionsSystemTicket] = allFromFile[F, FieldRecord]("testdata/system_tickets.txt")
    .map(r => EuroMillions.systemTicket(r.numbers, r.stars))
    .collect{
      case Valid(ticket) => ticket
    }
}
