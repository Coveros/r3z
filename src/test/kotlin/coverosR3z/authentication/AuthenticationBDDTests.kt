package coverosR3z.authentication

import coverosR3z.persistence.PureMemoryDatabase

/**
 *
 * As a employee,
 * I want to be able to securely use the system,
 * so that I know my time entries are confidential and cannot be manipulated by others
 */
class AuthenticationBDDTests {

    // I can change my time entry

    // I cannot change someone else's time

    // I can register
//    fun `I should be able to register a employee with a valid password`() {
//        // given I am not currently registered
//        authPersistence = AuthPersistence(PureMemoryDatabase())
//        val au = AuthenticationUtilities(authPersistence)
//
//        // when I register a new employee with employeename "matt" and password "asdfoiajwefowejf"
//        au.register("matt", "asdfoiajwefowejf")
//
//        // then the system records the registration successfully
//        au.isEmployeeRegistered("matt")
//    }

    // I can log in
    fun `I should be able to log in once I'm a registered employee`() {
    }

    // Bad password while logging in

    // Bad password while registering


}
