package coverosR3z.server

import coverosR3z.FullSystem
import coverosR3z.authentication.FakeAuthenticationUtilities
import coverosR3z.authentication.api.LoginAPI
import coverosR3z.authentication.api.RegisterAPI
import coverosR3z.authentication.types.NO_USER
import coverosR3z.authentication.utility.IAuthenticationUtilities
import coverosR3z.config.utility.SystemOptions
import coverosR3z.fakeTechempower
import coverosR3z.logging.ILogger.Companion.logImperative
import coverosR3z.logging.LogTypes
import coverosR3z.misc.*
import coverosR3z.misc.types.Date
import coverosR3z.misc.utility.FileReader.Companion.read
import coverosR3z.misc.utility.encode
import coverosR3z.misc.utility.getTime
import coverosR3z.misc.utility.toStr
import coverosR3z.server.api.HomepageAPI
import coverosR3z.server.types.*
import coverosR3z.server.utility.CRLF
import coverosR3z.server.utility.SocketWrapper
import coverosR3z.server.utility.parseHttpMessage
import coverosR3z.timerecording.FakeTimeRecordingUtilities
import coverosR3z.timerecording.api.EnterTimeAPI
import org.junit.*
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.experimental.categories.Category
import java.io.File
import java.net.Socket
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import javax.net.ssl.SSLSocket
import javax.net.ssl.SSLSocketFactory

/**
 * Bear in mind this set of tests is to focus on the server functionality,
 * and *not* the underlying business code / database code.  That's why it
 * is fine to use fakes for the business code.  If you want to see
 * the server running with everything real, see [ServerPerformanceTests]
 */
class ServerTests {

    private lateinit var client : SocketWrapper

    @Before
    fun init() {
        // following is only used for ssl tests
        // note: the keystore is required by the ssl server.  See [SSLServer.init]
        val props = System.getProperties()
        props.setProperty("javax.net.ssl.trustStore", "src/test/resources/certs/truststore")
        props.setProperty("javax.net.ssl.trustStorePassword", "passphrase")

        val clientSocket = Socket("localhost", port)
        client = SocketWrapper(clientSocket, "client")
    }


    companion object {

        val redirectRegex = """Location: timeentries\?date=....-..-..""".toRegex()

        const val port = 2000
        const val sslTestPort = port + 443
        private lateinit var fs : FullSystem
        private val au = FakeAuthenticationUtilities()
        private val tru = FakeTimeRecordingUtilities()

        @JvmStatic
        @BeforeClass
        fun initServer() {
            fs = FullSystem.startSystem(SystemOptions(port = port, sslPort = sslTestPort), businessCode = BusinessCode(
                tru,
                au,
                fakeTechempower
            ))
        }

        @JvmStatic
        @AfterClass
        fun stopServer() {
            logImperative("stopping server")
            fs.shutdown()
        }
    }

    /**
     * If we try something and are unauthenticated,
     * receive a 401 error page
     */
    @IntegrationTest(usesPort = true)
    @Category(IntegrationTestCategory::class)
    @Test
    fun testShouldReturnUnauthenticatedAs401Page() {
        client.write("POST /entertime HTTP/1.1$CRLF")
        val body = "test=test"
        client.write("Content-Length: ${body.length}$CRLF$CRLF")
        client.write(body)

        val statusline = client.readLine()

        assertEquals("HTTP/1.1 401 UNAUTHORIZED", statusline)
    }

    /**
     * If we ask for the homepage, we'll get a 200 OK
     */
    @IntegrationTest(usesPort = true)
    @Category(IntegrationTestCategory::class)
    @Test
    fun testShouldGet200Response() {
        client.write("GET /homepage HTTP/1.1$CRLF$CRLF")

        val statusline = client.readLine()

        assertEquals("HTTP/1.1 200 OK", statusline)
    }

