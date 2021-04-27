package no.ndla.audioapi.model

package object search {

  object LanguageValue {

    case class LanguageValue[T](lang: String, value: T)

    def apply[T](lang: String, value: T): LanguageValue[T] = LanguageValue(lang, value)

  }

  case class SearchableLanguageValues(languageValues: Seq[LanguageValue.LanguageValue[String]])

  case class SearchableLanguageList(languageValues: Seq[LanguageValue.LanguageValue[Seq[String]]])
}
