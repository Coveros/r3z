package coverosR3z.authentication

class FakeAuthPersistence(
        var createExecutorBehavior : () -> Unit = {},
        var isEmployeeRegisteredBehavior : () -> Boolean = {false}
) : IAuthPersistence {

    override fun createExecutor(name: String) {
        createExecutorBehavior()
    }

    override fun isEmployeeRegistered(name: String) : Boolean {
        return isEmployeeRegisteredBehavior()
    }


}