package coverosR3z.persistence.utility

import coverosR3z.authentication.types.Session
import coverosR3z.authentication.types.User
import coverosR3z.logging.ILogger
import coverosR3z.logging.ILogger.Companion.logImperative
import coverosR3z.persistence.types.AbstractDataAccess
import coverosR3z.persistence.types.ChangeTrackingSet
import coverosR3z.persistence.types.toChangeTrackingSet
import coverosR3z.techempower.types.World
import coverosR3z.timerecording.persistence.TimeEntryPersistence
import coverosR3z.timerecording.types.*


/**
 * Why use those heavy-handed database applications when you
 * can simply store your data in simple collections?
 *
 * Here, things are simple.  Anything you need, you make.
 *
 * @param dbDirectory if this is null, the database won't use the disk at all.  If you set it to a directory, like
 *                      File("db/") the database will use that directory for all persistence.
 */
open class PureMemoryDatabase(
    protected val dbDirectory: String? = null,
    private val diskPersistence: DatabaseDiskPersistence? = null,
    protected val employees: ChangeTrackingSet<Employee> = ChangeTrackingSet(),
    protected val users: ChangeTrackingSet<User> = ChangeTrackingSet(),
    protected val projects: ChangeTrackingSet<Project> = ChangeTrackingSet(),
    protected val timeEntries: ChangeTrackingSet<TimeEntry> = ChangeTrackingSet(),
    protected val sessions: ChangeTrackingSet<Session> = ChangeTrackingSet(),
    protected val submittedPeriods: ChangeTrackingSet<SubmittedPeriod> = ChangeTrackingSet(),
    protected val worlds: ChangeTrackingSet<World> = ChangeTrackingSet()
) {

    fun stop() {
        diskPersistence?.stop()
    }

    fun copy(): PureMemoryDatabase {
        return PureMemoryDatabase(
            employees = this.employees.toList().toChangeTrackingSet(),
            users = this.users.toList().toChangeTrackingSet(),
            projects = this.projects.toList().toChangeTrackingSet(),
            timeEntries = this.timeEntries.toList().toChangeTrackingSet(),
            sessions = this.sessions.toList().toChangeTrackingSet(),
            submittedPeriods = this.submittedPeriods.toList().toChangeTrackingSet(),
            worlds = this.worlds.toList().toChangeTrackingSet(),
        )
    }

    ////////////////////////////////////
    //   DATA ACCESS
    ////////////////////////////////////

    inner class EmployeeDataAccess : AbstractDataAccess<Employee>(employees, diskPersistence, Employee.directoryName)
    inner class ProjectDataAccess : AbstractDataAccess<Project>(projects, diskPersistence, Project.directoryName)
    inner class UserDataAccess : AbstractDataAccess<User>(users, diskPersistence, User.directoryName)
    inner class SessionDataAccess : AbstractDataAccess<Session>(sessions, diskPersistence, Session.directoryName)
    inner class TimeEntryDataAccess : AbstractDataAccess<TimeEntry>(timeEntries, diskPersistence, TimeEntry.directoryName)
    inner class SubmittedPeriodsAccess : AbstractDataAccess<SubmittedPeriod>(submittedPeriods, diskPersistence, SubmittedPeriod.directoryName)
    inner class WorldDataAccess : AbstractDataAccess<World>(worlds, diskPersistence, SubmittedPeriod.directoryName)


    ////////////////////////////////////
    //   BOILERPLATE
    ////////////////////////////////////



    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as PureMemoryDatabase

        if (employees != other.employees) return false
        if (users != other.users) return false
        if (projects != other.projects) return false
        if (timeEntries != other.timeEntries) return false
        if (sessions != other.sessions) return false
        if (submittedPeriods != other.submittedPeriods) return false
        if (worlds != other.worlds) return false

        return true
    }

    override fun hashCode(): Int {
        var result = employees.hashCode()
        result = 31 * result + users.hashCode()
        result = 31 * result + projects.hashCode()
        result = 31 * result + timeEntries.hashCode()
        result = 31 * result + sessions.hashCode()
        result = 31 * result + submittedPeriods.hashCode()
        result = 31 * result + worlds.hashCode()
        return result
    }


    ////////////////////////////////////
    //   DATABASE CONTROL
    ////////////////////////////////////

    companion object {

        /**
         * This starts the database with memory-only, that is
         * no disk persistence.  This is mainly
         * used for testing purposes.  For production use,
         * check out [DatabaseDiskPersistence.startWithDiskPersistence]
         */
        fun startMemoryOnly(logger: ILogger): PureMemoryDatabase {
            val pmd = PureMemoryDatabase()

            logImperative("creating an initial employee")
            val tep = TimeEntryPersistence(pmd, logger = logger)
            tep.persistNewEmployee(EmployeeName("Administrator"))

            return pmd
        }
    }
}