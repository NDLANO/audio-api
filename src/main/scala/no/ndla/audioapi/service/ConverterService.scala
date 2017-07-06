/*
 * Part of NDLA audio_api.
 * Copyright (C) 2016 NDLA
 *
 * See LICENSE
 *
 */

package no.ndla.audioapi.service

import com.netaporter.uri.Uri
import com.typesafe.scalalogging.LazyLogging
import com.netaporter.uri.dsl._
import no.ndla.audioapi.AudioApiProperties._
import no.ndla.audioapi.auth.User
import no.ndla.audioapi.model.api.NotFoundException
import no.ndla.audioapi.model.domain.Audio
import no.ndla.audioapi.model.Language.{AllLanguages, NoLanguage, UnknownLanguage}
import no.ndla.audioapi.model.{api, domain}
import no.ndla.mapping.License.getLicense

import scala.util.{Failure, Success, Try}


trait ConverterService {
  this: User with Clock =>
  val converterService: ConverterService

  class ConverterService extends LazyLogging {
    def toApiAudioMetaInformation(audioMetaInformation: domain.AudioMetaInformation, language: String): Try[api.AudioMetaInformation] = {
      val supportedLanguages = audioMetaInformation.titles
        .map(_.language.getOrElse(UnknownLanguage))
        .map(lang => if (lang == NoLanguage) UnknownLanguage else lang)
        .distinct

      if (supportedLanguages.contains(language) || language == AllLanguages) {
        val title = audioMetaInformation.getTitleByLanguage(audioMetaInformation, language)
        val tags = audioMetaInformation.getTagsByLanguage(audioMetaInformation, title.get.language.get)

        Success(api.AudioMetaInformation(
          audioMetaInformation.id.get,
          language,
          title.get.title,
          audioMetaInformation.filePaths.map(toApiAudio),
          toApiCopyright(audioMetaInformation.copyright),
          supportedLanguages,
          if (tags.isEmpty) Seq.empty else tags.get.tags
        ))
      } else {
        Failure(new NotFoundException)
      }
    }

    def toApiAudio(audio: domain.Audio): api.Audio = {
      val audioUrl: Uri = s"$Domain/$AudioFilesUrlSuffix/${audio.filePath}"
      api.Audio(audioUrl, audio.mimeType, audio.fileSize)
    }

    def toApiCopyright(copyright: domain.Copyright): api.Copyright =
      api.Copyright(toApiLicence(copyright.license), copyright.origin, copyright.authors.map(toApiAuthor))

    def toApiLicence(licenseAbbrevation: String): api.License = {
      getLicense(licenseAbbrevation) match {
        case Some(license) => api.License(license.license, Option(license.description), license.url)
        case None =>
          logger.warn("Could not retrieve license information for {}", licenseAbbrevation)
          api.License("unknown", None, None)
      }
    }

    def toApiAuthor(author: domain.Author): api.Author =
      api.Author(author.`type`, author.name)

    def toDomainAudioMetaInformation(audio: api.NewAudioMetaInformation, filePaths: Seq[Audio]): domain.AudioMetaInformation = {
      domain.AudioMetaInformation(None,
        audio.titles.map(toDomainTitle),
        filePaths,
        toDomainCopyright(audio.copyright),
        audio.tags.getOrElse(Seq()).map(toDomainTag),
        authUser.id(),
        clock.now()
      )
    }

    def toDomainTitle(title: api.Title): domain.Title = {
      domain.Title(title.title, title.language)
    }

    def toDomainCopyright(copyright: api.Copyright): domain.Copyright = {
      domain.Copyright(copyright.license.license, copyright.origin, copyright.authors.map(toDomainAuthor))
    }

    def toDomainAuthor(author: api.Author): domain.Author = {
      domain.Author(author.`type`, author.name)
    }

    def toDomainTag(tag: api.Tag): domain.Tag = {
      domain.Tag(tag.tags, tag.language)
    }

  }
}
