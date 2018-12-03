package university.innopolis.mlang.program

import scala.collection.mutable

class BuildingContext private[program]() {

  private[program] val instructions: mutable.Buffer[Instruction] = mutable.Buffer.empty

  def add(instruction: Instruction): Unit =
    instructions.append(instruction)

  def emit(): Precompiled =
    new Precompiled(instructions.toList)

}
