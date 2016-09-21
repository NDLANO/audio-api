/*
 * Part of NDLA audio_api.
 * Copyright (C) 2016 NDLA
 *
 * See LICENSE
 *
 */

package no.ndla.audioapi.service

import java.net.URL

import com.typesafe.scalalogging.LazyLogging
import no.ndla.audioapi.AudioApiProperties
import no.ndla.audioapi.integration.{MigrationApiClient, MigrationAudioMeta}
import no.ndla.audioapi.model.domain
import no.ndla.audioapi.model.domain._
import no.ndla.audioapi.repository.AudioRepositoryComponent

import scala.util.{Success, Try}

trait ImportServiceComponent {
  this: MigrationApiClient with AudioStorageService with AudioRepositoryComponent =>
  val importService: ImportService

  class ImportService extends LazyLogging {
    def importAudio(audioId: String): Try[domain.AudioMetaInformation] = {
      migrationApiClient.getAudioMetaData(audioId).map(uploadAndPersist)
    }

    private def uploadAndPersist(audioMeta: Seq[MigrationAudioMeta]): domain.AudioMetaInformation = {
      val audioFilePaths = audioMeta.map(uploadAudioFile(_).get)
      persistMetaData(audioMeta, audioFilePaths)
    }

    private def persistMetaData(audioMeta: Seq[MigrationAudioMeta], audioObjects: Seq[Audio]): domain.AudioMetaInformation = {
      val titles = audioMeta.map(x => Title(x.title, x.language))
      val mainNode = audioMeta.find(_.isMainNode).get
      val authors = audioMeta.flatMap(_.authors)
      val origin = authors.find(_.`type`.toLowerCase() == "opphavsmann")
      val copyright = Copyright(mainNode.license, origin.map(_.name), authors.diff(Seq(origin).flatten).map(x => Author(x.`type`, x.name)))
      val domainMetaData = domain.AudioMetaInformation(None, titles, audioObjects, copyright)

      audioRepository.withExternalId(mainNode.nid) match {
        case None => audioRepository.insert(domainMetaData, mainNode.nid)
        case Some(existingAudio) => audioRepository.update(domainMetaData, existingAudio.id.get)
      }
    }

    private def uploadAudioFile(audioMeta: MigrationAudioMeta): Try[Audio] = {
      val destinationPath = s"${AudioApiProperties.AudioUrlContextPath}/${audioMeta.fileName}"
      audioStorage.storeAudio(new URL(audioMeta.url), audioMeta.mimeType, audioMeta.fileSize, destinationPath)
        .map(Audio(_, audioMeta.mimeType, audioMeta.fileSize.toLong, audioMeta.language))
    }
  }
}