package no.ndla.audioapi.model

import no.ndla.audioapi.model.domain.WithLanguage

package object search {

  object LanguageValue {

    case class LanguageValue[T](lang: String, value: T) extends WithLanguage {
      override def language: String = lang
    }

    def apply[T](lang: String, value: T): LanguageValue[T] = LanguageValue(lang, value)

  }

}
