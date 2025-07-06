import java.util.List;
import java.util.Random;

public class ChaseRule extends Rule {

    public ChaseRule(Game game) {
        super(game);
    }

    public Site move() {
        Site monster = game.getMonsterSite();
        Site rogue = game.getRogueSite();
        Site move;

        List<Site> path = dungeon.shortestPathBFS(monster, rogue);

        if (!path.isEmpty()) {
            // Ensure that the path has at least two sites (starting point and next point)
            if (path.size() > 1) {
                move = path.get(1);
            } else {
                // If there is only one site in the path, it indicates that the monster is already in the rogue's position
                move = path.get(0);
            }
        } else {
            // Dealing with situations where there is no path connection between monster and rogue
            move = handleNoPath(monster);
        }
        return move;
    }

    // Dealing with situations where there is no path connection between monster and rogue
    private Site handleNoPath(Site monster) {
        // randomly moving or staying in place when there is no path connection
        List<Site> neighbors = dungeon.getNeighbor(monster);

        // randomly moving
        if (!neighbors.isEmpty()) {
            Random rand = new Random();
            int index = rand.nextInt(neighbors.size());
            return neighbors.get(index);
        }

        // staying in place
        return monster;
    }

}
