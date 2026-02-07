package net.runelite.client.plugins.microbot.tutorialisland;

import com.google.inject.Provides;
import lombok.extern.slf4j.Slf4j;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.overlay.OverlayManager;

import javax.inject.Inject;
import java.awt.*;

@PluginDescriptor(
        name = "Tutorial Island Bot",
        description = "Completes Tutorial Island automatically with character creation and walks to Falador bank",
        tags = {"tutorial", "island", "automation", "starter", "microbot", "ironman"},
        enabledByDefault = false
)
@Slf4j
public class TutorialIslandPlugin extends Plugin {

    @Inject
    private TutorialIslandConfig config;

    @Inject
    private OverlayManager overlayManager;

    @Inject
    private TutorialIslandScript tutorialIslandScript;

    @Inject
    private TutorialIslandOverlay tutorialIslandOverlay;

    @Provides
    TutorialIslandConfig provideConfig(ConfigManager configManager) {
        return configManager.getConfig(TutorialIslandConfig.class);
    }

    @Override
    protected void startUp() throws AWTException {
        log.info("Tutorial Island Plugin started");
        
        if (overlayManager != null && tutorialIslandOverlay != null) {
            overlayManager.add(tutorialIslandOverlay);
            tutorialIslandOverlay.resetStartTime();
        }
        
        if (tutorialIslandScript != null) {
            tutorialIslandScript.run(config);
        }
    }

    @Override
    protected void shutDown() {
        log.info("Tutorial Island Plugin stopped");
        
        if (tutorialIslandScript != null) {
            tutorialIslandScript.shutdown();
        }
        
        if (overlayManager != null && tutorialIslandOverlay != null) {
            overlayManager.remove(tutorialIslandOverlay);
        }
    }
}
