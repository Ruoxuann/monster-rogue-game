import java.util.*;

public class Dungeon {
    private boolean[][] isRoom;        // flag to determine if a site is a room
    private boolean[][] isCorridor;    // flag to determine if a site is a corridor

    private boolean[][] isWall;        // flag to determine if a site is a wall
    private int N;                     // Dungeon's dimension

    private int[][] rogueSteps;
    private int[][] monsterSteps;
    private int[][] averageSteps;

    private int[][] differenceSteps;


    private Site[][] sites;

    private List<Site> corridorEntrances; // the entrance index of the corridor
    private Map<Site, List<Site>> corridorPaths; // all corridor entrances, key is a site, value is a list of sites
    private List<Site> loopEntrances;
    private Map<Site, List<Site>> loopPath;
    private List<Site> loopInCorr; // the entrance of the loop in corridor
    private Map<Site, List<Site>> loopPathInCorr; // the loop path in corridor, key is a site, value is a list of sites

    private List<List<Site>> connectedWalls;
    private List<List<Site>> soundedWallLoop;


    public int size() {
        return N;
    }

    public List<Site> getLoopEntrances() {
        return loopEntrances;
    }

    public Map<Site, List<Site>> getLoopPath() {
        return loopPath;
    }

    // check which corridor a site is in
    public List<Site> getCorridorContainingSite(Site site) {
        for (List<Site> path : corridorPaths.values()) {
            if (path.contains(site)) {
                return path;
            }
        }
        return null; // if the site is not in any corridor
    }

    public List<List<Site>> getSoundedWallLoop() {
        return soundedWallLoop;
    }

    // get list of site which is closed to with rogue and has max steps
    public List<Site> getMaxStepSites() {
        List<Site> maxStepSites = new ArrayList<>();

        int maxStep = Integer.MIN_VALUE;
        for (int i = 0; i < averageSteps.length; i++) {
            for (int j = 0; j < averageSteps.length; j++) {
                int steps = averageSteps[i][j];
                if (steps > maxStep && differenceSteps[i][j] > 0) {
                    maxStep = steps;

                }
            }
        }
        for (int i = 0; i < averageSteps.length; i++) {
            for (int j = 0; j < averageSteps.length; j++) {
                if (averageSteps[i][j] == maxStep && differenceSteps[i][j] > 0) {
                    maxStepSites.add(new Site(i, j));

                }
            }
        }
        return maxStepSites;
    }

    /**********************************check methods********************************/

    private boolean isValid(int row, int col) {
        return row >= 0 && row < N && col >= 0 && col < N;
    }

    public boolean isCorridor(Site v) {
        int i = v.i();
        int j = v.j();
        if (i < 0 || j < 0 || i >= N || j >= N) return false;
        return isCorridor[i][j];
    }

    public boolean isRoom(Site v) {
        int i = v.i();
        int j = v.j();
        if (i < 0 || j < 0 || i >= N || j >= N) return false;
        return isRoom[i][j];
    }

    public boolean isWall(Site v) {
        return (!isRoom(v) && !isCorridor(v));
    }


    /******************************dungeon initialize***********************************/

