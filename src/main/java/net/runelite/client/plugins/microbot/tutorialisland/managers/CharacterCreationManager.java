package net.runelite.client.plugins.microbot.tutorialisland.managers;

import lombok.extern.slf4j.Slf4j;
import net.runelite.api.widgets.Widget;
import net.runelite.client.plugins.microbot.Microbot;
import net.runelite.client.plugins.microbot.globval.WidgetIndices;
import net.runelite.client.plugins.microbot.util.math.Rs2Random;
import net.runelite.client.plugins.microbot.util.widget.Rs2Widget;

import javax.inject.Inject;
import javax.inject.Singleton;

import static net.runelite.client.plugins.microbot.util.Global.sleep;
import static net.runelite.client.plugins.microbot.util.Global.sleepUntil;

@Slf4j
@Singleton
public class CharacterCreationManager {

    @Inject
    private ErrorRecoveryManager errorRecovery;

    private static final int CHARACTER_CREATOR_WIDGET_ID = WidgetIndices.CharacterCreator.GROUP_INDEX;
    private static final int GENDER_FEMALE_BUTTON = WidgetIndices.CharacterCreator.GENDER_BUTTON_FEMALE_DYNAMIC_CONTAINER;
    private static final int CONFIRM_BUTTON = WidgetIndices.CharacterCreator.CONFIRM_BUTTON_DYNAMIC_CONTAINER;

    private static final int HEAD_ARROW_LEFT = WidgetIndices.CharacterCreator.DESIGN_HEAD_BUTTON_ARROW_LEFT_DYNAMIC_CONTAINER;
    private static final int HEAD_ARROW_RIGHT = WidgetIndices.CharacterCreator.DESIGN_HEAD_BUTTON_ARROW_RIGHT_DYNAMIC_CONTAINER;
    private static final int JAW_ARROW_LEFT = WidgetIndices.CharacterCreator.DESIGN_JAW_BUTTON_ARROW_LEFT_DYNAMIC_CONTAINER;
    private static final int JAW_ARROW_RIGHT = WidgetIndices.CharacterCreator.DESIGN_JAW_BUTTON_ARROW_RIGHT_DYNAMIC_CONTAINER;
    private static final int TORSO_ARROW_LEFT = WidgetIndices.CharacterCreator.DESIGN_TORSO_BUTTON_ARROW_LEFT_DYNAMIC_CONTAINER;
    private static final int TORSO_ARROW_RIGHT = WidgetIndices.CharacterCreator.DESIGN_TORSO_BUTTON_ARROW_RIGHT_DYNAMIC_CONTAINER;
    private static final int ARMS_ARROW_LEFT = WidgetIndices.CharacterCreator.DESIGN_ARMS_BUTTON_ARROW_LEFT_DYNAMIC_CONTAINER;
    private static final int ARMS_ARROW_RIGHT = WidgetIndices.CharacterCreator.DESIGN_ARMS_BUTTON_ARROW_RIGHT_DYNAMIC_CONTAINER;
    private static final int HANDS_ARROW_LEFT = WidgetIndices.CharacterCreator.DESIGN_HANDS_BUTTON_ARROW_LEFT_DYNAMIC_CONTAINER;
    private static final int HANDS_ARROW_RIGHT = WidgetIndices.CharacterCreator.DESIGN_HANDS_BUTTON_ARROW_RIGHT_DYNAMIC_CONTAINER;
    private static final int LEGS_ARROW_LEFT = WidgetIndices.CharacterCreator.DESIGN_LEGS_BUTTON_ARROW_LEFT_DYNAMIC_CONTAINER;
    private static final int LEGS_ARROW_RIGHT = WidgetIndices.CharacterCreator.DESIGN_LEGS_BUTTON_ARROW_RIGHT_DYNAMIC_CONTAINER;
    private static final int FEET_ARROW_LEFT = WidgetIndices.CharacterCreator.DESIGN_FEET_BUTTON_ARROW_LEFT_DYNAMIC_CONTAINER;
    private static final int FEET_ARROW_RIGHT = WidgetIndices.CharacterCreator.DESIGN_FEET_BUTTON_ARROW_RIGHT_DYNAMIC_CONTAINER;

