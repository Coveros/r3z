package coverosR3z.authentication

import coverosR3z.domainobjects.Employee
import coverosR3z.persistence.PureMemoryDatabase

class AuthenticationPersistence(val pmd : PureMemoryDatabase) : IAuthPersistence {

    override fun createExecutor(name: String) {
        TODO("Not yet implemented")
    }

    override fun isUserRegistered(name: String): Boolean {
        val employees : List<Employee> = pmd.getAllUsers()

        return employees.any { u -> u.name == name }
    }

}