    /**
     * If the client asks for a file, give it
     */
    @IntegrationTest(usesPort = true)
    @Category(IntegrationTestCategory::class)
    @Test
    fun testShouldGetFileResponse() {
        client.write("GET /sample.html HTTP/1.1$CRLF$CRLF")

        val result = parseHttpMessage(client, FakeAuthenticationUtilities(), testLogger)

        assertEquals(StatusCode.OK, result.statusCode)
        assertEquals(toStr(read("static/sample.html")!!), result.data.rawData)
    }

    /**
     * If the client asks for a file, give it
     * CSS edition
     */
    @IntegrationTest(usesPort = true)
    @Category(IntegrationTestCategory::class)
    @Test
    fun testShouldGetFileResponse_CSS() {
        client.write("GET /sample.css HTTP/1.1$CRLF$CRLF")

        val result = parseHttpMessage(client, FakeAuthenticationUtilities(), testLogger)

        assertEquals(StatusCode.OK, result.statusCode)
        assertEquals(toStr(read("static/sample.css")!!), result.data.rawData)
    }

    /**
     * If the client asks for a file, give it
     * JS edition
     */
    @IntegrationTest(usesPort = true)
    @Category(IntegrationTestCategory::class)
    @Test
    fun testShouldGetFileResponse_JS() {
        client.write("GET /sample.js HTTP/1.1$CRLF$CRLF")

        val result = parseHttpMessage(client, FakeAuthenticationUtilities(), testLogger)

        assertEquals(StatusCode.OK, result.statusCode)
        assertEquals(toStr(read("static/sample.js")!!), result.data.rawData)
    }

    /**
     * Action for an invalid request
     */
    @IntegrationTest(usesPort = true)
    @Category(IntegrationTestCategory::class)
    @Test
    fun testShouldParseMultipleClientRequestTypes_BadRequest() {
        client.write("FOO /test.utl HTTP/1.1$CRLF$CRLF")

        val result: AnalyzedHttpData = parseHttpMessage(client, FakeAuthenticationUtilities(), testLogger)

        assertEquals(StatusCode.BAD_REQUEST, result.statusCode)
    }

    /**
     * What should the server return if we ask for something
     * the server doesn't have?
     */
    @IntegrationTest(usesPort = true)
    @Category(IntegrationTestCategory::class)
    @Test
    fun testShouldGetHtmlFileResponseFromServer_unfound() {
        client.write("GET /doesnotexist.html HTTP/1.1$CRLF$CRLF")

        val result: AnalyzedHttpData = parseHttpMessage(client, FakeAuthenticationUtilities(), testLogger)

        assertEquals(StatusCode.NOT_FOUND, result.statusCode)
    }

    /**
     * What should the server return if we ask for something
     * the server does have, but it's not a suffix we recognize?
     * See [coverosR3z.server.utility.StaticFilesUtilities.Companion.loadStaticFilesToCache]
     */
    @IntegrationTest(usesPort = true)
    @Category(IntegrationTestCategory::class)
    @Test
    fun testShouldGetHtmlFileResponseFromServer_unfoundUnknownSuffix() {
        client.write("GET /sample_template.utl HTTP/1.1$CRLF$CRLF")

        val result: AnalyzedHttpData = parseHttpMessage(client, FakeAuthenticationUtilities(), testLogger)

        assertEquals(StatusCode.NOT_FOUND, result.statusCode)
    }


    /**
     * When we POST some data unauthorized, we should receive that message
     */
    @IntegrationTest(usesPort = true)
    @Category(IntegrationTestCategory::class)
    @Test
    fun testShouldGetUnauthorizedResponseAfterPost() {
        client.write("POST /${EnterTimeAPI.path} HTTP/1.1$CRLF$CRLF")

        val result: AnalyzedHttpData = parseHttpMessage(client, FakeAuthenticationUtilities(), testLogger)

        assertEquals(StatusCode.UNAUTHORIZED, result.statusCode)
    }

