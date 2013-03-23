/**
 * Test case for the "create-domain-class" Griffon command.
 */

import griffon.test.AbstractCliTestCase

class CreateDomainClassTests extends AbstractCliTestCase {
    void testDefault() {
        execute(["create-domain-class"])

        assertEquals 0, waitForProcess()
        verifyHeader()

        // Make sure that the script was found.
        assertFalse "CreateDomainClass script not found.", output.contains("Script not found:")
    }
}
