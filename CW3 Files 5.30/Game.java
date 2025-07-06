public class Game {

    // portable newline
    private final static String NEWLINE = System.getProperty("line.separator");

    private Dungeon dungeon;
    private char MONSTER;
    private char ROGUE = '@';
    private int N;
    private Site monsterSite;
    private Site rogueSite;
    private Role monster;
    private Role rogue;

    // initialize board from file
    public Game(In in) {
        // read in data
        N = Integer.parseInt(in.readLine());
        char[][] board = new char[N][N];
        for (int i = 0; i < N; i++) {
            String s = in.readLine();
            for (int j = 0; j < N; j++) {
                board[i][j] = s.charAt(2 * j);

                // check for monster's location
                if (board[i][j] >= 'A' && board[i][j] <= 'Z') {
                    MONSTER = board[i][j];
                    board[i][j] = '.';
                    monsterSite = new Site(i, j);
                }

                // check for rogue's location
                if (board[i][j] == ROGUE) {
                    board[i][j] = '.';
                    rogueSite = new Site(i, j);
                }
            }
        }
        dungeon = new Dungeon(board);
        monster = new Monster(this);
        rogue = new Rogue(this);

        dungeon.printAverageSteps(rogueSite, monsterSite);
        dungeon.printStepDifferences(rogueSite, monsterSite);
    }

    // return position of monster and rogue
    public Site getMonsterSite() {
        return monsterSite;
    }

    public Site getRogueSite() {
        return rogueSite;
    }

    public Dungeon getDungeon() {
        return dungeon;
    }


    // play until monster catches the rogue
    public void play(int sleep) {
        try {

            System.out.print("\033[H\033[2J");
            for (int t = 1; true; t++) {
                System.out.printf("\033[%d;%dH", 0, 0);
                System.out.println("Move " + t);
                System.out.println();

                // monster moves
                if (monsterSite.equals(rogueSite)) break;
                Site next = monster.move();
                if (dungeon.isLegalMove(monsterSite, next)) monsterSite = next;
                else throw new RuntimeException("Monster caught cheating");

                System.out.printf("\033[%d;%dH", 2, 0);
                System.out.println(this);
                Thread.sleep(sleep);

                // rogue moves
                if (monsterSite.equals(rogueSite)) break;
                next = rogue.move();
                if (dungeon.isLegalMove(rogueSite, next)) rogueSite = next;
                else throw new RuntimeException("Rogue caught cheating");

                System.out.printf("\033[%d;%dH", 2, 0);
                System.out.println(this);
                Thread.sleep(sleep);
            }

            System.out.println("Caught by monster");
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }


    // string representation of game state (inefficient because of Site and string concat)
    public String toString() {
        StringBuilder sb = new StringBuilder();
        Site currentSite = new Site(0, 0); // Reusable Site instance

        for (int i = 0; i < N; i++) {
            for (int j = 0; j < N; j++) {
                currentSite.set(i, j); // Update the reusable Site instance

                if (rogueSite.equals(monsterSite) && rogueSite.equals(currentSite)) {
                    sb.append("* ");
                } else if (rogueSite.equals(currentSite)) {
                    sb.append(ROGUE).append(" ");
                    //sb.append(String.format("\033[%dm%s\033[0m ", 36, ROGUE));
                } else if (monsterSite.equals(currentSite)) {
                    sb.append(MONSTER).append(" ");
                    //sb.append(String.format("\033[%dm%s\033[0m ", 31, MONSTER));
                } else if (dungeon.isRoom(currentSite)) {
                    sb.append(". ");
                } else if (dungeon.isCorridor(currentSite)) {
                    sb.append("+ ");
                } else if (dungeon.isWall(currentSite)) {
                    sb.append("  ");
                }
            }
            sb.append(NEWLINE);
        }
        return sb.toString();
    }


    public static void main(String[] args) {
        //java Game Dungeons/111.txt 100
        String filePath;
        int playTime = 50;

        if (args.length == 0) {
            filePath = "Dungeons/333.txt";
        } else if (args.length == 1) {
            filePath = args[0];
        } else {
            filePath = args[0];
            try {
                playTime = Integer.parseInt(args[1]);
            } catch (NumberFormatException e) {
                System.out.println("Invalid play time. Using default value 50.");
                //playTime = 50;
            }
        }

        In stdin = new In(filePath);
        Game game = new Game(stdin);
        game.play(playTime);
        stdin.close();
    }
}




