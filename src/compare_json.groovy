import groovy.json.JsonSlurper
import java.util.logging.Logger

/**
 * Created by mfoley on 7/8/16.
 */

// build the file path
String homeDir = System.getProperty("user.home")
String fileBase = homeDir + "/"
String beforeFileName = fileBase + "site_4_generic_attributes.json"
String afterFileName = fileBase + "site_4_generic_attributes2.json"
def beforeDoc = new File(beforeFileName)
def afterDoc = new File(afterFileName)

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
String entryKey
def entryKeyList = []

// create the file for log output
def logOutput = new File(fileBase + "site4_log_output.txt")
logOutput.createNewFile()
def logWriter = new PrintWriter(logOutput)

// write header for file
logWriter.println("comparing generic_attributes files\n")
logWriter.flush()

// verify the size
try {
    assert beforeJson.size() == afterJson.size()
}
catch (AssertionError assertionError) {
    logWriter.println("WARNING: The two jsons do not have the same number of values.")
    logWriter.flush()
}

// loop through the entire before json file
beforeJson.each{ beforeJsonAttribute ->

    // keep track of all the attributes we have checked already
    attributeID = beforeJsonAttribute['id']
    attributeIDList << attributeID

    // determine if there is an attribute in the after json file that exactly matches
    // the attribute in the before json file
    match = afterJson.any{ afterJsonAttribute -> beforeJsonAttribute.equals(afterJsonAttribute) }

    if(match == true) {
        logWriter.println("INFO: Attribute ${attributeID} matches in both files.")
        logWriter.flush()
    }
    else {

        // determine if the attribute exists at all in the after json file
        for(i=0; i<afterJson.size(); i++) {
            afterJsonAttributeID = afterJson[i].get('id')

            // attribute exists
            if(afterJsonAttributeID == attributeID) {
                attributeExists = true
                logWriter.println("WARNING: Attribute ${attributeID} exists in both files, but does not match.")
                logWriter.flush()

                // loop through each sub-attribute of the beforeJsonAttribute
                beforeJsonAttribute.each { beforeJsonSubAttribute ->

                    // keep track of all the sub-attributes we have checked already
                    subAttributeKey = beforeJsonSubAttribute.key
                    subAttributeKeyList << subAttributeKey

                    // determine if there is a sub-attribute in the afterJsonAttribute that exactly matches
                    // the sub-attribute from the before json attribute
                    match = afterJson[i].any{ afterJsonSubAttribute ->
                        beforeJsonSubAttribute.equals(afterJsonSubAttribute) }

                    if(match == true) {
                        logWriter.println("--> INFO: ${attributeID}.${subAttributeKey} matches in both files.")
                        logWriter.flush()
                    }
                    else {

                        // determine if the sub-attribute exists at all in the afterJsonAttribute
                        if (afterJson[i].containsKey(beforeJsonSubAttribute.key)) {
                            logWriter.println("--> WARNING: ${attributeID}.${subAttributeKey} exists in both files, " +
                                    "but does not match.")
                            logWriter.flush()
                        }
                        else {
                            logWriter.println("--> WARNING: ${attributeID}.${subAttributeKey} exists in " +
                                    "${beforeFileName}, but does not exist in ${afterFileName}.")
                            logWriter.flush()
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
                        logWriter.println("--> WARNING: ${attributeID}.${subAttributeKey} exists in ${afterFileName}, " +
                                "but does not exist in ${beforeFileName}.")
                        logWriter.flush()
                    }
                }

                subAttributeKeyList.clear()
                break
            }
        }

        // attribute does not exist at all in the new json file
        if(attributeExists == false) {
            logWriter.println("WARNING: Attribute ${attributeID} exists in ${beforeFileName}, but does not exist in " +
                    "${afterFileName}.")
            logWriter.flush()
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
        logWriter.println("WARNING: Attribute ${attributeID} exists in ${afterFileName}, but does not exist " +
                "in ${beforeFileName}.")
        logWriter.flush()
    }
}

// loop through each attributeID
attributeIDList.each { attribute ->

    // build the file path
    fileBase = homeDir + "/ng_refactor/"
    beforeFileName = fileBase + "before/" + attribute + ".json"
    afterFileName = fileBase + "after/" + attribute + ".json"
    beforeDoc = new File(beforeFileName)
    afterDoc = new File(afterFileName)

    // retrieve generic attributes json from file
    beforeJson = new JsonSlurper().parse(beforeDoc)
    afterJson = new JsonSlurper().parse(afterDoc)

    // write header for file
    logWriter.println("\n\ncomparing ${attribute} files\n")
    logWriter.flush()

    // loop through the entire before json file
    beforeJson.each { beforeJsonEntry ->

        // loop through each entry of the beforeJson file
        beforeJsonEntry.each { beforeJsonSubAttribute ->

            // keep track of all the keys we have checked already
            entryKey = beforeJsonEntry.key
            entryKeyList << entryKey

            // determine if there is an entry in the afterJson file that exactly matches
            // the entry from the before json attribute
            match = afterJson.any { afterJsonEntry ->
                beforeJsonEntry.equals(afterJsonEntry)
            }

            if (match == true) {
                logWriter.println("INFO: ${beforeJsonEntry.key} matches in both files.")
                logWriter.flush()
            } else {

                // determine if the entry exists at all in the afterJson file
                if (afterJson.containsKey(beforeJsonEntry.key)) {
                    logWriter.println("WARNING: ${beforeJsonEntry.key} exists in both files, but does not match.")
                    logWriter.flush()
                } else {
                    logWriter.println("WARNING: ${beforeJsonEntry.keyy} exists in ${beforeFileName}, but does not " +
                            "exist in ${afterFileName}.")
                    logWriter.flush()
                }
            }
        }
    }

    // loop through each entry of the afterJsonAttribute
    afterJson.each { afterJsonEntry ->

        // check if we've already checked this entry key
        entryKey = afterJsonEntry.key
        match = entryKeyList.contains(entryKey)

        // if we didn't already check it, it must be missing from the beforeJson
        if(match == false) {
            logWriter.println("WARNING: ${afterEntryKey} exists in ${afterFileName}, but does not exist in " +
                    "${beforeFileName}.")
            logWriter.flush()
        }
    }

    // loop through the afterJson
    afterJson.each { afterJsonEntry ->

        // check if we've already checked this attribute
        entryKey = afterJsonEntry.key
        match = entryKeyList.contains(entryKey)

        // if we didn't already check it, it must be missing from the beforeJson
        if(match == false) {
            logWriter.println("WARNING: Entry ${entryKey} exists in ${afterFileName}, but does not exist " +
                    "in ${beforeFileName}.")
            logWriter.flush()
        }
    }
}
logWriter.close()