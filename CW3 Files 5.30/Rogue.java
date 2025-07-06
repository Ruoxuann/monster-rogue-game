public class Rogue extends Role {
    private Rule rule;

    public Rogue(Game game) {
        super(game);
        this.rule = new EscapeRule(game);
    }

    public Site move() {
        return rule.move();
    }

}