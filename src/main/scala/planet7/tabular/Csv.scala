package planet7.tabular

/**
 * Csv encapsulate a tabular data structure, as found in a CSV file or spreadsheet. This class is a thin wrapper around an
 * Iterator[Row] and a header Row. It allows client code to:
 *  - read CSV data from a tabular data-source (String, File, InputStream, or anything else)
 *  - Change the column structure (add, remove, rename and reorder columns)
 *  - Map data values according to a function (e.g. a lookup, a data-conversion, a default value)
 *  - Export the results
 *
 * This class may be useful for:
 *  - ETL of externally-generated data
 *  - Pre-formatting data before Diffing it during a reconciliation. @see Diff
 *
 * Csv is designed to be lazy. All materialisation of the datasource is done OUTSIDE of the Csv, i.e. as late as possible. Thus,
 * there should be no appreciable time cost to constructing a Csv, merging Csvs, restructuring columns, and so forth. The time
 * cost is only paid once, when the client code exports or otherwise materialises then Csv.
 *
 * TODO - CAS - 12/08/2014 - Csv should be closeable - or returned iterator could close streams on next() == null
 * TODO - CAS - 07/08/2014 - A Y-shaped pipeline (spits out two CSVs)
 * TODO - CAS - 07/08/2014 - Aggregator 1 - combine multiple columns
 * TODO - CAS - 07/08/2014 - Aggregator 2 - combine multiple rows - provide a predicate for row grouping/inclusion/exclusion
 */
case class Csv(header: Row, rows: Iterator[Row]) {

  def columnStructure(columns: (String, String)*): Csv = {
    val columnXformer = columnXformerFor(columns: _*)
    val headerRenamer = headerRenamerFor(columns: _*)
    Csv(headerRenamer(columnXformer(header)), rows.map(columnXformer))
  }

  private[tabular] def columnXformerFor(columns: (String, String)*): (Row) => Row = {
    val desiredColumnIndices: Array[Int] = columns.map { case (sourceCol, targetCol) => header.data.indexOf(sourceCol) }(collection.breakOut)

    // TODO - CAS - 15/08/2014 - If row has fewer elements than lookup, i.e. it is invalid, this fn throws ArrayIndexOutOfBoundsException
    row => Row(desiredColumnIndices map (index => if (index == -1) "" else row.data(index)))
  }

  private[tabular] def headerRenamerFor(columns: (String, String)*) = (row: Row) => Row(columns.map {
    case (before, after) => after
  }(collection.breakOut))

  def withMappings(mappings: (String, (String) => String)*): Csv = Csv(header, rows.map(valuesTx(mappings: _*)))

  private[tabular] def valuesTx(mappings: (String, (String) => String)*): Row => Row = (row: Row) => {
    val desiredMappings: Map[Int, (String) => String] = mappings.map { case (column, mapper) => header.data.indexOf(column) -> mapper }(collection.breakOut)

    // TODO - CAS - 21/08/2014 - Mutates the row.data Array. Find another way.
    desiredMappings.foreach{
      case (index, mapper) => row.data(index) = mapper(row.data(index))
    }

    Row(row.data)
  }

  def filter(predicates: (String, String => Boolean)*): Csv = Csv(header, rows.withFilter(nextRowFilter(predicates:_*)))

  private[tabular] def nextRowFilter(predicates: (String, String => Boolean)*): Row => Boolean = row => predicates.forall {
    case (columnName, predicate) => predicate(row.data(header.data.indexOf(columnName)))
  }
}

object Csv {
  def apply[A](x: A)(implicit f: A => TabularDataSource): Csv = {
    val dataSource = f(x)
    Csv(dataSource.header, dataSource.rows)
  }
}