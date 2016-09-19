/*
 * Part of NDLA audio_api.
 * Copyright (C) 2016 NDLA
 *
 * See LICENSE
 *
 */

package no.ndla.audioapi

import com.typesafe.scalalogging.LazyLogging
import no.ndla.imageapi.DBMigrator
import org.eclipse.jetty.server.Server
import org.eclipse.jetty.servlet.{DefaultServlet, ServletContextHandler}
import org.scalatra.servlet.ScalatraListener


object JettyLauncher extends LazyLogging {
  def main(args: Array[String]) {
    logger.info(io.Source.fromInputStream(getClass.getResourceAsStream("/log-license.txt")).mkString)

    PropertiesLoader.load()
    DBMigrator.migrate(ComponentRegistry.dataSource)

    val startMillis = System.currentTimeMillis()
    val port = AudioApiProperties.ApplicationPort

    val servletContext = new ServletContextHandler
    servletContext.setContextPath("/")

    servletContext.addEventListener(new ScalatraListener)
    servletContext.addServlet(classOf[DefaultServlet], "/")
    servletContext.setInitParameter("org.eclipse.jetty.servlet.Default.dirAllowed", "false")

    val server = new Server(port)
    server.setHandler(servletContext)
    server.start()

    val startTime = System.currentTimeMillis() - startMillis
    logger.info(s"Started at port $port in $startTime ms.")

    server.join()
  }
}
