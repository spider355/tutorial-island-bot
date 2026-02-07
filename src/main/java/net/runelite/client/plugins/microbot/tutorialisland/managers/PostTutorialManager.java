package net.runelite.client.plugins.microbot.tutorialisland.managers;

import lombok.extern.slf4j.Slf4j;
import net.runelite.api.coords.WorldPoint;
import net.runelite.client.plugins.microbot.Microbot;
import net.runelite.client.plugins.microbot.tutorialisland.TutorialIslandConfig;
import net.runelite.client.plugins.microbot.util.gameobject.Rs2GameObject;
import net.runelite.client.plugins.microbot.util.player.Rs2Player;
import net.runelite.client.plugins.microbot.util.walker.Rs2Walker;

import javax.inject.Inject;
import javax.inject.Singleton;

import static net.runelite.client.plugins.microbot.util.Global.sleep;
import static net.runelite.client.plugins.microbot.util.Global.sleepUntil;

@Slf4j
@Singleton
public class PostTutorialManager {

    @Inject
    private ErrorRecoveryManager errorRecovery;

    private static final WorldPoint LUMBRIDGE_CASTLE_COURTYARD = new WorldPoint(3222, 3218, 0);
    private static final WorldPoint WEST_OF_LUMBRIDGE = new WorldPoint(3200, 3220, 0);
    private static final WorldPoint DRAYNOR_VILLAGE = new WorldPoint(3093, 3244, 0);
    private static final WorldPoint FALADOR_GATE = new WorldPoint(2966, 3346, 0);
    private static final WorldPoint FALADOR_WEST_BANK = new WorldPoint(2945, 3368, 0);

    private static final int ARRIVAL_DISTANCE = 10;
    private static final int BANK_ARRIVAL_DISTANCE = 5;

    private static final int FALADOR_GATE_CLOSED_ID = 24063;
    private static final int BANK_DOOR_ID = 24101;

    private boolean hasReachedBank = false;
    private WorldPoint currentWaypoint = null;
    private int waypointIndex = 0;

    private WorldPoint lastPosition = null;
    private long lastPositionChangeTime = System.currentTimeMillis();
    private static final long STUCK_THRESHOLD_MS = 15000;

    public boolean walkToFaladorBank(TutorialIslandConfig config) {
        if (hasReachedBank) {
            log.debug("Already at Falador bank");
            return true;
        }

        try {
            if (isAtFaladorBank()) {
                log.info("Arrived at Falador West Bank!");
                hasReachedBank = true;
                Microbot.log("Reached Falador bank successfully!");
                return true;
            }

            currentWaypoint = getCurrentWaypoint();
            
            if (currentWaypoint == null) {
                log.error("No waypoint determined");
                return false;
            }

            log.debug("Current waypoint: {} (index {})", currentWaypoint,

