package university.innopolis.mlang.backends.fanuc

import java.time.LocalDateTime

case class FanucProgram(name: String,
                        attributes: Attributes,
                        instructions: List[FanucInstruction]) {
  override def toString: String =
    "/PROG  " + name + '\n' +
      "/ATTR\n" + attributes.toString

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
  override def toString: String =
    s"""OWNER		= $owner
       |COMMENT		= "$comment"
       |PROG_SIZE	= $progSize
       |CREATE		= DATE 18-10-10  TIME 23:50:10;
       |MODIFIED	= DATE 18-10-10  TIME 23:56:44;
       |FILE_NAME	= $fileName
       |VERSION		= $version
       |LINE_COUNT	= $lineCount
       |MEMORY_SIZE	= $memorySize
       |""".stripMargin
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
       |$idnt PAUSE_REQUEST	$pauseRequest
       |""".stripMargin
}

sealed trait ProtectType

case object ProtectTypeReadWrite extends ProtectType {
  override def toString: String = "READ_WRITE"
}

sealed trait FanucInstruction

case class UFrame(i: Int) extends FanucInstruction {
  override def toString: String = s"  UFRAME_NUM=$i"
}

case class UTool(i: Int) extends FanucInstruction {
  override def toString: String = s"  UTOOL_NUM=$i"
}

case class Payload(i: Int) extends FanucInstruction {
  override def toString: String = s"  PAYLOAD[$i]"
}

case class Override(i: Int) extends FanucInstruction {
  override def toString: String = s"  OVERRIDE=$i%"
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

  override def toString: String = s"$moveType $pointRegister $velocity$velocityType $smoothnessType"
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


