package university.innopolis.mlang.backends

import university.innopolis.mlang.program.Program

object RawBackend extends Backend {
  override type Output = String

  override def convert(p: Program): String = ???

  def read(input: String): Program = ???
}
