public class Monster extends Role {
    private Rule rule;

    public Monster(Game game) {
        super(game);
        this.rule = new ChaseRule(game);
    }


    public Site move() {
        return rule.move();
    }

}
