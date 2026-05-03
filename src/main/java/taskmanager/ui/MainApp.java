package taskmanager.ui;

import taskmanager.api.*;
import taskmanager.impl.*;
import taskmanager.model.*;
import javax.swing.*;

/**
 * Entry point for the Smart Task Manager application.
 *
 * <p>How to run:
 * <ol>
 *   <li>Run: mvn compile exec:java -Dexec.mainClass=taskmanager.api.MainApp</li>
 * </ol>
 *
 * <p>If no API key is provided, the app runs in demo mode with simulated weather data.
 */
public class MainApp {

    /**
     * Application entry point.
     *
     * @param args command-line arguments (unused)
     */
    public static void main(String[] args) {

        // Build TaskManager using the Builder pattern
        TaskManager tm = TaskManager.builder()
                .withWeatherApiKey("307efd65a6c277f3c324816239b193d0") 
                .build();

        // Launch Swing UI on the Event Dispatch Thread
        SwingUtilities.invokeLater(() -> {
            SmartTaskManagerFrame frame = new SmartTaskManagerFrame(tm);
            frame.setVisible(true);
        });
    }
}