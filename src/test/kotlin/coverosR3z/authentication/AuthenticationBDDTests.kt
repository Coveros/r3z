package coverosR3z.authentication

/**
 *
 As a user, I want to be able to securely add and change my time entries,
 so that I know my time entries are confidential and have integrity
 */
class AuthenticationBDDTests {

    // I can register
    fun `I should be able to register a user with a valid password`() {
        // given I am not currently registered
        val au = AuthenticationUtilities(mockAuthPersistence)

        // when I register a new user with username "matt" and password "asdfoiajwefowejf"
        au.register("matt", "asdfoiajwefowejf")

        // then the system records the registration successfully
        au.isUserRegistered("matt")
    }

    // I can log in
    fun `I should be able to log in once I'm a registered user`() {
        TODO("UNIMPLEMENTED")
    }

}
