package university.innopolis.mlang.tests

import org.scalatest._

import university.innopolis.mlang.backends.fanuc._

class FanucTest extends FlatSpec {
  "Pos" should "be generated correctly" in {
    val testString =
      s"""P[1]{
         |	GP1:
         |	UF : 3, UT : 3,		CONFIG : 'N U T, 0, 0, 1',
         |	X =     0.000  mm,	Y =   357.153  mm,	Z =    63.185  mm,
         |	W =    -0.995 deg,	P =    -6.457 deg,	R =   -90.779 deg
         |	GP2:
         |	UF : 3, UT : 3,
         |	J1=    15.648 deg
         |};""".stripMargin

    val actualString = Positions(
      List(Position(
        List(
          CartesianPoint(3, 3,
            CartesianCoordinates(0, 357.153, 63.185, -0.995, -6.457, -90.779),
            "N U T, 0, 0, 1"),
          JointPoint(3, 3,
            JointCoordinates(List(15.648)))
        )))).toString

    assertResult(testString)(actualString)
  }
}
