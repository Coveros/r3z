package coverosR3z.authentication

class FakeAuthPersistence(
        var createUserBehavior : () -> Unit = {},
        var isEmployeeRegisteredBehavior : () -> Boolean = {false}
) : IAuthPersistence {

    override fun createUser(name: String) {
        createUserBehavior()
    }

    override fun isEmployeeRegistered(name: String) : Boolean {
        return isEmployeeRegisteredBehavior()
    }


}