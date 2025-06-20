package algo.transit.utils;

import algo.transit.enums.TType;
import org.jetbrains.annotations.NotNull;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

public class CLParser {
    public static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");

    public static void printUsage() {
        System.out.println("Usage: java -jar transit.jar START_STOP END_STOP START_TIME [OPTIONS]");
        System.out.println("Options:");
        System.out.println("  --walking-speed <speed>      Set walking speed in meters per minute (default: 80.0)");
        System.out.println("  --max-walk-time <time>       Set maximum walking time in minutes (default: 10.0)");
        System.out.println("  --forbidden-modes <modes>    Set forbidden transport modes (e.g., BUS, TRAIN)");
        System.out.println("  --mode-weights <mode:weight> Set custom weights for transport modes");
        System.out.println("  --arrive-by                  Find path arriving at specified time, not departing");
        System.out.println("  --optimization-goal <goal>   Set optimization goal: time|transfers|walking (default: time)");
        System.out.println("  --output-format <format>     Set output format: detailed|summary (default: detailed)");
        System.out.println("  --show-stats                 Show detailed statistics about the found path");
        System.out.println("  --visualize                  Enable visualization of the pathfinding algorithm");
        System.out.println("  --help                       Display this help message");
    }

    public static @NotNull CLArgs parseCommandLineArgs(String @NotNull [] args) {
        CLArgs cmdArgs = new CLArgs();

        if (args.length < 3) {
            throw new IllegalArgumentException("Insufficient arguments. Use --help for usage information.");
        }

        if (args[0].equals("--help")) {
            printUsage();
            System.exit(0);
        }

        cmdArgs.setStartStop(args[0]);
        cmdArgs.setEndStop(args[1]);

        try {
            cmdArgs.setStartTime(LocalTime.parse(args[2], TIME_FORMATTER));
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("Invalid time format. Use HH:MM format (e.g., 08:00)");
        }

        // Parse optional arguments
        for (int i = 3; i < args.length; i++) {
            String arg = args[i];

            try {
                switch (arg) {
                    case "--walking-speed" -> {
                        if (i + 1 < args.length) {
                            cmdArgs.setWalkingSpeed(Double.parseDouble(args[++i]));
                        } else {
                            throw new IllegalArgumentException("Missing value for --walking-speed");
                        }
                    }
                    case "--max-walk-time" -> {
                        if (i + 1 < args.length) {
                            cmdArgs.setMaxWalkTime(Double.parseDouble(args[++i]));
                        } else {
                            throw new IllegalArgumentException("Missing value for --max-walk-time");
                        }
                    }
                    case "--forbidden-modes" -> {
                        if (i + 1 >= args.length || args[i + 1].startsWith("--")) {
                            throw new IllegalArgumentException("Missing values for --forbidden-modes");
                        }
                        while (i + 1 < args.length && !args[i + 1].startsWith("--")) {
                            try {
                                cmdArgs.getForbiddenModes().add(TType.valueOf(args[++i]));
                            } catch (IllegalArgumentException e) {
                                System.err.println("Invalid transport type: " + args[i] + ". Skipping.");
                            }
                        }
                    }
                    case "--mode-weights" -> {
                        if (i + 1 >= args.length || args[i + 1].startsWith("--")) {
                            throw new IllegalArgumentException("Missing values for --mode-weights");
                        }
                        while (i + 1 < args.length && !args[i + 1].startsWith("--")) {
                            String weightSpec = args[++i];
                            String[] parts = weightSpec.split(":");
                            if (parts.length == 2) {
                                try {
                                    cmdArgs.getModeWeights().put(TType.valueOf(parts[0]), Double.parseDouble(parts[1]));
                                } catch (NumberFormatException e) {
                                    System.err.println("Invalid weight format: " + weightSpec + ". Expected format: mode:weight");
                                } catch (IllegalArgumentException e) {
                                    System.err.println("Invalid transport type: " + parts[0] + ". Skipping.");
                                }
                            } else {
                                System.err.println("Invalid mode weight format: " + weightSpec + ". Expected format: mode:weight");
                            }
                        }
                    }
                    case "--arrive-by" -> cmdArgs.setArriveBy(true);
                    case "--optimization-goal" -> {
                        if (i + 1 < args.length) {
                            String goal = args[++i].toLowerCase();
                            if (goal.equals("time") || goal.equals("transfers") || goal.equals("walking")) {
                                cmdArgs.setOptimizationGoal(goal);
                            } else {
                                System.err.println("Invalid optimization goal: " + goal +
                                        ". Using default (time). Valid options: time, transfers, walking");
                                cmdArgs.setOptimizationGoal("time");
                            }
                        } else {
                            throw new IllegalArgumentException("Missing value for --optimization-goal");
                        }
                    }
                    case "--output-format" -> {
                        if (i + 1 < args.length) {
                            String format = args[++i].toLowerCase();
                            if (format.equals("detailed") || format.equals("summary")) {
                                cmdArgs.setOutputFormat(format);
                            } else {
                                System.err.println("Invalid output format: " + format +
                                        ". Using default (detailed). Valid options: detailed, summary");
                                cmdArgs.setOutputFormat("detailed");
                            }
                        } else {
                            throw new IllegalArgumentException("Missing value for --output-format");
                        }
                    }
                    case "--show-stats" -> cmdArgs.setShowStats(true);
                    case "--visualize" -> cmdArgs.setVisualize(true);
                    case "--help" -> {
                        printUsage();
                        System.exit(0);
                    }
                    default -> throw new IllegalArgumentException("Unknown argument: " + arg);
                }
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("Invalid numeric value for " + arg);
            }
        }

        return cmdArgs;
    }
}