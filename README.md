# 🤖 Smart Study Planner (JavaFX + Gemini AI)

A modular desktop application built with **Java 21** designed to help students optimize their learning through data-driven study plans and integrated AI assistance.

## 🚀 Key Features
* **AI Study Guide**: Integrated with **Gemini 3 Flash** to generate 7-day study plans, conceptual notes, and practice papers based on student performance.
* **Performance Tracking**: Secure **SQLite** integration to track marks across various subjects and topics.
* **Adaptive Scheduling**: Internal logic that prioritizes "Weak Topics" (scores < 60%) and adjusts study methodology accordingly.
* **Visual Syllabus Tree**: Interactive hierarchical view of chapters and topics.

## 🛠️ Tech Stack
* **Language**: Java 21 (Modular Architecture)
* **UI Framework**: JavaFX (FXML, CSS)
* **AI**: Google Gemini API (via REST)
* **Database**: SQLite JDBC
* **Build Tool**: Maven

## 📦 How to Run
1. **Download the Installer**: Go to the [Releases](your-github-link-here) section and download `SmartStudyPlanner.exe`.
2. **API Key**: Ensure you have a stable internet connection for the AI features.
use this API key == ($env:GEMINI_API_KEY="AIzaSyAIz6jUROdQgvY8BmSCDHPmnQtOrhTOv3E"; mvn javafx:run )

3. **Database**: The app will automatically initialize a local `study_db.db` file upon first launch.

## use this api key to run this ==  ##

🔑 $env:GEMINI_API_KEY="AIzaSyAIz6jUROdQgvY8BmSCDHPmnQtOrhTOv3E"; mvn javafx:run 
