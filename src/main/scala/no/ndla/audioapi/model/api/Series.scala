/*
 * Part of NDLA audio_api.
 * Copyright (C) 2016 NDLA
 *
 * See LICENSE
 *
 */

package no.ndla.audioapi.model.api

import org.scalatra.swagger.annotations.ApiModel
import org.scalatra.swagger.runtime.annotations.ApiModelProperty

import scala.annotation.meta.field

// format: off
@ApiModel(description = "Meta information about the series")
case class Series(
    @(ApiModelProperty @field)(description = "The unique id of this series") id: Long,
    @(ApiModelProperty @field)(description = "The revision number of this series") revision: Int,
    @(ApiModelProperty @field)(description = "The title of the series file") title: Title,
)
// format: on
