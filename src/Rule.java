public abstract class Rule {
    protected Game game;
    protected Dungeon dungeon;

    public Rule(Game game) {
        this.game = game;
        this.dungeon = game.getDungeon();
    }

    public abstract Site move();
}
