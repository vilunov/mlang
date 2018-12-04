package university.innopolis.mlang.tests

import java.time.LocalDateTime

import org.scalatest._

import university.innopolis.mlang.backends.fanuc._

trait FanucTestData {

  val expectedPoints: String =
    s"""P[1]{
       |	GP1:
       |	UF : 3, UT : 3,		CONFIG : 'N U T, 0, 0, 1',
       |	X =     0.000  mm,	Y =   357.153  mm,	Z =    63.185  mm,
       |	W =    -0.995 deg,	P =    -6.457 deg,	R =   -90.779 deg
       |	GP2:
       |	UF : 3, UT : 3,
       |	J1=    15.648 deg
       |};""".stripMargin

  val testPositions: Positions = Positions(
    Position(
      CartesianPoint(3, 3,
        CartesianCoordinates(0, 357.153, 63.185, -0.995, -6.457, -90.779),
        "N U T, 0, 0, 1"),
      JointPoint(3, 3, JointCoordinates(List(15.648)))
    )
  )

  val expectedInstructions: String =
    """   0:UFRAME_NUM=0    ;
      |   1:UTOOL_NUM=0    ;
      |   2:L P[0] 0mm/sec FINE    ;""".stripMargin

  val testInstructions: Instructions = Instructions(
    UFrame(0),
    UTool(0),
    LinearInstruction(PointRegister(0), 0, OtherMMSec, SmoothnessFine)
  )

  val expectProgram: String =
    s"""/PROG  LETTERP
       |/ATTR
       |OWNER		= MNEDITOR;
       |COMMENT		= "";
       |PROG_SIZE	= 1;
       |CREATE		= DATE 18-10-10  TIME 23:50:10;
       |MODIFIED	= DATE 18-10-10  TIME 23:56:44;
       |FILE_NAME	= ;
       |VERSION		= 1;
       |LINE_COUNT	= 3;
       |MEMORY_SIZE	= 2000;
       |/MN
       |$expectedInstructions
       |/POS
       |$expectedPoints
       |/END
       |""".stripMargin

  val testProgram: FanucProgram = FanucProgram(
    name = "LETTERP",
    attributes = Attributes(
      owner = "MNEDITOR",
      comment = "",
      progSize = 1,
      create = LocalDateTime.now(),
      modified = LocalDateTime.now(),
      fileName = "",
      version = 1,
      lineCount = 3,
      memorySize = 2000,
    ),
    instructions = testInstructions,
    positions = testPositions
  )
}

class FanucTest extends FlatSpec with FanucTestData {

  "Positions" should "serialize correctly" in {
    assertResult(expectedPoints)(testPositions.toString)
  }

  "Instructions" should "serialize correctly" in {
    assertResult(expectedInstructions)(testInstructions.toString)
  }

  "Program" should "serialize correctly" in {
    assertResult(expectProgram)(testProgram.toString)
  }
}
