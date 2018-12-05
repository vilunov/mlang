package university.innopolis.mlang.backends

import university.innopolis.mlang.program.{BuildingContext, Program}
import university.innopolis.mlang.program.dsl.compile

trait Backend {
  type Output

  def apply(p: Program): Output

  def translate()(implicit buildingContext: BuildingContext): String = {
    val res = buildingContext.emit()
    val prog = compile(res)
    apply(prog).toString
  }
}
