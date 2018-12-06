package university.innopolis.mlang

import university.innopolis.mlang.backends.RawBackend
import university.innopolis.mlang.program.dsl._
import university.innopolis.mlang.backends.fanuc.FanucBackend
import university.innopolis.mlang.io.ReadWrite

object Main extends App {
  implicit val builder: BuildingContext = new BuildingContext

  move(Joints(35, 19, 20, 35, 17, 60), velocity = 100, smoothness = Fine, trajectory = Joint)
  move(Joints(-10, -30, 15, -15, 60, 12), velocity = 100, smoothness = Fine, trajectory = Joint)
  move(Joints(0, 0, 0, 0, 0, 90), velocity = 100, smoothness = Fine, trajectory = Joint)

//  ReadWrite.write(FanucBackend.translate(), "generated.ls")
  ReadWrite.write(RawBackend.translate(), "kek.ml")
}
