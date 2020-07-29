package coverosR3z.authentication

import coverosR3z.domainobjects.RegistrationResult
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class AuthenticationUtilitiesTests {
    lateinit var authUtils : AuthenticationUtilities

    @Before
    fun init() {
        authUtils = AuthenticationUtilities(FakeAuthPersistence())
    }

    @Test
    fun `It should not be possible to register a new user with an empty password`() {
        val result = authUtils.register("matt", "")

        assertEquals("the result should clearly indicate an empty password", RegistrationResult.EMPTY_PASSWORD, result)
    }

    /**
     * At a certain point, a password can be too long.  We'll call it at 100
     */
    @Test
    fun `It should not be possible to create a password longer than 100 characters`() {
        val password = "a".repeat(100)
        assert(password.length == 100)
        val result = authUtils.register("matt", password)

        assertEquals(RegistrationResult.PASSWORD_TOO_LONG, result)
    }

    @Test
    fun `A 99-character password should succeed`() {
        val password = "a".repeat(99)
        assert(password.length == 99)
        val result = authUtils.register("matt", password)

        assertEquals(RegistrationResult.SUCCESS, result)
    }

    @Test
    fun `A 101-character password should fail`() {
        val password = "a".repeat(101)
        assert(password.length == 101)
        val result = authUtils.register("matt", password)

        assertEquals(RegistrationResult.PASSWORD_TOO_LONG, result)
    }

}