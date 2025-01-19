package Hoot.Docs;

import Hoot.Runtime.Faces.Logging;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

/**
 * Launches the documentation server.
 * @author nik <nikboyd@sonic.net>
 * @see "Copyright 2010,2025 Nikolas S Boyd."
 * @see "Permission is granted to copy this work provided this copyright statement is retained in all copies."
 */
@SpringBootApplication
public class AppMain implements Logging {

    static final Class<AppMain> Main = AppMain.class;
    static final String Started = Main.getSimpleName()+" started";
    static final String Stopped = Main.getSimpleName()+" stopped";
    public void reportStart() { report(Started); }
    public void reportStop() { report(Stopped); }
    void addExitHook() {
        Runtime.getRuntime().addShutdownHook(
            new Thread(() -> { reportStop(); }));
    }

    static ConfigurableApplicationContext MainContext;
    static AppMain getMain() { return MainContext.getBean(Main); }
    static void startMain(String... args) { MainContext = SpringApplication.run(Main, args); }
    public static void main(String... args) {
        startMain(args);
        getMain().addExitHook();
        getMain().reportStart();
    }

} // AppMain
