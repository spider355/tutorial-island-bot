package net.runelite.client.plugins.microbot.tutorialisland;

import lombok.extern.slf4j.Slf4j;
import net.runelite.client.plugins.microbot.Microbot;
import net.runelite.client.plugins.microbot.tutorialisland.enums.TutorialStage;
import net.runelite.client.plugins.microbot.tutorialisland.managers.PostTutorialManager;
import net.runelite.client.ui.overlay.OverlayPanel;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.components.LineComponent;
import net.runelite.client.ui.overlay.components.TitleComponent;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.awt.*;
import java.time.Duration;
import java.time.Instant;

@Slf4j
@Singleton
public class TutorialIslandOverlay extends OverlayPanel {

    private final TutorialIslandScript script;
    private final TutorialIslandConfig config;
    private final PostTutorialManager postTutorialManager;
    private Instant startTime;

    private static final int TOTAL_STAGES = 12;

    @Inject
    public TutorialIslandOverlay(
            TutorialIslandScript script, 
            TutorialIslandConfig config,
            PostTutorialManager postTutorialManager) {
        this.script = script;
        this.config = config;
        this.postTutorialManager = postTutorialManager;
        setPosition(OverlayPosition.TOP_LEFT);
        startTime = Instant.now();
    }

    @Override
    public Dimension render(Graphics2D graphics) {
        if (script == null) {
            return null;
        }

        panelComponent.getChildren().clear();

        panelComponent.getChildren().add(TitleComponent.builder()
                .text("Tutorial Island Bot")
                .color(new Color(255, 165, 0))
                .build());

        TutorialStage currentStage = script.getCurrentStage();

        if (currentStage == TutorialStage.COMPLETED && config.walkToFalador()) {
            renderPostTutorialInfo();
        } else {
            renderTutorialInfo(currentStage);
        }

        String timeRunning = getTimeRunning();
        panelComponent.getChildren().add(LineComponent.builder()
                .left("Time:")
                .right(timeRunning)
                .rightColor(Color.CYAN)
                .build());

        if (config.enableDebugLogging()) {
            int varbitValue = Microbot.getVarbitValue(281);
            panelComponent.getChildren().add(LineComponent.builder()
                    .left("Varbit:")
                    .right(String.valueOf(varbitValue))
                    .rightColor(Color.LIGHT_GRAY)
                    .build());
        }

        return super.render(graphics);
    }

    private void renderTutorialInfo(TutorialStage currentStage) {
        String stageProgress = getStageProgress(currentStage);
        Color stageColor = getStageColor(currentStage);

        panelComponent.getChildren().add(LineComponent.builder()
                .left("Stage:")
                .right(stageProgress)
                .rightColor(stageColor)
                .build());

        panelComponent.getChildren().add(LineComponent.builder()
                .left("Task:")
                .right(currentStage.getDisplayName())
                .rightColor(Color.WHITE)
                .build());

        if (currentStage.ordinal() >= TutorialStage.FINAL_INSTRUCTOR.ordinal()) {
            String ironmanStatus = config.enableIronmanMode() ? 
                config.ironmanType().getDisplayName() : "Regular";
            Color ironmanColor = config.enableIronmanMode() ? 
                new Color(139, 69, 19) : Color.GRAY;
            
            panelComponent.getChildren().add(LineComponent.builder()
                    .left("Account:")
                    .right(ironmanStatus)
                    .rightColor(ironmanColor)
                    .build());
        }
    }

    private void renderPostTutorialInfo() {
        panelComponent.getChildren().add(LineComponent.builder()
                .left("Stage:")
                .right("Post-Tutorial")
                .rightColor(Color.GREEN)
                .build());

        String status = postTutorialManager.getStatusMessage();
        panelComponent.getChildren().add(LineComponent.builder()
                .left("Status:")
                .right(status)
                .rightColor(Color.YELLOW)
                .build());

        int progress = postTutorialManager.getProgressPercentage();
        panelComponent.getChildren().add(LineComponent.builder()
                .left("Progress:")
                .right(progress + "%")
                .rightColor(getProgressColor(progress))
                .build());

        int etaSeconds = postTutorialManager.getEstimatedTimeRemaining();
        if (etaSeconds >= 0) {
            String eta = formatSeconds(etaSeconds);
            panelComponent.getChildren().add(LineComponent.builder()
                    .left("ETA:")
                    .right(eta)
                    .rightColor(Color.CYAN)
                    .build());
        }
    }

    private String getStageProgress(TutorialStage stage) {
        int currentStageNumber = getStageNumber(stage);
        return currentStageNumber + "/" + TOTAL_STAGES;
    }

    private int getStageNumber(TutorialStage stage) {
        switch (stage) {
            case NOT_STARTED:
            case CHARACTER_CREATION:
                return 0;
            case GIELINOR_GUIDE:
                return 1;
            case SURVIVAL_EXPERT:
                return 2;
            case MASTER_CHEF:
                return 3;
            case QUEST_GUIDE:
                return 4;
            case MINING_INSTRUCTOR:
                return 5;
            case COMBAT_INSTRUCTOR:
                return 6;
            case FINANCIAL_ADVISOR:
                return 7;
            case BROTHER_BRACE:
                return 8;
            case MAGIC_INSTRUCTOR:
                return 9;
            case FINAL_INSTRUCTOR:
                return 10;
            case COMPLETED:
                return 12;
            default:
                return 0;
        }
    }

    private Color getStageColor(TutorialStage stage) {
        switch (stage) {
            case NOT_STARTED:
            case CHARACTER_CREATION:
                return Color.YELLOW;
            case COMPLETED:
                return Color.GREEN;
            default:
                return new Color(0, 255, 128);
        }
    }

    private Color getProgressColor(int progress) {
        if (progress >= 75) {
            return Color.GREEN;
        } else if (progress >= 50) {
            return Color.YELLOW;
        } else if (progress >= 25) {
            return Color.ORANGE;
        } else {
            return Color.RED;
        }
    }

    private String getTimeRunning() {
        Duration duration = Duration.between(startTime, Instant.now());
        long hours = duration.toHours();
        long minutes = duration.toMinutes() % 60;
        long seconds = duration.getSeconds() % 60;

        return String.format("%02d:%02d:%02d", hours, minutes, seconds);
    }

    private String formatSeconds(int totalSeconds) {
        int minutes = totalSeconds / 60;
        int seconds = totalSeconds % 60;
        return String.format("%d:%02d", minutes, seconds);
    }

    public void resetStartTime() {
        startTime = Instant.now();
        log.debug("Overlay timer reset");
    }

    public long getSecondsRunning() {
        return Duration.between(startTime, Instant.now()).getSeconds();
    }
}
