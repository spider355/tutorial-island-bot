package net.runelite.client.plugins.microbot.tutorialisland.managers;

import lombok.extern.slf4j.Slf4j;
import net.runelite.api.widgets.Widget;
import net.runelite.client.plugins.microbot.Microbot;
import net.runelite.client.plugins.microbot.tutorialisland.TutorialIslandConfig;
import net.runelite.client.plugins.microbot.util.math.Rs2Random;
import net.runelite.client.plugins.microbot.util.widget.Rs2Widget;

import javax.inject.Inject;
import javax.inject.Singleton;

import static net.runelite.client.plugins.microbot.util.Global.sleep;
import static net.runelite.client.plugins.microbot.util.Global.sleepUntil;

@Slf4j
@Singleton
public class IronmanAccountManager {

    @Inject
    private ErrorRecoveryManager errorRecovery;

    private static final int ACCOUNT_TYPE_WIDGET_GROUP = 558;
    private static final int IRONMAN_OPTION_CHILD = 12;
    private static final int HARDCORE_OPTION_CHILD = 13;
    private static final int ULTIMATE_OPTION_CHILD = 14;
    private static final int REGULAR_OPTION_CHILD = 11;
    private static final int CONFIRM_BUTTON_CHILD = 15;

    public boolean isAccountSelectionOpen() {
        return Rs2Widget.isWidgetVisible(ACCOUNT_TYPE_WIDGET_GROUP, 0);
    }

    public boolean selectAccountType(TutorialIslandConfig config) {
        if (!isAccountSelectionOpen()) {
            log.warn("Account selection interface is not open");
            return false;
        }

        if (!config.enableIronmanMode()) {
            log.info("Ironman mode disabled, selecting regular account");
            return selectRegularAccount(config);
        }

        log.info("Ironman mode enabled, selecting: {}", config.ironmanType().getDisplayName());
        
        switch (config.ironmanType()) {
            case REGULAR_IRONMAN:
                return selectIronman(config);
            case HARDCORE_IRONMAN:
                return selectHardcoreIronman(config);
            case ULTIMATE_IRONMAN:
                return selectUltimateIronman(config);
            default:
                log.error("Unknown ironman type: {}", config.ironmanType());
                return selectRegularAccount(config);
        }
    }

    private boolean selectRegularAccount(TutorialIslandConfig config) {
        log.debug("Selecting regular account");
        return selectOption(REGULAR_OPTION_CHILD, "Regular Account", config);
    }

    private boolean selectIronman(TutorialIslandConfig config) {
        log.debug("Selecting Ironman mode");
        return selectOption(IRONMAN_OPTION_CHILD, "Ironman", config);
    }

    private boolean selectHardcoreIronman(TutorialIslandConfig config) {
        log.debug("Selecting Hardcore Ironman mode");
        return selectOption(HARDCORE_OPTION_CHILD, "Hardcore Ironman", config);
    }

    private boolean selectUltimateIronman(TutorialIslandConfig config) {
        log.debug("Selecting Ultimate Ironman mode");
        return selectOption(ULTIMATE_OPTION_CHILD, "Ultimate Ironman", config);
    }

    private boolean selectOption(int childId, String optionName, TutorialIslandConfig config) {
        Widget optionWidget = Rs2Widget.getWidget(ACCOUNT_TYPE_WIDGET_GROUP, childId);
        
        if (optionWidget == null) {
            log.warn("{} option widget not found", optionName);
            return errorRecovery.handleError("selectAccountType_" + optionName, 
                "Widget not found");
        }

        Rs2Widget.clickWidget(optionWidget);
        sleep(Rs2Random.between(300, 600));

        sleep(config.actionDelay());

        return confirmSelection(config);
    }

    private boolean confirmSelection(TutorialIslandConfig config) {
        log.debug("Confirming account type selection");

        Widget confirmButton = Rs2Widget.getWidget(ACCOUNT_TYPE_WIDGET_GROUP, CONFIRM_BUTTON_CHILD);
        
        if (confirmButton == null) {
            log.warn("Confirm button widget not found");
            return errorRecovery.handleError("confirmAccountType", 
                "Confirm button not found");
        }

        Rs2Widget.clickWidget(confirmButton);
        sleep(Rs2Random.between(400, 700));

        boolean closed = sleepUntil(() -> !isAccountSelectionOpen(), 5000);

        if (closed) {
            log.info("Account type selection confirmed and interface closed");
            Microbot.log("Account type selected successfully!");
            errorRecovery.resetError("confirmAccountType");
            return true;
        } else {
            log.warn("Account selection interface did not close after confirmation");
            return errorRecovery.handleError("confirmAccountType", 
                "Interface didn't close");
        }
    }

    public boolean waitForAccountSelection(int timeoutMs) {
        log.debug("Waiting for account selection interface (timeout: {}ms)", timeoutMs);
        
        boolean appeared = sleepUntil(this::isAccountSelectionOpen, timeoutMs);
        
        if (appeared) {
            log.info("Account selection interface appeared");
            sleep(500);
            return true;
        } else {
            log.warn("Account selection interface did not appear within timeout");
            return false;
        }
    }
}
