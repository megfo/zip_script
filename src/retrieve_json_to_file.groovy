import groovy.json.JsonSlurper
import groovy.json.JsonBuilder

/**
 * Created by mfoley on 7/10/16.
 */

String base = "http://lxqgk01.nanigans.com/gk/sites/76218/generic_attributes/"
String fileBase = "/Users/mfoley/ng_refactor/before/generic_attributes"
File f
String apiString
URL apiUrl

attributeID = new String()

// build the url
apiString = base
apiUrl = new URL(apiString)

// create the file that will be output to
f = new File(fileBase + '.json')
f.createNewFile()
writer = new PrintWriter(f)

// read the json from the url
def content = new JsonSlurper().parse(apiUrl)

// write the json to the file
f.write(new JsonBuilder(content).toPrettyString())

writer.close()

// loop through the generic_attributes json
content.each{ jsonAttribute ->
    attributeID = jsonAttribute['id']

    // build the url
    apiString = base + attributeID
    apiUrl = new URL(apiString)

    // create the file that will be output to
    f = new File(fileBase + '_' + attributeID + '.json')
    f.createNewFile()
    writer = new PrintWriter(f)

    // read json from the url
    content = new JsonSlurper().parse(apiUrl)

    // write the json to the file
    f.write(new JsonBuilder(content).toPrettyString())

    writer.close()
}