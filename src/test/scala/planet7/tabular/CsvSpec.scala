package planet7.tabular

import java.io.{ByteArrayInputStream, FileInputStream}
import java.nio.charset.StandardCharsets

import org.scalatest.{MustMatchers, WordSpec}
import planet7.relational.TestData._

import scala.io.Source

class CsvSpec extends WordSpec with MustMatchers {
  "We can construct a Csv from a RelationalInputSource, including blank rows" in {
    val data = """
                 |
                 |Some,Header,Columns
                 |
                 |
                 |D,E,F
                 |
                 |G,H,I
                 |
               """.stripMargin.trim

    val unblankedData = """Some,Header,Columns
                          |D,E,F
                          |G,H,I""".stripMargin

    val csv = Csv(data)
    csv.header.toString mustEqual "Some,Header,Columns"
    export(csv) mustEqual unblankedData
  }

  "We can rename and restructure the columns in a Csv" in {
    val data = """Index,Name,Value
                 |D,E,F
                 |G,H,I""".stripMargin

    val result = """Amount,Name
                 |F,E
                 |I,H""".stripMargin

    val csv = Csv(data).columnStructure(
      "Value" -> "Amount", "Name"
    )

    csv.header mustEqual Row(Array("Amount", "Name"))
    export(csv) mustEqual result
  }

  "We cannot read from the same datasource twice" in {
    def file = asFile("large_dataset.csv")

    val possibleLoadMethods = Map(
      //      "string" -> fromString(string),
      "file" -> fromFile(file) //,
      //      "stringInputStream" -> fromInputStream(new ByteArrayInputStream(string.getBytes(StandardCharsets.UTF_8))),
      //      "fileInputStream" -> fromInputStream(new FileInputStream(file)),
      //      "exp. memoryMappedFile" -> experimentalFromMemoryMappedFile(file),
      //      "exp. scanner" -> experimentalFromScanner(file),
      //      "exp. wholeFile" -> experimentalFromWholeFile(file)
    )

    val poo: TabularDataSource = fromFile(file)
    poo.rows(identity)

    // Should throw exception:
    poo.rows(identity)
  }

  "All methods of accessing data produce the same result" in {
    fail("write me, or make me implicit in the next test")
  }

  "Performance test for different file-access methods" in {
    import planet7.timing._

    val all = new Timer()
    all {

      def processLargeDataset(datasource: TabularDataSource) = {
        val csv = Csv(datasource)
        //      .renameAndRestructure("first_name" -> "First Name", "last_name", "fee paid")
        //      .remap("last_name" -> (_.toUpperCase))

        export(csv)
        // Assert it is correct
      }

      def file = asFile("large_dataset.csv")
      def string = Source.fromFile(file).mkString

      val possibleLoadMethods = Map(
        //      "string" -> fromString(string),
        "file" -> fromFile(file) //,
        //      "stringInputStream" -> fromInputStream(new ByteArrayInputStream(string.getBytes(StandardCharsets.UTF_8))),
        //      "fileInputStream" -> fromInputStream(new FileInputStream(file)),
        //      "exp. memoryMappedFile" -> experimentalFromMemoryMappedFile(file),
        //      "exp. scanner" -> experimentalFromScanner(file),
        //      "exp. wholeFile" -> experimentalFromWholeFile(file)
      )

      val timer = new Timer(3)
      import timer._

      val label = "file"
      val loadMethod = fromFile(file)
      for {
      //      (label, loadMethod) <- possibleLoadMethods
        i <- 1 to 20
      } {
        println(s"i: ${i}")
        t"$label" {processLargeDataset(loadMethod)}
      }

      println(timer)
//      timer.file.average must be < 200.0
    }

    println(all)
  }

  "Performance test for pimped CsvReader datasource" in {
    // Use iterator from CsvReader
    fail("write me")
  }

//  "An empty Csv2 behaves itself" in {
//    assert(Csv2("").rows=== Nil)
//    assert(Csv2("Some,Header,Columns").rows === Nil)
//  }
//
//  "Merge Csv2s, so that we can gather similar data from multiple sources" in {
//    val left = Csv2( """
//                      |ID,Name,Value
//                      |A,B,C
//                    """.stripMargin)
//
//    val right = Csv2( """
//                       |ID,Value,Name
//                       |D,F,E
//                     """.stripMargin).restructure("ID", "Name", "Value") // Put columns into the same order
//
//    assert(Csv2(left, right) === Csv2( """
//                                       |ID,Name,Value
//                                       |A,B,C
//                                       |D,E,F
//                                     """.stripMargin))
//  }
//
//  "We can add columns to Csv2s" in {
//    val twoColumnsOfData = """
//                             |ID,Name
//                             |A,B
//                           """.stripMargin
//
//    val threeColumnsOfData = """
//                               |ID,Value,Name
//                               |A,Some default value,B
//                             """.stripMargin
//
//    def transform(data: String) =
//      Csv2(twoColumnsOfData)
//        .restructure("ID", "Value", "Name")
//        .remap("Value" -> (_ => "Some default value"))
//
//    assert(transform(twoColumnsOfData) === Csv2(threeColumnsOfData))
//
//  }
//
//  "The default String representation of Csv2 is the entire contents, formatted as a Csv2" in {
//    assert(Csv2(
//      List("foo", "bar"),
//      List(
//        List("one", "two"),
//        List("uno", "dos"),
//        List("ichi", "ni"),
//        List("eins", "zwei"))).toString ===
//      """foo,bar
//        |one,two
//        |uno,dos
//        |ichi,ni
//        |eins,zwei
//        |""".stripMargin)
//  }
//
//  "For debugging and REPL development, Csv2 provides a truncated String" in {
//    assert(Csv2(
//      List("foo", "bar"),
//      List(
//        List("one", "two"),
//        List("uno", "dos"),
//        List("ichi", "ni"),
//        List("eins", "zwei"))).toTruncString ===
//      """foo,bar
//        |one,two
//        |uno,dos
//        |ichi,ni
//        |...""".stripMargin)
//  }
}