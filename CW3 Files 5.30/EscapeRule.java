import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EscapeRule extends Rule {
    private List<Site> loop;

    private boolean inLoop = false;

    public EscapeRule(Game game) {
        super(game);
    }

    public Site move() {
        Site monster = game.getMonsterSite();
        Site rogue = game.getRogueSite();
        Site move;
        // If there is a corridor entrance with a loop
        if (!dungeon.getLoopEntrances().isEmpty()) {

            // Move on the loop if rogue is on the corridor and rogue is at the entrance of the loop corridor
            if (!inLoop && dungeon.getLoopEntrances().contains(rogue)) {
                setLoop(dungeon.getLoopPath().get(rogue));
                inLoop = true;
                return (moveInLoop(rogue, monster));
            }


            // There is a loop, but rogue is outside the loop
            if (!inLoop) {
                move = goToCircle(rogue, monster);
                if (move == null) {
                    if (!dungeon.getSoundedWallLoop().isEmpty()) {
                        System.out.println("Go to circle failed! Start wall loop strategy!");
                        move = wallLoopStrategy(rogue, monster);
                        if (move == null) {
                            System.out.println("Wall loop strategy failed! Start no loop Strategy!");
                            move = noLoopStrategy(rogue, monster);
                        }
                    } else {
                        System.out.println("Wall loop strategy failed! Start no loop Strategy!");
                        move = noLoopStrategy(rogue, monster);
                    }
                }
            } else {
                move = moveInLoop(rogue, monster);
            }
        }

        // There is no corridor loop, but there is a wall loop
        else if (!dungeon.getSoundedWallLoop().isEmpty() && dungeon.getLoopEntrances().isEmpty()) {
            move = wallLoopStrategy(rogue, monster);
            if (move == null) {
                System.out.println("no corridor circle! no wall loop! Start no loop strategy!");
                move = noLoopStrategy(rogue, monster);
            }
        } else {
            move = noLoopStrategy(rogue, monster);
        }
        return move;
    }

    private void setLoop(List<Site> loop) {
        this.loop = loop;
    }

    private Site goToCircle(Site rogue, Site monster) {
        Site move = null;
        List<Site> enter = dungeon.getLoopEntrances();
        Site bestCorridorEntrance = bestCorridorEntrance(rogue, monster, enter);

        if (bestCorridorEntrance != null) {
            // Moving to the next step at the optimal entrance
            move = dungeon.shortestPathBFS(rogue, bestCorridorEntrance).get(1);
        }
        return move;
    }

    private Site moveInLoop(Site rogue, Site monster) {
        List<Site> choose = new ArrayList<>();

        List<Site> paths = loop;

        for (int i = 0; i < paths.size(); i++) {

            if (paths.get(i).equals(rogue)) {
                if (i == 0) {
                    choose.add(paths.get(i));
                    choose.add(paths.get(paths.size() - 1));
                    choose.add(paths.get(i + 1));
                    return bestNextStepInLoop(rogue, monster, choose);
                }
                if (i == paths.size() - 1) {
                    choose.add(paths.get(i));
                    choose.add(paths.get(i - 1));
                    choose.add(paths.get(0));
                    return bestNextStepInLoop(rogue, monster, choose);
                }

                choose.add(paths.get(i));
                choose.add(paths.get(i - 1));
                choose.add(paths.get(i + 1));
                return bestNextStepInLoop(rogue, monster, choose);
            }
        }

        return null;
    }

    private Site wallLoopStrategy(Site rogue, Site monster) {
        List<Site> closerWallLoopNodeList = new ArrayList<>();
        List<List<Site>> wallLoopList = dungeon.getSoundedWallLoop();
        //Site move = null;

        // Check if the rogue is in the wall loop
        for (List<Site> list : wallLoopList) {
            if (list.contains(rogue)) {
                System.out.println("rogue in wall loop! Begin loop!");
                return bestNextStepWallLoop(rogue, monster);
            }
        }

        // Traverse all wall loops and find all nodes from rogue to wall loop that are closer than Monster to wall loop
        for (List<Site> wallLoop : wallLoopList) {

            for (Site wallLoopNode : wallLoop) {
                int rogueDistance = dungeon.manhattanDistanceShortestPath(rogue, wallLoopNode);
                int monsterDistance = dungeon.manhattanDistanceShortestPath(monster, wallLoopNode);

                if (rogueDistance < monsterDistance) {
                    closerWallLoopNodeList.add(wallLoopNode);
                }
            }

            if (!closerWallLoopNodeList.isEmpty()) {
                Site bestWallLoopEntrance = null;
                int minDistance = Integer.MAX_VALUE;

                // Find the wall loop node closest to the rogue
                for (Site site : closerWallLoopNodeList) {
                    int rogueDistance = dungeon.manhattanDistanceShortestPath(rogue, site);
                    if (rogueDistance < minDistance) {
                        minDistance = rogueDistance;
                        bestWallLoopEntrance = site;
                    }
                }

                List<Site> neighbors = dungeon.getNeighbor(rogue);
                Site bestNeighbor = null;
                minDistance = Integer.MAX_VALUE;

                // Find the neighbor with the shortest path from rogue to bestWallLoopEntry
                for (Site neighbor : neighbors) {
                    int neighborDistance = dungeon.manhattanDistanceShortestPath(neighbor, bestWallLoopEntrance);
                    if (neighborDistance < minDistance) {
                        minDistance = neighborDistance;
                        bestNeighbor = neighbor;
                    }
                }

                return bestNeighbor;
            }
        }

        return null;
    }

    private Site noLoopStrategy(Site rogue, Site monster) {
        return findMaxAverageStepDirection(rogue, monster);
    }


    /**********************************help methods********************************/

    private Site bestCorridorEntrance(Site rogue, Site monster, List<Site> corridorEntrances) {
        Site bestEntrance = null;
        List<Site> closerEntrances = new ArrayList<>();
        Map<Site, List<Site>> entranceToCorridorPath = new HashMap<>();

        // Record the corridor entrance closer to rogue than Monster
        for (Site entranceSite : corridorEntrances) {
            int rogueDistance = dungeon.shortestPathBFS(rogue, entranceSite).size();
            int monsterDistance = dungeon.shortestPathBFS(monster, entranceSite).size();

            if (rogueDistance < monsterDistance) {
                closerEntrances.add(entranceSite);

                entranceToCorridorPath.put(entranceSite, dungeon.getCorridorContainingSite(entranceSite));
            }
        }

        // Choose the corridor with the longest step length
        int maxLength = Integer.MIN_VALUE;

        for (Site entranceSite : closerEntrances) {
            List<Site> corridorPath = entranceToCorridorPath.get(entranceSite);
            int Length = corridorPath.size();

            if (Length > maxLength) {
                maxLength = Length;
                bestEntrance = entranceSite;
            }
        }
        return bestEntrance;
    }

    private Site bestNextStep(Site rogue, Site monster) {
        // Initialize to the shortest path length between rogue and monster
        int minDistanceToMonster = dungeon.manhattanDistanceShortestPath(rogue, monster);
        Site bestMove = rogue;

        // Find at least one neighbor that can move
        List<Site> neighbors = dungeon.getNeighbor(rogue);

        // Traverse all neighbors
        for (Site neighborSite : neighbors) {
            // Calculate the shortest path length from the current neighbor to Monster
            int distanceToMonster = dungeon.manhattanDistanceShortestPath(neighborSite, monster);

            if (distanceToMonster > minDistanceToMonster) {
                // Farthest Neighbor
                minDistanceToMonster = distanceToMonster;
                bestMove = neighborSite;
            }

        }
        return bestMove;
    }

    // Find farthest direction neighbors
    private Site findMaxAverageStepDirection(Site rogue, Site monster) {
        Site maxSiteInStepMatrix = null;
        for (Site maxStepSite : dungeon.getMaxStepSites()) {
            maxSiteInStepMatrix = maxStepSite;
        }

        // start moving to farthest direction neighbors
        if (rogue.equals(maxSiteInStepMatrix)) return bestNextStep(rogue, monster);
        return dungeon.shortestPathBFS(rogue, maxSiteInStepMatrix).get(1);
    }


    private Site bestNextStepInLoop(Site rogue, Site monster, List<Site> neighbors) {
        Site res = neighbors.get(0);
        int maxDistance = dungeon.shortestPathBFS(res, monster).size();

        for (Site n : neighbors) {
            int distance = dungeon.shortestPathBFS(n, monster).size();
            if (distance > maxDistance) {
                maxDistance = distance;

                res = n;
            }
        }

        return res;
    }

    private Site bestNextStepWallLoop(Site rogue, Site monster) {

        List<List<Site>> wallLoops = dungeon.getSoundedWallLoop();
        List<Site> wallLoopPath = null;

        List<Site> neighbors = dungeon.getNeighbor(rogue);

        for (List<Site> wallLoop : wallLoops) {
            if (wallLoop.contains(rogue)) {
                wallLoopPath = wallLoop;
            }
        }

        Site bestNeighbor = null;
        int maxDistance = dungeon.manhattanDistanceShortestPath(rogue, monster);

        for (Site neighbor : neighbors) {
            int neighborDistance = neighbor.manhattanTo(monster);
            assert wallLoopPath != null;
            if (wallLoopPath.contains(neighbor) && neighborDistance > maxDistance) {
                bestNeighbor = neighbor;
                maxDistance = neighborDistance;

            }

        }

        if (bestNeighbor == null) return noLoopStrategy(rogue, monster);
        return bestNeighbor;
    }

}
