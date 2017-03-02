import groovy.json.JsonSlurper
import java.util.logging.Logger

/**
 * Created by mfoley on 7/8/16.
 */

// base url for generic attributes endpoints
String log_level =  args[0] // example 'verbose' if you want INFO messages logged, ' ' if not
String include_values_diffs =  args[1] // example 'Y' if you want to see values differences, 'N' if not

// build the file path
String homeDir = System.getProperty("user.home")
String fileBase = homeDir + "/ng_refactor/"
String beforeFileName = fileBase + "before/generic_attributes.json"
String afterFileName = fileBase + "after/generic_attributes.json"
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
Boolean fileNotFound = false

// create the file for log output
def logOutput = new File(fileBase + "log_output.txt")
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
        if(log_level == 'verbose') {
            logWriter.println("INFO: Attribute ${attributeID} matches in both files.")
        }
        logWriter.flush()
    }
    else {

        // determine if the attribute exists at all in the after json file
        for(i=0; i<afterJson.size(); i++) {
            afterJsonAttributeID = afterJson[i].get('id')

            // attribute exists
            if(afterJsonAttributeID == attributeID) {
                attributeExists = true
                // TODO: Only print this if include_values_diffs=Y or an attribute other than values differs
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
                        if(log_level == 'verbose') {
                            logWriter.println("--> INFO: ${attributeID}.${subAttributeKey} matches in both files.")
                        }
                        logWriter.flush()
                    }
                    else {

                        // determine if the sub-attribute exists at all in the afterJsonAttribute
                        if (afterJson[i].containsKey(beforeJsonSubAttribute.key)) {
                            if(subAttributeKey!='values' || include_values_diffs=='Y') {
                                logWriter.println("--> WARNING: ${attributeID}.${subAttributeKey} exists in both " +
                                        "files, but does not match.")
                            }
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

    fileNotFound = false
    // build the file path
    fileBase = homeDir + "/ng_refactor/"
    beforeFileName = fileBase + "before/" + attribute + ".json"
    afterFileName = fileBase + "after/" + attribute + ".json"
    beforeDoc = new File(beforeFileName)
    afterDoc = new File(afterFileName)

    // retrieve generic attributes json from file
    beforeJson = new JsonSlurper().parse(beforeDoc)
    try {
        afterJson = new JsonSlurper().parse(afterDoc)
    }
    catch (ex) {
        fileNotFound = true
        logWriter.println("\n\nWARNING: File ${afterFileName} not found.")
        logWriter.flush()
    }

    if (fileNotFound == false ) {
        // TODO: Only print this if:
        // * log_level=verbose -or-
        // * a difference exists other than values
        // * a difference exists for values and include_values_diffs=Y
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
                    if(log_level == 'verbose') {
                        logWriter.println("INFO: ${beforeJsonEntry.key} matches in both files.")
                    }
                    logWriter.flush()
                } else {

                    // determine if the entry exists at all in the afterJson file
                    if (afterJson.containsKey(beforeJsonEntry.key)) {
                        // only print the warning if:
                        // * the key is not 'values' -or-
                        // * the key is values and parameter says to include value differences
                        if(beforeJsonEntry.key!='values' || include_values_diffs=='Y') {
                            logWriter.println("WARNING: ${beforeJsonEntry.key} exists in both files, but does not " +
                                    "match.")
                        }
                        logWriter.flush()
                    } else {
                        logWriter.println("WARNING: ${beforeJsonEntry.key} exists in ${beforeFileName}, but does not " +
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
            if (match == false) {
                logWriter.println("WARNING: ${afterJsonEntry.key} exists in ${afterFileName}, but does not exist in " +
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
            if (match == false) {
                logWriter.println("WARNING: Entry ${entryKey} exists in ${afterFileName}, but does not exist " +
                        "in ${beforeFileName}.")
                logWriter.flush()
            }
        }
    }
}
logWriter.close()