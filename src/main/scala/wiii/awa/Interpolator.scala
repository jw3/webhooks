package wiii.awa

import scala.reflect.ClassTag
import scala.reflect.runtime.universe._
import scala.reflect.runtime.{currentMirror => m}

/**
 * Simple string interpolation using the scope of the passed object
 */
object Interpolator {
    val tagOpen = "{{"
    val tagClose = "}}"
    val regxOpen = """\{\{"""
    val regxClose = """\}\}"""

    def interpolate[T: TypeTag : ClassTag](template: String, obj: T) = {
        val res = m.reflect(obj)
        typeOf[T].members
            .filter(_.isMethod)
            .map(_.asMethod)
            .filter(m => template.contains(s"$tagOpen${name(m)}$tagClose"))
            .map(res.reflectMethod)
            .map(m => name(m.symbol) -> m.apply()).
            foldLeft(template)((s, r) => s.replaceAll(s"$regxOpen${r._1}$regxClose", r._2.toString))
    }

    private def name(s: MethodSymbol): String = s.name.toString
}
