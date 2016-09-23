/*
 * Part of NDLA audio_api.
 * Copyright (C) 2016 NDLA
 *
 * See LICENSE
 *
 */

package no.ndla.audioapi.controller

import javax.servlet.http.HttpServletRequest

import com.typesafe.scalalogging.LazyLogging
import no.ndla.network.{ApplicationUrl, CorrelationID}
import org.apache.logging.log4j.ThreadContext
import org.json4s.{DefaultFormats, Formats}
import org.scalatra.{BadGateway, BadRequest, InternalServerError, ScalatraServlet}
import org.scalatra.json.NativeJsonSupport
import no.ndla.audioapi.AudioApiProperties.{CorrelationIdHeader, CorrelationIdKey}
import no.ndla.audioapi.model.api.{Error, ValidationException}
import no.ndla.network.model.HttpRequestException

abstract class NdlaController extends ScalatraServlet with NativeJsonSupport with LazyLogging {
  protected implicit override val jsonFormats: Formats = DefaultFormats

  before() {
    contentType = formats("json")
    CorrelationID.set(Option(request.getHeader(CorrelationIdHeader)))
    ThreadContext.put(CorrelationIdKey, CorrelationID.get.getOrElse(""))
    ApplicationUrl.set(request)
    logger.info("{} {}{}", request.getMethod, request.getRequestURI, Option(request.getQueryString).map(s => s"?$s").getOrElse(""))
  }

  after() {
    CorrelationID.clear()
    ThreadContext.remove(CorrelationIdKey)
    ApplicationUrl.clear
  }

  error {
    case v: ValidationException => BadRequest(Error(Error.VALIDATION, v.getMessage))
    case hre: HttpRequestException => BadGateway(Error(Error.REMOTE_ERROR, hre.getMessage))
    case t: Throwable => {
      t.printStackTrace()
      logger.error(t.getMessage)
      InternalServerError(Error())
    }
  }

  def long(paramName: String)(implicit request: HttpServletRequest): Long = {
    val paramValue = params(paramName)
    paramValue.forall(_.isDigit) match {
      case true => paramValue.toLong
      case false => throw new ValidationException(s"Invalid value for $paramName. Only digits are allowed.")
    }
  }
}