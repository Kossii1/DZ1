import java.io.File
import kotlin.system.exitProcess

data class Movie(val id: Int, val title: String, val duration: Int)

data class Showtime(val movieId: Int, val time: String, val seats: MutableList<String>)

data class Ticket(val showtime: Showtime, val seat: String)

class CinemaManager(private val moviesFile: File, private val showTimesFile: File, private val soldTicketsFile: File) {
    private val movies: MutableList<Movie> = mutableListOf()
    val showTimes: MutableList<Showtime> = mutableListOf()
    val soldTickets: MutableList<Ticket> = mutableListOf()

    init {
        loadMovies()
        loadShowTimes()
        loadSoldTickets()
    }

    fun sellTicket(showtime: Showtime, seat: String) {
        if (!isSeatTaken(showtime, seat)) {
            showtime.seats.add(seat)
            soldTickets.add(Ticket(showtime, seat))
            saveShowTimes()
            saveSoldTickets()
            println("Билет продан.")
        } else {
            println("Извините, выбранное место уже занято.")
        }
    }

    fun returnTicket(ticket: Ticket) {
        if (soldTickets.contains(ticket)) {
            val showtime = ticket.showtime
            showtime.seats.remove(ticket.seat)
            soldTickets.remove(ticket)
            saveShowTimes()
            saveSoldTickets()
            println("Билет возвращен.")
        } else {
            println("Указанный билет не найден.")
        }
    }

    fun displayAvailableSeats(showtime: Showtime) {
        val takenSeats = showtime.seats
        val allSeats = mutableListOf<String>()
        for (row in 'A'..'E') {
            for (num in 1..10) {
                allSeats.add("$row$num")
            }
        }
        val availableSeats = allSeats.filterNot { seat -> takenSeats.contains(seat) }
        println("Доступные места для сеанса ${showtime.time}:")
        availableSeats.forEach { seat -> println(seat) }
    }

    fun editMovie(movie: Movie) {
        val index = movies.indexOfFirst { it.id == movie.id }
        if (index != -1) {
            movies[index] = movie
            saveMovies()
            println("Данные о фильме обновлены.")
        } else {
            println("Фильм с указанным ID не найден.")
        }
    }

    fun editShowtime(showtime: Showtime) {
        val index = showTimes.indexOfFirst { it.movieId == showtime.movieId && it.time == showtime.time }
        if (index != -1) {
            showTimes[index] = showtime
            saveShowTimes()
            println("Данные о сеансе обновлены.")
        } else {
            println("Сеанс с указанным фильмом и временем не найден.")
        }
    }

    fun markSeatsTaken(showtime: Showtime, seats: List<String>) {
        seats.forEach { seat ->
            if (!isSeatTaken(showtime, seat)) {
                showtime.seats.add(seat)
            }
        }
        saveShowTimes()
        println("Места отмечены как занятые.")
    }

    private fun isSeatTaken(showtime: Showtime, seat: String): Boolean {
        return showtime.seats.contains(seat)
    }

    private fun loadMovies() {
        if (moviesFile.exists()) {
            movies.clear()
            val lines = moviesFile.readLines()
            lines.forEach { line ->
                val parts = line.split(',')
                if (parts.size == 3) {
                    val id = parts[0].toInt()
                    val title = parts[1]
                    val duration = parts[2].toInt()
                    val movie = Movie(id, title, duration)
                    movies.add(movie)
                } else {
                    println("Некорректные входные данные в файле с информацией о фильмах!")
                    exitProcess(0)
                }
            }
        } else {
            println("Добавьте файл с информацией о фильмах movies.csv в ту же папку, где находится запущенный файл программы!")
            exitProcess(0)
        }
    }

    private fun loadShowTimes() {
        if (showTimesFile.exists()) {
            showTimes.clear()
            val lines = showTimesFile.readLines()
            lines.forEach { line ->
                val parts = line.split(',')
                if (parts.size >= 3) {
                    val movieId = parts[0].toInt()
                    val time = parts[1]
                    val seats = parts.subList(2, parts.size).toMutableList()
                    val showtime = Showtime(movieId, time, seats)
                    showTimes.add(showtime)
                } else {
                    println("Некорректные входные данные в файле сеансов!")
                    exitProcess(0)
                }
            }
        } else {
            println("Добавьте файл с информацией о сеансах showTimes.csv в ту же папку, где находится запущенный файл программы!")
            exitProcess(0)
        }
    }

    private fun loadSoldTickets() {
        if (soldTicketsFile.exists()) {
            soldTickets.clear()
            val lines = soldTicketsFile.readLines()
            lines.forEach { line ->
                val parts = line.split(',')
                if (parts.size == 3) {
                    val movieId = parts[0].toInt()
                    val time = parts[1]
                    val seat = parts[2]
                    val showtime = Showtime(movieId, time, mutableListOf())
                    val ticket = Ticket(showtime, seat)
                    soldTickets.add(ticket)
                } else {
                    println("Некорректные входные данные в файле с информацией о проданных билетах!")
                    exitProcess(0)
                }
            }
        } else {
            println("Добавьте файл с информацией о проданных билетах sold_tickets в ту же папку, где находится запущенный файл программы!")
            exitProcess(0)
        }
    }

    private fun saveMovies() {
        moviesFile.bufferedWriter().use { writer ->
            movies.forEach { movie ->
                writer.write("${movie.id},${movie.title},${movie.duration}")
                writer.newLine()
            }
        }
    }

    private fun saveShowTimes() {
        showTimesFile.bufferedWriter().use { writer ->
            showTimes.forEach { showtime ->
                writer.write("${showtime.movieId},${showtime.time}")
                showtime.seats.forEach { seat ->
                    writer.write(",$seat")
                }
                writer.newLine()
            }
        }
    }

    private fun saveSoldTickets() {
        soldTicketsFile.bufferedWriter().use { writer ->
            soldTickets.forEach { ticket ->
                val showtime = ticket.showtime
                writer.write("${showtime.movieId},${showtime.time},${ticket.seat}")
                writer.newLine()
            }
        }
    }
}

fun main() {
    try{
        val moviesFile = File("movies.csv")
        val showtimesFile = File("showTimes.csv")
        val soldTicketsFile = File("sold_tickets.csv")
        val cinemaManager = CinemaManager(moviesFile, showtimesFile, soldTicketsFile)
        // Пример использования методов
        val selectedShowtime = cinemaManager.showTimes.first()  // Выбранный сеанс
        val selectedSeat = "A1"  // Выбранное место

        // Продажа билета
        cinemaManager.sellTicket(selectedShowtime, selectedSeat)

        // Возврат билета
        val ticketToReturn = cinemaManager.soldTickets.first() // Выбранный билет для возврата
        cinemaManager.returnTicket(ticketToReturn)

        // Отображение доступных мест для сеанса
        cinemaManager.displayAvailableSeats(selectedShowtime)

        // Редактирование данных о фильмах
        val editedMovie = Movie(1, "Новое название фильма", 120) // Измененные данные о фильме
        cinemaManager.editMovie(editedMovie)

        // Редактирование данных о сеансе
        val editedShowtime = Showtime(1, "16:00", mutableListOf("A2", "A3")) // Измененные данные о сеансе
        cinemaManager.editShowtime(editedShowtime)

        // Отметка занятых мест
        val seatsToMarkTaken = listOf("A4", "A5") // Места для отметки
        cinemaManager.markSeatsTaken(selectedShowtime, seatsToMarkTaken)
    } catch(e: Exception){
        println("Что-то произошло!")
    }
}