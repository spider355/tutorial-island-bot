package net.runelite.client.plugins.microbot.tutorialisland.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum TutorialStage {
    
    NOT_STARTED("Not Started", 0, 2),
    CHARACTER_CREATION("Character Creation", 0, 2),
    GIELINOR_GUIDE("Gielinor Guide", 3, 40),
    SURVIVAL_EXPERT("Survival Expert", 50, 120),
    MASTER_CHEF("Master Chef", 130, 200),
    QUEST_GUIDE("Quest Guide", 210, 280),
    MINING_INSTRUCTOR("Mining Instructor", 300, 390),
    COMBAT_INSTRUCTOR("Combat Instructor", 400, 520),
    FINANCIAL_ADVISOR("Financial Advisor", 525, 560),
    BROTHER_BRACE("Brother Brace", 570, 620),
    MAGIC_INSTRUCTOR("Magic Instructor", 630, 670),
    FINAL_INSTRUCTOR("Final Instructor", 680, 999),
    COMPLETED("Completed", 1000, Integer.MAX_VALUE);

    private final String displayName;
    private final int minVarbit;
    private final int maxVarbit;

    public static TutorialStage fromVarbit(int varbitValue) {
        for (TutorialStage stage : values()) {
            if (varbitValue >= stage.minVarbit && varbitValue <= stage.maxVarbit) {
                return stage;
            }
        }
        return NOT_STARTED;
    }

    public boolean isBefore(TutorialStage other) {
        return this.ordinal() < other.ordinal();
    }

    public boolean isAfter(TutorialStage other) {
        return this.ordinal() > other.ordinal();
    }

    public TutorialStage getNext() {
        int nextOrdinal = this.ordinal() + 1;
        TutorialStage[] stages = values();
        return nextOrdinal < stages.length ? stages[nextOrdinal] : COMPLETED;
    }
}
