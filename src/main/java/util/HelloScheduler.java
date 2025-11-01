package util;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Timer;
import java.util.TimerTask;

/**
 * A simple scheduler that prints "Hello" every 2 minutes.
 * Runs for a maximum of 10 minutes to avoid indefinite execution.
 */
public class HelloScheduler {
    
    private static final long INTERVAL_MS = 2 * 60 * 1000; // 2 minutes in milliseconds
    private static final long MAX_RUNTIME_MS = 10 * 60 * 1000; // 10 minutes max runtime
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm:ss");
    
    public static void main(String[] args) {
        System.out.println("HelloScheduler started at " + getCurrentTime());
        System.out.println("Will say hello every 2 minutes for the next 10 minutes...\n");
        
        Timer timer = new Timer();
        
        // Schedule the hello task
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                System.out.println("[" + getCurrentTime() + "] Hello");
            }
        }, 0, INTERVAL_MS); // Start immediately, repeat every 2 minutes
        
        // Schedule shutdown after max runtime
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                System.out.println("\n[" + getCurrentTime() + "] Max runtime reached. Shutting down...");
                timer.cancel();
                System.exit(0);
            }
        }, MAX_RUNTIME_MS);
    }
    
    private static String getCurrentTime() {
        return LocalDateTime.now().format(TIME_FORMATTER);
    }
}
