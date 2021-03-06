package coverosR3z.timerecording.api

import coverosR3z.authentication.api.RegisterAPI
import coverosR3z.authentication.types.Invitation
import coverosR3z.authentication.types.NO_USER
import coverosR3z.authentication.types.Role
import coverosR3z.server.api.MessageAPI
import coverosR3z.system.misc.utility.safeAttr
import coverosR3z.system.misc.utility.safeHtml
import coverosR3z.server.types.*
import coverosR3z.server.utility.AuthUtilities.Companion.doGETRequireAuth
import coverosR3z.server.utility.AuthUtilities.Companion.doPOSTAuthenticated
import coverosR3z.server.utility.PageComponents
import coverosR3z.server.utility.ServerUtilities.Companion.redirectTo
import coverosR3z.timerecording.types.Employee
import coverosR3z.timerecording.types.EmployeeName
import coverosR3z.timerecording.types.NO_EMPLOYEE
import coverosR3z.timerecording.types.maxEmployeeNameSize

class CreateEmployeeAPI(private val sd: ServerData) {

    enum class Elements(private val elemName: String, private val id: String) : Element {
        EMPLOYEE_INPUT("employee_name", "employee_name"),
        CREATE_BUTTON("", "employee_create_button"),
        DELETE_BUTTON("", "delete_button");

        override fun getId(): String {
            return this.id
        }

        override fun getElemName(): String {
            return this.elemName
        }

        override fun getElemClass(): String {
            throw IllegalAccessError()
        }
    }

    companion object : GetEndpoint, PostEndpoint {

        /**
         * The required inputs for this API
         */
        override val requiredInputs = setOf(Elements.EMPLOYEE_INPUT)

        override val path: String
            get() = "createemployee"

        override fun handleGet(sd: ServerData): PreparedResponseData {
            val ce = CreateEmployeeAPI(sd)
            return doGETRequireAuth(sd.ahd.user, Role.ADMIN) { ce.createEmployeeHTML() }
        }

        override fun handlePost(sd: ServerData): PreparedResponseData {
            val ce = CreateEmployeeAPI(sd)
            return doPOSTAuthenticated(sd, requiredInputs, path, Role.SYSTEM, Role.ADMIN) { ce.createEmployee() }
        }

    }

    fun createEmployee() : PreparedResponseData {
        val employeeNameString = checkNotNull(sd.ahd.data.mapping[Elements.EMPLOYEE_INPUT.getElemName()])
        val employeeNameTrimmed = employeeNameString.trim()
        val employeename = EmployeeName(employeeNameTrimmed)
        return if (sd.bc.tru.findEmployeeByName(employeename) != NO_EMPLOYEE) {
            MessageAPI.createEnumMessageRedirect(MessageAPI.Message.FAILED_CREATE_EMPLOYEE_DUPLICATE)
        } else {
            val employee = sd.bc.tru.createEmployee(employeename)
            sd.bc.au.createInvitation(employee)
            redirectTo(path)
        }

    }

    private fun existingEmployeesHTML(): String {
        // a map of employees to invitations
        val empsToInvs = mutableMapOf<Employee, Invitation?>()
        for (employee in sd.bc.tru.listAllEmployees()) {
            val invitations = sd.bc.au.listAllInvitations().filter { it.employee == employee }
            if (invitations.none()) {
                empsToInvs[employee] = null
            } else {
                for (invitation in invitations) {
                    empsToInvs[employee] = invitation
                }
            }
        }

        val employeeRows =
            empsToInvs.entries.sortedByDescending { it.key.id.value }.joinToString("") {
                val invitationLink = if (it.value == null) "" else
                    """<a href="https://${sd.so.host}:${sd.so.sslPort}/${RegisterAPI.path}?${RegisterAPI.Elements.INVITATION_INPUT.getElemName()}=${safeAttr(it.value?.code?.value ?: "")}">copy this link</a>""".trimIndent()
                val maybeDisabled = if (sd.bc.au.getUserByEmployee(it.key) == NO_USER) "" else "disabled"
"""
<tr>
    <td>${safeHtml(it.key.name.value)}</td>
    <td>$invitationLink</td>
    <td>
         <form action="${DeleteEmployeeAPI.path}" method="post">
            <input type="hidden" name="${DeleteEmployeeAPI.Elements.EMPLOYEE_ID.getElemName()}" value="${it.key.id.value}" />
            <button $maybeDisabled id="${Elements.DELETE_BUTTON.getId()}">Delete</button>
        </form>
    </td>
</tr>
"""
            }

        return """
                <div class="container">
                <table>
                    <thead>
                        <tr>
                            <th id="name">Name</th>
                            <th id="invitation">Invitation code</th>
                            <th id="act"></th>
                        </tr>
                    </thead>
                    <tbody>
                        $employeeRows
                    </tbody>
                </table>
                </div>
        """
    }

    private fun createEmployeeHTML() : String {
        val body = """
        <div id="outermost_container">    
            <div id="inner_container">
                <form action="$path" method="post">
                    <p>
                        <label for="${Elements.EMPLOYEE_INPUT.getElemName()}">Name:</label>
                        <input autocomplete="off" name="${Elements.EMPLOYEE_INPUT.getElemName()}" id="${Elements.EMPLOYEE_INPUT.getId()}" type="text"  minlength="1" maxlength="$maxEmployeeNameSize" required="required" autofocus />
                    </p>
                
                    <p>
                        <button id="${Elements.CREATE_BUTTON.getId()}">Create new employee</button>
                    </p>
                
                </form>
                ${existingEmployeesHTML()}
            </div>    
        </div>
    """
        return PageComponents(sd).makeTemplate("Create Employee", "CreateEmployeeAPI", body,
            extraHeaderContent="""
                <link rel="stylesheet" href="createemployee.css" />
                <script src="createemployee.js"></script>
                """.trimIndent())
    }
}
