package university.innopolis.mlang.backends

import university.innopolis.mlang.program.Program

object RawBackend extends Backend {
  override type Output = String

  override def apply(p: Program): String = p.toString
}
