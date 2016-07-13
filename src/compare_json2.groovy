import groovy.json.JsonSlurper

/**
 * Created by mfoley on 7/8/16.
 */
def document1 = new File("/Users/mfoley/site_4_generic_attributes.json")
def document2 = new File("/Users/mfoley/site_4_generic_attributes2.json")
def f = new File("/Users/mfoley/site_4_error_output.txt")
def map1 = new JsonSlurper().parseText(document1.text)
def map2 = new JsonSlurper().parseText(document2.text)

try {
    assert map1 == map2
}
catch(AssertionError assertionError) {
    f.createNewFile()
    writer = new PrintWriter(f)

    writer.println(assertionError)

    writer.close()
}