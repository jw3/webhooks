package rxthings.webhooks

import scala.reflect.ClassTag
import scala.reflect.runtime.universe._
import scala.reflect.runtime.{currentMirror => m}

/**
 * Simple string interpolation using the scope of the passed object
 * Perhaps will look at something more advanced, such as a Mustache parser
 */
object Interpolator {
  val tagOpen = "{{"
  val tagClose = "}}"
  val regxOpen = """\{\{"""
  val regxClose = """\}\}"""

  /**
   * Find all Terms on obj
   * Filter to Terms that appear within tags in the template
   * Invoke the Term Getter
   * Replace the tags with the Term value
   * Remove all unparsed tags
   */
  def interpolate[T: TypeTag](template: String, obj: T) = {
    val res = m.reflect(obj)(ClassTag(obj.getClass))
    typeOf[T].members
    .filter(_.isTerm).map(_.asTerm).filter(t => t.isVal || t.isVar)
    .filter(v => template.contains(s"$tagOpen${name(v)}$tagClose"))
    .map(t => res.reflectMethod(t.getter.asMethod))
    .map(m => name(m.symbol.accessed) -> m.apply())
    .foldLeft(template)((s, r) => s.replaceAll(s"$regxOpen${r._1}$regxClose", r._2.toString))
    .replaceAll(s"$regxOpen.+?$regxClose", "")
  }

  private def name(s: Symbol): String = s.name.toString.trim
}
