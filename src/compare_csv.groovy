/**
 * Created by mfoley on 7/20/16.
 */
@Grab('org.apache.commons:commons-csv:1.2')
import org.apache.commons.csv.CSVParser
import static org.apache.commons.csv.CSVFormat.*

import java.nio.file.Paths
import groovy.json.JsonOutput

def listing = []

// variables for file storage
def homeDir = new String(System.getProperty("user.home"))
def fileBase = new String(homeDir + "/ng_refactor_csvs/76218/Age")
// def folder = new String("/" + args[1] + "/")

Paths.get(fileBase).withReader { reader ->
    CSVParser csv = new CSVParser(reader, DEFAULT.withHeader())

    for (record in csv.iterator()) {
        listing << record.toMap()
    }
}

Paths.get('Age.json').withWriter { jsonWriter ->
    jsonWriter.write JsonOutput.prettyPrint(JsonOutput.toJson(listing))
}
