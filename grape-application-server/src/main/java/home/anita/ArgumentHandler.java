package home.anita;

import lombok.extern.slf4j.Slf4j;

/**
 * Abstract base class for handling command line arguments.
 * Provides utility methods for parsing integer and boolean arguments.
 */
@Slf4j
public abstract class ArgumentHandler {

    /**
     * Parses an integer value from command line arguments.
     * Expects format: --argName value
     *
     * @param args         command line arguments array
     * @param argName      the argument name (without -- prefix)
     * @param defaultValue value to return if argument not found or invalid
     * @return parsed integer value or default value
     */
    protected static int getIntFromArgs(String[] args, String argName, int defaultValue) {
        for (int i = 0; i < args.length - 1; i++) {
            if (("--" + argName).equals(args[i])) {
                try {
                    return Integer.parseInt(args[i + 1]);
                } catch (NumberFormatException e) {
                    log.warn("Invalid value for {}: {}. Using default: {}", argName, args[i + 1], defaultValue);
                    return defaultValue;
                }
            }
        }
        return defaultValue;
    }

    /**
     * Parses a boolean value from command line arguments.
     * Expects format: --argName=true or --argName=false
     *
     * @param args         command line arguments array
     * @param argName      the argument name (without -- prefix)
     * @param defaultValue value to return if argument not found
     * @return parsed boolean value or default value
     */
    protected static boolean getBooleanFromArgs(String[] args, String argName, boolean defaultValue) {
        for (String arg : args) {
            if (("--" + argName + "=true").equals(arg)) {
                return true;
            }
            if (("--" + argName + "=false").equals(arg)) {
                return false;
            }
        }
        return defaultValue;
    }
}