import java.util.Scanner
import java.sql.DriverManager
import java.sql.Connection
import java.sql.ResultSet
import java.sql.Statement

object Project0 {

    private val scanner = new Scanner(System.in)
    private var statement: Statement = null

    def main(args: Array[String]): Unit = {

        val driver = "com.mysql.jdbc.Driver"
        val url = "jdbc:mysql://localhost:3306/diary"
        val username = "root"
        val password = "##"

        Class.forName(driver)
        val connection = DriverManager.getConnection(url, username, password)
        statement = connection.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE);        

        var continue = true

        while (continue) {
            var nextStep = getCommand()
            if (nextStep == "new") {
                createEntry()
            }
            else if (nextStep == "read") {
                readEntry()
            }
            else if (nextStep == "quit") {
                continue = false
            }
            else {
                println("")
                println("That's not one of the options. Please type \"new\", \"read\", or \"quit\". ")
                println("")
            }
        }

        println("")
        println("Your secrets are safe with me...")
        println("")
        println("See ya!")
        println("")

    }

    def getCommand(): String = {
        println("")
        println("WELCOME! This is a diary! You got several options for what to do next:\n")
        println("Type \"new\" to add a new journal entry.")
        println("Type \"read\" to read past entries.")
        println("Type \"quit\" to quit.\n")

        return scanner.nextLine().toLowerCase()
    }

    def createEntry(): Unit = {
        println("")
        println("Lovely. Pour your heart out.")
        println("")
        var newEntry = scanner.nextLine()
        println("")
        print("Juicy stuff. Hit enter again to go back to the start. ")
        scanner.nextLine()
        println("")
        persistEntry(newEntry)
    }

    def persistEntry(entry: String): Unit = {
        var resultSet = statement.executeQuery("SELECT * FROM entries ORDER BY id;")
        var resultCount = 0
        while (resultSet.next()) resultCount += 1
        statement.executeUpdate(s"INSERT INTO entries (id, content) VALUES (${resultCount + 1}, \"$entry\");")
    }

    def readEntry(): Unit = {
        var resultSet = statement.executeQuery("SELECT * FROM entries ORDER BY id;")
        var resultCount = 0

        while (resultSet.next()) {
            var id = resultSet.getInt("id")
            var content = resultSet.getString("content")
            println("")
            println(s"Entry $id: $content")
            resultCount += 1
        }
        
        if (resultCount == 0) {
            println("")
            print("Sorry, this journal's empty. There's nothing to read. Hit enter to head back to the start. ")
        } else {
            println("")
            print("Done reading? Hit enter to get back. Or you can type \"update\" to make some edits, or \"delete\" to get rid of an entry. ")
        }

        var nextCommand = scanner.nextLine().toLowerCase()

        if (nextCommand == "update" || nextCommand == "delete") {
            var updateEntry = 0

            while (updateEntry <= 0) {
                println("")
                print(s"To select an entry, please enter a whole number between 1 and $resultCount. ")
                var errorInPlay = true

                while (errorInPlay) {
                    errorInPlay = false

                    try {
                        updateEntry = scanner.nextInt()
                        scanner.nextLine()

                        if (updateEntry <= 0) {
                            errorInPlay = true
                        } else {
                            resultSet.beforeFirst()
                            var foundID = false
                            while (resultSet.next() && !foundID) {
                                var id = resultSet.getInt("id")
                                if (id == updateEntry) foundID = true
                            }
                            if (!foundID) {
                                errorInPlay = true
                            }
                        }
                        if (errorInPlay) throw new UserInputException

                    } catch {
                        case userInput: UserInputException => {
                            println("")
                            print(s"That entry does not exist. Please enter a whole number between 1 and $resultCount. ")
                        }
                        case pokemon: Exception => {
                            println("")
                            println("Something went wrong. Let's try again...")
                        }
                    }
                }
            }

            resultSet.absolute(updateEntry)
            if (nextCommand == "update") {
                println("")
                println("Edit away. You'll have to write a whole new entry.")
                println("")
                var entryEdit = scanner.nextLine()
                resultSet.updateString("content", entryEdit)
                resultSet.updateRow()
                println("")
                println("Nice! Your edits have been saved.")
            } else {
                resultSet.deleteRow()
                resultSet.beforeFirst()
                var counter = 1
                while (resultSet.next()) {
                    var id = resultSet.getInt("id")
                    if (id != counter) {
                        resultSet.updateInt(1, counter)
                        resultSet.updateRow()
                    }
                    counter += 1
                } 
                println("")
                print("Alright, the evidence is destroyed. Press enter to go back. ")
                scanner.nextLine()
            }
        }
    }
}