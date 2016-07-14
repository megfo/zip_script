import groovy.json.JsonSlurper
import groovy.json.JsonBuilder
import groovy.time.*

/**
 * Created by mfoley on 7/10/16.
 */

// base url for generic attributes endpoints
String base = "http://lxqgk01.nanigans.com/gk/sites/76218/generic_attributes/"

// variables for file storage
def homeDir = new String(System.getProperty("user.home"))
def fileBase = new String(homeDir + "/ng_refactor/")
def folder = new String("/" + args[0] + "/")

// variables for building urls
def apiString = new String()
URL apiUrl
def attributeID = new String()

// variables for performance details
Long timeOfRequest
Long timeOfResponse
Long duration
def durationString = new String()

// create the file where the performance details will be output to
def perfDetails = new File(fileBase + folder + 'perfDetails.txt')
perfDetails.createNewFile()
perfWriter = new PrintWriter(perfDetails)

// build the url
apiString = base
apiUrl = new URL(apiString)

// create the file that the json will be output to
def f = new File(fileBase + folder + 'generic_attributes.json')
f.createNewFile()
writer = new PrintWriter(f)

// capture time just prior to requesting the json
timeOfRequest = System.currentTimeMillis()

// read the json from the url
def content = new JsonSlurper().parse(apiUrl)

// capture time just after receiving the json
timeOfResponse = System.currentTimeMillis()

// write the performance details to file
duration = timeOfResponse - timeOfRequest
durationString = Objects.toString(duration, null)
perfWriter.println(apiString + ',' + durationString)

perfWriter.flush()

// write the json to the file
f.write(new JsonBuilder(content).toPrettyString())

writer.close()

// loop through the generic_attributes json
// for each attribute in the json, call its specific api endpoint
content.each{ jsonAttribute ->
    attributeID = jsonAttribute['id']

    // build the url
    apiString = base + attributeID
    apiUrl = new URL(apiString)

    // create the file that the json will be output to
    f = new File(fileBase + folder + attributeID + '.json')
    f.createNewFile()
    writer = new PrintWriter(f)

    // capture time just prior to requesting the json
    timeOfRequest = System.currentTimeMillis()

    // read json from the url
    content = new JsonSlurper().parse(apiUrl)

    // capture time just after receiving the json
    timeOfResponse = System.currentTimeMillis()

    // write the performance details to file
    duration = timeOfResponse - timeOfRequest
    durationString = Objects.toString(duration, null)
    perfWriter.println(apiString + ',' + durationString)

    perfWriter.flush()

    // write the json to the file
    f.write(new JsonBuilder(content).toPrettyString())

    writer.close()
}
perfWriter.close()