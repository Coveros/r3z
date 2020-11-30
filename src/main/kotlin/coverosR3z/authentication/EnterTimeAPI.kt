package coverosR3z.authentication

import coverosR3z.domainobjects.*
import coverosR3z.misc.safeHtml
import coverosR3z.misc.successHTML
import coverosR3z.server.*
import coverosR3z.timerecording.ITimeRecordingUtilities

enum class EnterTimeElements (val elemName: String, val id: String) {
    PROJECT_INPUT("project_entry", "project_entry"),
    TIME_INPUT("time_entry", "time_entry"),
    DETAIL_INPUT("detail_entry", "detail_entry"),
    ENTER_TIME_BUTTON("", "enter_time_button"),
}

fun doGetTimeEntriesPage(tru: ITimeRecordingUtilities, rd: RequestData): PreparedResponseData {
    return if (isAuthenticated(rd)) {
        okHTML(existingTimeEntriesHTML(rd.user.name.value, tru.getAllEntriesForEmployee(rd.user.employeeId ?: NO_EMPLOYEE.id)))
    } else {
        redirectTo(NamedPaths.HOMEPAGE.path)
    }
}

fun doGETEnterTimePage(tru : ITimeRecordingUtilities, rd : RequestData): PreparedResponseData {
    return if (isAuthenticated(rd)) {
        okHTML(entertimeHTML(rd.user.name.value, tru.listAllProjects()))
    } else {
        redirectTo(NamedPaths.HOMEPAGE.path)
    }
}


fun handlePOSTTimeEntry(tru: ITimeRecordingUtilities, user: User, data: Map<String, String>) : PreparedResponseData {
    val isAuthenticated = user != NO_USER
    return if (isAuthenticated) {
        val projectId = ProjectId.make(data[EnterTimeElements.PROJECT_INPUT.elemName])
        val time = Time.make(data[EnterTimeElements.TIME_INPUT.elemName])
        val details = Details.make(data[EnterTimeElements.DETAIL_INPUT.elemName])

        val project = tru.findProjectById(projectId)
        val employee = tru.findEmployeeById(checkNotNull(user.employeeId){employeeIdNotNullMsg})

        val timeEntry = TimeEntryPreDatabase(
                employee,
                project,
                time,
                Date.now(),
                details)

        tru.recordTime(timeEntry)

        okHTML(successHTML)
    } else {
        handleUnauthorized()
    }
}


fun entertimeHTML(username: String, projects : List<Project>) : String {
    return """
    <!DOCTYPE html>        
    <html>
    <head>
        <title>enter time</title>
        <meta name="viewport" content="width=device-width, initial-scale=1">
        <link rel="stylesheet" href="entertime.css" />
        <script src="entertime.js"></script>
    </head>
    <body>
        <form action="entertime" method="post">

            <p>
                Hello there, <span id="username">${safeHtml(username)}</span>!
            </p>

            <p>
                <label for="project_entry">Project:</label>
                <select name="project_entry" id="project_entry"/>
                """ + projects.joinToString("") { "<option value =\"${it.id.value}\">${safeHtml(it.name.value)}</option>\n" } +"""             <option selected disabled hidden>Choose here</option>
            </select>
            </p>

            <p>
                <label for="${EnterTimeElements.TIME_INPUT.elemName}">Time:</label>
                <input name="${EnterTimeElements.TIME_INPUT.elemName}" id="${EnterTimeElements.TIME_INPUT.id}" type="text" />
            </p>

            <p>
                <label for="${EnterTimeElements.DETAIL_INPUT.elemName}">Details:</label>
                <input name="${EnterTimeElements.DETAIL_INPUT.elemName}" id="${EnterTimeElements.DETAIL_INPUT.id}" type="text" />
            </p>

            <p>
                <button id="${EnterTimeElements.ENTER_TIME_BUTTON.id}">Enter time</button>
            </p>

        </form>
    </body>
</html>
"""
}

