package coverosR3z.timerecording

import coverosR3z.domainobjects.*

interface ITimeEntryPersistence {
    fun persistNewTimeEntry(entry: TimeEntryPreDatabase)
    fun persistNewProject(projectName: ProjectName) : Project
    fun persistNewUser(username: EmployeeName): Employee

    /**
     * Provided a user and date, give the number of minutes they worked on that date
     */
    fun queryMinutesRecorded(employee: Employee, date: Date): Int
    fun readTimeEntries(employee: Employee): List<TimeEntry>
    fun readTimeEntriesOnDate(employee: Employee, date: Date): List<TimeEntry>

}