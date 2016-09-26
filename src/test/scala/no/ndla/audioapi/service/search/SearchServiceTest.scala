/*
 * Part of NDLA audio_api.
 * Copyright (C) 2016 NDLA
 *
 * See LICENSE
 *
 */


package no.ndla.audioapi.service.search

import com.typesafe.scalalogging.LazyLogging
import no.ndla.audioapi.integration.JestClientFactory
import no.ndla.audioapi.model.Sort
import no.ndla.audioapi.model.domain._
import no.ndla.audioapi.{TestEnvironment, UnitSuite}
import org.elasticsearch.common.settings.Settings
import org.elasticsearch.node.{Node, NodeBuilder}

import scala.reflect.io.Path


class SearchServiceTest extends UnitSuite with TestEnvironment with LazyLogging {

  val esHttpPort = 29999
  val esDataDir = "esTestData"
  var esNode: Node = _

  override val jestClient = JestClientFactory.getClient(searchServer = s"http://localhost:$esHttpPort")

  override val searchService = new SearchService
  override val elasticContentIndex = new ElasticContentIndex
  override val searchConverterService = new SearchConverterService

  val byNcSa = Copyright("by-nc-sa", Some("Gotham City"), List(Author("Forfatter", "DC Comics")))
  val publicDomain = Copyright("publicdomain", Some("Metropolis"), List(Author("Forfatter", "Bruce Wayne")))
  val copyrighted = Copyright("copyrighted", Some("New York"), List(Author("Forfatter", "Clark Kent")))

  val audio1 = AudioMetaInformation(Some(1), List(Title("Batmen er på vift med en bil", Some("nb"))), List(Audio("file.mp3", "audio/mpeg", 1024, Some("nb"))), copyrighted, List(Tag(List("fisk"), Some("nb"))))
  val audio2 = AudioMetaInformation(Some(2), List(Title("Pingvinen er ute og går", Some("nb"))), List(Audio("file2.mp3", "audio/mpeg", 1024, Some("nb"))), publicDomain, List(Tag(List("fugl"), Some("nb"))))
  val audio3 = AudioMetaInformation(Some(3), List(Title("Superman er ute og flyr", Some("nb"))), List(Audio("file4.mp3", "audio/mpeg", 1024, Some("nb"))), byNcSa, List(Tag(List("supermann"), Some("nb"))))
  val audio4 = AudioMetaInformation(Some(4), List(Title("Donald Duck kjører bil", Some("nb"))), List(Audio("file3.mp3", "audio/mpeg", 1024, Some("nb"))), publicDomain, List(Tag(List("and"), Some("nb"))))

  override def beforeAll = {
    val settings = Settings.settingsBuilder()
      .put("path.home", esDataDir)
      .put("index.number_of_shards", "1")
      .put("index.number_of_replicas", "0")
      .put("http.port", esHttpPort)
      .build()

    esNode = new NodeBuilder().settings(settings).node()
    esNode.start()

    val indexName = elasticContentIndex.createIndex()
    elasticContentIndex.updateAliasTarget(None, indexName)
    elasticContentIndex.indexDocuments(List(audio1, audio2, audio3, audio4), indexName)

    blockUntil(() => searchService.countDocuments() == 4)
  }

  override def afterAll() = {
    esNode.close()
    Path(esDataDir).deleteRecursively()
  }


  test("That getStartAtAndNumResults returns default values for None-input") {
    searchService.getStartAtAndNumResults(None, None) should equal((0, DEFAULT_PAGE_SIZE))
  }

  test("That getStartAtAndNumResults returns SEARCH_MAX_PAGE_SIZE for value greater than SEARCH_MAX_PAGE_SIZE") {
    searchService.getStartAtAndNumResults(None, Some(1000)) should equal((0, MAX_PAGE_SIZE))
  }

  test("That getStartAtAndNumResults returns the correct calculated start at for page and page-size with default page-size") {
    val page = 74
    val expectedStartAt = (page - 1) * DEFAULT_PAGE_SIZE
    searchService.getStartAtAndNumResults(Some(page), None) should equal((expectedStartAt, DEFAULT_PAGE_SIZE))
  }

  test("That getStartAtAndNumResults returns the correct calculated start at for page and page-size") {
    val page = 123
    val pageSize = 321
    val expectedStartAt = (page - 1) * pageSize
    searchService.getStartAtAndNumResults(Some(page), Some(pageSize)) should equal((expectedStartAt, pageSize))
  }

  test("That all returns all documents ordered by title ascending") {
    val results = searchService.all(None, None, None, None, Sort.ByTitleAsc)
    results.totalCount should be (3)
    results.results.head.id should be (4)
    results.results.last.id should be (3)
  }

  test("That all filtering on license only returns documents with given license") {
    val results = searchService.all(None, Some("publicdomain"), None, None, Sort.ByTitleAsc)
    results.totalCount should be (2)
    results.results.head.id should be (4)
    results.results.last.id should be (2)
  }

  test("That paging returns only hits on current page and not more than page-size") {
    val page1 = searchService.all(None, None, Some(1), Some(2), Sort.ByTitleAsc)
    val page2 = searchService.all(None, None, Some(2), Some(2), Sort.ByTitleAsc)
    page1.totalCount should be (3)
    page1.page should be (1)
    page1.results.size should be (2)
    page1.results.head.id should be (4)
    page1.results.last.id should be (2)
    page2.totalCount should be (3)
    page2.page should be (2)
    page2.results.size should be (1)
    page2.results.head.id should be (3)
  }

  test("That search matches title") {
    val results = searchService.matchingQuery(Seq("Pingvinen"), Some("nb"), None, None, None, Sort.ByTitleAsc)
    results.totalCount should be (1)
    results.results.head.id should be (2)
  }

  test("That search matches tags") {
    val results = searchService.matchingQuery(Seq("and"), Some("nb"), None, None, None, Sort.ByTitleAsc)
    results.totalCount should be (1)
    results.results.head.id should be (4)
  }

  test("That search does not return batmen since it has license copyrighted and license is not specified") {
    val results = searchService.matchingQuery(Seq("batmen"), Some("nb"), None, None, None, Sort.ByTitleAsc)
    results.totalCount should be (0)
  }

  test("That search returns batmen since license is specified as copyrighted") {
    val results = searchService.matchingQuery(Seq("batmen"), Some("nb"), Some("copyrighted"), None, None, Sort.ByTitleAsc)
    results.totalCount should be (1)
    results.results.head.id should be (1)
  }

  def blockUntil(predicate: () => Boolean) = {
    var backoff = 0
    var done = false

    while (backoff <= 16 && !done) {
      if (backoff > 0) Thread.sleep(200 * backoff)
      backoff = backoff + 1
      try {
        done = predicate()
      } catch {
        case e: Throwable => logger.warn("problem while testing predicate", e)
      }
    }

    require(done, s"Failed waiting for predicate")
  }
}