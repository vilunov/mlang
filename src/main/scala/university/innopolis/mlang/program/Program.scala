package university.innopolis.mlang.program

case class Program(instructions: List[Instruction])

object Program {

  def apply(func: BuildFunc): Precompiled = new Precompiled().apply(func)
}