    private static final int HAIR_COLOR_LEFT = WidgetIndices.CharacterCreator.COLOUR_HAIR_BUTTON_ARROW_LEFT_DYNAMIC_CONTAINER;
    private static final int HAIR_COLOR_RIGHT = WidgetIndices.CharacterCreator.COLOUR_HAIR_BUTTON_ARROW_RIGHT_DYNAMIC_CONTAINER;
    private static final int TORSO_COLOR_LEFT = WidgetIndices.CharacterCreator.COLOUR_TORSO_BUTTON_ARROW_LEFT_DYNAMIC_CONTAINER;
    private static final int TORSO_COLOR_RIGHT = WidgetIndices.CharacterCreator.COLOUR_TORSO_BUTTON_ARROW_RIGHT_DYNAMIC_CONTAINER;
    private static final int LEGS_COLOR_LEFT = WidgetIndices.CharacterCreator.COLOUR_LEGS_BUTTON_ARROW_LEFT_DYNAMIC_CONTAINER;
    private static final int LEGS_COLOR_RIGHT = WidgetIndices.CharacterCreator.COLOUR_LEGS_BUTTON_ARROW_RIGHT_DYNAMIC_CONTAINER;
    private static final int FEET_COLOR_LEFT = WidgetIndices.CharacterCreator.COLOUR_FEET_BUTTON_ARROW_LEFT_DYNAMIC_CONTAINER;
    private static final int FEET_COLOR_RIGHT = WidgetIndices.CharacterCreator.COLOUR_FEET_BUTTON_ARROW_RIGHT_DYNAMIC_CONTAINER;
    private static final int SKIN_COLOR_LEFT = WidgetIndices.CharacterCreator.COLOUR_SKIN_BUTTON_ARROW_LEFT_DYNAMIC_CONTAINER;
    private static final int SKIN_COLOR_RIGHT = WidgetIndices.CharacterCreator.COLOUR_SKIN_BUTTON_ARROW_RIGHT_DYNAMIC_CONTAINER;

    public boolean isCharacterCreationOpen() {
        return Rs2Widget.isWidgetVisible(CHARACTER_CREATOR_WIDGET_ID, 0);
    }

    public boolean createFemaleCharacter() {
        log.info("Starting female character creation");

        if (!isCharacterCreationOpen()) {
            log.error("Character creation interface not open");
            return false;
        }

        try {
            if (!selectFemaleGender()) {
                if (!errorRecovery.handleError("selectFemaleGender", "Failed to select female gender")) {
                    return false;
                }
                return false;
            }
            errorRecovery.resetError("selectFemaleGender");

            if (!randomizeDesign()) {
                if (!errorRecovery.handleError("randomizeDesign", "Failed to randomize design")) {
                    return false;
                }
                return false;
            }
            errorRecovery.resetError("randomizeDesign");

            if (!randomizeColors()) {
                if (!errorRecovery.handleError("randomizeColors", "Failed to randomize colors")) {
                    return false;
                }
                return false;
            }
            errorRecovery.resetError("randomizeColors");

            if (!confirmCharacter()) {
                if (!errorRecovery.handleError("confirmCharacter", "Failed to confirm character")) {
                    return false;
                }
                return false;
            }
            errorRecovery.resetError("confirmCharacter");

            log.info("Character creation completed successfully");
            Microbot.log("Female character created!");
            return true;

        } catch (Exception e) {
            log.error("Exception during character creation", e);
            errorRecovery.handleError("createFemaleCharacter", e.getMessage());
            return false;
        }
    }

    private boolean selectFemaleGender() {
        log.debug("Selecting female gender");

        Widget femaleButton = Rs2Widget.getWidget(CHARACTER_CREATOR_WIDGET_ID, GENDER_FEMALE_BUTTON);
        if (femaleButton == null) {
            log.warn("Female gender button widget not found");
            return false;
        }

        Widget clickableButton = femaleButton.getChild(0);
        if (clickableButton != null) {
            Rs2Widget.clickWidget(clickableButton);
            sleep(Rs2Random.between(300, 600));
            return true;
        }

        log.warn("Female gender clickable button not found");
        return false;
    }

