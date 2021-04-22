package no.ndla.audioapi.model.domain

import no.ndla.audioapi.AudioApiProperties
import org.json4s.FieldSerializer.ignore
import org.json4s.{DefaultFormats, FieldSerializer, Formats}
import org.json4s.native.Serialization
import scalikejdbc._

import scala.util.Try

case class Series(
    id: Option[Long],
    revision: Option[Int],
    title: Seq[Title],
    episodes: Option[Seq[AudioMetaInformation]] = None
)

object Series extends SQLSyntaxSupport[Series] {
  val jsonEncoder: Formats = DefaultFormats

  val repositorySerializer: Formats = jsonEncoder +
    FieldSerializer[Series](
      ignore("id") orElse
        ignore("revision")
    )

  override val tableName = "seriesdata"
  override val schemaName: Option[String] = Some(AudioApiProperties.MetaSchema)

  def fromResultSet(s: SyntaxProvider[Series])(rs: WrappedResultSet): Try[Series] = fromResultSet(s.resultName)(rs)

  def fromResultSet(s: ResultName[Series])(rs: WrappedResultSet): Try[Series] = {
    implicit val formats: Formats = jsonEncoder
    val meta = Try(Serialization.read[Series](rs.string(s.c("document"))))

    meta.map(
      _.copy(
        id = Some(rs.long(s.c("id"))),
        revision = Some(rs.int(s.c("revision"))),
      )
    )
  }
}
