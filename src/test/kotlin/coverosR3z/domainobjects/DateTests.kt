package coverosR3z.domainobjects

import kotlinx.serialization.builtins.list
import kotlinx.serialization.json.JsonDecodingException
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test
import coverosR3z.jsonSerialzation as json

class DateTests {

    private val date = Date(18438)

    // Json also has .Default configuration which provides more reasonable settings,
    // but is subject to change in future versions


    @Test
    fun `can serialize Date with Kotlin serialization`() {
        // serializing objects
        val jsonData = json.stringify(Date.serializer(), date)
        assertEquals("""{"epochDay":18438}""", jsonData)

        // serializing lists
        val jsonList = json.stringify(Date.serializer().list, listOf(date))
        assertEquals("""[{"epochDay":18438}]""", jsonList)

        // parsing data back
        val obj: Date = json.parse(Date.serializer(), """{"epochDay":18438}""")
        assertEquals(date, obj)
    }

    /**
     * If the persisted data is corrupted, I want this to indicate what
     * was the problem so I can repair it.
     */
    @Test
    fun `failed deserialization should make it clear what went wrong`() {
        val ex = assertThrows(JsonDecodingException::class.java) { json.parse(Date.serializer(), """{"epochDay":18438L,"stringValue":"2020-06-25"}""") }
        assertEquals("Unexpected JSON token at offset 19: Failed to parse 'int'.\n" +
                " JSON input: {\"epochDay\":18438L,\"stringValue\":\"2020-06-25\"}", ex.message)
    }

    /**
     * If the persisted data is corrupted, I want this to indicate what
     * was the problem so I can repair it.
     */
    @Test
    fun `failed deserialization should make it clear what went wrong, too high a year`() {
        val ex = assertThrows(AssertionError::class.java) { json.parse(Date.serializer(), """{"epochDay":91438}""") }
        assertEquals("no way on earth people are using this before 2020 or past 2100, you had a date of 2220-05-08", ex.message)
    }
}