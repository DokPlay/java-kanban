# Task Tracker (Sprint 5)
📄 [Читать на Русском](README.md)
> A Java educational project — a task tracker with interfaces, managers, and view history. Created independently as part of a practice assignment.

---

## 📌 Features

- Adding and retrieving:
  - Simple tasks (`Task`)
  - Epics (`Epic`)
  - Subtasks (`Subtask`), linked to an epic
- Retrieving lists of tasks, epics, and subtasks
- Retrieving subtasks for a specific epic
- **History of the last 10 viewed tasks** (duplicates allowed)

---

## 🧠 Architecture

- `TaskManager` — interface for the task manager
- `InMemoryTaskManager` — implementation that stores data in memory
- `HistoryManager` — interface for the view history manager
- `InMemoryHistoryManager` — limits history to 10 tasks
- `Managers` — utility class that provides default manager instances

---

## ✅ Technologies Used

- Java 17+ / 21+ / 24 
- IntelliJ IDEA 2025 Ultimate
- JUnit 5 

---

## 🚀 Running

### Main

The entry class `Main.java` demonstrates adding tasks and printing history:
```bash
Main.java → Run
```

### Tests

1. Make sure IntelliJ marks `src/test/java` as **Test Sources Root**
2. Use:
   ```
   Right-click on test folder → Run All Tests
   ```
   or run `InMemoryTaskManagerTest.java`

---

## 🧪 Test Coverage (JUnit 5)

✔️ Adding tasks and retrieving by ID  
✔️ Checking view history  
✔️ Limiting history to 10 records  
✔️ Error when linking a subtask to a non-existent epic


---


**Author:** DokPlay  
**Project:** Educational, created as part of self-study practice in Java.

---

## ⚖️ License
This project is licensed under the MIT License.
