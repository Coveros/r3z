package coverosR3z.authentication

import coverosR3z.domainobjects.*
import coverosR3z.server.*
import coverosR3z.timerecording.ITimeRecordingUtilities
import coverosR3z.misc.failureHTML
import coverosR3z.misc.safeHtml
import coverosR3z.misc.successHTML


fun doGETRegisterPage(tru: ITimeRecordingUtilities, rd: RequestData): PreparedResponseData {
    return if (isAuthenticated(rd)) {
        redirectTo(NamedPaths.AUTHHOMEPAGE.path)
    } else {
        val employees = tru.listAllEmployees()
        okHTML(registerHTML(employees))
    }
}

fun handlePOSTRegister(au: IAuthenticationUtilities, user: User, data: Map<String, String>) : PreparedResponseData {
    return if (user == NO_USER) {
        val username = UserName.make(data["username"])
        val password = Password.make(data["password"])
        val employeeId = EmployeeId.make(data["employee"])
        val result = au.register(username, password, employeeId)
        if (result == RegistrationResult.SUCCESS) {
            okHTML(successHTML)
        } else {
            okHTML(failureHTML)
        }
    } else {
        redirectTo(NamedPaths.AUTHHOMEPAGE.path)
    }
}

fun registerHTML(employees: List<Employee>) : String {
    return """
<!DOCTYPE html>        
<html>
  <head>
      <link href="https://fonts.googleapis.com/css?family=Ubuntu" rel="stylesheet">
      <title>register</title>
      <link rel="stylesheet" href="general.css" />
      <meta name="viewport" content="width=device-width, initial-scale=1.0">
  </head>
  <header><a class="home-button" href="homepage">r3z</a></header>
  <body>
    <br>
    <h2>Register a User</h2>
    
    <form method="post" action="register">
      <table> 
          <tbody>
            <tr>
                <td>
                    <label for="username">Username</label><br>
                    <input type="text" name="username" id="username">
                </td>
            </tr>
            <tr>
                <td>
                    <label for="password">Password</label><br>
                    <input type="password" name="password" id="password">
                </td>
            </tr>
            <tr>
                <td>
                    <label for="employee">Employee</label><br>
                    <select id="employee" name="employee">
                       """+employees.joinToString("") { "<option value =\"${it.id.value}\">${safeHtml(it.name.value)}</option>\n" } +"""
                       <option selected disabled hidden>Choose here</option>
                  </td>
                </select>
            </tr>
            <tr>
                <td>
                    <button id="register_button" class="submit">Register</button>
                </td>
            </tr>
        </tbody>
      </table>
    </form>
  </body>
</html>
"""
}