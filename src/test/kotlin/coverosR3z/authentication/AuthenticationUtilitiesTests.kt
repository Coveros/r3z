package coverosR3z.authentication

import coverosR3z.domainobjects.RegistrationResult
import org.junit.Assert.assertEquals
import org.junit.Test

class AuthenticationUtilitiesTests {

    @Test
    fun `It should not be possible to register a new user with an empty password`() {
        val authUtils = AuthenticationUtilities(FakeAuthPersistence())
        val expected = RegistrationResult.FAILURE

        val result = authUtils.register("matt", "")

        assertEquals("the result should clearly indicate an empty password", expected, result)
    }
}