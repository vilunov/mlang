package university.innopolis.mlang.io

import scala.io.Source
import java.io._

object ReadWrite {
  def read(filePath: String): Seq[String] =
    Source.fromFile(filePath).mkString.split('\n')

  def write(content: String, filePath: String): Unit = {
    val file = new File(filePath)
    val writer = new BufferedWriter(new FileWriter(file))
    writer.write(content)
    writer.close()
  }
}
