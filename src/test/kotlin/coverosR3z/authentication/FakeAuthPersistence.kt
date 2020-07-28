package coverosR3z.authentication

class FakeAuthPersistence(
        val createExecutorBehavior : () -> Unit = {},
        val isUserRegisteredBehavior : () -> Unit = {}
) : IAuthPersistence {

    override fun createExecutor(name: String) {
        createExecutorBehavior()
    }

    override fun isUserRegistered(name: String) {
        isUserRegisteredBehavior()
    }


}