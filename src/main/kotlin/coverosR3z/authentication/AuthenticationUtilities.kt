package coverosR3z.authentication

import coverosR3z.domainobjects.RegistrationResult


class AuthenticationUtilities(ap : IAuthPersistence){

    fun register(username: String, password: String) : RegistrationResult {
        if (password.isEmpty()) {
            return RegistrationResult.EMPTY_PASSWORD
        }
        if (password.length >= 100) {
            return RegistrationResult.PASSWORD_TOO_LONG
        }
        return RegistrationResult.SUCCESS

    }

}