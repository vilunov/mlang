package university.innopolis.mlang.program

class Precompiled private[program](private val instructions: List[Instruction] = List.empty) {

  def ++(program: Precompiled): Precompiled =
    new Precompiled(instructions ++ program.instructions)

  def compile(): Program =
    new Program(instructions)
}
