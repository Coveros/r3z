package coverosR3z.authentication

interface IAuthPersistence {
    fun createExecutor(name : String)
    fun isEmployeeRegistered(name : String) : Boolean
 }