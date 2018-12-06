package university.innopolis.mlang.program.ast

/**
  * Internal representation of 'mlang' program
  */
case class Program(memory: Map[String, Expression],
                   statements: List[Statement])

/**
  * Language construct which evaluates into a value at the runtime
  */
sealed trait Expression

final case class UnaryExpression(operand: Operand,
                                 unaryOp: List[UnaryOp] = List.empty) extends Expression

sealed trait Operand
sealed trait MoveTarget extends Operand
final case class Identifier(ident: String) extends MoveTarget {
  require(ident.matches("[a-zA-Z][a-zA-Z0-9]*"))
}
final case class IntLiteral(value: Int) extends Operand
final case class StringLiteral(value: String) extends Operand
final case class FloatLiteral(value: Double) extends Operand
final case class BooleanLiteral(value: Boolean) extends Operand
final case class ExpressionOperand(expression: Expression) extends Operand
final case class TypeOperand(typeLiteral: TypeLiteral,
                             parameters: Map[String, Operand] = Map.empty) extends MoveTarget

final case class BinaryExpression(binOp: BinOp, left: Expression, right: Expression) extends Expression
final case class DotExpression(expression: Expression, ident: String) extends Expression

final case class StatementBlock(statements: List[Statement])

sealed trait Statement
sealed trait Command extends Statement
final case class MoveCommand(moveTarget: MoveTarget,
                             parameters: Map[String, Operand] = Map.empty) extends Command

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
