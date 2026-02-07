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

            log.debug("Current waypoint: {} (index {})", currentWaypoint, waypointIndex);

            if (hasReachedWaypoint(currentWaypoint)) {
                log.info("Reached waypoint: {}", currentWaypoint);
                waypointIndex++;
                errorRecovery.resetError("walkToWaypoint_" + waypointIndex);
                return true;
            }

            return walkToWaypoint(currentWaypoint, config);

        } catch (Exception e) {
            log.error("Error in post-tutorial navigation", e);
            return errorRecovery.handleError("walkToFaladorBank", e.getMessage());
        }
    }

    private WorldPoint getCurrentWaypoint() {
        WorldPoint playerPos = Rs2Player.getWorldLocation();

        if (playerPos == null) {
            log.warn("Player position is null");
            return null;
        }

        WorldPoint[] waypoints = {
            LUMBRIDGE_CASTLE_COURTYARD,
            WEST_OF_LUMBRIDGE,
            DRAYNOR_VILLAGE,
            FALADOR_GATE,
            FALADOR_WEST_BANK
        };

        if (waypointIndex < waypoints.length) {
            return waypoints[waypointIndex];
        }

        return FALADOR_WEST_BANK;
    }

    private boolean walkToWaypoint(WorldPoint waypoint, TutorialIslandConfig config) {
        log.debug("Walking to waypoint: {}", waypoint);

        boolean walkStarted = Rs2Walker.walkTo(waypoint);

        if (!walkStarted) {
            String errorKey = "walkToWaypoint_" + waypointIndex;
            return errorRecovery.handleError(errorKey, 
                "Failed to start walking to waypoint: " + waypoint);
        }

        sleep(config.actionDelay());

        handleObstacles(waypoint, config);

        errorRecovery.resetError("walkToWaypoint_" + waypointIndex);
        return true;
    }

    private void handleObstacles(WorldPoint destination, TutorialIslandConfig config) {
        WorldPoint playerPos = Rs2Player.getWorldLocation();

        if (playerPos == null) return;

        if (destination.equals(FALADOR_GATE) || destination.equals(FALADOR_WEST_BANK)) {
            if (playerPos.distanceTo(FALADOR_GATE) < 5) {
                if (Rs2GameObject.interact(FALADOR_GATE_CLOSED_ID, "Open")) {
                    log.debug("Opening Falador gate");
                    sleep(config.actionDelay());
                    sleep(1000);
                }
            }
        }

        if (destination.equals(FALADOR_WEST_BANK)) {
            if (playerPos.distanceTo(FALADOR_WEST_BANK) < 3) {
                if (Rs2GameObject.interact(BANK_DOOR_ID, "Open")) {
                    log.debug("Opening bank door");
                    sleep(config.actionDelay());
                    sleep(1000);
                }
            }
        }
    }

    private boolean hasReachedWaypoint(WorldPoint waypoint) {
        WorldPoint playerPos = Rs2Player.getWorldLocation();

        if (playerPos == null || waypoint == null) {
            return false;
        }

        int distance = playerPos.distanceTo(waypoint);
        
        int threshold = waypoint.equals(FALADOR_WEST_BANK) ? 
            BANK_ARRIVAL_DISTANCE : ARRIVAL_DISTANCE;

        return distance <= threshold;
    }

    private boolean isAtFaladorBank() {
        WorldPoint playerPos = Rs2Player.getWorldLocation();

        if (playerPos == null) {
            return false;
        }

        return playerPos.distanceTo(FALADOR_WEST_BANK) <= BANK_ARRIVAL_DISTANCE;
    }

    public void reset() {
        hasReachedBank = false;
        currentWaypoint = null;
        waypointIndex = 0;
        errorRecovery.resetAll();
        log.info("Post-tutorial manager reset");
    }

    public int getProgressPercentage() {
        WorldPoint[] waypoints = {
            LUMBRIDGE_CASTLE_COURTYARD,
            WEST_OF_LUMBRIDGE,
            DRAYNOR_VILLAGE,
            FALADOR_GATE,
            FALADOR_WEST_BANK
        };

        if (hasReachedBank) {
            return 100;
        }

        int totalWaypoints = waypoints.length;
        int completedWaypoints = waypointIndex;

        return (int) ((completedWaypoints / (double) totalWaypoints) * 100);
    }

    public String getStatusMessage() {
        if (hasReachedBank) {
            return "Arrived at Falador Bank";
        }

        if (currentWaypoint == null) {
            return "Calculating route...";
        }

        if (currentWaypoint.equals(LUMBRIDGE_CASTLE_COURTYARD)) {
            return "Leaving Lumbridge...";
        } else if (currentWaypoint.equals(WEST_OF_LUMBRIDGE)) {
            return "Heading west...";
        } else if (currentWaypoint.equals(DRAYNOR_VILLAGE)) {
            return "Passing Draynor Village...";
        } else if (currentWaypoint.equals(FALADOR_GATE)) {
            return "Approaching Falador...";
        } else if (currentWaypoint.equals(FALADOR_WEST_BANK)) {
            return "Entering Falador Bank...";
        }

        return "Navigating to Falador...";
    }

    public int getEstimatedTimeRemaining() {
        if (hasReachedBank) {
            return 0;
        }

        WorldPoint playerPos = Rs2Player.getWorldLocation();
        
        if (playerPos == null || currentWaypoint == null) {
            return -1;
        }

        int distanceRemaining = playerPos.distanceTo(FALADOR_WEST_BANK);
        
        return distanceRemaining + 30;
    }

    public boolean isPlayerStuck() {
        WorldPoint currentPos = Rs2Player.getWorldLocation();

        if (currentPos == null) {
            return false;
        }

        if (lastPosition == null || !currentPos.equals(lastPosition)) {
            lastPosition = currentPos;
            lastPositionChangeTime = System.currentTimeMillis();
            return false;
        }

        long timeSinceMove = System.currentTimeMillis() - lastPositionChangeTime;
        
        if (timeSinceMove > STUCK_THRESHOLD_MS) {
            log.warn("Player appears to be stuck at {}", currentPos);
            return true;
        }

        return false;
    }

    public boolean attemptUnstuck(TutorialIslandConfig config) {
        log.info("Attempting to unstuck player");
        Microbot.log("Player stuck - trying to recover...");

        WorldPoint playerPos = Rs2Player.getWorldLocation();
        
        if (playerPos == null) {
            return false;
        }

        WorldPoint unstuckPoint = new WorldPoint(
            playerPos.getX() + 3,
            playerPos.getY() + 3,
            playerPos.getPlane()
        );

        Rs2Walker.walkTo(unstuckPoint);
        sleep(2000);

        lastPosition = null;
        lastPositionChangeTime = System.currentTimeMillis();

        return true;
    }
}