    private boolean randomizeDesign() {
        log.debug("Randomizing character design");

        randomizeBodyPart("Head", HEAD_ARROW_LEFT, HEAD_ARROW_RIGHT);
        sleep(Rs2Random.between(200, 400));

        randomizeBodyPart("Jaw", JAW_ARROW_LEFT, JAW_ARROW_RIGHT);
        sleep(Rs2Random.between(200, 400));

        randomizeBodyPart("Torso", TORSO_ARROW_LEFT, TORSO_ARROW_RIGHT);
        sleep(Rs2Random.between(200, 400));

        randomizeBodyPart("Arms", ARMS_ARROW_LEFT, ARMS_ARROW_RIGHT);
        sleep(Rs2Random.between(200, 400));

        randomizeBodyPart("Hands", HANDS_ARROW_LEFT, HANDS_ARROW_RIGHT);
        sleep(Rs2Random.between(200, 400));

        randomizeBodyPart("Legs", LEGS_ARROW_LEFT, LEGS_ARROW_RIGHT);
        sleep(Rs2Random.between(200, 400));

        randomizeBodyPart("Feet", FEET_ARROW_LEFT, FEET_ARROW_RIGHT);
        sleep(Rs2Random.between(200, 400));

        return true;
    }

    private boolean randomizeColors() {
        log.debug("Randomizing character colors");

        randomizeColor("Hair", HAIR_COLOR_LEFT, HAIR_COLOR_RIGHT);
        sleep(Rs2Random.between(200, 400));

        randomizeColor("Torso", TORSO_COLOR_LEFT, TORSO_COLOR_RIGHT);
        sleep(Rs2Random.between(200, 400));

        randomizeColor("Legs", LEGS_COLOR_LEFT, LEGS_COLOR_RIGHT);
        sleep(Rs2Random.between(200, 400));

        randomizeColor("Feet", FEET_COLOR_LEFT, FEET_COLOR_RIGHT);
        sleep(Rs2Random.between(200, 400));

        randomizeColor("Skin", SKIN_COLOR_LEFT, SKIN_COLOR_RIGHT);
        sleep(Rs2Random.between(200, 400));

        return true;
    }

    private void randomizeBodyPart(String partName, int leftArrowId, int rightArrowId) {
        int clicks = Rs2Random.between(1, 3);
        boolean clickLeft = Rs2Random.random() < 0.5;

        for (int i = 0; i < clicks; i++) {
            int arrowId = clickLeft ? leftArrowId : rightArrowId;
            Widget arrow = Rs2Widget.getWidget(CHARACTER_CREATOR_WIDGET_ID, arrowId);

            if (arrow != null) {
                Widget clickableArrow = arrow.getChild(9);
                if (clickableArrow != null) {
                    Rs2Widget.clickWidget(clickableArrow);
                    sleep(Rs2Random.between(100, 250));
                }
            }

            if (Rs2Random.random() < 0.3) {
                clickLeft = !clickLeft;
            }
        }

        log.debug("Randomized {} with {} clicks", partName, clicks);
    }

    private void randomizeColor(String colorName, int leftArrowId, int rightArrowId) {
        int clicks = Rs2Random.between(1, 4);
        boolean clickLeft = Rs2Random.random() < 0.5;

        for (int i = 0; i < clicks; i++) {
            int arrowId = clickLeft ? leftArrowId : rightArrowId;
            Widget arrow = Rs2Widget.getWidget(CHARACTER_CREATOR_WIDGET_ID, arrowId);

            if (arrow != null) {
                Widget clickableArrow = arrow.getChild(9);
                if (clickableArrow != null) {
                    Rs2Widget.clickWidget(clickableArrow);
                    sleep(Rs2Random.between(100, 250));
                }
            }

            if (Rs2Random.random() < 0.3) {
                clickLeft = !clickLeft;
            }
        }

        log.debug("Randomized {} color with {} clicks", colorName, clicks);
    }

    private boolean confirmCharacter() {
        log.debug("Confirming character creation");

        Widget confirmButton = Rs2Widget.getWidget(CHARACTER_CREATOR_WIDGET_ID, CONFIRM_BUTTON);
        if (confirmButton == null) {
            log.warn("Confirm button widget not found");
            return false;
        }

        Widget clickableButton = confirmButton.getChild(0);
        if (clickableButton != null) {
            Rs2Widget.clickWidget(clickableButton);
            
            boolean closed = sleepUntil(() -> !isCharacterCreationOpen(), 5000);
            
            if (closed) {
                log.info("Character creation interface closed successfully");
                return true;
            } else {
                log.warn("Character creation interface did not close");
                return false;
            }
        }

        log.warn("Confirm button clickable element not found");
        return false;
    }
}