    // Initializing Dungeon to record the index of Corridor Room and Corridor Entrances
    public Dungeon(char[][] board) {

        N = board.length;
        isRoom = new boolean[N][N];
        isCorridor = new boolean[N][N];
        isWall = new boolean[N][N];

        rogueSteps = new int[N][N];
        monsterSteps = new int[N][N];
        averageSteps = new int[N][N];
        differenceSteps = new int[N][N];


        // Initialize step size matrix
        for (int i = 0; i < N; i++) {
            for (int j = 0; j < N; j++) {
                this.rogueSteps[i][j] = Integer.MAX_VALUE;
                this.monsterSteps[i][j] = Integer.MAX_VALUE;
                this.averageSteps[i][j] = Integer.MIN_VALUE;
                this.differenceSteps[i][j] = Integer.MAX_VALUE;
            }
        }

        corridorEntrances = new ArrayList<>();
        corridorPaths = new HashMap<>();

        loopEntrances = new ArrayList<>();
        loopPath = new HashMap<>();

        connectedWalls = new ArrayList<>();
        soundedWallLoop = new ArrayList<>();

        loopPathInCorr = new HashMap<>();
        loopInCorr = new ArrayList<>();


        sites = new Site[N][N];

        for (int i = 0; i < N; i++) {
            for (int j = 0; j < N; j++) {
                if (board[i][j] == '.') {
                    sites[i][j] = new Site(i, j);
                    isRoom[i][j] = true;
                } else if (board[i][j] == '+') {
                    sites[i][j] = new Site(i, j);
                    isCorridor[i][j] = true;
                } else {
                    sites[i][j] = new Site(i, j);
                    isWall[i][j] = true;
                }
            }
        }

        // find the entrance to the corridor, and record it
        for (int i = 0; i < N; i++) {
            for (int j = 0; j < N; j++) {
                if (isCorridor[i][j] && hasAdjacentRoom(i, j)) {
                    corridorEntrances.add(sites[i][j]);
                }
                if (isWall[i][j]) {
                    findConnectedWallsFrom(i, j);
                }
            }
        }


        // calculate the corridor path and record it
        for (Site entranceSite : corridorEntrances) {

            List<Site> corridorPath = dfsCorridor(entranceSite);
            // At least two corridors can form a loop
            if (corridorPath.size() > 1) {
                List<Site> corridorNeighbor = getCorridorNeighbor(entranceSite);

                if (!corridorNeighbor.isEmpty()) {
                    for (Site s : corridorNeighbor) {
                        List<Site> path = findCircleBFS(entranceSite, s);
                        if (!path.isEmpty()) {
                            loopEntrances.add(entranceSite);
                            loopPath.put(entranceSite, path);
                            break;
                        }
                    }
                }
            }
            corridorPaths.put(entranceSite, corridorPath);
        }

        if (loopInCorr != null && !loopInCorr.isEmpty()) {
            for (Site corr : loopInCorr) {
                List<Site> loopCorr = findCircleBFS(corr, getNeighbor(corr).get(0));
                loopEntrances.add(corr);
                loopPath.put(corr, loopCorr);
                loopPathInCorr.put(corr, loopCorr);
            }

        }


//        System.out.println("corridor entrances site" + corridorEntrances);
//        System.out.println("corridorPaths:" + corridorPaths);
//        System.out.println("loop entrances:" + loopEntrances);
//        System.out.println("loop path:" + loopPath);
//        System.out.println("loop in corridor:" + loopPathInCorr);
//        System.out.println("connected walls:" + connectedWalls);
        checkConnectedWallsForLoops();
        //System.out.println("sounded wall loops:" + soundedWallLoop);
    }

    /************************Calculate Steps*******************************/
    public void initializeSteps(Site rogueSite, Site monsterSite) {
        bfsStep(rogueSite, rogueSteps);
        bfsStep(monsterSite, monsterSteps);
    }

    // steps for BFS
    private void bfsStep(Site start, int[][] steps) {
        Queue<Site> queue = new LinkedList<>();
        queue.add(start);
        steps[start.i()][start.j()] = 0;

        int[] dx = {1, -1, 0, 0};
        int[] dy = {0, 0, 1, -1};

        while (!queue.isEmpty()) {
            Site current = queue.poll();
            int curSteps = steps[current.i()][current.j()];

            for (int i = 0; i < 4; i++) {
                int nx = current.i() + dx[i];
                int ny = current.j() + dy[i];

                if (nx >= 0 && ny >= 0 && nx < N && ny < N && (isRoom[nx][ny] || isCorridor[nx][ny]) && steps[nx][ny] == Integer.MAX_VALUE) {
                    steps[nx][ny] = curSteps + 1;
                    queue.add(sites[nx][ny]);
                }
            }
        }
    }

