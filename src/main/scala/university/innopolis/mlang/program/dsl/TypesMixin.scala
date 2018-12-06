package university.innopolis.mlang.program.dsl

import university.innopolis.mlang.program.ast._

private[dsl] trait TrajectoryMixin {
  sealed trait Trajectory {
    def toTrajectoryProperty: Map[String, Operand]
  }

  case object Joint extends Trajectory {
    override def toTrajectoryProperty: Map[String, Operand] = Map("trajectory" -> StringLiteral("JOINT"))
  }

  final case class Circular(secondary: Cartesian) extends Trajectory {
    override def toTrajectoryProperty: Map[String, Operand] = Map(
      "trajectory" -> StringLiteral("CIRCULAR"),
      "secondary" -> secondary.typeOperand,
    )
  }
}

private[dsl] trait SmoothnessMixing {
  sealed trait Smoothness {
    def toSmoothnessProperty: Option[Operand]
  }

  case object Fine extends Smoothness {
    override val toSmoothnessProperty = Some(IntLiteral(0))
  }

  final case class Cnt(i: Int) extends Smoothness {
    require(i >= 0)
    override val toSmoothnessProperty = Some(IntLiteral(i))
  }
}

private[dsl] trait VelocityMixin {
  sealed trait Velocity {
    def toVelocityProperty: Option[Operand]
  }

  implicit class IntegerVelocity(i: Int) extends Velocity {
    override def toVelocityProperty: Option[Operand] = Some(IntLiteral(i))
  }
}

private[dsl] trait TypesMixin extends TrajectoryMixin with VelocityMixin with SmoothnessMixing {
  case object Undefined extends Trajectory with Velocity with Smoothness {
    override val toTrajectoryProperty: Map[String, Operand] = Map.empty
    override val toVelocityProperty: Option[Operand] = None
    override val toSmoothnessProperty: Option[Operand] = None
  }
}
