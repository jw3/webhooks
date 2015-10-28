package wiii.awa

import scala.reflect.ClassTag
import scala.reflect.runtime.universe._
import scala.reflect.runtime.{currentMirror => m}

object Interpolator {
    val rg = """[{{](.+?)[}}]""".r

    def interpolate[T: TypeTag : ClassTag](template: String, obj: T) = {
        val res = m.reflect(obj)
        val repl = typeOf[T].members
                   .filter(_.isMethod)
                   .map(_.asMethod)
                   .filter(m => template.contains(s"{{${name(m)}}}"))
                   .map(res.reflectMethod)
                   .map(m => name(m.symbol) -> m.apply())

        repl.foldLeft(template)((s, r) => s.replaceAll( s"""\\{\\{${r._1}\\}\\}""", r._2.toString))
    }
    def name(s: MethodSymbol): String = s.name.toString
}