    /**
     * When we POST some data, we should receive a success message back
     */
    @IntegrationTest(usesPort = true)
    @Category(IntegrationTestCategory::class)
    @Test
    fun testShouldGetSuccessResponseAfterPost() {
        au.getUserForSessionBehavior = { NO_USER }
        client.write("POST /${RegisterAPI.path} HTTP/1.1$CRLF")
        client.write("Cookie: sessionId=abc123$CRLF")
        val body = "${RegisterAPI.Elements.EMPLOYEE_INPUT.getElemName()}=1&${RegisterAPI.Elements.USERNAME_INPUT.getElemName()}=abcdef&${RegisterAPI.Elements.PASSWORD_INPUT.getElemName()}=password12345"
        client.write("Content-Length: ${body.length}$CRLF$CRLF")
        client.write(body)
        val result: AnalyzedHttpData = parseHttpMessage(client, au, testLogger)
        assertEquals(StatusCode.SEE_OTHER, result.statusCode)
    }

    /**
     * When we POST some data that lacks all the types needed, get a 500 error
     */
    @IntegrationTest(usesPort = true)
    @Category(IntegrationTestCategory::class)
    @Test
    fun testShouldGetInternalServerError() {
        au.getUserForSessionBehavior = { DEFAULT_USER }
        client.write("POST /${EnterTimeAPI.path} HTTP/1.1$CRLF")
        client.write("Cookie: sessionId=abc123$CRLF")
        val body = "project_entry=1&time_entry=2"
        client.write("Content-Length: ${body.length}$CRLF$CRLF")
        client.write(body)

        val result: AnalyzedHttpData = parseHttpMessage(client, au, testLogger)

        assertEquals(StatusCode.INTERNAL_SERVER_ERROR, result.statusCode)
    }

    /**
     * If the body doesn't have properly URL formed text. Like not including a key
     * and a value separated by an =
     */
    @IntegrationTest(usesPort = true)
    @Category(IntegrationTestCategory::class)
    @Test
    fun testShouldGetInternalServerError_improperlyFormedBody() {
        client.write("POST /${EnterTimeAPI.path} HTTP/1.1$CRLF")
        client.write("Cookie: sessionId=abc123$CRLF")
        val body = "test foo bar"
        client.write("Content-Length: ${body.length}$CRLF$CRLF")
        client.write(body)

        val result: AnalyzedHttpData = parseHttpMessage(client, FakeAuthenticationUtilities(), testLogger)

        assertEquals(StatusCode.INTERNAL_SERVER_ERROR, result.statusCode)
    }

    /**
     * If we as client are connected but then close the connection from our side,
     * we should see a CLIENT_CLOSED_CONNECTION remark
     */
    @IntegrationTest(usesPort = true)
    @Category(IntegrationTestCategory::class)
    @Test
    fun testShouldIndicateClientClosedConnection() {
        client.socket.shutdownOutput()

        val result: AnalyzedHttpData = parseHttpMessage(client, FakeAuthenticationUtilities(), testLogger)

        assertEquals(Verb.CLIENT_CLOSED_CONNECTION, result.verb)
    }

    /**
     * On some pages, like register and login, you are *supposed* to be
     * unauthenticated to post to them.  If you *are* authenticated and
     * post to those pages, you should get redirected to the authenticated
     * homepage
     */
    @IntegrationTest(usesPort = true)
    @Category(IntegrationTestCategory::class)
    @Test
    fun testShouldGetRedirectedWhenPostingAuthAndRequireUnAuth() {
        au.getUserForSessionBehavior = { DEFAULT_USER }
        client.write("POST /${LoginAPI.path} HTTP/1.1$CRLF")
        client.write("Cookie: sessionId=abc123$CRLF")
        val body = "${LoginAPI.Elements.USERNAME_INPUT.getElemName()}=alice&${LoginAPI.Elements.PASSWORD_INPUT.getElemName()}=password12345"
        client.write("Content-Length: ${body.length}$CRLF$CRLF")
        client.write(body)

        val result: AnalyzedHttpData = parseHttpMessage(client, FakeAuthenticationUtilities(), testLogger)

        assertEquals(StatusCode.SEE_OTHER, result.statusCode)
    }

