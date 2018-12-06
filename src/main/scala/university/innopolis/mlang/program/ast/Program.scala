package university.innopolis.mlang.program.ast

/**
  * Internal representation of 'mlang' program
  */
case class Program(memory: Map[String, Expression],
                   statements: List[Statement]) {
  override def toString: String = {
    val memoryDecl = memory.map {
      case (ident, expr) => s"$ident = $expr"
    }.mkString("\n")

    val m = s"memory {\n$memoryDecl}\n"

    s"${m}program {\n\t${statements.mkString("\n\t")}}\n"
  }
}

/**
  * Language construct which evaluates into a value at the runtime
  */
sealed trait Expression

final case class UnaryExpression(operand: Operand,
                                 unaryOp: List[UnaryOp] = List.empty) extends Expression {
  override def toString: String = s"${unaryOp.mkString}$operand"
}

sealed trait Operand
sealed trait MoveTarget extends Operand
final case class Identifier(ident: String) extends MoveTarget {
  require(ident.matches("[a-zA-Z][a-zA-Z0-9]*"))

  override def toString: String = ident
}
final case class IntLiteral(value: Int) extends Operand {
  override def toString: String = value.toString
}
final case class StringLiteral(value: String) extends Operand {
  override def toString: String = value.toString
}
final case class FloatLiteral(value: Double) extends Operand {
  override def toString: String = value.toString
}
final case class BooleanLiteral(value: Boolean) extends Operand {
  override def toString: String = value.toString
}
final case class ExpressionOperand(expression: Expression) extends Operand {
  override def toString: String = expression.toString
}
final case class TypeOperand(typeLiteral: TypeLiteral,
                             parameters: Map[String, Operand] = Map.empty) extends MoveTarget {
  override def toString: String = {
    val parameterList = parameters.map {
      case (ident, operand) => s"$ident: $operand"
    }.mkString(",")
    s"$typeLiteral($parameterList)"
  }
}

final case class BinaryExpression(binOp: BinOp, left: Expression, right: Expression) extends Expression {
  override def toString: String = s"$left $binOp $right"
}
final case class DotExpression(expression: Expression, ident: String) extends Expression {
  override def toString: String = s"$expression.$ident"
}

sealed trait Statement
sealed trait Command extends Statement
final case class MoveCommand(moveTarget: MoveTarget,
                             parameters: Map[String, Operand] = Map.empty) extends Command {
  override def toString: String = {
    val params = parameters.map {
      case (ident, op) => s"$ident: $op"
    }.toList

    val parameterList = if (params.nonEmpty) s": { ${params.mkString(", ")} }\n" else ""
    s"move $moveTarget$parameterList"
  }
}

final case class AssignmentStatement(left: Expression, right: Expression) extends Statement {
  override def toString: String = s"$left = $right"
}
final case class IfStatement(condition: Expression,
                             left: List[Statement],
                             right: Option[List[Statement]]) extends Statement {
  override def toString: String = {
    val elseBlock = right.fold("")(l => s" else {\n\t${l.mkString("\n\t")}}")
    val ifBlock = s"{\n${left.mkString("\n")}}"
    s"if $condition $left$elseBlock\n"
  }
}

final case class ForClause(identifier: Option[Identifier], left: Expression, right: Expression) extends Statement {
  override def toString: String = {
    val ident = identifier.fold("")(i => s"$i in")
    s"$ident $left to $right"
  }
}
final case class ForStatement(clause: ForClause, statementBlock: List[Statement]) extends Statement {
  override def toString: String = {
    val statements = s"{\n${statementBlock.mkString(",")}}\n"
    s"for $clause $statements"
  }
}



sealed trait TypeLiteral
case object Point extends TypeLiteral {
  override def toString: String = "Point"
}

sealed trait UnaryOp
case object Not extends UnaryOp {
  override def toString: String = "!"
}

sealed trait BinOp
/**
  * `&&` and `||`
  */
sealed trait LogicalOp extends BinOp
case object Or extends LogicalOp {
  override def toString: String = "||"
}
case object And extends LogicalOp {
  override def toString: String = "&&"
}

/**
  * `==` | `!=` | `<` | `<=`| `>` | `>=`
  */
sealed trait RelationalOp extends BinOp
case object Equal extends RelationalOp {
  override def toString: String = "=="
}
case object NotEqual extends RelationalOp {
  override def toString: String = "!="
}
case object Less extends RelationalOp {
  override def toString: String = "<"
}
case object LessOrEqual extends RelationalOp {
  override def toString: String = "<="
}
case object Greater extends RelationalOp {
  override def toString: String = ">"
}
case object GreaterOrEqual extends RelationalOp {
  override def toString: String = ">="
}

/**
  * `+` | `-`
  */
sealed trait AddOp extends BinOp
case object Addition extends AddOp {
  override def toString: String = "+"
}
case object Minus extends AddOp {
  override def toString: String = "-"
}

/**
  * `*` | `/`
  */
sealed trait MultOp extends BinOp
case object Multiplication extends MultOp {
  override def toString: String = "*"
}
case object Division extends MultOp {
  override def toString: String = "/"
}
