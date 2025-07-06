public abstract class Role {
    protected Game game;

    public Role(Game game) {
        this.game = game;
    }

    public abstract Site move();
}