    public void printAverageSteps(Site rogueSite, Site monsterSite) {
        initializeSteps(rogueSite, monsterSite);
        int[][] rogueSteps = this.rogueSteps;
        int[][] monsterSteps = this.monsterSteps;

        System.out.println("Step Sums Matrix:");
        for (int i = 0; i < N; i++) {
            for (int j = 0; j < N; j++) {
                int rogueStep = rogueSteps[i][j];
                int monsterStep = monsterSteps[i][j];

                String symbol;

                if (rogueStep == Integer.MAX_VALUE && monsterStep == Integer.MAX_VALUE) {
                    symbol = "   X ";
                } else if (rogueStep == Integer.MAX_VALUE || monsterStep == Integer.MAX_VALUE) {
                    symbol = "   X ";
                } else {
                    int average = (rogueStep + monsterStep) / 2;
                    averageSteps[i][j] = average;
                    symbol = String.format("%4d ", average);
                }

                if (rogueSite.i() == i && rogueSite.j() == j) {
                    symbol = "   R ";
                } else if (monsterSite.i() == i && monsterSite.j() == j) {
                    symbol = "   M ";
                }

                System.out.print(symbol);
            }
            System.out.println();
        }


    }

    public void printStepDifferences(Site rogueSite, Site monsterSite) {
        initializeSteps(rogueSite, monsterSite);
        int[][] rogueSteps = this.rogueSteps;
        int[][] monsterSteps = this.monsterSteps;

        System.out.println("Step Differences Matrix:");
        for (int i = 0; i < N; i++) {
            for (int j = 0; j < N; j++) {
                int rogueStep = rogueSteps[i][j];
                int monsterStep = monsterSteps[i][j];
                String symbol;

                if (rogueStep == Integer.MAX_VALUE && monsterStep == Integer.MAX_VALUE) {
                    symbol = "   X ";
                } else if (rogueStep == Integer.MAX_VALUE || monsterStep == Integer.MAX_VALUE) {
                    symbol = "   X ";
                } else {
                    int difference = monsterStep - rogueStep;
                    differenceSteps[i][j] = difference;
                    if (difference == 0) {
                        symbol = "   0 ";
                    } else {
                        symbol = String.format("%4d ", difference);
                    }
                }

                if (rogueSite.i() == i && rogueSite.j() == j) {
                    symbol = "   R ";
                } else if (monsterSite.i() == i && monsterSite.j() == j) {
                    symbol = "   M ";
                }

                System.out.print(symbol);
            }
            System.out.println();
        }


    }

    /********************* Find Wall *******************************/
    public void findConnectedWallsFrom(int i, int j) {
        boolean[][] visited = new boolean[N][N];
        List<Site> wallChunk = new ArrayList<>();
        dfsWall(i, j, visited, wallChunk);

        // Check if the connecting block of the current wall has been added
        boolean alreadyAdded = false;
        for (List<Site> existingChunk : connectedWalls) {
            if (new HashSet<>(existingChunk).containsAll(wallChunk)) {
                alreadyAdded = true;
                break;
            }
        }

        // If it has not been added before, add the current connected block to connectedWalls and wallChunkMap
        if (!alreadyAdded) {
            connectedWalls.add(wallChunk);
        }
    }

    private void dfsWall(int i, int j, boolean[][] visited, List<Site> wallChunk) {
        if (i < 0 || i >= N || j < 0 || j >= N || visited[i][j] || !isWall[i][j]) {
            return;
        }

        visited[i][j] = true;
        wallChunk.add(sites[i][j]);

        dfsWall(i + 1, j, visited, wallChunk);
        dfsWall(i - 1, j, visited, wallChunk);
        dfsWall(i, j + 1, visited, wallChunk);
        dfsWall(i, j - 1, visited, wallChunk);
        dfsWall(i + 1, j + 1, visited, wallChunk);
        dfsWall(i + 1, j - 1, visited, wallChunk);
        dfsWall(i - 1, j + 1, visited, wallChunk);
        dfsWall(i - 1, j - 1, visited, wallChunk);
    }

    // Used to find the loop around the wall
    public void checkConnectedWallsForLoops() {
        for (List<Site> wallChunk : connectedWalls) {
            // Find neighbors of adjacent rooms or corridors in the wall
            List<Site> neighbors = findConnectedRoomsOrCorridors(wallChunk);
            System.out.println("wall neighbors " + neighbors);
            // Check if these neighbors have formed a loop
            List<Site> wallCycleLoop = findConnectedCircle(neighbors);
            System.out.println("connected wall cycle neighbors" + wallCycleLoop);
            if (!wallCycleLoop.isEmpty()) soundedWallLoop.add(findConnectedCircle(neighbors));
        }
    }

