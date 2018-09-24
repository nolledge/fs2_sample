package de.cnolle.repositories

import cats.data.Validated.Valid
import cats.effect.Sync
import de.cnolle.models.EuroMillions.{DrawResult, EuroMillionsSystemTicket}
import de.cnolle.models.{EuroMillions, FieldRecord}

class DrawRepository[F[_]: Sync] extends StreamedJsonFileSource with algebra.DrawRepository[F] {

  def first():fs2.Stream[F, DrawResult] = allFromFile[F, FieldRecord]("testdata/draw_result.txt")
    .map(r => EuroMillions.drawResult(r.numbers, r.stars))
    .collect{
      case Valid(dw) => dw
    }
    .head
}
