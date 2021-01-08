package coverosR3z.authentication.utility

import coverosR3z.authentication.types.*
import coverosR3z.misc.utility.generateRandomString
import coverosR3z.misc.types.DateTime
import coverosR3z.timerecording.types.EmployeeId
import java.time.LocalDateTime
import java.time.ZoneOffset

interface IAuthenticationUtilities {
    /**
     * Register a user through auth persistent, providing a username, password, and
     * optional employeeId (defaults to null)
     */
    fun register(username: UserName, password: Password, employeeId: EmployeeId? = null) : RegistrationResult

    /**
     * Takes a user's username and password and returns a result, and a user
     * as well if the [LoginResult] was successful.
     */
    fun login(username: UserName, password: Password): Pair<LoginResult, User>

    /**
     * Returns the user if there is a valid session,
     * otherwise returns null
     */
    fun getUserForSession(sessionToken: String): User

    /**
     * Adds a new session to the sessions data structure, with
     * the user and a generated session value
     */
    fun createNewSession(user: User, time : DateTime = DateTime(LocalDateTime.now().toEpochSecond(ZoneOffset.UTC)), rand : () -> String = { generateRandomString(16) }) : String

    /**
     * Wipes out the session entry for this user
     */
    fun logout(user: User)


}