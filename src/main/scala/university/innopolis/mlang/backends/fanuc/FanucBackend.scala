package university.innopolis.mlang.backends.fanuc

import university.innopolis.mlang.backends.Backend
import university.innopolis.mlang.program.Program

object FanucBackend extends Backend {
  override type Output = FanucProgram

  override def convert(p: Program): FanucProgram = ???

  def serialize(input: FanucProgram): String = ???
}