    private List<Site> findConnectedCircle(List<Site> neighbors) {
        List<Site> circle = new ArrayList<>();
        Set<Site> visited = new HashSet<>();

        // Starting from the first neighbor for DFS
        if (dfsFindCircle(neighbors, neighbors.get(0), visited, circle)) {
            // If the loop is found, return the loop list
            return circle;
        } else {
            // If the loop is not found, return an empty list
            return Collections.emptyList();
        }
    }

    private boolean dfsFindCircle(List<Site> neighbors, Site current, Set<Site> visited, List<Site> circle) {
        // Add the current node to the visited list
        visited.add(current);
        // Add the current node to the loop list
        circle.add(current);

        //System.out.println("current node" + current);

        List<Site> currentNeighbors = getNeighbor3(current);

        //System.out.println("current neighbors " + currentNeighbors);

        System.out.println();

        // Traverse the adjacent nodes of the current node
        for (Site neighbor : neighbors) {
            // If adjacent nodes have not been accessed and are adjacent to the current node, recursively call DFS
            if (!visited.contains(neighbor) && currentNeighbors.contains(neighbor)) {
                if (dfsFindCircle(neighbors, neighbor, visited, circle)) {
                    // If a loop is found in recursion, return true directly
                    return true;
                }
            }
        }

        // If all adjacent nodes of the current node have been visited and the last adjacent node can reach to the starting node, it indicates that a loop has been found
        if (circle.size() == neighbors.size() && visited.size() == neighbors.size() && currentNeighbors.contains(neighbors.get(0))) {
            return true;
        } else {
            // Otherwise, backtracking will remove the current node from the loop list and return false
            circle.remove(circle.size() - 1);
            return false;
        }
    }


    // Find the neighbor of the room or corridor block connected to the wall
    private List<Site> findConnectedRoomsOrCorridors(List<Site> wallChunk) {
        Set<Site> neighbors = new HashSet<>();

        // Traverse each node in the wall block
        for (Site cell : wallChunk) {
            int row = cell.i();
            int col = cell.j();

            // Check if the upper, lower, left, and right neighbors of each cell are rooms or corridors, and add them to the neighbor list
            if (isValid(row - 1, col) && (isRoom[row - 1][col] || isCorridor[row - 1][col])) {
                neighbors.add(sites[row - 1][col]);
            }
            if (isValid(row + 1, col) && (isRoom[row + 1][col] || isCorridor[row + 1][col])) {
                neighbors.add(sites[row + 1][col]);
            }
            if (isValid(row, col - 1) && (isRoom[row][col - 1] || isCorridor[row][col - 1])) {
                neighbors.add(sites[row][col - 1]);
            }
            if (isValid(row, col + 1) && (isRoom[row][col + 1] || isCorridor[row][col + 1])) {
                neighbors.add(sites[row][col + 1]);
            }
            if (isValid(row - 1, col - 1) && (isRoom[row - 1][col - 1] || isCorridor[row - 1][col - 1])) {
                neighbors.add(sites[row - 1][col - 1]);
            }
            if (isValid(row - 1, col + 1) && (isRoom[row - 1][col + 1] || isCorridor[row - 1][col + 1])) {
                neighbors.add(sites[row - 1][col + 1]);
            }
            if (isValid(row + 1, col - 1) && (isRoom[row + 1][col - 1] || isCorridor[row + 1][col - 1])) {
                neighbors.add(sites[row + 1][col - 1]);
            }
            if (isValid(row + 1, col + 1) && (isRoom[row + 1][col + 1] || isCorridor[row + 1][col + 1])) {
                neighbors.add(sites[row + 1][col + 1]);
            }

        }

        return new ArrayList<>(neighbors);
    }


    /*************** Corridor ************************/

    // DFS searches for corridor paths and saves the corridor path starting from the starting in the corridor list as a corridor path
    private List<Site> dfsCorridor(Site site) {
        List<Site> corridor = new ArrayList<>();
        dfsHelper(site.i(), site.j(), corridor);
        return corridor;
    }

