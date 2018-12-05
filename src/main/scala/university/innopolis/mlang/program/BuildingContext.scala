package university.innopolis.mlang.program

import scala.collection.mutable

import university.innopolis.mlang.program.ast._

/**
  * A context for constructing mlang programs.
  * Can be used with the DSL methods when used as an implicit value.
  */
class BuildingContext {

  private type Block = mutable.Buffer[Statement]
  private[this] val blockStack: mutable.ArrayStack[Block] = new mutable.ArrayStack()
  private[this] val varStack: mutable.ArrayStack[Expression] = new mutable.ArrayStack()
  delve()

  def add(instruction: Statement): Unit =
    blockStack.head.append(instruction)

  def delve(): Unit =
    blockStack.push(mutable.Buffer.empty)

  /**
    * Flush the internal state and create a program chunk
    * @return program chunk containing instructions
    */
  def emit(): List[Statement] = {
    require(blockStack.length == 1 && varStack.isEmpty,
      "Stacks should be empty at the end")

    val instructions = blockStack.pop()
    delve()
    instructions.toList
  }

  def blockCond(condition: Expression): Unit = {
    val blockElse = blockStack.pop().toList
    val blockIf = blockStack.pop().toList
    add(IfStatement(
      condition,
      StatementBlock(blockIf),
      Option(blockIf).filter(_.nonEmpty).map(StatementBlock),
    ))
  }

  def blockLoop(varName: Identifier, from: Int, to: Int): Unit = {
    val body = blockStack.pop().toList
    add(ForStatement(
      ForClause(Some(varName), IntLiteral(from), IntLiteral(to)),
      StatementBlock(body),
    ))
  }

}
