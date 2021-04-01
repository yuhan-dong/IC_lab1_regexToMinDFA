/**
 * @author yuhan
 * @date 06.03.2021 - 15:16
 * @purpose
 */
public class Edge {
    public int u;
    public int v;
    public Character key;
    public int mark;

    public Edge(int u, int v, Character key) {
        super();
        this.u = u;
        this.v = v;
        this.key = key;
    }

    public int getMark() {
        return mark;
    }

    public void setMark(int mark) {
        this.mark = mark;
    }

    public int getU() {
        return u;
    }

    public void setU(int u) {
        this.u = u;
    }

    public int getV() {
        return v;
    }

    public void setV(int v) {
        this.v = v;
    }

    public Character getKey() {
        return key;
    }

    public void setKey(Character key) {
        this.key = key;
    }

    @Override
    public String toString() {
        return u + "--->" + v + " : " + key;
    }

    @Override
    public boolean equals(Object arg0) {
        Edge tmp = (Edge) arg0;
        return tmp.u == this.u && tmp.v == this.v && tmp.key == this.key;
    }

    @Override
    public int hashCode() {
        return u + v + key;
    }

}
