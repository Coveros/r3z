package coverosR3z.timerecording

import coverosR3z.DEFAULT_PASSWORD
import coverosR3z.DEFAULT_USER
import coverosR3z.authentication.*
import coverosR3z.domainobjects.*
import coverosR3z.misc.toStr
import coverosR3z.server.*
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class EnterTimeAPITests {

    lateinit var au : IAuthenticationUtilities
    lateinit var tru : ITimeRecordingUtilities

    @Before
    fun init() {
        au = FakeAuthenticationUtilities()
        tru = FakeTimeRecordingUtilities()
    }

    /**
     * If we pass in valid information, it should indicate success
     */
    @Test
    fun testHandlePOSTTimeEntry() {
        val data = mapOf("project_entry" to "1", "time_entry" to "60", "detail_entry" to "not much to say")
        val response = handlePOSTTimeEntry(tru, DEFAULT_USER,data).fileContents
        assertTrue("we should have gotten the success page.  Got: $response", toStr(response).contains("SUCCESS"))
    }

    /**
     * If we are missing required data
     */
    @Test
    fun testHandlePOSTTimeEntry_missingProject() {
        val data = mapOf("time_entry" to "60", "detail_entry" to "not much to say")
        val ex = assertThrows(IllegalStateException::class.java){handlePOSTTimeEntry(tru, DEFAULT_USER,data)}
        assertEquals(projectIdNotNullMsg, ex.message)
    }

    /**
     * If we are missing required data
     */
    @Test
    fun testHandlePOSTTimeEntry_missingTimeEntry() {
        val data = mapOf("project_entry" to "1", "detail_entry" to "not much to say")
        val ex = assertThrows(IllegalStateException::class.java){handlePOSTTimeEntry(tru, DEFAULT_USER,data)}
        assertEquals(timeNotNullMsg, ex.message)
    }

    /**
     * If we are missing required data
     */
    @Test
    fun testHandlePOSTTimeEntry_missingDetailEntry() {
        val data = mapOf("project_entry" to "1", "time_entry" to "60")
        val ex = assertThrows(IllegalStateException::class.java){handlePOSTTimeEntry(tru, DEFAULT_USER,data)}
        assertEquals(detailsNotNullMsg, ex.message)
    }

    /**
     * If we are missing required data
     */
    @Test
    fun testHandlePOSTTimeEntry_missingEmployee() {
        val data = mapOf("project_entry" to "1", "time_entry" to "60", "detail_entry" to "not much to say")
        val employeeId = null
        val ex = assertThrows(IllegalStateException::class.java){
            handlePOSTTimeEntry(tru, User(UserId(1), UserName("name"), Hash.createHash(DEFAULT_PASSWORD), Salt(""), employeeId),data)
        }
        assertEquals(employeeIdNotNullMsg, ex.message)
    }

    /**
     * If we aren't authenticated, react appropriately
     */
    @Test
    fun testHandlePOSTTimeEntry_unauthenticated() {
        val response = handlePOSTTimeEntry(tru, NO_USER, emptyMap())
        assertEquals(handleUnauthorized(), response)
    }

    /**
     * If we pass in something that cannot be parsed as an integer as the project id
     */
    @Test
    fun testHandlePOSTTimeEntry_nonNumericProject() {
        val data = mapOf("project_entry" to "aaaaa", "time_entry" to "60", "detail_entry" to "not much to say")
        val ex = assertThrows(IllegalStateException::class.java){handlePOSTTimeEntry(tru, DEFAULT_USER,data).fileContents}
        assertEquals("Must be able to parse aaaaa as integer", ex.message)
    }

    /**
     * If we pass in a negative number as the project id
     */
    @Test
    fun testHandlePOSTTimeEntry_negativeProject() {
        val data = mapOf("project_entry" to "-1", "time_entry" to "60", "detail_entry" to "not much to say")
        val ex = assertThrows(IllegalArgumentException::class.java){handlePOSTTimeEntry(tru, DEFAULT_USER,data).fileContents}
        assertEquals("Valid identifier values are 1 or above", ex.message)
    }

    /**
     * If we pass in 0 as the project id
     */
    @Test
    fun testHandlePOSTTimeEntry_zeroProject() {
        val data = mapOf("project_entry" to "0", "time_entry" to "60", "detail_entry" to "not much to say")
        val ex = assertThrows(IllegalArgumentException::class.java){handlePOSTTimeEntry(tru, DEFAULT_USER,data).fileContents}
        assertEquals("Valid identifier values are 1 or above", ex.message)
    }

    /**
     * If the project id passed is above the maximum id
     */
    @Test
    fun testHandlePOSTTimeEntry_aboveMaxProject() {
        val data = mapOf("project_entry" to (maximumProjectsCount+1).toString(), "time_entry" to "60", "detail_entry" to "not much to say")
        val ex = assertThrows(IllegalArgumentException::class.java){handlePOSTTimeEntry(tru, DEFAULT_USER,data).fileContents}
        assertEquals("No project id allowed over $maximumProjectsCount", ex.message)
    }


    /**
     * If the time entered is more than a day's worth
     */
    @Test
    fun testHandlePOSTTimeEntry_aboveMaxTime() {
        val data = mapOf("project_entry" to "1", "time_entry" to ((60*60*24)+1).toString(), "detail_entry" to "not much to say")
        val ex = assertThrows(IllegalArgumentException::class.java){handlePOSTTimeEntry(tru, DEFAULT_USER,data).fileContents}
        assertEquals("Entries do not span multiple days, thus must be <=24 hrs", ex.message)
    }

    /**
     * If the time entered is negative
     */
    @Test
    fun testHandlePOSTTimeEntry_negativeTime() {
        val data = mapOf("project_entry" to "1", "time_entry" to "-60", "detail_entry" to "not much to say")
        val ex = assertThrows(IllegalArgumentException::class.java){handlePOSTTimeEntry(tru, DEFAULT_USER,data).fileContents}
        assertEquals("Doesn't make sense to have zero or negative time", ex.message)
    }

    /**
     * If the time entered is zero
     */
    @Test
    fun testHandlePOSTTimeEntry_zeroTime() {
        val data = mapOf("project_entry" to "1", "time_entry" to "0", "detail_entry" to "not much to say")
        val ex = assertThrows(IllegalArgumentException::class.java){handlePOSTTimeEntry(tru, DEFAULT_USER,data).fileContents}
        assertEquals("Doesn't make sense to have zero or negative time", ex.message)
    }

    /**
     * If the time entered is non-numeric, like "a"
     */
    @Test
    fun testHandlePOSTTimeEntry_nonNumericTime() {
        val data = mapOf("project_entry" to "1", "time_entry" to "aaa", "detail_entry" to "not much to say")
        val ex = assertThrows(IllegalStateException::class.java){handlePOSTTimeEntry(tru, DEFAULT_USER,data).fileContents}
        assertEquals("Must be able to parse aaa as integer", ex.message)
    }

    /**
     * Just to check that we get the proper OK result when we authenticate.
     */
    @Test
    fun testDoGetTimeEntriesPage() {
        val rd = createRequestData(user = DEFAULT_USER)
        val result = doGetTimeEntriesPage(tru, rd)
        assertEquals(ResponseStatus.OK, result.responseStatus)
    }

    /**
     * JIf we aren't authenticated, we should get redirected back to
     * the homepage.  We'll just check a redirect happened.
     */
    @Test
    fun testDoGetTimeEntriesPageUnAuth() {
        val rd = createRequestData(user = NO_USER)
        val result = doGetTimeEntriesPage(tru, rd)
        assertEquals(ResponseStatus.SEE_OTHER, result.responseStatus)
    }

    /**
     * Just to check that we get the proper OK result when we authenticate.
     */
    @Test
    fun testDoGETEnterTimePage() {
        val rd = createRequestData(user = DEFAULT_USER)
        val result = doGETEnterTimePage(tru, rd)
        assertEquals(ResponseStatus.OK, result.responseStatus)
    }

    /**
     * JIf we aren't authenticated, we should get redirected back to
     * the homepage.  We'll just check a redirect happened.
     */
    @Test
    fun testDoGETEnterTimePageUnAuth() {
        val rd = createRequestData(user = NO_USER)
        val result = doGETEnterTimePage(tru, rd)
        assertEquals(ResponseStatus.SEE_OTHER, result.responseStatus)
    }

    /**
     * A helper method to make a [RequestData] easier.
     */
    private fun createRequestData(
        verb: Verb = Verb.GET,
        path: String = "(NOTHING REQUESTED)",
        data : Map<String, String> = emptyMap(),
        user : User = NO_USER,
        sessionToken : String = "NO TOKEN"
    ): RequestData {
        return RequestData(verb, path, data, user, sessionToken)
    }

}