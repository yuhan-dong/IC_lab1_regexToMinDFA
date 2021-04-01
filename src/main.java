/**
 * @author yuhan
 * @date 06.03.2021 - 18:54
 * @purpose
 */
public class main {
    public static void main(String[] args) {
        // (a|b)*ab a(b|c)*
        String regexp = "a(b|c)*";
        toMinDFA minDFA = new toMinDFA(regexp);
    }
}
