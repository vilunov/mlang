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