    public List<Site> getCorridorNeighbor(Site v) {
        List<Site> neighbor = getNeighbor(v);
        List<Site> corr = new ArrayList<>();
        for (Site i : neighbor) {
            if (isCorridor(i)) {
                corr.add(i);
            }
        }
        return corr;
    }

    private void dfsHelper(int i, int j, List<Site> corridor) {
        // Check if boundary conditions and nodes are corridors
        if (i < 0 || i >= N || j < 0 || j >= N || !isCorridor[i][j]) {
            return;
        }

        // If the corridor path already contains the current node, return to avoid looping
        if (corridor.contains(sites[i][j])) {
            System.out.println("maybe a loop " + (i * N + j) + "ij: " + i + " " + j);
            loopInCorr.add(sites[i][j]);
            return;
        }

        // Add the current node to the corridor list
        corridor.add(sites[i][j]);

        // change the flag to indicate this corridor has been visited
        // The basic operation of recursion marks the corridor as visited
        isCorridor[i][j] = false;

        // Recursively for right, left, top, and bottom, mark all visited corridors
        dfsHelper(i + 1, j, corridor);
        dfsHelper(i - 1, j, corridor);
        dfsHelper(i, j + 1, corridor);
        dfsHelper(i, j - 1, corridor);

        // change the flag back to indicate this is a corridor
        isCorridor[i][j] = true;
    }

    // check if there are adjacent rooms in the corridor
    private boolean hasAdjacentRoom(int i, int j) {
        return (i > 0 && isRoom[i - 1][j]) ||
                (i < N - 1 && isRoom[i + 1][j]) ||
                (j > 0 && isRoom[i][j - 1]) ||
                (j < N - 1 && isRoom[i][j + 1]);
    }


    /*********************************** legal move check *********************************************/

    // Determine if the movement from v to w is legal
    public boolean isLegalMove(Site v, Site w) {
        // get the position
        int i1 = v.i();
        int j1 = v.j();
        int i2 = w.i();
        int j2 = w.j();
        // check if it is out of boundary
        if (i1 < 0 || j1 < 0 || i1 >= N || j1 >= N) return false;
        if (i2 < 0 || j2 < 0 || i2 >= N || j2 >= N) return false;
        // both are wall
        if (isWall(v) || isWall(w)) return false;
        // movement should less than or equal to 1 step
        if (Math.abs(i1 - i2) > 1) return false;
        if (Math.abs(j1 - j2) > 1) return false;
        // it is legal for both are rooms
        if (isRoom(v) && isRoom(w)) return true;
        // allow to move along row
        if (i1 == i2) return true;
        // allow to move along column
        if (j1 == j2) return true;

        return false;
    }

    /************************ BFS find the shortest path for Monster to Rogue **********************************/

    // Using BFS to explore neighbors until the target
    public List<Site> shortestPathBFS(Site start, Site target) {

        Queue<Site> queue = new LinkedList<>();
        Set<Site> visited = new HashSet<>();
        Map<Site, Site> parent = new HashMap<>();
        List<Site> path = new ArrayList<>();

        queue.offer(start);
        visited.add(start);

        while (!queue.isEmpty()) {

            Site current = queue.poll();

            // If the current site is target, returns the shortest path
            if (current.equals(target)) {
                path.add(current);
                reconstructPath(parent, start, target, path);
                return path;
            }

            // Get the neighbor list of the current node, true for line first, false for slash first
            List<Site> neighborList = getNeighbor(current);

            // Sort the neighbor list by Manhattan distance to the target node
            neighborList.sort(Comparator.comparingInt(n -> n.manhattanTo(target)));
            // Traverse the neighbors of the current node
            for (Site neighbor : neighborList) {
                // If the neighboring node is not visited, mark and join the queue
                if (!visited.contains(neighbor)) {
                    visited.add(neighbor);
                    parent.put(neighbor, current);
                    queue.offer(neighbor);
                }
            }
        }
        return Collections.emptyList();
    }

