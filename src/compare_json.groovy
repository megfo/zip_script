import groovy.json.JsonSlurper
import java.util.logging.Logger

/**
 * Created by mfoley on 7/8/16.
 */

// variables for file location
String homeDir = System.getProperty("user.home")
String fileBase = homeDir + "/"
def beforeDoc = new File(fileBase + "site_4_generic_attributes.json")
def afterDoc = new File(fileBase + "site_4_generic_attributes2.json")

// retrieve generic attributes json from file
def beforeJson = new JsonSlurper().parse(beforeDoc)
def afterJson = new JsonSlurper().parse(afterDoc)

// attributes for processing
Boolean match = false
String attributeID
def attributeIDList = []
Integer i = 0
String afterJsonAttributeID
String subAttributeKey
def subAttributeKeyList = []
Boolean attributeExists = false

// create the file for log output
def logOutput = new File(fileBase + "site4_log_output.txt")
logOutput.createNewFile()
def logWriter = new PrintWriter(logOutput)
// Logger logger = Logger.getLogger("")

// verify the size
try {
    assert beforeJson.size() == afterJson.size()
}
catch (AssertionError assertionError) {
    logWriter.println("WARNING: The two jsons do not have the same number of values.")
    logWriter.flush()
    // logger.warning "The two jsons do not have the same number of values."
}

// loop through the entire original json file
beforeJson.each{ beforeJsonAttribute ->

    // keep track of all the attributes we have checked already
    attributeID = beforeJsonAttribute['id']
    attributeIDList << attributeID

    // determine if there is an attribute in the new json file that exactly matches
    // the attribute in the original json file
    match = afterJson.any{ afterJsonAttribute -> beforeJsonAttribute.equals(afterJsonAttribute) }

    if(match == true) {
        logWriter.println("INFO: Attribute ${attributeID} matches in both files.")
        logWriter.flush()
        // logger.info "Attribute ${attributeID} matches in both files."
    }
    else {

        // determine if the attribute exists at all in the new json file
        for(i=0; i<afterJson.size(); i++) {
            afterJsonAttributeID = afterJson[i].get('id')

            // attribute exists
            if(afterJsonAttributeID == attributeID) {
                attributeExists = true
                logWriter.println("WARNING: Attribute ${attributeID} exists in both files, but does not match.")
                logWriter.flush()
                // logger.warning "Attribute ${attributeID} exists in both files, but does not match."

                // loop through each sub-attribute of the beforeJsonAttribute
                beforeJsonAttribute.each { beforeJsonSubAttribute ->

                    // keep track of all the sub-attributes we have checked already
                    subAttributeKey = beforeJsonSubAttribute.key
                    subAttributeKeyList << subAttributeKey

                    // determine if there is a sub-attribute in the afterJsonAttribute that exactly matches
                    // the sub-attribute from the original json attribute
                    match = afterJson[i].any{ afterJsonSubAttribute ->
                        beforeJsonSubAttribute.equals(afterJsonSubAttribute) }

                    if(match == true) {
                        logWriter.println("--> INFO: ${attributeID}.${subAttributeKey} matches in both files.")
                        logWriter.flush()
                        // logger.info "--> ${attributeID}.${subAttributeKey} matches in both files."
                    }
                    else {

                        // determine if the sub-attribute exists at all in the afterJsonAttribute
                        if (afterJson[i].containsKey(beforeJsonSubAttribute.key)) {
                            logWriter.println("--> WARNING: ${attributeID}.${subAttributeKey} exists in both files, " +
                                    "but does not match.")
                            logWriter.flush()
                            // logger.warning "--> ${attributeID}.${subAttributeKey} exists in both files, " +
                            //        "but does not match."
                        }
                        else {
                            logWriter.println("--> WARNING: ${attributeID}.${subAttributeKey} exists in beforeJson, " +
                                    "but does not exist in afterJson.")
                            logWriter.flush()
                            // logger.warning "--> ${attributeID}.${subAttributeKey} exists in beforeJson, " +
                            //        "but does not exist in afterJson."
                        }
                    }
                }

                // loop through each sub-attribute of the afterJsonAttribute
                afterJson[i].each {

                    // check if we've already checked this sub-attribute
                    subAttributeKey = it.key
                    match = subAttributeKeyList.contains(subAttributeKey)

                    // if we didn't already check it, it must be missing from the beforeJson
                    if(match == false) {
                        logWriter.println("--> WARNING: ${attributeID}.${subAttributeKey} exists in afterJson, but " +
                                "does not exist in beforeJson.")
                        logWriter.flush()
                        // logger.warning "--> ${attributeID}.${subAttributeKey} exists in afterJson, but does not " +
                        //        "exist in beforeJson."
                    }
                }

                subAttributeKeyList.clear()
                break
            }
        }

        // attribute does not exist at all in the new json file
        if(attributeExists == false) {
            logWriter.println("WARNING: Attribute ${attributeID} exists in beforeJson, but does not exist in " +
                    "afterJson.")
            logWriter.flush()
            // logger.warning "Attribute ${attributeID} exists in beforeJson, but does not exist in afterJson."
        }

        attributeExists = false
    }
}

// loop through the afterJson
afterJson.each { afterJsonEntry ->

    // check if we've already checked this attribute
    attributeID = afterJsonEntry['id']
    match = attributeIDList.contains(attributeID)

    // if we didn't already check it, it must be missing from the beforeJson
    if(match == false) {
        logWriter.println("WARNING: Attribute ${attributeID} exists in afterJson, but does not exist in beforeJson.")
        logWriter.flush()
        // logger.warning "Attribute ${attributeID} exists in afterJson, but does not exist in beforeJson."
    }
}
logWriter.close()