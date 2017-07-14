/*
 * Part of NDLA audio_api.
 * Copyright (C) 2016 NDLA
 *
 * See LICENSE
 *
 */

package no.ndla.audioapi.service

import no.ndla.audioapi.model.api
import no.ndla.audioapi.model.domain.{Audio, AudioMetaInformation, Author, Copyright, Tag, Title, _}
import no.ndla.audioapi.{TestEnvironment, UnitSuite}
import org.joda.time.{DateTime, DateTimeZone}
import org.mockito.Mockito._

import scala.util.{Failure, Success}

class ConverterServiceTest extends UnitSuite with TestEnvironment {
  val service = new ConverterService

  def updated = new DateTime(2017, 4, 1, 12, 15, 32, DateTimeZone.UTC).toDate
  val copyrighted = Copyright("copyrighted", Some("New York"), Seq(Author("Forfatter", "Clark Kent")))
  val audioMeta = AudioMetaInformation(
    Some(1),
    Seq(Title("Batmen er på vift med en bil", Some("nb"))),
    Seq(Audio("file.mp3", "audio/mpeg", 1024, Some("nb"))),
    copyrighted,
    Seq(Tag(Seq("fisk"), Some("nb"))),
    "ndla124",
    updated)

  test("that toApiAudioMetaInformation converts a domain class to an api class") {

    val expected = api.AudioMetaInformation(
      audioMeta.id.get,
      "nb",
      "Batmen er på vift med en bil",
      audioMeta.filePaths.map(service.toApiAudio).head,
      service.toApiCopyright(audioMeta.copyright),
      Seq("fisk"),
      Seq("nb")
    )

    service.toApiAudioMetaInformation(audioMeta, "nb") should equal(Success(expected))
  }

  test("that toApiAudioMetaInformation should return Failure if language is not supported") {
    val audioWithNoTitles = audioMeta.copy(titles = Seq.empty)
    val randomLanguage = "norsk"

    service.toApiAudioMetaInformation(audioMeta, randomLanguage).isFailure should be (true)
    service.toApiAudioMetaInformation(audioWithNoTitles, randomLanguage).isFailure should be (true)
  }

  test("That toApiLicense invokes mapping api to retrieve license information") {
    val licenseAbbr = "by-sa"
    val license = api.License(licenseAbbr, Some("Creative Commons Attribution-ShareAlike 2.0 Generic"), Some("https://creativecommons.org/licenses/by-sa/2.0/"))

    service.toApiLicence(licenseAbbr) should equal (license)
  }

  test("That toApiLicense returns unknown if the license is invalid") {
    val licenseAbbr = "garbage"

    service.toApiLicence(licenseAbbr) should equal (api.License("unknown", None, None))
  }
}
