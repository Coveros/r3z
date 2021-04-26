package coverosR3z.timerecording.api

import coverosR3z.authentication.types.Role
import coverosR3z.server.api.MessageAPI
import coverosR3z.system.misc.utility.safeHtml
import coverosR3z.server.types.*
import coverosR3z.server.utility.AuthUtilities.Companion.doGETRequireAuth
import coverosR3z.server.utility.AuthUtilities.Companion.doPOSTAuthenticated
import coverosR3z.server.utility.PageComponents
import coverosR3z.server.utility.ServerUtilities.Companion.redirectTo
import coverosR3z.timerecording.types.NO_PROJECT
import coverosR3z.timerecording.types.ProjectName
import coverosR3z.timerecording.types.maxProjectNameSize

class ProjectAPI(private val sd: ServerData) {

    enum class Elements(private val elemName: String, private val id: String) : Element {
        PROJECT_INPUT("project_name", "project_name"),
        CREATE_BUTTON("", "project_create_button"),;

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

        override val requiredInputs = setOf(
            Elements.PROJECT_INPUT
        )
        override val path: String
            get() = "createproject"

        override fun handleGet(sd: ServerData): PreparedResponseData {
            val p = ProjectAPI(sd)
            return doGETRequireAuth(sd.ahd.user, Role.ADMIN) { p.createProjectHTML() }
        }

        override fun handlePost(sd: ServerData): PreparedResponseData {
            val p = ProjectAPI(sd)
            return doPOSTAuthenticated(sd.ahd.user, requiredInputs, sd.ahd.data, Role.ADMIN) { p.handlePOST() }
        }


    }

    fun handlePOST() : PreparedResponseData {
        val projectNameString = checkNotNull(sd.ahd.data.mapping[Elements.PROJECT_INPUT.getElemName()])
        val projectNameTrimmed = projectNameString.trim()
        val projectName = ProjectName(projectNameTrimmed)
        return if (sd.bc.tru.findProjectByName(projectName) != NO_PROJECT) {
            MessageAPI.createMessageRedirect(MessageAPI.Message.FAILED_CREATE_PROJECT_DUPLICATE)
        } else {
            sd.bc.tru.createProject(projectName)
            return redirectTo(path)
        }
    }


    private fun existingProjectsHtml(): String {
        val projectRows = sd.bc.tru.listAllProjects().sortedByDescending { it.id.value }.joinToString("") {
"""
    <tr>
        <td>${safeHtml(it.name.value)}</td>
    </tr>
"""
            }

        return """
                <div class="container">
                <table>
                    <thead>
                        <tr>
                            <th>Name</th>
                        </tr>
                    </thead>
                    <tbody>
                        $projectRows
                    </tbody>
                </table>
                </div>
        """
    }

    private fun createProjectHTML() : String {
        val body = """
            <div id="outermost_container">
                <form action="$path" method="post">
                    <p>
                        <label for="${Elements.PROJECT_INPUT.getElemName()}">Name:</label>
                        <input autocomplete="off" name="${Elements.PROJECT_INPUT.getElemName()}" id="${Elements.PROJECT_INPUT.getId()}" type="text" minlength="1" maxlength="$maxProjectNameSize" required="required" pattern="[\s\S]*\S[\s\S]*" oninvalid="this.setCustomValidity('Enter one or more non-whitespace characters')" oninput="this.setCustomValidity('')"  autofocus />
                    </p>
                
                    <p>
                        <button id="${Elements.CREATE_BUTTON.getId()}">Create new project</button>
                    </p>
                
                </form>
                ${existingProjectsHtml()}
            </div>
"""
        return PageComponents(sd).makeTemplate("create project", "ProjectAPI", body, extraHeaderContent="""<link rel="stylesheet" href="createprojects.css" />""")
    }
}

