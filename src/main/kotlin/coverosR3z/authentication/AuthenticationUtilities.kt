package coverosR3z.authentication

enum class LoginStatus {VALID_LOGIN, INVALID_LOGIN}

class AuthenticationUtilities(ap : IAuthPersistence){
    fun register(username: String, password: String) {}

    fun login(username: String, password: String): LoginStatus {
        TODO("NOT IMPLEMENTED")
        return LoginStatus.INVALID_LOGIN
    }
}