    /**
     * This is to try out a new client factory, so we send requests
     * with valid content and valid protocol more easily.
     */
    @IntegrationTest(usesPort = true)
    @Category(PerformanceTestCategory::class)
    @Test
    fun testWithValidClient_LoginPage_PERFORMANCE() {
        val numberOfRequests = 100

        // so we don't see spam
        fs.logger.logSettings[LogTypes.DEBUG] = false
        val headers = listOf("Connection: keep-alive")
        val body = mapOf(
                LoginAPI.Elements.USERNAME_INPUT.getElemName() to DEFAULT_USER.name.value,
                LoginAPI.Elements.PASSWORD_INPUT.getElemName() to DEFAULT_PASSWORD.value)
        val myClient = Client.make(Verb.POST, LoginAPI.path, headers, body, au, port)

        val (time, _) = getTime {
            for (i in 1..numberOfRequests) {
                myClient.send()
                val result = myClient.read()

                assertEquals(StatusCode.SEE_OTHER, result.statusCode)
            }
        }
        println("Time was $time")
        File("${granularPerfArchiveDirectory}testWithValidClient_LoginPage_PERFORMANCE")
            .appendText("${Date.now().stringValue}\tnumberOfRequests: $numberOfRequests\ttime: $time\n")

        // turn logging back on for other tests
        fs.logger.logSettings[LogTypes.DEBUG] = true
    }

    /**
     * I used this to see just how fast the server ran.  Able to get
     * 25,000 requests per second on 12/26/2020
     */
    @IntegrationTest(usesPort = true)
    @Category(PerformanceTestCategory::class)
    @Test
    fun testHomepage_PERFORMANCE() {
        val numberOfThreads = 10
        val numberOfRequests = 300

        val cachedThreadPool: ExecutorService = Executors.newCachedThreadPool(Executors.defaultThreadFactory())

        // so we don't see spam
        fs.logger.logSettings[LogTypes.DEBUG] = false
        val (time, _) = getTime {
            val threadList = (1..numberOfThreads).map {  cachedThreadPool.submit(makeClientThreadRepeatedRequestsHomepage(numberOfRequests, port)) }
            threadList.forEach { it.get() }
        }
        println("Time was $time")
        File("${granularPerfArchiveDirectory}testHomepage_PERFORMANCE")
            .appendText("${Date.now().stringValue}\tnumberOfThreads: $numberOfThreads\tnumberOfRequests: $numberOfRequests\ttime: $time\n")

        // turn logging back on for other tests
        fs.logger.logSettings[LogTypes.DEBUG] = true
    }

    /**
     * I used this to see just how fast the server ran.  Able to get
     * 25,000 requests per second on 12/26/2020
     */
    @IntegrationTest(usesPort = true)
    @Category(PerformanceTestCategory::class)
    @Test
    fun testEnterTime_PERFORMANCE() {
        val threadCount = 10
        val requestCount = 100

        val cachedThreadPool: ExecutorService = Executors.newCachedThreadPool(Executors.defaultThreadFactory())

        // so we don't see spam
        fs.logger.logSettings[LogTypes.DEBUG] = false
        au.getUserForSessionBehavior = { DEFAULT_USER }
        val (time, _) = getTime {
            val threadList = (1..threadCount).map {  cachedThreadPool.submit(makeClientThreadRepeatedTimeEntries(requestCount, port)) }
            threadList.forEach { it.get() }
        }
        println("Time was $time")
        File("${granularPerfArchiveDirectory}testEnterTime_PERFORMANCE")
            .appendText("${Date.now().stringValue}\tthreadCount: $threadCount\trequestCount: $requestCount\ttime:$time\n")

        // turn logging back on for other tests
        fs.logger.logSettings[LogTypes.DEBUG] = true
    }

