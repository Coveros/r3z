package coverosR3z.server

import coverosR3z.authentication.AuthenticationPersistence
import coverosR3z.authentication.AuthenticationUtilities
import coverosR3z.authentication.CurrentUser
import coverosR3z.authentication.IAuthenticationUtilities
import coverosR3z.domainobjects.DateTime
import coverosR3z.domainobjects.SYSTEM_USER
import coverosR3z.logging.*
import coverosR3z.persistence.PureMemoryDatabase
import coverosR3z.timerecording.ITimeRecordingUtilities
import coverosR3z.timerecording.TimeEntryPersistence
import coverosR3z.timerecording.TimeRecordingUtilities
import java.net.ServerSocket
import java.net.SocketException
import java.util.concurrent.Executors

/**
 * This is the top-level entry into the web server
 */
class Server(val port: Int, private val dbDirectory: String) {

    fun startServer(authUtils: IAuthenticationUtilities? = null) {
        halfOpenServerSocket = ServerSocket(port)

        val cu = CurrentUser(SYSTEM_USER)
        val pmd = PureMemoryDatabase.start(dbDirectory)
        val tru = TimeRecordingUtilities(TimeEntryPersistence(pmd), cu)
        val au = authUtils ?: AuthenticationUtilities(AuthenticationPersistence(pmd))
        logStart("System is ready.  DateTime is ${DateTime(getCurrentMillis() / 1000)} in UTC")

        val cachedThreadPool = Executors.newCachedThreadPool()
        try {
            while (true) {
                logTrace("waiting for socket connection")

                val server = SocketWrapper(halfOpenServerSocket.accept(), "server")
                val thread = Thread {
                    logTrace("client from ${server.socket.inetAddress?.hostAddress} has connected")
                    do {
                        val requestData = handleRequest(server, au, tru)
                        val shouldKeepAlive = requestData.headers.any { it.toLowerCase().contains("connection: keep-alive") }
                        if (shouldKeepAlive) {
                            logTrace("This is a keep-alive connection")
                        }
                    } while (shouldKeepAlive)

                    logTrace("closing server socket")
                    server.close()
                }
                cachedThreadPool.submit(thread)
            }
        } catch (ex : SocketException) {
            if (ex.message == "Interrupted function call: accept failed") {
                logWarn("Server was shutdown while waiting on accept")
            }
        }
    }

    companion object {

        lateinit var halfOpenServerSocket : ServerSocket

        /**
         * Given the command-line arguments, returns the first value
         * as an integer for use as a port number, or defaults to 8080
         */
        fun extractFirstArgumentAsPort(args: Array<String>): Int {
            return if (args.isEmpty() || args[0].isBlank()) {
                8080
            } else {
                args[0].toIntOrNull() ?: 8080
            }
        }

        fun handleRequest(server: ISocketWrapper, au: IAuthenticationUtilities, tru: ITimeRecordingUtilities) : AnalyzedHttpData {
            lateinit var analyzedHttpData : AnalyzedHttpData
            val responseData: PreparedResponseData = try {
                analyzedHttpData = parseHttpMessage(server, au)
                if (analyzedHttpData.verb == Verb.CLIENT_CLOSED_CONNECTION) {
                    return analyzedHttpData
                }
                // now that we know who the user is (if they authenticated) we can update the current user
                val cu = CurrentUser(analyzedHttpData.user)
                val truWithUser = tru.changeUser(cu)
                logDebug("client requested ${analyzedHttpData.verb} ${analyzedHttpData.path}", cu)
                handleRequestAndRespond(ServerData(au, truWithUser, analyzedHttpData))
            } catch (ex: Exception) {
                handleInternalServerError(ex.message ?: "NO ERROR MESSAGE DEFINED")
            }

            returnData(server, responseData)
            return analyzedHttpData
        }
    }

}