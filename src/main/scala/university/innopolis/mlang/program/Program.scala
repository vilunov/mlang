package university.innopolis.mlang.program

import scala.language.experimental.macros

case class Program(instructions: List[Instruction])

object Program {

  def apply(program: BuildFunc): Precompiled =
    macro Macros.programImpl
}
