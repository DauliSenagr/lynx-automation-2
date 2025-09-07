package com.adda52.utils.architecture;

import org.apache.commons.lang3.SystemUtils;

/**
 * @author Dauli Sengar
 * @since 15th May 2022
 * Utility class for retrieving information about the operating system architecture.
 */
public class Architecture {

    /**
     * Enum representing different types of operating systems.
     */
    public enum OSType {
        WINDOWS,    // Represents the Windows operating system
        MAC,        // Represents the macOS operating system
        LINUX,      // Represents the Linux operating system
        OTHER       // Represents other or unknown operating systems
    }

    /**
     * Retrieves the type of operating system.
     *
     * @return The operating system type as OSType enum value
     */
    public static OSType getOperatingSystemType() {
        if (SystemUtils.IS_OS_WINDOWS) {
            return OSType.WINDOWS;
        } else if (SystemUtils.IS_OS_MAC) {
            return OSType.MAC;
        } else if (SystemUtils.IS_OS_LINUX) {
            return OSType.LINUX;
        } else {
            return OSType.OTHER;
        }
    }

    /**
     * Checks if the operating system architecture is 64-bit.
     *
     * @return True if the system architecture is 64-bit, false otherwise
     */
    public static boolean is64bits() {
        return SystemUtils.OS_ARCH.contains("64");
    }
}

