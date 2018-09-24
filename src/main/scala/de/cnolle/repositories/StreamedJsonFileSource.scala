package de.cnolle.repositories

import java.nio.file.Paths

import cats.effect.Sync
import io.circe.fs2._
import fs2.{io, text}
import _root_.io.circe.Decoder

trait StreamedJsonFileSource {

  protected def allFromFile[F[_]: Sync, E: Decoder](filePath: String): fs2.Stream[F, E] =
    io.file
      .readAll[F](Paths.get(filePath), 4096)
      .through(text.utf8Decode)
      .through(text.lines)
      .through(stringStreamParser)
      .through(decoder[F, E])

}
