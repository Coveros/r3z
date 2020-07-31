package coverosR3z.persistence

import coverosR3z.domainobjects.*
import coverosR3z.exceptions.UserNotRegisteredException
import kotlinx.serialization.Serializable

/**
 * Why use those heavy-handed database applications when you
 * can simply store your data in simple collections?
 *
 * Here, things are simple.  Anything you need, you make.
 */
@Serializable
class PureMemoryDatabase {

    private val employees : MutableSet<Employee> = mutableSetOf()
    private val projects : MutableSet<Project> = mutableSetOf()
    private val timeEntries : MutableMap<Employee, MutableSet<TimeEntry>> = mutableMapOf()

    fun addTimeEntry(timeEntry : TimeEntryPreDatabase) {
        var userTimeEntries = timeEntries[timeEntry.employee]
        if (userTimeEntries == null) {
            userTimeEntries = mutableSetOf()
            timeEntries[timeEntry.employee] = userTimeEntries
        }
        val newIndex = userTimeEntries.size + 1
        userTimeEntries.add(TimeEntry(
                newIndex,
                timeEntry.employee,
                timeEntry.project,
                timeEntry.time,
                timeEntry.date,
                timeEntry.details))
    }

    fun addNewProject(projectName: ProjectName) : Int {
        val newIndex = projects.size + 1
        projects.add(Project(newIndex, projectName.value))
        return newIndex
    }

    fun addNewUser(username: EmployeeName) : Int {
        val newIndex = employees.size + 1
        employees.add(Employee(newIndex, username.value))
        return newIndex
    }

    /**
     * gets the number of minutes a particular [Employee] has worked
     * on a certain date.
     *
     * @throws [UserNotRegisteredException] if the user isn't known.
     */
    fun getMinutesRecordedOnDate(employee: Employee, date: Date): Int {
        val userTimeEntries = timeEntries[employee]
                ?: if (!employees.contains(employee)) {
                    throw UserNotRegisteredException()
                } else {
                    return 0
                }
        return userTimeEntries
                .filter { te -> te.employee.id == employee.id && te.date == date }
                .sumBy { te -> te.time.numberOfMinutes }
    }

    fun getAllTimeEntriesForUser(employee: Employee): List<TimeEntry> {
        return timeEntries[employee]!!.filter{ te -> te.employee.id == employee.id}
    }

    fun getAllTimeEntriesForUserOnDate(employee: Employee, date: Date): List<TimeEntry> {
        return timeEntries[employee]!!.filter{ te -> te.employee.id == employee.id && te.date == date}
    }

    fun getProjectById(id: Int) : Project? {
        assert(id > 0)
        return projects.singleOrNull { p -> p.id == id }
    }

    fun getUserById(id: Int): Employee? {
        assert(id > 0)
        return employees.singleOrNull { u -> u.id == id}
    }

    fun getAllUsers() : List<Employee> {
        return employees.toList()
    }

    fun getAllProjects(): List<Project> {
        return projects.toList()
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as PureMemoryDatabase

        if (employees != other.employees) return false
        if (projects != other.projects) return false
        if (timeEntries != other.timeEntries) return false

        return true
    }

    override fun hashCode(): Int {
        var result = employees.hashCode()
        result = 31 * result + projects.hashCode()
        result = 31 * result + timeEntries.hashCode()
        return result
    }


}