const val enterTimeCSS = """
    img {
  height: 200px;
}

.date {
    text-align: right;
}

.entry {
  padding-left: 6px;
  padding-right: 3px;
}

hr {
    margin-top: 50px;
    margin-bottom: 50px;
    margin-left: 10%;
    margin-right: 10%;
}

body {
    font-family: "serif";
    font-size: larger;
    background-color: #f7f7f8;
}

.content-interior {
    text-align: center;
}

.content-header {
    font-family: sans-serif;
    font-weight: 700;
}

.content-subheader {
    font-style: italic;
    padding-left: 20px;
}

pre {
  border: 4px dashed gray;
  padding: 10px 5px 10px 10px;
	font-size: 10pt;
  overflow-x: auto;
  overflow-y: auto;
  max-height: 400px;
}

.content-body {
    line-height: 1.2em;
}

.quote {
    padding-left: 5%;
    padding-right: 5%;
    margin-bottom: 0.5em;
    font-size: 12px;
}

.prime {
    margin-left: auto;
    margin-right: auto;
    margin-top: 20px;
    border:  double gray;
    max-width: 690px;
    min-width: 300px;
    background-color: white;
}

li.menu_item {
    list-style-type:none;
    margin:0;
    padding-left:10px;
    padding-right:10px;
    display: inline;
    font: 16px monospace;
    text-align: center;
}

ul.navmenu {
    display: block;
    text-align: center;
    padding: 0px;
}

li.menu_item a{
    text-decoration: none;
}

li.menu_item a:hover{
    text-decoration: underline;
}

.secondary {
    margin: 10px;
}

.content_interior {
    margin-left: 3%;
    margin-right: 3%;
}

.content {
    margin: 0.5%;
    border: 1px solid grey;
}

a {
    color: inherit;
}

h2 {
  margin-bottom: 0;
}

a:visited {
    color: grey;
}

div.banner h2.titleheader a {
    color: inherit;
    text-decoration: none;
}

div.banner h2.titleheader a:visited {
    color: inherit;
    text-decoration: none;
}

 @media all and (max-width: 500px) {

   body {
     margin: 0;
   }

    .prime {
        margin-top: 0;
        border:  none;
    }

    .secondary {
        margin: 0;
    }
 }

 @media print {

   body {
     margin: 0;
     font-family: "serif";
     font-size: normal;
     background-color: initial;
   }

   pre {
     border: none;
     padding: 0;
     font-size: inherit;
     overflow-x: inherit;
     overflow-y: inherit;
     max-height: initial;
   }

    .prime {
      margin-left: 0;
      margin-right:0;
      margin-top: 0;
      border:  none;
      max-width: initial;
      min-width: initial;
      background-color: initial;
    }

    .secondary {
        margin: 0;
    }

    .content.banner, .content.nav {
       display: none;
    }

    .content-body {
        line-height: initial;
    }
 }
"""

const val enterTimeJS = """
    console.log("Hello from JavaScript land")
"""


fun existingTimeEntriesHTML(username : String, te : Map<Date, Set<TimeEntry>>) : String {
    return """
<!DOCTYPE html>        
<html>
    <head>
        <title>your time entries</title>
        <meta name="viewport" content="width=device-width, initial-scale=1">
        <style>
        table,
        td {
            border: 1px solid #333;
        }

        thead,
        tfoot {
            background-color: #333;
            color: #fff;
        }

        </style>
    </head>
    <body>
        <p>
            Here are your entries, <span id="username">$username</span>
        </p>
        <table>
            <thead>
                <tr>
                    <th>Project</th>
                    <th>Time</th>
                    <th>Details</th>
                    <th>Date</th>
                </tr>
            </thead>
            <tbody>
                
            """ + te.flatMap { it.value }.joinToString("") { "<tr><td>${safeHtml(it.project.name.value)}</td><td>${it.time.numberOfMinutes}</td><td>${safeHtml(it.details.value)}</td><td>${it.date.stringValue}</td></tr>\n" } + """    
            </tbody>
        </table>

    </body>
</html>
"""
}