package coverosR3z.timerecording

import coverosR3z.DEFAULT_EMPLOYEE_NAME
import coverosR3z.DEFAULT_USER
import coverosR3z.authentication.FakeAuthenticationUtilities
import coverosR3z.authentication.IAuthenticationUtilities
import coverosR3z.domainobjects.employeeNameNotNullMsg
import coverosR3z.exceptions.InexactInputsException
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Before
import org.junit.Test
import java.lang.IllegalArgumentException

class EmployeeAPITests {


    lateinit var au : IAuthenticationUtilities
    lateinit var tru : ITimeRecordingUtilities

    @Before
    fun init() {
        au = FakeAuthenticationUtilities()
        tru = FakeTimeRecordingUtilities()
    }

    /**
     * A basic happy path
     */
    @Test
    fun testHandlePOSTNewEmployee() {
        val data = mapOf(EmployeeElements.EMPLOYEE_INPUT.elemName to DEFAULT_EMPLOYEE_NAME.value)
        handlePOSTNewEmployee(tru, DEFAULT_USER, data)
    }

    /**
     * Huge name
     */
    @Test
    fun testHandlePOSTNewEmployee_HugeName() {
        val data = mapOf(EmployeeElements.EMPLOYEE_INPUT.elemName to "a".repeat(31))
        val ex = assertThrows(IllegalArgumentException::class.java){handlePOSTNewEmployee(tru, DEFAULT_USER, data)}
        assertEquals("Max size of employee name is 30", ex.message)
    }

    /**
     * Big name, but acceptable
     */
    @Test
    fun testHandlePOSTNewEmployee_BigName() {
        val data = mapOf(EmployeeElements.EMPLOYEE_INPUT.elemName to "a".repeat(30))
        handlePOSTNewEmployee(tru, DEFAULT_USER, data)
    }

    /**
     * Missing data
     */
    @Test
    fun testHandlePOSTNewEmployee_noBody() {
        val data = emptyMap<String,String>()
        val ex = assertThrows(InexactInputsException::class.java){handlePOSTNewEmployee(tru, DEFAULT_USER, data)}
        assertEquals("expected keys: [employee_name]. received keys: []", ex.message)
    }
}