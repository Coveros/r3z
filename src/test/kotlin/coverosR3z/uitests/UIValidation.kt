package coverosR3z.uitests

import coverosR3z.bdd.BDDHelpers
import coverosR3z.authentication.api.RegisterAPI
import coverosR3z.authentication.types.maxPasswordSize
import coverosR3z.authentication.types.maxUserNameSize
import coverosR3z.authentication.types.minPasswordSize
import coverosR3z.authentication.types.minUserNameSize
import coverosR3z.persistence.utility.PureMemoryDatabase
import coverosR3z.server.types.BusinessCode
import coverosR3z.server.utility.Server
import io.github.bonigarcia.wdm.WebDriverManager
import org.junit.*
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.openqa.selenium.By
import org.openqa.selenium.WebDriver

class UIValidation {

    @UITest
    @Test
    fun `validation - Validation should stop me entering invalid input on the registration page`() {
        // validation won't allow it through - missing username
        rp.register("", "password12345", "Administrator")
        assertEquals("register", driver.title)

        // validation won't allow it through - missing password
        rp.register("alice", "", "Administrator")
        assertEquals("register", driver.title)

        // validation won't allow it through - missing employee
        driver.get("$domain/${RegisterAPI.path}")
        driver.findElement(By.id("username")).sendKeys("alice")
        driver.findElement(By.id("password")).sendKeys("password12345")
        driver.findElement(By.id("register_button")).click()
        assertEquals("register", driver.title)

        // validation won't allow it through - username too short
        rp.register("a".repeat(minUserNameSize-1), "password12345", "Administrator")
        assertEquals("register", driver.title)

        // Text entry will stop taking characters at the maximum size, so
        // what gets entered will just be truncated to [maxUserNameSize]
        val tooLongUsername = "a".repeat(maxUserNameSize+1)
        rp.register(tooLongUsername, "password12345", "Administrator")
        assertFalse(pmd.actOnUsers { users -> users.any { it.name.value == tooLongUsername } })

        // validation won't allow it through - password too short
        rp.register("alice", "a".repeat(minPasswordSize-1), "Administrator")
        assertEquals("register", driver.title)

        // Text entry will stop taking characters at the maximum size, so
        // what gets entered will just be truncated to [maxPasswordSize]
        // therefore, if we use a password too long, the system will
        // only record the password that was exactly at max size
        val maxPassword = "a".repeat(maxPasswordSize)
        rp.register("cool", maxPassword + "z", "Administrator")
        lp.login("cool", maxPassword)
        assertEquals("SUCCESS", driver.title)
    }

    /*
     _ _       _                  __ __        _    _           _
    | | | ___ | | ___  ___  _ _  |  \  \ ___ _| |_ | |_  ___  _| | ___
    |   |/ ._>| || . \/ ._>| '_> |     |/ ._> | |  | . |/ . \/ . |<_-<
    |_|_|\___.|_||  _/\___.|_|   |_|_|_|\___. |_|  |_|_|\___/\___|/__/
                 |_|
     alt-text: Helper Methods
     */


    companion object {
        private const val port = 2000
        private const val domain = "http://localhost:$port"

        private val webDriver = Drivers.CHROME
        private lateinit var sc : Server
        private lateinit var driver: WebDriver
        private lateinit var rp : RegisterPage
        private lateinit var lp : LoginPage
        private lateinit var llp : LoggingPage
        private lateinit var etp : EnterTimePage
        private lateinit var eep : EnterEmployeePage
        private lateinit var epp : EnterProjectPage
        private lateinit var lop : LogoutPage
        private lateinit var createEmployee : BDDHelpers
        private lateinit var recordTime : BDDHelpers
        private lateinit var businessCode : BusinessCode
        private lateinit var pmd : PureMemoryDatabase


        @BeforeClass
        @JvmStatic
        fun setup() {
            // install the most-recent chromedriver
            WebDriverManager.chromedriver().setup()
            WebDriverManager.firefoxdriver().setup()

            // setup for BDD
            createEmployee = BDDHelpers("createEmployeeBDD.html")
            recordTime = BDDHelpers("enteringTimeBDD.html")
        }

        @AfterClass
        @JvmStatic
        fun shutDown() {
            createEmployee.writeToFile()
            recordTime.writeToFile()

        }

    }

    @Before
    fun init() {
        // start the server
        sc = Server(port)
        pmd = Server.makeDatabase()
        businessCode = Server.initializeBusinessCode(pmd)
        sc.startServer(businessCode)

        driver = webDriver.driver()

        rp = RegisterPage(driver, domain)
        lp = LoginPage(driver, domain)
        etp = EnterTimePage(driver, domain)
        eep = EnterEmployeePage(driver, domain)
        epp = EnterProjectPage(driver, domain)
        llp = LoggingPage(driver, domain)
        lop = LogoutPage(driver, domain)

    }

    @After
    fun cleanup() {
        sc.halfOpenServerSocket.close()
        driver.quit()
    }

}