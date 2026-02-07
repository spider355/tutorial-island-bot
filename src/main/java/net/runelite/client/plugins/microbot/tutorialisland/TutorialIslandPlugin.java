package net.runelite.client.plugins.microbot.tutorialisland;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;
import net.runelite.client.config.ConfigSection;

@ConfigGroup("tutorialisland")
public interface TutorialIslandConfig extends Config {

    @ConfigSection(
            name = "General Settings",
            description = "General bot settings",
            position = 0
    )
    String generalSection = "general";

    @ConfigSection(
            name = "Account Settings",
            description = "Account configuration options",
            position = 1
    )
    String accountSection = "account";

    @ConfigSection(
            name = "Advanced Settings",
            description = "Advanced configuration options",
            position = 2
    )
    String advancedSection = "advanced";

    @ConfigItem(
            keyName = "walkToFalador",
            name = "Walk to Falador Bank",
            description = "After completing Tutorial Island, walk to Falador bank",
            position = 1,
            section = generalSection
    )
    default boolean walkToFalador() {
        return true;
    }

    @ConfigItem(
            keyName = "actionDelay",
            name = "Action Delay (ms)",
            description = "Delay between actions in milliseconds (100-2000)",
            position = 2,
            section = generalSection
    )
    default int actionDelay() {
        return 600;
    }

    @ConfigItem(
            keyName = "randomizeDelay",
            name = "Randomize Delay",
            description = "Add random variance to action delays for more human-like behavior",
            position = 3,
            section = generalSection
    )
    default boolean randomizeDelay() {
        return true;
    }

    @ConfigItem(
            keyName = "enableIronmanMode",
            name = "Enable Ironman Mode",
            description = "Select Ironman mode at the end of Tutorial Island",
            position = 1,
            section = accountSection
    )
    default boolean enableIronmanMode() {
        return false;
    }

    @ConfigItem(
            keyName = "ironmanType",
            name = "Ironman Type",
            description = "Type of Ironman account to create (only if Ironman Mode enabled)",
            position = 2,
            section = accountSection
    )
    default IronmanType ironmanType() {
        return IronmanType.REGULAR_IRONMAN;
    }

    @ConfigItem(
            keyName = "enableDebugLogging",
            name = "Debug Logging",
            description = "Enable detailed debug logging for troubleshooting",
            position = 1,
            section = advancedSection
    )
    default boolean enableDebugLogging() {
        return false;
    }

    @ConfigItem(
            keyName = "maxRetries",
            name = "Max Retries",
            description = "Maximum number of retries before shutting down (1-20)",
            position = 2,
            section = advancedSection
    )
    default int maxRetries() {
        return 10;
    }

    enum IronmanType {
        REGULAR_IRONMAN("Ironman"),
        HARDCORE_IRONMAN("Hardcore Ironman"),
        ULTIMATE_IRONMAN("Ultimate Ironman");

        private final String displayName;

        IronmanType(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }
}
