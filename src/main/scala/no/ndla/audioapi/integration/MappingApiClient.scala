/*
 * Part of NDLA audio_api.
 * Copyright (C) 2016 NDLA
 *
 * See LICENSE
 *
 */

package no.ndla.audioapi.integration

import no.ndla.audioapi.AudioApiProperties
import no.ndla.audioapi.caching.Memoize
import no.ndla.audioapi.model.api.License
import no.ndla.network.NdlaClient

import scala.util.{Failure, Success}
import scalaj.http.Http

trait MappingApiClient {
  this: NdlaClient =>
  val mappingApiClient: MappingApiClient

  class MappingApiClient {
    private val allLanguageMappingsEndpoint = s"http://${AudioApiProperties.MappingHost}/iso639"
    private val allLicenseDefinitionsEndpoint = s"http://${AudioApiProperties.MappingHost}/licenses"

    def getLicenseDefinition(licenseName: String): Option[License] = {
      getLicenseDefinitions().find(_.license == licenseName).map(l => License(l.license, l.description, l.url))
    }

    def get6391CodeFor6392Code(languageCode6392: String): Option[String] = getLanguageMapping().find(_._1 == languageCode6392).map(_._2)

    def languageCodeSupported(languageCode: String): Boolean = getLanguageMapping().exists(_._1 == languageCode)

    private val getLicenseDefinitions = Memoize[Seq[LicenseDefinition]](AudioApiProperties.LicenseMappingCacheAgeInMs, () => {
      ndlaClient.fetch[Seq[LicenseDefinition]](Http(allLicenseDefinitionsEndpoint)) match {
        case Success(definitions) => definitions
        case Failure(ex) => throw ex
      }
    })

    private val getLanguageMapping = Memoize[Map[String, String]](AudioApiProperties.IsoMappingCacheAgeInMs, () => {
      ndlaClient.fetch[Map[String, String]](Http(allLanguageMappingsEndpoint)) match {
        case Success(map) => map
        case Failure(ex) => throw ex
      }
    })
  }
}

case class LicenseDefinition(license: String, description: String, url: Option[String])
