/*
 * Part of NDLA audio_api.
 * Copyright (C) 2016 NDLA
 *
 * See LICENSE
 *
 */

package no.ndla.audioapi.model.search

import java.util.Date

// TODO: Add podcastmeta here for searching for podcast descriptions
case class SearchableAudioInformation(
    id: String,
    titles: SearchableLanguageValues,
    tags: SearchableLanguageList,
    license: String,
    authors: Seq[String],
    lastUpdated: Date,
    defaultTitle: Option[String],
    audioType: String
)
