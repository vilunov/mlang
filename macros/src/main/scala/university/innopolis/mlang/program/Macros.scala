package university.innopolis.mlang.program

import scala.reflect.macros.whitebox

object Macros {
  private def mapBlock(c: whitebox.Context)(block: c.Tree): List[c.Tree] = {
    import c.universe._

    block match {
      case f: c.universe.Block => f.children.map { i => q"$i(b)" }
      case f => List(q"$f(b)")
    }
  }

  def programImpl(c: whitebox.Context)
                 (program: c.Tree): c.Tree = {
    import c.universe._

    val calls = mapBlock(c)(program)
    q"{ applyImpl { b => {..$calls} } }"
  }

  def condImpl(c: whitebox.Context)
              (p: c.Tree)(first: c.Tree)(second: c.Tree): c.Tree = {
    import c.universe._

    val firstCalls = mapBlock(c)(first)
    val firstLambda = q"{ b => {..$firstCalls} }"
    val secondCalls = mapBlock(c)(second)
    val secondLambda = q"{ b => {..$secondCalls} }"

    q"{ condImpl($p)($firstLambda)($secondLambda) }"
  }
}
