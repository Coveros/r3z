package coverosR3z.server

import coverosR3z.authentication.AuthenticationPersistence
import coverosR3z.authentication.AuthenticationUtilities
import coverosR3z.authentication.CurrentUser
import coverosR3z.domainobjects.SYSTEM_USER
import coverosR3z.logging.logInfo
import coverosR3z.persistence.PureMemoryDatabase
import coverosR3z.timerecording.TimeEntryPersistence
import coverosR3z.timerecording.TimeRecordingUtilities
import java.net.ServerSocket

/**
 * This is the top-level class that handles communication with clients.
 * The intention is that this class solely acts to control the socket
 * connections and is unfamiliar with the specifics of what is being transmitted
 */
class SocketCommunication(val port : Int) {

    fun startServer() {
        val halfOpenServerSocket = ServerSocket(port)
        val pmd = PureMemoryDatabase()
        val cu = CurrentUser(SYSTEM_USER)
        val tru = TimeRecordingUtilities(TimeEntryPersistence(pmd), cu)
        val au = AuthenticationUtilities(AuthenticationPersistence(pmd))
        while (true) {
            logInfo("waiting for socket connection")
            val server = SocketWrapper(halfOpenServerSocket.accept())
            logInfo("client from ${server.socket.inetAddress?.hostAddress} has connected")
            handleRequest(server, au, tru)
        }
    }

    companion object {
        fun handleRequest(server: SocketWrapper, au: AuthenticationUtilities, tru: TimeRecordingUtilities) {
            val requestData = ServerUtilities.parseClientRequest(server, au)

            // now that we know who the user is (if they authenticated) we can update the current user
            val truWithUser = tru.changeUser(CurrentUser(requestData.user))

            val responseData = ServerUtilities(au, truWithUser).handleRequestAndRespond(requestData)
            ServerUtilities.returnData(server, responseData)
        }
    }

}