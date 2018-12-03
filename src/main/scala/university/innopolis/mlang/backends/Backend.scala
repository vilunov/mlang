package university.innopolis.mlang.backends

import university.innopolis.mlang.program.Program

trait Backend {
  type Output

  def convert(p: Program): Output
}
