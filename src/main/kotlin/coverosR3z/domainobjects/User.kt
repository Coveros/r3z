package coverosR3z.domainobjects

import kotlinx.serialization.Serializable

private const val maxEmployeeCount = 100_000_000
private const val maxEmployeeMsg = "No way this company has more than 100 million employees"
private const val minIdMsg = "Valid identifier values are 1 or above"
private const val nameCannotBeEmptyMsg = "All users must have a non-empty name"


/**
 * Holds a user's name before we have a whole object, like [User]
 */
@Serializable
data class UserName(val value: String) {
    init {
        assert(value.isNotEmpty()) {nameCannotBeEmptyMsg}
    }
}

@Serializable
data class User(val id: Int, val name: String) {

    init {
        assert(name.isNotEmpty()) {nameCannotBeEmptyMsg}
        assert(id < maxEmployeeCount) { maxEmployeeMsg }
        assert(id > 0) { minIdMsg }
    }

}

@Serializable
data class UserId(val id: Int) {
    init {
        assert(id < maxEmployeeCount) {maxEmployeeMsg }
        assert(id > 0) { minIdMsg }
    }
}