    /**
     * If we ask for the homepage on a secure server, we'll get a 200 OK
     */
    @IntegrationTest(usesPort = true)
    @Category(IntegrationTestCategory::class)
    @Test
    fun testShouldGet200Response_Secure() {
        val sslClientSocket = SSLSocketFactory.getDefault().createSocket("localhost", sslTestPort) as SSLSocket
        client = SocketWrapper(sslClientSocket, "client")
        client.write("GET /homepage HTTP/1.1$CRLF$CRLF")

        val statusline = client.readLine()

        assertEquals("HTTP/1.1 200 OK", statusline)
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
     * Simply GETs from the homepage many times
     */
    private fun makeClientThreadRepeatedRequestsHomepage(numRequests : Int, port : Int): Thread {
        return Thread {
            val client =
                Client.make(Verb.GET, HomepageAPI.path, listOf("Connection: keep-alive"), authUtilities = au, port = port)
            for (i in 1..numRequests) {
                client.send()
                val result = client.read()
                assertEquals(StatusCode.OK, result.statusCode)
            }
        }
    }

    /**
     * Enters time for a user on many days
     */
    private fun makeClientThreadRepeatedTimeEntries(numRequests : Int, port : Int): Thread {
        return Thread {

            val client =
                Client.make(
                    Verb.POST,
                    EnterTimeAPI.path,
                    listOf("Connection: keep-alive", "Cookie: sessionId=abc123"),
                    authUtilities = au,
                    port = port)
            for (i in 1..numRequests) {
                val data = PostBodyData(mapOf(
                    EnterTimeAPI.Elements.DATE_INPUT.getElemName() to Date(A_RANDOM_DAY_IN_JUNE_2020.epochDay + i / 100).stringValue,
                    EnterTimeAPI.Elements.DETAIL_INPUT.getElemName() to "some details go here",
                    EnterTimeAPI.Elements.PROJECT_INPUT.getElemName() to "1",
                    EnterTimeAPI.Elements.TIME_INPUT.getElemName() to "1",
                ))
                val clientWithData = client.addPostData(data)
                clientWithData.send()
                val result = clientWithData.read()
                assertEquals(StatusCode.SEE_OTHER, result.statusCode)
                assertTrue("headers: ${result.headers}", result.headers.any {it.matches(redirectRegex)})
            }
        }
    }



}

class Client(private val socketWrapper: SocketWrapper, val data : String, val au: IAuthenticationUtilities, val path: String = "", private val headers: String = "") {

    fun send() {
        socketWrapper.write(data)
    }

    fun read() : AnalyzedHttpData {
        return parseHttpMessage(socketWrapper, au, testLogger)
    }

    fun addPostData(body: PostBodyData) : Client {
        val bodyString = body.mapping.map{ it.key + "=" + encode(it.value) }.joinToString("&")
        val data =  "${Verb.POST} /$path HTTP/1.1$CRLF" + "Content-Length: ${bodyString.length}$CRLF" + headers + CRLF + CRLF + bodyString
        return Client(this.socketWrapper, data = data, au)
    }

    companion object {

        fun make(
            verb : Verb,
            path : String,
            headers : List<String>? = null,
            body : Map<String,String>? = null,
            authUtilities: IAuthenticationUtilities = FakeAuthenticationUtilities(),
            port : Int
        ) : Client {
            val clientSocket = Socket("localhost", port)
            val bodyString = body?.map{ it.key + "=" + encode(it.value) }?.joinToString("&") ?: ""
            val headersString = headers?.joinToString(CRLF) ?: ""

            val data = when (verb) {
                Verb.GET -> "${verb.name} /$path HTTP/1.1$CRLF" + headersString + CRLF + CRLF
                Verb.POST -> "${verb.name} /$path HTTP/1.1$CRLF" + "Content-Length: ${bodyString.length}$CRLF" + headersString + CRLF + CRLF + bodyString
                else -> throw IllegalArgumentException("unexpected Verb")
            }

            return Client(SocketWrapper(clientSocket, "client"), data, authUtilities, path, headersString)
        }

    }
}
