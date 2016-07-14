import groovy.json.JsonSlurper
import groovy.json.JsonBuilder

/**
 * Created by mfoley on 7/10/16.
 */

String base = "http://lxqgk01.nanigans.com:84/gk/sites/4/generic_attributes/"
String attribute = "locations.zips"
String fileBase = "/Users/mfoley/"
File f
String apiString
URL apiUrl

apiString = base + attribute
apiUrl = new URL(apiString)

f = new File(fileBase + attribute + '.json')
f.createNewFile()
writer = new PrintWriter(f)

// read json from url
def content = new JsonSlurper().parse(apiUrl)

// write  json to file
f.write(new JsonBuilder(content).toPrettyString())

// loop through the entire original json file
originalJson.each{ originalJsonAttribute ->

    // keep track of all the attributes we have checked already
    attributeID = originalJsonAttribute['id']
    attributeIDList << attributeID
