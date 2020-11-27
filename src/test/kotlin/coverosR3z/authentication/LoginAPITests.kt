package coverosR3z.authentication

import coverosR3z.DEFAULT_PASSWORD
import coverosR3z.DEFAULT_USER
import coverosR3z.domainobjects.*
import coverosR3z.misc.toStr
import org.junit.Assert
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Before
import org.junit.Test

class LoginAPITests {

    lateinit var au : IAuthenticationUtilities

    @Before
    fun init() {
        au = FakeAuthenticationUtilities()
    }

    /**
     * Happy path - all values provided as needed
     */
    @Test
    fun testHandlePostLogin_happyPath() {
        val data = mapOf(
                "username" to DEFAULT_USER.name.value,
                "password" to DEFAULT_PASSWORD.value)
        val responseData = handlePOSTLogin(au, NO_USER, data)
        Assert.assertTrue("The system should indicate success.  File was $responseData",
                toStr(responseData.fileContents).contains("SUCCESS"))
        Assert.assertTrue("A cookie should be set if valid login",
                responseData.headers.any { it.contains("Set-Cookie") })
    }

    /**
     * Error path - username is not passed in
     */
    @Test
    fun testHandlePostLogin_missingUser() {
        val data = mapOf(
                "password" to DEFAULT_PASSWORD.value)
        val ex = assertThrows(IllegalStateException::class.java){handlePOSTLogin(au, NO_USER, data)}
        assertEquals(usernameNotNullMsg, ex.message)
    }

    /**
     * Error path - username is blank
     */
    @Test
    fun testHandlePostLogin_blankUser() {
        val data = mapOf(
                "username" to "",
                "password" to DEFAULT_PASSWORD.value)
        val ex = assertThrows(IllegalArgumentException::class.java) { handlePOSTLogin(au, NO_USER, data) }
        assertEquals(usernameCannotBeEmptyMsg, ex.message)
    }

    /**
     * Error path - password is not passed in
     */
    @Test
    fun testHandlePostLogin_missingPassword() {
        val data = mapOf(
            "username" to DEFAULT_USER.name.value)
        val ex = assertThrows(IllegalStateException::class.java){handlePOSTLogin(au, NO_USER, data)}
        assertEquals(passwordMustNotBeNullMsg, ex.message)
    }

    /**
     * Error path - password is blank
     */
    @Test
    fun testHandlePostLogin_blankPassword() {
        val data = mapOf(
            "username" to DEFAULT_USER.name.value,
            "password" to "")
        val ex = assertThrows(java.lang.IllegalArgumentException::class.java) { handlePOSTLogin(au, NO_USER, data) }
        assertEquals(passwordMustNotBeBlankMsg, ex.message)

    }

}