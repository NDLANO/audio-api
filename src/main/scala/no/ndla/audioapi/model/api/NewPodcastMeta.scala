package no.ndla.audioapi.model.api

import org.scalatra.swagger.annotations.ApiModel
import org.scalatra.swagger.runtime.annotations.ApiModelProperty

import scala.annotation.meta.field

// format: off
@ApiModel(description = "Meta information about podcast audio")
case class NewPodcastMeta(
  @(ApiModelProperty @field)(description = "Header for the podcast") header: String,
  @(ApiModelProperty @field)(description = "Introduction for the podcast") introduction: String,
  @(ApiModelProperty @field)(description = "Cover photo for the podcast") coverPhotoId: String,
  @(ApiModelProperty @field)(description = "Cover photo alttext for the podcast") coverPhotoAltText: String,
  @(ApiModelProperty @field)(description = "Manuscript for the podcast") manuscript: String
)
// format: on
