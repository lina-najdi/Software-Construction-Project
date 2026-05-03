# Software-Construction-Project
# Smart Task Manager

A weather-aware task management desktop application built with Java, Swing, and Project Reactor.

---

## How to Run

**Requirements:**
- Java 17+
- Maven

**Steps:**
```bash
mvn compile
mvn exec:java -Dexec.mainClass=taskmanager.ui.MainApp
```

---

## Where to Put the API Key

1. Sign up for a free API key at [openweathermap.org](https://openweathermap.org)
2. Open `src/main/java/taskmanager/ui/MainApp.java`
3. Replace `YOUR_API_KEY_HERE` with your key:

```java
TaskManager tm = TaskManager.builder()
        .withWeatherApiKey("YOUR_REAL_KEY_HERE")
        .build();
```

> If no API key is provided, the app runs in demo mode with simulated weather data (32°C, Clear).

---

## TaskManager Usage Example

```java
// Build the TaskManager
TaskManager tm = TaskManager.builder()
        .withWeatherApiKey("your_api_key")
        .build();

// Add a task
Task task = new Task("task-001", "Morning Run",
        LocalDateTime.now().plusHours(2), true);
tm.addTask(task);

// Get all tasks
List<Task> tasks = tm.getTasks();

// Fetch weather for a city (non-blocking)
tm.fetchWeather("Riyadh")
  .subscribe(forecast -> System.out.println(forecast.getCondition()));

// Remove a task
tm.removeTask("task-001");
```
