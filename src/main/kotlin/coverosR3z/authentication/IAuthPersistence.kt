package coverosR3z.authentication

interface IAuthPersistence {
    fun createUser(name : String)
    fun isEmployeeRegistered(name : String) : Boolean
 }