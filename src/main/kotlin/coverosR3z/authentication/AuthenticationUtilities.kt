package coverosR3z.authentication

import coverosR3z.domainobjects.RegistrationResult


class AuthenticationUtilities(ap : IAuthPersistence){

    fun register(username: String, password: String) : RegistrationResult {
        if (password.isEmpty()) {
            return RegistrationResult.FAILURE
        }
        return RegistrationResult.SUCCESS
    }

}