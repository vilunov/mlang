package university.innopolis.mlang.backends.fanuc

import java.time.LocalDateTime

case class FanucProgram(name: String,
                        attributes: Attributes,
                        instructions: Instructions,
                        positions: Positions) {

  require(attributes.lineCount == instructions.instructions.length)

  override def toString: String =
    s"""/PROG  $name
       |/ATTR
       |${attributes.toString}
       |/MN
       |$instructions
       |/POS
       |$positions
       |/END
       |""".stripMargin

}

case class Attributes(owner: String,
                      comment: String,
                      progSize: Int,
                      create: LocalDateTime,
                      modified: LocalDateTime,
                      fileName: String,
                      version: Int,
                      lineCount: Int,
                      memorySize: Int,
                      protect: ProtectType = ProtectTypeReadWrite,
                      tcd: TCD = TCD()) {

  require(progSize >= 0)
  require(version >= 0)
  require(memorySize >= 0)

  override def toString: String =
    s"""OWNER		= $owner;
       |COMMENT		= "$comment";
       |PROG_SIZE	= $progSize;
       |CREATE		= DATE 18-10-10  TIME 23:50:10;
       |MODIFIED	= DATE 18-10-10  TIME 23:56:44;
       |FILE_NAME	= $fileName;
       |VERSION		= $version;
       |LINE_COUNT	= $lineCount;
       |MEMORY_SIZE	= $memorySize;""".stripMargin
}

case class TCD(stackSize: Int = 0,
               taskPriority: Int = 50,
               timeSlice: Int = 0,
               busyLampOff: Int = 0,
               abortRequest: Int = 0,
               pauseRequest: Int = 0) {
  private val idnt: String = " " * 6

  override def toString: String =
    s"""TCD:  STACK_SIZE	$stackSize
       |$idnt TASK_PRIORITY	$taskPriority
       |$idnt TIME_SLICE	$timeSlice
       |$idnt BUSY_LAMP_OFF	$busyLampOff
       |$idnt ABORT_REQUEST	$abortRequest
       |$idnt PAUSE_REQUEST	$pauseRequest""".stripMargin
}

sealed trait ProtectType

case object ProtectTypeReadWrite extends ProtectType {
  override def toString: String = "READ_WRITE"
}

sealed trait FanucInstruction

case class UFrame(i: Int) extends FanucInstruction {
  override def toString: String = s"UFRAME_NUM=$i"
}

case class UTool(i: Int) extends FanucInstruction {
  override def toString: String = s"UTOOL_NUM=$i"
}

case class Payload(i: Int) extends FanucInstruction {
  override def toString: String = s"PAYLOAD[$i]"
}

case class Override(i: Int) extends FanucInstruction {
  override def toString: String = s"OVERRIDE=$i%"
}

sealed trait Register

case class PointRegister(i: Int) extends Register {
  override def toString: String = s"P[$i]"
}

sealed trait VelocityType {
  val value: String

  override def toString: String = value
}

abstract class JointVelocityType(val value: String) extends VelocityType

abstract class OtherVelocityType(val value: String) extends VelocityType

case object JointPercent extends JointVelocityType("%")
case object JointSec extends JointVelocityType("sec")
case object JointMsec extends JointVelocityType("msec")

case object OtherMMSec extends OtherVelocityType("mm/sec")
case object OtherCMMin extends OtherVelocityType("cm/min")
case object OtherInchMin extends OtherVelocityType("inch/min")
case object OtherDegSec extends OtherVelocityType("deg/sec")
case object OtherSec extends OtherVelocityType("sec")
case object OtherMsec extends OtherVelocityType("msec")

sealed trait SmoothnessType

case object SmoothnessFine extends SmoothnessType {
  override def toString: String = s"FINE"
}

case class SmoothnessCNT(coef: Int) extends SmoothnessType {
  override def toString: String = s"CNT$coef"
}

abstract class MoveInstruction(val moveType: Char) extends FanucInstruction {
  val pointRegister: PointRegister
  val velocity: Int
  val velocityType: VelocityType
  val smoothnessType: SmoothnessType

  override def toString: String =
    s"$moveType $pointRegister $velocity$velocityType $smoothnessType"
}

case class LinearInstruction(pointRegister: PointRegister,
                             velocity: Int,
                             velocityType: OtherVelocityType,
                             smoothnessType: SmoothnessType)
  extends MoveInstruction('L')

case class CircularInstruction(pointRegister: PointRegister,
                               secondPointRegister: PointRegister,
                               velocity: Int,
                               velocityType: OtherVelocityType,
                               smoothnessType: SmoothnessType)
  extends MoveInstruction('C') {

  override def toString: String =
    s"$moveType $pointRegister : $secondPointRegister $velocity$velocityType $smoothnessType"
}

case class JointInstruction(pointRegister: PointRegister,
                            velocity: Int,
                            velocityType: JointVelocityType,
                            smoothnessType: SmoothnessType)
  extends MoveInstruction('J')

case class ArcInstruction(pointRegister: PointRegister,
                          velocity: Int,
                          velocityType: OtherVelocityType,
                          smoothnessType: SmoothnessType)
  extends MoveInstruction('A')


sealed trait Coordinates

case class CartesianCoordinates(x: Double, y: Double, z: Double,
                                w: Double, p: Double, r: Double)
  extends Coordinates {

  override def toString: String =
    f"""	X =$x%10.3f  mm,	Y =$y%10.3f  mm,	Z =$z%10.3f  mm,
       |	W =$w%10.3f deg,	P =$p%10.3f deg,	R =$r%10.3f deg
       |""".stripMargin
}

case class JointCoordinates(joints: List[Double]) extends Coordinates {
  override def toString: String =
    joints.zipWithIndex.map { case (j, i) =>
      val suffix =
        if (i + 1 == joints.length) "\n" else if (i % 3 == 2) ",\n" else ","
      f"J${i + 1}=$j%10.3f deg$suffix"
    }.mkString("\t", "\t", "")
}

sealed trait Point {
  val userFrame: Int
  val userTool: Int
  val coordinates: Coordinates
}

case class CartesianPoint(userFrame: Int,
                          userTool: Int,
                          coordinates: CartesianCoordinates,
                          config: String)
  extends Point {

  override def toString: String =
    s"	UF : $userFrame, UT : $userTool,\t\tCONFIG : '$config',\n$coordinates"
}

case class JointPoint(userFrame: Int,
                      userTool: Int,
                      coordinates: JointCoordinates)
  extends Point {

  override def toString: String =
    s"	UF : $userFrame, UT : $userTool,\n$coordinates"
}

case class Position(internals: Point*) {
  override def toString: String =
    internals.zipWithIndex
      .map { case (j, i) => s"	GP${i + 1}:\n$j" }
      .mkString
}


case class Positions(positions: Position*) {
  override def toString: String =
    positions.zipWithIndex
      .map { case (j, i) => s"P[${i + 1}]{\n$j};" }
      .mkString("\n")
}

case class Instructions(instructions: FanucInstruction*) {
  require(instructions.length <= 9999)

  override def toString: String = {
    instructions.zipWithIndex
      .map { case(j, i) => f"$i%4d:$j    ;" }
      .mkString("\n")
  }
}
