package university.innopolis.mlang.program

/**
  * Language construct which evaluates into a value at the runtime
  */
sealed trait Expression

sealed trait UnaryExpression extends Expression
final case class UnaryOperatorExpression(unaryOp: UnaryOp,
                                         unaryExpression: Option[UnaryExpression]) extends UnaryExpression

sealed trait Operand extends UnaryExpression
sealed trait MoveTarget extends Operand
final case class Identifier(ident: String) extends MoveTarget
final case class StringLiteral(value: String) extends Operand
final case class FloatLiteral(value: Double) extends Operand
final case class BooleanLiteral(value: Boolean) extends Operand
final case class ExpressionOperand(expression: Expression) extends Operand
final case class TypeOperand(typeLiteral: TypeLiteral, parameters: List[ParameterDeclaration]) extends MoveTarget
final case class ParameterDeclaration(identifier: Identifier, operand: Operand)

final case class BinaryExpression(binOp: BinOp, left: Expression, right: Expression) extends Expression
final case class DotExpression(expression: Expression, ident: String) extends Expression

final case class StatementBlock(statements: List[Statement])
sealed trait Statement
sealed trait Command extends Statement
final case class MoveCommand(moveTarget: MoveTarget, parameters: List[ParameterDeclaration]) extends Command

final case class AssignmentStatement(left: Expression, right: Expression) extends Statement
final case class IfStatement(condition: Expression,
                             left: StatementBlock,
                             right: Option[StatementBlock]) extends Statement

final case class Range(left: Expression, right: Expression)
final case class ForClause(identifier: Option[Identifier], left: Expression, right: Expression) extends Statement
final case class ForStatement(clause: ForClause, statementBlock: StatementBlock) extends Statement



sealed trait TypeLiteral
case object Point extends TypeLiteral

sealed trait UnaryOp
case object Not extends UnaryOp

sealed trait BinOp
/**
  * `&&` and `||`
  */
sealed trait LogicalOp extends BinOp
case object Or extends LogicalOp
case object And extends LogicalOp

/**
  * `==` | `!=` | `<` | `<=`| `>` | `>=`
  */
sealed trait RelationalOp extends BinOp
case object Equal extends RelationalOp
case object NotEqual extends RelationalOp
case object Less extends RelationalOp
case object LessOrEqual extends RelationalOp
case object Greater extends RelationalOp
case object GreaterOrEqual extends RelationalOp

/**
  * `+` | `-`
  */
sealed trait AddOp extends BinOp
case object Addition extends AddOp
case object Minus extends AddOp

/**
  * `*` | `/`
  */
sealed trait MultOp extends BinOp
case object Multiplication extends MultOp
case object Division extends MultOp

/**
  * Language construct which represents an instruction, e.g. state mutation or a robot movement
  */
sealed trait Instruction
final case class InstructionMoveConst(i: Expression) extends Instruction
final case class InstructionMoveVar(ident: String) extends Instruction
final case class InstructionAssign(ident: String, value: Int) extends Instruction
final case class InstructionCondition(cond: Expression,
                                      left: List[Instruction],
                                      right: List[Instruction]) extends Instruction



final case class MemoryBlock(declarations: List[Declaration])
final case class Declaration(ident: String, expression: Expression)
