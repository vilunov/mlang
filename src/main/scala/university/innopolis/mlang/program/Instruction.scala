package university.innopolis.mlang.program

/**
  * Language construct which evaluates into a value at the runtime
  */
sealed trait Expression
final case class ExpressionIdent(ident: String) extends Expression

/**
  * Language construct which represents an instruction, e.g. state mutation or a robot movement
  */
sealed trait Instruction
final case class InstructionMoveConst(i: Int) extends Instruction
final case class InstructionMoveVar(ident: String) extends Instruction
final case class InstructionAssign(ident: String, value: Int) extends Instruction
final case class InstructionCondition(cond: Expression,
                                      left: List[Instruction],
                                      right: List[Instruction]) extends Instruction
