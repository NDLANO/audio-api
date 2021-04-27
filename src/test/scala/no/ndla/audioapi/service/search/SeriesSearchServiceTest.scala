/*
 * Part of NDLA audio_api.
 * Copyright (C) 2016 NDLA
 *
 * See LICENSE
 *
 */

package no.ndla.audioapi.service.search

import no.ndla.audioapi.TestData.searchSettings
import no.ndla.audioapi.integration.{Elastic4sClientFactory, NdlaE4sClient}
import no.ndla.audioapi.model.Sort
import no.ndla.audioapi.model.domain._
import no.ndla.audioapi.{AudioApiProperties, TestEnvironment, UnitSuite}
import no.ndla.scalatestsuite.IntegrationSuite
import org.joda.time.{DateTime, DateTimeZone}
import org.mockito.invocation.InvocationOnMock
import org.scalatest.Outcome

import java.util.Date
import scala.util.Success

class SeriesSearchServiceTest
    extends IntegrationSuite(EnableElasticsearchContainer = true)
    with UnitSuite
    with TestEnvironment {
  override val e4sClient: NdlaE4sClient =
    Elastic4sClientFactory.getClient(elasticSearchHost.getOrElse("http://localhost:9200"))

  override val seriesSearchService = new SeriesSearchService
  override val seriesIndexService = new SeriesIndexService
  override val searchConverterService = new SearchConverterService

  // Skip tests if no docker environment available
  override def withFixture(test: NoArgTest): Outcome = {
    assume(elasticSearchContainer.isSuccess)
    super.withFixture(test)
  }

  override def beforeAll(): Unit = {
    super.beforeAll()

    if (elasticSearchContainer.isSuccess) {
      seriesIndexService.createIndexWithName(AudioApiProperties.SeriesSearchIndex)

      blockUntil(() => seriesSearchService.countDocuments == 0)
    }
  }

  test("Write tests") {
    ???
  }

  def blockUntil(predicate: () => Boolean): Unit = {
    var backoff = 0
    var done = false

    while (backoff <= 16 && !done) {
      if (backoff > 0) Thread.sleep(200 * backoff)
      backoff = backoff + 1
      try {
        done = predicate()
      } catch {
        case e: Throwable => println("problem while testing predicate", e)
      }
    }

    require(done, s"Failed waiting for predicate")
  }
}
