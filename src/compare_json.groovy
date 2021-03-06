import groovy.json.JsonSlurper
import java.util.logging.Logger

/**
 * Created by mfoley on 7/8/16.
 */

// parameters passed into script
String log_level =  args[0] // 'verbose' if you want INFO messages logged, ' ' if not
String include_values_diffs =  args[1] // 'Y' if you want to see values differences, 'N' if not
String include_parent_id_diffs = args[2] // 'Y' if you want to see parent_id differences, 'N' if not
String include_attributes_diffs = args[3] // 'Y' if you want to see attributes differences, 'N' if not

// create the list of attribute types to exclude given the parameters input
def excludeList = []
if (include_values_diffs == 'N') {
    excludeList << "values"
}
if (include_parent_id_diffs == 'N') {
    excludeList << "parent_id"
}
if (include_attributes_diffs == 'N') {
    excludeList << "attributes"
}
Boolean exclude = false

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
Boolean headerPrinted = false
def ruleTypeMap = [:]
String ruleType
def ruleOperatorMap = [:]
String ruleOperator
def ruleValueMap = [:]
String ruleValue
def ruleMap = [:]
def rulesChecked = []

// create the file for log output
def logOutput = new File(fileBase + "log_output.txt")
logOutput.createNewFile()
def logWriter = new PrintWriter(logOutput)

// write header for file
logWriter.println("comparing generic_attributes\n")
logWriter.flush()

