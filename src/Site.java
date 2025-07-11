public class Site {
    private int i;
    private int j;

    // initialize board from file
    public Site(int i, int j) {
        this.i = i;
        this.j = j;
    }

    public int i() {
        return i;
    }

    public int j() {
        return j;
    }

    // Manhattan distance between invoking Site and w
    public int manhattanTo(Site w) {
        Site v = this;
        int i1 = v.i();
        int j1 = v.j();
        int i2 = w.i();
        int j2 = w.j();
        return Math.abs(i1 - i2) + Math.abs(j1 - j2);
    }

    // does invoking site equal site w?
    public boolean equals(Object w) {
        if (w.getClass() != getClass()) {
            return false;
        } else {
            return this.equals((Site) w);
        }
    }

    // does invoking site equal site w?
    public boolean equals(Site w) {
        return (manhattanTo(w) == 0);
    }

    // overwrite for List/Map container
    public int hashCode() {
        return this.i << 16 + this.j;
    }

    public String toString() {
        return "(" + this.i() + ", " + this.j() + ")";
    }

    public void set(int i, int j) {
        this.i = i;
        this.j = j;
    }
}

