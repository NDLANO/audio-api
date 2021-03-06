/*
 * Part of NDLA audio_api.
 * Copyright (C) 2016 NDLA
 *
 * See LICENSE
 *
 */

package no.ndla.audioapi.integration

import no.ndla.audioapi.AudioApiProperties.{MigrationHost, MigrationPassword, MigrationUser, Environment}
import no.ndla.network.NdlaClient

import scala.util.Try
import scalaj.http.Http
import io.lemonlabs.uri.dsl._

trait MigrationApiClient {
  this: NdlaClient =>
  val migrationApiClient: MigrationApiClient

  class MigrationApiClient {
    val DBSource = "red"
    val AudioMetadataEndpoint = s"$MigrationHost/audio/:audio_id" ? (s"db-source" -> s"$DBSource")
    val NodeDataEndpoint = s"$MigrationHost/contents/:node_id" ? (s"db-source" -> s"$DBSource")

    def getAudioMetaData(audioNid: String): Try[Seq[MigrationAudioMeta]] = {
      ndlaClient.fetchWithBasicAuth[Seq[MigrationAudioMeta]](Http(AudioMetadataEndpoint.replace(":audio_id", audioNid)),
                                                             MigrationUser,
                                                             MigrationPassword)
    }

    def getNodeData(nid: String): Try[MigrationNodeData] = {
      ndlaClient.fetchWithBasicAuth[MigrationNodeData](Http(NodeDataEndpoint.replace(":node_id", nid)),
                                                       MigrationUser,
                                                       MigrationPassword)
    }
  }
}

case class MigrationNodeData(
    titles: Seq[MigrationTitle]
)

case class MigrationTitle(title: String, language: String)

case class MigrationAudioMeta(nid: String,
                              tnid: String,
                              title: String,
                              fileName: String,
                              url: String,
                              mimeType: String,
                              fileSize: String,
                              language: Option[String],
                              license: String,
                              authors: Seq[MigrationAuthor]) {
  def isMainNode = nid == tnid || tnid == "0"

}
case class MigrationAuthor(`type`: String, name: String)
