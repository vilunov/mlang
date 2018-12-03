package university.innopolis.mlang.program

import scala.language.experimental.macros

/**
  * Internal representation of 'mlang' program
  */
case class Program(instructions: List[Instruction])

object Program {

  def apply(program: BuildFunc): Precompiled =
    macro Macros.programImpl
}
