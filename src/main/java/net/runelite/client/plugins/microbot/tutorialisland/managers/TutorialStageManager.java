package net.runelite.client.plugins.microbot.tutorialisland.managers;

import lombok.extern.slf4j.Slf4j;
import net.runelite.client.plugins.microbot.Microbot;
import net.runelite.client.plugins.microbot.tutorialisland.TutorialIslandConfig;
import net.runelite.client.plugins.microbot.tutorialisland.enums.TutorialStage;
import net.runelite.client.plugins.microbot.tutorialisland.stages.*;
import net.runelite.client.plugins.microbot.util.player.Rs2Player;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Singleton
public class TutorialStageManager {

    private static final int TUTORIAL_ISLAND_VARBIT = 281;

    @Inject
    private ErrorRecoveryManager errorRecovery;

    @Inject
    private GielinorGuideStage gielinorGuideStage;

    @Inject
    private SurvivalExpertStage survivalExpertStage;

    @Inject
    private MasterChefStage masterChefStage;

    @Inject
    private QuestGuideStage questGuideStage;

    @Inject
    private MiningInstructorStage miningInstructorStage;

    @Inject
    private CombatInstructorStage combatInstructorStage;

    @Inject
    private FinancialAdvisorStage financialAdvisorStage;

    @Inject
    private BrotherBraceStage brotherBraceStage;

    @Inject
    private MagicInstructorStage magicInstructorStage;

    @Inject
    private FinalInstructorStage finalInstructorStage;

    private TutorialStage lastDetectedStage = TutorialStage.NOT_STARTED;
    private final Map<TutorialStage, IStageHandler> stageHandlers = new HashMap<>();

    @Inject
    private void initialize() {
        stageHandlers.put(TutorialStage.GIELINOR_GUIDE, gielinorGuideStage);
        stageHandlers.put(TutorialStage.SURVIVAL_EXPERT, survivalExpertStage);
        stageHandlers.put(TutorialStage.MASTER_CHEF, masterChefStage);
        stageHandlers.put(TutorialStage.QUEST_GUIDE, questGuideStage);
        stageHandlers.put(TutorialStage.MINING_INSTRUCTOR, miningInstructorStage);
        stageHandlers.put(TutorialStage.COMBAT_INSTRUCTOR, combatInstructorStage);
        stageHandlers.put(TutorialStage.FINANCIAL_ADVISOR, financialAdvisorStage);
        stageHandlers.put(TutorialStage.BROTHER_BRACE, brotherBraceStage);
        stageHandlers.put(TutorialStage.MAGIC_INSTRUCTOR, magicInstructorStage);
        stageHandlers.put(TutorialStage.FINAL_INSTRUCTOR, finalInstructorStage);
    }

    public TutorialStage detectCurrentStage() {
        if (!Microbot.isLoggedIn()) {
            return TutorialStage.CHARACTER_CREATION;
        }

        if (Rs2Player.hasCompletedTutorialIsland()) {
            return TutorialStage.COMPLETED;
        }

        int varbitValue = Microbot.getVarbitValue(TUTORIAL_ISLAND_VARBIT);
        TutorialStage detectedStage = TutorialStage.fromVarbit(varbitValue);

        if (detectedStage != lastDetectedStage) {
            log.info("Tutorial stage changed: {} -> {} (varbit: {})", 
                lastDetectedStage, detectedStage, varbitValue);
            Microbot.log("Tutorial: " + detectedStage.getDisplayName());
            lastDetectedStage = detectedStage;
            
            errorRecovery.resetAll();
        }

        return detectedStage;
    }

    public boolean handle

