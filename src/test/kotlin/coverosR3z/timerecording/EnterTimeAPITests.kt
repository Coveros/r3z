package coverosR3z.timerecording

import coverosR3z.*
import coverosR3z.authentication.*
import coverosR3z.domainobjects.*
import coverosR3z.exceptions.InexactInputsException
import coverosR3z.misc.toStr
import coverosR3z.server.*
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class EnterTimeAPITests {

    lateinit var au : FakeAuthenticationUtilities
    lateinit var tru : FakeTimeRecordingUtilities

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
        val data = mapOf(
                EnterTimeElements.PROJECT_INPUT.elemName to "1",
                EnterTimeElements.TIME_INPUT.elemName to "60",
                EnterTimeElements.DETAIL_INPUT.elemName to "not much to say",
                EnterTimeElements.DATE_INPUT.elemName to DEFAULT_DATE_STRING)
        val response = handlePOSTTimeEntry(tru, DEFAULT_USER,data).fileContents
        assertTrue("we should have gotten the success page.  Got: $response", toStr(response).contains("SUCCESS"))
    }

    /**
     * If we are missing required data
     */
    @Test
    fun testHandlePOSTTimeEntry_missingProject() {
        val data = mapOf(
                EnterTimeElements.TIME_INPUT.elemName to "60",
                EnterTimeElements.DETAIL_INPUT.elemName to "not much to say",
                EnterTimeElements.DATE_INPUT.elemName to DEFAULT_DATE_STRING)
        val ex = assertThrows(InexactInputsException::class.java){ handlePOSTTimeEntry(tru, DEFAULT_USER,data) }
        assertEquals("expected keys: [project_entry, time_entry, detail_entry, date_entry]. received keys: [time_entry, detail_entry, date_entry]", ex.message)
    }

    /**
     * If we are missing required data
     */
    @Test
    fun testHandlePOSTTimeEntry_missingTimeEntry() {
        val data = mapOf(
                EnterTimeElements.PROJECT_INPUT.elemName to "1",
                EnterTimeElements.DETAIL_INPUT.elemName to "not much to say",
                EnterTimeElements.DATE_INPUT.elemName to DEFAULT_DATE_STRING)
        val ex = assertThrows(InexactInputsException::class.java){ handlePOSTTimeEntry(tru, DEFAULT_USER,data) }
        assertEquals("expected keys: [project_entry, time_entry, detail_entry, date_entry]. received keys: [project_entry, detail_entry, date_entry]", ex.message)
    }

    /**
     * If we are missing required data
     */
    @Test
    fun testHandlePOSTTimeEntry_missingDetailEntry() {
        val data = mapOf(
                EnterTimeElements.PROJECT_INPUT.elemName to "1",
                EnterTimeElements.TIME_INPUT.elemName to "60",
                EnterTimeElements.DATE_INPUT.elemName to DEFAULT_DATE_STRING,
        )
        val ex = assertThrows(InexactInputsException::class.java){ handlePOSTTimeEntry(tru, DEFAULT_USER, data) }
        assertEquals("expected keys: [project_entry, time_entry, detail_entry, date_entry]. received keys: [project_entry, time_entry, date_entry]", ex.message)
    }

    /**
     * If we are missing required data
     */
    @Test
    fun testHandlePOSTTimeEntry_missingEmployee() {
        val data = mapOf(
                EnterTimeElements.PROJECT_INPUT.elemName to "1",
                EnterTimeElements.TIME_INPUT.elemName to "60",
                EnterTimeElements.DETAIL_INPUT.elemName to "not much to say",
                EnterTimeElements.DATE_INPUT.elemName to DEFAULT_DATE_STRING)
        val employeeId = null
        val ex = assertThrows(IllegalStateException::class.java){
            handlePOSTTimeEntry(tru, User(UserId(1), UserName("name"), DEFAULT_HASH, DEFAULT_SALT, employeeId),data)
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
        val data = mapOf(EnterTimeElements.PROJECT_INPUT.elemName to "aaaaa", EnterTimeElements.TIME_INPUT.elemName to "60", EnterTimeElements.DETAIL_INPUT.elemName to "not much to say", EnterTimeElements.DATE_INPUT.elemName to A_RANDOM_DAY_IN_JUNE_2020.epochDay.toString())
        val ex = assertThrows(IllegalStateException::class.java){ handlePOSTTimeEntry(tru, DEFAULT_USER,data) }
        assertEquals("Must be able to parse aaaaa as integer", ex.message)
    }

    /**
     * If we pass in a negative number as the project id
     */
    @Test
    fun testHandlePOSTTimeEntry_negativeProject() {
        val data = mapOf(EnterTimeElements.PROJECT_INPUT.elemName to "-1", EnterTimeElements.TIME_INPUT.elemName to "60", EnterTimeElements.DETAIL_INPUT.elemName to "not much to say", EnterTimeElements.DATE_INPUT.elemName to A_RANDOM_DAY_IN_JUNE_2020.epochDay.toString())
        val ex = assertThrows(IllegalArgumentException::class.java){ handlePOSTTimeEntry(tru, DEFAULT_USER,data) }
        assertEquals("Valid identifier values are 1 or above", ex.message)
    }

    /**
     * If we pass in 0 as the project id
     */
    @Test
    fun testHandlePOSTTimeEntry_zeroProject() {
        val data = mapOf(EnterTimeElements.PROJECT_INPUT.elemName to "0", EnterTimeElements.TIME_INPUT.elemName to "60", EnterTimeElements.DETAIL_INPUT.elemName to "not much to say", EnterTimeElements.DATE_INPUT.elemName to A_RANDOM_DAY_IN_JUNE_2020.epochDay.toString())
        val ex = assertThrows(IllegalArgumentException::class.java){ handlePOSTTimeEntry(tru, DEFAULT_USER,data) }
        assertEquals("Valid identifier values are 1 or above", ex.message)
    }

    /**
     * If the project id passed is above the maximum id
     */
    @Test
    fun testHandlePOSTTimeEntry_aboveMaxProject() {
        val data = mapOf(EnterTimeElements.PROJECT_INPUT.elemName to (maximumProjectsCount+1).toString(), EnterTimeElements.TIME_INPUT.elemName to "60", EnterTimeElements.DETAIL_INPUT.elemName to "not much to say", EnterTimeElements.DATE_INPUT.elemName to A_RANDOM_DAY_IN_JUNE_2020.epochDay.toString())
        val ex = assertThrows(IllegalArgumentException::class.java){ handlePOSTTimeEntry(tru, DEFAULT_USER,data) }
        assertEquals("No project id allowed over $maximumProjectsCount", ex.message)
    }


    /**
     * If the time entered is more than a day's worth
     */
    @Test
    fun testHandlePOSTTimeEntry_aboveMaxTime() {
        val data = mapOf(EnterTimeElements.PROJECT_INPUT.elemName to "1", EnterTimeElements.TIME_INPUT.elemName to ((60*60*24)+1).toString(), EnterTimeElements.DETAIL_INPUT.elemName to "not much to say", EnterTimeElements.DATE_INPUT.elemName to A_RANDOM_DAY_IN_JUNE_2020.epochDay.toString())
        val ex = assertThrows(IllegalArgumentException::class.java){ handlePOSTTimeEntry(tru, DEFAULT_USER,data) }
        assertEquals("${lessThanTimeInDayMsg}86401", ex.message)
    }

    /**
     * If the time entered is negative
     */
    @Test
    fun testHandlePOSTTimeEntry_negativeTime() {
        val data = mapOf(EnterTimeElements.PROJECT_INPUT.elemName to "1", EnterTimeElements.TIME_INPUT.elemName to "-60", EnterTimeElements.DETAIL_INPUT.elemName to "not much to say", EnterTimeElements.DATE_INPUT.elemName to A_RANDOM_DAY_IN_JUNE_2020.epochDay.toString())
        val ex = assertThrows(IllegalArgumentException::class.java){ handlePOSTTimeEntry(tru, DEFAULT_USER,data) }
        assertEquals("${noNegativeTimeMsg}-60", ex.message)
    }

    /**
     * If the time entered is zero, it's fine.
     */
    @Test
    fun testHandlePOSTTimeEntry_zeroTime() {
        val data = mapOf(
                EnterTimeElements.PROJECT_INPUT.elemName to "1",
                EnterTimeElements.TIME_INPUT.elemName to "0",
                EnterTimeElements.DETAIL_INPUT.elemName to "not much to say",
                EnterTimeElements.DATE_INPUT.elemName to DEFAULT_DATE_STRING)
        val result = handlePOSTTimeEntry(tru, DEFAULT_USER,data).fileContents
        assertTrue("we should have gotten the success page.  Got: $result", toStr(result).contains("SUCCESS"))
    }

    /**
     * If the time entered is non-numeric, like "a"
     */
    @Test
    fun testHandlePOSTTimeEntry_nonNumericTime() {
        val data = mapOf(
                EnterTimeElements.PROJECT_INPUT.elemName to "1",
                EnterTimeElements.TIME_INPUT.elemName to "aaa",
                EnterTimeElements.DETAIL_INPUT.elemName to "not much to say",
                EnterTimeElements.DATE_INPUT.elemName to DEFAULT_DATE_STRING)
        val ex = assertThrows(IllegalStateException::class.java){ handlePOSTTimeEntry(tru, DEFAULT_USER, data) }
        assertEquals("Must be able to parse aaa as integer", ex.message)
    }

    //ToDo Add negative tests for date (outside of date range, malformed, etc.)

    /**
     * Just to check that we get the proper OK result when we authenticate.
     */
    @Test
    fun testDoGETTimeEntriesPage() {
        val rd = createRequestData(user = DEFAULT_USER)
        val result = doGetTimeEntriesPage(tru, rd)
        assertEquals(StatusCode.OK, result.statusCode)
    }

    /**
     * Checking some of the content
     */
    @Test
    fun testDoGETTimeEntriesPage_content() {
        val rd = createRequestData(user = DEFAULT_USER)
        tru.getAllEntriesForEmployeeBehavior = {setOf(TimeEntry(1, DEFAULT_EMPLOYEE, DEFAULT_PROJECT, DEFAULT_TIME, A_RANDOM_DAY_IN_JUNE_2020, Details("whatevs")))}

        val result = toStr(doGetTimeEntriesPage(tru, rd).fileContents)

        assertTrue("page should have this content.  Page:\n$result", result.contains("<tr><td>Default_Project</td><td>60</td><td>whatevs</td><td>2020-06-25</td></tr>"))
    }

    /**
     * JIf we aren't authenticated, we should get redirected back to
     * the homepage.  We'll just check a redirect happened.
     */
    @Test
    fun testDoGETTimeEntriesPageUnAuth() {
        val rd = createRequestData(user = NO_USER)
        val result = doGetTimeEntriesPage(tru, rd)
        assertEquals(StatusCode.SEE_OTHER, result.statusCode)
    }

    /**
     * Just to check that we get the proper OK result when we authenticate.
     */
    @Test
    fun testDoGETEnterTimePage() {
        val rd = createRequestData(user = DEFAULT_USER)
        val result = doGETEnterTimePage(tru, rd)
        assertEquals(StatusCode.OK, result.statusCode)
    }

    /**
     * JIf we aren't authenticated, we should get redirected back to
     * the homepage.  We'll just check a redirect happened.
     */
    @Test
    fun testDoGETEnterTimePageUnAuth() {
        val rd = createRequestData(user = NO_USER)
        val result = doGETEnterTimePage(tru, rd)
        assertEquals(StatusCode.SEE_OTHER, result.statusCode)
    }

    /**
     * What should happen if we send too many inputs to the API?
     * It should complain.  We want precision.
     *
     * In this test we expect project, time, detail, and date to
     * be sent.  If we get project, time, detail, FOO, and date,
     * we throw the exception
     *
     * See [InexactInputsException]
     */
    @Test
    fun testDoPOST_TooManyInputs() {
        val data = mapOf(
                EnterTimeElements.PROJECT_INPUT.elemName to "1",
                EnterTimeElements.TIME_INPUT.elemName to "60",
                EnterTimeElements.DETAIL_INPUT.elemName to "not much to say",
                "foo" to "bar",
                EnterTimeElements.DATE_INPUT.elemName to DEFAULT_DATE_STRING)

        val ex = assertThrows(InexactInputsException::class.java) { handlePOSTTimeEntry(tru, DEFAULT_USER,data).fileContents }
        assertEquals("expected keys: [project_entry, time_entry, detail_entry, date_entry]. received keys: [project_entry, time_entry, detail_entry, foo, date_entry]", ex.message)
    }

    /*
     _ _       _                  __ __        _    _           _
    | | | ___ | | ___  ___  _ _  |  \  \ ___ _| |_ | |_  ___  _| | ___
    |   |/ ._>| || . \/ ._>| '_> |     |/ ._> | |  | . |/ . \/ . |<_-<
    |_|_|\___.|_||  _/\___.|_|   |_|_|_|\___. |_|  |_|_|\___/\___|/__/
                 |_|
     alt-text: Helper Methods
     */

    /**
     * A helper method to make a [AnalyzedHttpData] easier.
     */
    private fun createRequestData(
        verb: Verb = Verb.GET,
        path: String = "(NOTHING REQUESTED)",
        data : Map<String, String> = emptyMap(),
        user : User = NO_USER,
        sessionToken : String = "NO TOKEN"
    ): AnalyzedHttpData {
        return AnalyzedHttpData(verb, path, data, user, sessionToken, emptyList(), null)
    }

}