    // the shortest path for Manhattan distance
    public int manhattanDistanceShortestPath(Site start, Site target) {
        List<Site> shortestPath = shortestPathBFS(start, target);
        if (shortestPath.isEmpty()) {
            // If the shortest path is not found, return a sufficiently large value indicating that the path does not exist
            return Integer.MAX_VALUE;
        } else {
            // Calculate the Manhattan distance for the shortest path
            int distance = 0;
            for (int i = 1; i < shortestPath.size(); i++) {
                //distance += manhattanDistance(shortestPath.get(i - 1), shortestPath.get(i));
                distance += shortestPath.get(i - 1).manhattanTo(shortestPath.get(i));
            }
            return distance;
        }
    }


    /******************* Assistance method for the traversal ***************************/

    // The shortest path
    private void reconstructPath(Map<Site, Site> parent, Site start, Site target, List<Site> path) {
        while (!start.equals(target)) {
            path.add(parent.get(target));
            target = parent.get(target);
        }

        Collections.reverse(path);
    }


    // Get straight line neighbors first, then diagonal lines
    public List<Site> getNeighbor(Site v) {
        int i1 = v.i();
        int j1 = v.j();
        List<Site> possible_neighbor = new ArrayList<>();

        // Straight Neighbors
        if (isValid(i1 - 1, j1)) {
            possible_neighbor.add(sites[i1 - 1][j1]);
        }
        if (isValid(i1, j1 - 1)) {
            possible_neighbor.add(sites[i1][j1 - 1]);
        }
        if (isValid(i1 + 1, j1)) {
            possible_neighbor.add(sites[i1 + 1][j1]);
        }
        if (isValid(i1, j1 + 1)) {
            possible_neighbor.add(sites[i1][j1 + 1]);
        }

        // Diagonal Neighbor
        if (isValid(i1 - 1, j1 - 1)) {
            possible_neighbor.add(sites[i1 - 1][j1 - 1]);
        }
        if (isValid(i1 + 1, j1 + 1)) {
            possible_neighbor.add(sites[i1 + 1][j1 + 1]);
        }
        if (isValid(i1 - 1, j1 + 1)) {
            possible_neighbor.add(sites[i1 - 1][j1 + 1]);
        }
        if (isValid(i1 + 1, j1 - 1)) {
            possible_neighbor.add(sites[i1 + 1][j1 - 1]);
        }

        List<Site> neighbor = new ArrayList<>();

        for (Site s : possible_neighbor) {
            if (isLegalMove(v, s)) {
                neighbor.add(s);
            }
        }

        return neighbor;
    }

    // Get a straight neighbor
    public List<Site> getNeighbor3(Site v) {
        int i1 = v.i();
        int j1 = v.j();
        List<Site> possible_neighbor = new ArrayList<>();

        // Straight Neighbors
        if (isValid(i1 - 1, j1)) {
            possible_neighbor.add(sites[i1 - 1][j1]);
        }
        if (isValid(i1, j1 - 1)) {
            possible_neighbor.add(sites[i1][j1 - 1]);
        }
        if (isValid(i1 + 1, j1)) {
            possible_neighbor.add(sites[i1 + 1][j1]);
        }
        if (isValid(i1, j1 + 1)) {
            possible_neighbor.add(sites[i1][j1 + 1]);
        }

        List<Site> neighbor = new ArrayList<>();

        for (Site s : possible_neighbor) {
            if (isLegalMove(v, s)) {
                neighbor.add(s);
            }
        }

        return neighbor;
    }


    public List<Site> findCircleBFS(Site start, Site target) {
        Queue<Site> queue = new LinkedList<>();
        Set<Site> visited = new HashSet<>();
        Map<Site, Site> parent = new HashMap<>();
        List<Site> path = new ArrayList<>();

        queue.offer(start);
        visited.add(start);

        while (!queue.isEmpty()) {
            Site current = queue.poll();

            if (current.equals(target)) {
                path.add(current);
                reconstructPath(parent, start, target, path);
                //System.out.println(path);
                return path;
            }
            List<Site> neighborList = getNeighbor(current);

            for (Site neighbor : neighborList) {
                if (current.equals(start) && neighbor.equals(target)) {
                    continue;
                }
                if (!visited.contains(neighbor)) {
                    visited.add(neighbor);
                    parent.put(neighbor, current);
                    queue.offer(neighbor);
                }
            }
        }

        return Collections.emptyList();
    }

}
