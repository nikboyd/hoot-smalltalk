package Hoot.Boot;

import Hoot.Compiler.HootMain;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Launches the Hoot compiler main.
 *
 * @author nik <nikboyd@sonic.net>
 * @see "Copyright 2010,2025 Nikolas S Boyd."
 * @see "Permission is granted to copy this work provided this copyright statement is retained in all copies."
 */
@SpringBootApplication
public class AppMain {

    public static void main(String... args) { HootMain.main(args); }

} // AppMain
