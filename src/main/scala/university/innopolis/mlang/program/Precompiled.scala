package university.innopolis.mlang.program

class Precompiled private[program](private val instructions: List[Instruction] = List.empty) {

  def ++(program: Precompiled): Precompiled =
    new Precompiled(instructions ++ program.instructions)

  def apply(func: BuildFunc): Precompiled = {
    val builder = new BuildingContext
    func(builder)
    this ++ new Precompiled(builder.instructions.toList)
  }

  def compile(): Program =
    new Program(instructions)
}
