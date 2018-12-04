package university.innopolis.mlang.program

import scala.collection.mutable

class BuildingContext {

  private type Block = mutable.Buffer[Instruction]
  private[this] val blockStack: mutable.ArrayStack[Block] = new mutable.ArrayStack()
  private[this] val varStack: mutable.ArrayStack[Expression] = new mutable.ArrayStack()
  delve()

  def add(instruction: Instruction): Unit =
    blockStack.head.append(instruction)

  def add(expression: Expression): Unit =
    varStack.push(expression)

  def delve(): Unit =
    blockStack.push(mutable.Buffer.empty)

  def emit(): Precompiled = {
    require(blockStack.length == 1 && varStack.isEmpty,
      "Stacks should be empty at the end")

    val instructions = blockStack.pop()
    delve()
    new Precompiled(instructions.toList)
  }

  def compile(): Program = emit().compile()

  def blockCond(): Unit = {
    val blockElse = blockStack.pop().toList
    val blockIf = blockStack.pop().toList
    val condition = varStack.pop()
    add(InstructionCondition(condition, blockIf, blockElse))
  }

}