// verify the size
try {
    assert beforeJson.size() == afterJson.size()
}
catch (AssertionError assertionError) {
    logWriter.println("WARNING: The two jsons do not have the same number of values.\n")
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
        // attribute exactly matches in the before and after files

        // only log INFO messages if parameter asked for verbose logging
        if(log_level == 'verbose') {
            logWriter.println("INFO: Attribute ${attributeID} matches in both files.")
        }
        logWriter.flush()
    }
    else {
        // attribute does not have an exact match in the before and after files

        // determine if the attribute exists at all in the after json file
        for(i=0; i<afterJson.size(); i++) {
            afterJsonAttributeID = afterJson[i].get('id')

            if(afterJsonAttributeID == attributeID) {
                // attribute exists in both files, but differs

                attributeExists = true
                headerPrinted = false

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
                        // sub-attribute exactly matches in the before and after files

                        // only log INFO messages if parameter asked for verbose logging
                        if(log_level == 'verbose') {
                            // print header for this attribute if it hasn't already been printed
                            if(headerPrinted == false) {
                                logWriter.println("WARNING: Attribute ${attributeID} exists in both files, but does " +
                                        "not match.")
                                logWriter.flush()
                                headerPrinted = true
                            }
                            logWriter.println("--> INFO: ${attributeID}.${subAttributeKey} matches in both files.")
                        }
                        logWriter.flush()
                    }
                    else {
                        // sub-attribute does not have an exact match in the before and after files

                        // determine if the sub-attribute exists at all in the afterJsonAttribute
                        if (afterJson[i].containsKey(beforeJsonSubAttribute.key)) {
                            // sub-attribute exists

                            // determine if we want to report on this sub-attribute
                            exclude = false
                            exclude = excludeList.contains(subAttributeKey)

                            // write to log file if:
                            // log_level is verbose -or-
                            // sub-attribute is not one we want to exclude
                            if (log_level == 'verbose' || exclude == false) {
                                // print header for this attribute if it hasn't already been printed
                                if(headerPrinted == false) {
                                    logWriter.println("WARNING: Attribute ${attributeID} exists in both files, but " +
                                            "does not match.")
                                    logWriter.flush()
                                    headerPrinted = true
                                }
                                logWriter.println("--> WARNING: ${attributeID}.${subAttributeKey} exists in both " +
                                        "files, but does not match.")
                                logWriter.flush()
                            }
                        }
                        else {
                            // sub-attribute does not exist

                            // determine if we want to report on this sub-attribute
                            exclude = false
                            exclude = excludeList.contains(subAttributeKey)

                            // write to log file if:
                            // log_level is verbose -or-
                            // sub-attribute is not one we want to exclude
                            if (log_level == 'verbose' || exclude == false) {
                                // print header for this attribute if it hasn't already been printed
                                if(headerPrinted == false) {
                                    logWriter.println("WARNING: Attribute ${attributeID} exists in both files, but " +
                                            "does not match.")
                                    logWriter.flush()
                                    headerPrinted = true
                                }
                                logWriter.println("--> WARNING: ${attributeID}.${subAttributeKey} exists in " +
                                        "${beforeFileName}, but does not exist in ${afterFileName}.")
                                logWriter.flush()
                            }
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

                        // determine if we want to report on this sub-attribute
                        exclude = false
                        exclude = excludeList.contains(subAttributeKey)

                        // write to log file if:
                        // log_level is verbose -or-
                        // sub-attribute is not one we want to exclude
                        if (log_level == 'verbose' || exclude == false) {
                            // print header for this attribute if it hasn't already been printed
                            if (headerPrinted == false) {
                                logWriter.println("WARNING: Attribute ${attributeID} exists in both files, but does " +
                                        "not match.")
                                logWriter.flush()
                                headerPrinted = true
                            }
                            logWriter.println("--> WARNING: ${attributeID}.${subAttributeKey} exists in " +
                                    "${afterFileName}, but does not exist in ${beforeFileName}.")
                            logWriter.flush()
                        }
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

logWriter.println("\n------------------------------------------------------------------------------------------\n")
logWriter.flush()

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

        headerPrinted = false

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
                    // entry exactly matches in the before and after files

                    // only print INFO messages if parameter asked for verbose logging
                    if(log_level == 'verbose') {
                        // print header for this file if it hasn't already been printed
                        if(headerPrinted == false) {
                            logWriter.println("\ncomparing generic_attributes/${attribute}")
                            logWriter.flush()
                            headerPrinted = true
                        }
                        logWriter.println("INFO: ${beforeJsonEntry.key} matches in both files.")
                    }
                    logWriter.flush()
                } else {
                    // entry does not have an exact match in the before and after files

                    // determine if the entry exists at all in the afterJson file
                    if (afterJson.containsKey(beforeJsonEntry.key)) {
                        // Entry exists in both files, but differs

                        // determine if we want to report on this sub-attribute
                        exclude = false
                        exclude = excludeList.contains(beforeJsonEntry.key)

                        // write to log file if:
                        // log_level is verbose -or-
                        // sub-attribute is not one we want to exclude
                        if (log_level == 'verbose' || exclude == false) {
                            // print header for this file if it hasn't already been printed
                            if(headerPrinted == false) {
                                logWriter.println("\ncomparing generic_attributes/${attribute}")
                                logWriter.flush()
                                headerPrinted = true
                            }
                            logWriter.println("WARNING: ${beforeJsonEntry.key} exists in both files, but does not " +
                                    "match.")
                            logWriter.flush()
                        }

                        // Supply details for rules differences
                        if(beforeJsonEntry.key == 'rules') {

                            // get all the rules in the afterJson
                            afterJsonRules = afterJson.get(beforeJsonEntry.key)
                            rulesChecked.clear()

                            // check each rule in the beforeJson for a match in the afterJson
                            beforeJsonEntry.value.each { beforeJsonRuleEntry ->

                                // keep track of all the rules we have checked already
                                ruleMap = beforeJsonRuleEntry
                                rulesChecked << ruleMap

                                // determine if there is a rule in the afterJson file that exactly matches
                                // the rule from the before json file
                                match = afterJsonRules.any { afterJsonRuleEntry ->
                                    beforeJsonRuleEntry.equals(afterJsonRuleEntry)
                                }

                                ruleTypeMap = beforeJsonRuleEntry.find { key, value -> key == 'type' }
                                ruleType = ruleTypeMap.value
                                ruleOperatorMap = beforeJsonRuleEntry.find { key, value -> key == 'operator' }
                                ruleOperator = ruleOperatorMap.value
                                ruleValueMap = beforeJsonRuleEntry.find { key, value -> key == 'value' }
                                ruleValue = ruleValueMap.value

                                if (match == true) {
                                    // rule exactly matches in the before and after files

                                    // only log matches if requested verbose logging
                                    if(log_level == 'verbose') {
                                        logWriter.println("--> INFO: ${ruleType} ${ruleOperator} ${ruleValue} + " +
                                                "match found.")
                                        logWriter.flush()
                                    }
                                } else {
                                    // exact match does not exist
                                    logWriter.println("--> WARNING: ${ruleType} ${ruleOperator} ${ruleValue} " +
                                            "match not found (specifics differ or rule is either new or missing).")
                                    logWriter.flush()
                                }
                            }

                            // loop through each of the afterJsonRules
                            afterJsonRules.each { afterJsonRuleEntry ->

                                // check if we've already checked this rule
                                match = rulesChecked.any { afterJsonRuleEntry }

                                // if we didn't already check it, it must be missing from the beforeJson
                                if (match == false) {
                                    logWriter.println("--> WARNING: ${ruleType} ${ruleOperator} ${ruleValue} " +
                                            "match not found (specifics differ or rule is either new or missing).")
                                    logWriter.flush()
                                }
                            }
                        }
                    } else {
                        // Entry exists in the before file but not the after

                        // determine if we want to report on this sub-attribute
                        exclude = false
                        exclude = excludeList.contains(beforeJsonEntry.key)

                        // write to log file if:
                        // log_level is verbose -or-
                        // sub-attribute is not one we want to exclude
                        if (log_level == 'verbose' || exclude == false) {
                            // print header for this file if it hasn't already been printed
                            if(headerPrinted == false) {
                                logWriter.println("\ncomparing generic_attributes/${attribute}")
                                logWriter.flush()
                                headerPrinted = true
                            }
                            logWriter.println("WARNING: ${beforeJsonEntry.key} exists in ${beforeFileName}, but does " +
                                    "not exist in ${afterFileName}.")
                            logWriter.flush()
                        }
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

                // determine if we want to report on this sub-attribute
                exclude = false
                exclude = excludeList.contains(afterJsonEntry.key)

                // write to log file if:
                // log_level is verbose -or-
                // sub-attribute is not one we want to exclude
                if (log_level == 'verbose' || exclude == false) {
                    // print header for this file if it hasn't already been printed
                    if (headerPrinted == false) {
                        logWriter.println("\ncomparing generic_attributes/${attribute}")
                        logWriter.flush()
                        headerPrinted = true
                    }
                    logWriter.println("WARNING: ${afterJsonEntry.key} exists in ${afterFileName}, but does not exist " +
                            "in ${beforeFileName}.")
                    logWriter.flush()
                }
            }
        }

        // loop through the afterJson
        afterJson.each { afterJsonEntry ->

            // check if we've already checked this attribute
            entryKey = afterJsonEntry.key
            match = entryKeyList.contains(entryKey)

            // if we didn't already check it, it must be missing from the beforeJson
            if (match == false) {

                // determine if we want to report on this sub-attribute
                exclude = false
                exclude = excludeList.contains(entryKey)

                // write to log file if:
                // log_level is verbose -or-
                // sub-attribute is not one we want to exclude
                if (log_level == 'verbose' || exclude == false) {

                    // print header for this file if it hasn't already been printed
                    if (headerPrinted == false) {
                        logWriter.println("\ncomparing generic_attributes/${attribute}")
                        logWriter.flush()
                        headerPrinted = true
                    }
                    logWriter.println("WARNING: Entry ${entryKey} exists in ${afterFileName}, but does not exist " +
                            "in ${beforeFileName}.")
                    logWriter.flush()
                }
            }
        }
    }
}
logWriter.close()