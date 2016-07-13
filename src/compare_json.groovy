import groovy.json.JsonSlurper
import java.util.logging.Logger

/**
 * Created by mfoley on 7/8/16.
 */
def document1 = new File("/Users/mfoley/site_4_generic_attributes.json")
def document2 = new File("/Users/mfoley/site_4_generic_attributes2.json")

def originalJson = new JsonSlurper().parse(document1)
def newJson = new JsonSlurper().parse(document2)

def match
def attributeID = new String()
def attributeIDList = []

def i = new Integer(0)
def newJsonAttributeID = new String()

def subAttributeKey = new String()
def subAttributeKeyList = []
def attributeExists

Logger logger = Logger.getLogger("")

match = false
attributeExists = false

// verify the size
try {
    assert originalJson.size() == newJson.size()
}
catch (AssertionError assertionError) {
    logger.warning "The two jsons do not have the same number of values."
}

// loop through the entire original json file
originalJson.each{ originalJsonAttribute ->

    // keep track of all the attributes we have checked already
    attributeID = originalJsonAttribute['id']
    attributeIDList << attributeID

    // determine if there is an attribute in the new json file that exactly matches
    // the attribute in the original json file
    match = newJson.any{ newJsonAttribute -> originalJsonAttribute.equals(newJsonAttribute) }

    if(match == true) {
        logger.info "Attribute ${attributeID} matches in both files."
    }
    else {

        // determine if the attribute exists at all in the new json file
        for(i=0; i<newJson.size(); i++) {
            newJsonAttributeID = newJson[i].get('id')

            // attribute exists
            if(newJsonAttributeID == attributeID) {
                attributeExists = true
                logger.warning "Attribute ${attributeID} exists in both files, but does not match."

                // loop through each sub-attribute of the originalJsonAttribute
                originalJsonAttribute.each { originalJsonSubAttribute ->

                    // keep track of all the sub-attributes we have checked already
                    subAttributeKey = originalJsonSubAttribute.key
                    subAttributeKeyList << subAttributeKey

                    // determine if there is a sub-attribute in the newJsonAttribute that exactly matches
                    // the sub-attribute from the original json attribute
                    match = newJson[i].any{ newJsonSubAttribute ->
                        originalJsonSubAttribute.equals(newJsonSubAttribute) }

                    if(match == true) {
                        logger.info "--> ${attributeID}.${subAttributeKey} matches in both files."
                    }
                    else {

                        // determine if the sub-attribute exists at all in the newJsonAttribute
                        if (newJson[i].containsKey(originalJsonSubAttribute.key)) {
                            logger.warning "--> ${attributeID}.${subAttributeKey} exists in both files, " +
                                    "but does not match."
                        }
                        else {
                            logger.warning "--> ${attributeID}.${subAttributeKey} exists in originalJson, " +
                                    "but does not exist in newJson."
                        }
                    }
                }

                // loop through each sub-attribute of the newJsonAttribute
                newJson[i].each {

                    // check if we've already checked this sub-attribute
                    subAttributeKey = it.key
                    match = subAttributeKeyList.contains(subAttributeKey)

                    // if we didn't already check it, it must be missing from the originalJson
                    if(match == false)
                        logger.warning "--> ${attributeID}.${subAttributeKey} exists in newJson, but does exist in" +
                                " originalJson."
                }

                subAttributeKeyList.clear()
                break
            }
        }

        // attribute does not exist at all in the new json file
        if(attributeExists == false)
            logger.warning "Attribute ${attributeID} exists in originalJson, but does not exist in newJson."

        attributeExists = false
    }
}

// loop through the newJson
newJson.each { newJsonEntry ->

    // check if we've already checked this attribute
    attributeID = newJsonEntry['id']
    match = attributeIDList.contains(attributeID)

    // if we didn't already check it, it must be missing from the originalJson
    if(match == false)
        logger.warning "Attribute ${attributeID} exists in newJson, but does not exist in originalJson."
}