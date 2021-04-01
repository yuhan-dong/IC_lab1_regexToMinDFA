import java.util.*;

/**
 * @author yuhan
 * @date 06.03.2021 - 15:18
 * @purpose
 */
public class toMinDFA {
    public toMinDFA(String re) {
        List<Character> infixExpressionList = reToList(re);
        List<Character> suffixExpressionList = parseSuffixExpressionList(infixExpressionList);
        toNFA(suffixExpressionList);
        toDFA();
        hopcroft();
    }

    private final ArrayList<Edge> edgeNFA = new ArrayList<Edge>();
    private final ArrayList<Edge> edgeDFA = new ArrayList<Edge>();
    private final ArrayList<Edge> edgeMinDFA = new ArrayList<Edge>();
    //输入的字符
    private final HashSet<Character> input = new HashSet<>();
    //起始点和终态集合
    private int startNode = 0;
    private int endNode = 0;
    private final List<Integer> endNodes = new ArrayList<>();

    private final Map<Integer, HashSet<Integer>> OutEdges = new HashMap<>();
    private final Map<Integer, List<Integer>> nodeDFA = new HashMap<>();
    Stack<Edge> stackNFA = new Stack<Edge>();
    Map<String, List<Integer>> group = new HashMap<>();

    private void addEdge(int u, int v, char ch) {
        edgeNFA.add(new Edge(u, v, ch));
        //如果没有u则put，有u则add
        if (OutEdges.get(u) == null) {
            HashSet<Integer> list = new HashSet<Integer>();
            list.add(v);
            OutEdges.put(u, list);
        } else {
            OutEdges.get(u).add(v);
        }
    }

    //1.转化，添加
    //1.1 优先级设置为括号>闭包>连接>并联
    //1.1 Priority is set to () > * > Connection . > |
    public Integer getState(Character ch) {
        switch (ch) {
            case ')':
                return 5;
            case '*':
                return 4;
            case '.':
                return 3;
            case '|':
                return 2;
            case '(':
                return 1;
        }
        return -1;
    }

    //1.2 转化，添加
    public List<Character> reToList(String re) {
        List<Character> input = new ArrayList<>();
        //进行转化--添加
        for (int i = 0; i < re.length() - 1; i++) {
            //先取出第一个和第二个字符
            Character first = re.charAt(i);
            Character second = re.charAt(i + 1);
            if (Character.isLetter(first) && Character.isLetter(second)) {
                input.add(first);
                input.add('.');
            } else if ((first == ')' || first == '*') && (Character.isLetter(second) || second == '(')) {
                input.add(first);
                input.add('.');
            } else if (Character.isLetter(first) && second == '(') {
                input.add(first);
                input.add('.');
            } else
                input.add(first);
        }
        input.add(re.charAt(re.length() - 1));
        System.out.println("input：" + input);
        return input;
    }

    //2。转化为后缀表达式
    //遇见单个字符，构建一个基本单元，给一条边
    //2. Converted to postfix expression
    public List<Character> parseSuffixExpressionList(List<Character> ls) {
        //定义两个栈
        //Define two stacks,s1-Symbol stack,s2-Character stack
        Stack<Character> s1 = new Stack<Character>(); // 符号栈
        //说明：因为s2 这个栈，在整个转换过程中，没有pop操作，而且后面我们还需要逆序输出
        List<Character> s2 = new ArrayList<Character>(); // 储存中间结果的Lists2
        //遍历ls
        for (Character item : ls) {
            //如果是一个字母，加入s2
            ////If it is a letter, add s2
            if (isCharacter(item)) {
                s2.add(item);
                input.add(item);
            } else if (item.equals('(')) {
                s1.push(item);
            } else if (item.equals(')')) {
                //如果是右括号“)”，则依次弹出s1栈顶的运算符，并压入s2，直到遇到左括号为止，此时将这一对括号丢弃
                while (!(s1.peek().equals('('))) {
                    s2.add(s1.pop());
                }
                s1.pop();//!!! 将 ( 弹出 s1栈， 消除小括号
            } else {
                //当item的优先级小于等于s1栈顶运算符, 将s1栈顶的运算符弹出并加入到s2中，再次转到与s1中新的栈顶运算符相比较
                while (s1.size() != 0 && getState(s1.peek()) >= getState(item)) {
                    s2.add(s1.pop());
                }
                //还需要将item压入栈
                s1.push(item);
            }
        }
        //将s1中剩余的运算符依次弹出并加入s2
        while (s1.size() != 0) {
            s2.add(s1.pop());
        }
        return s2; //注意因为是存放到List, 因此按顺序输出就是对应的后缀表达式对应的List

    }

    //3.连接状态节点->NFA
    ////3. Connection status node -> NFA
    public ArrayList<Edge> toNFA(List<Character> ls) {
        int nodeNum = -2;
        int from = nodeNum;
        int end = nodeNum + 1;
        // 创建给栈, 只需要一个栈即可
        // 遍历 ls
        for (Character item : ls) {
            if (isCharacter(item)) {
                // 入栈
                //加边
                from = from + 2;
                end = end + 2;
                addEdge(from, end, item);
                stackNFA.push(new Edge(from, end, item));
            } else {
                if (item.equals('|')) {
                    Edge edge1 = stackNFA.pop();
                    Edge edge2 = stackNFA.pop();
                    from = from + 2;
                    end = end + 2;
                    //加4条空边
                    addEdge(from, edge1.getU(), '#');
                    addEdge(from, edge2.getU(), '#');
                    addEdge(edge1.getV(), end, '#');
                    addEdge(edge2.getV(), end, '#');
                    //作为nfa存入栈
                    Edge edge = new Edge(from, end, item);
                    stackNFA.push(edge);
                } else if (item.equals('*')) {
                    from = from + 2;
                    end = end + 2;
                    // pop出1个数，并运算加4条空边， 再入栈
                    Edge edge1 = stackNFA.pop();
                    addEdge(edge1.getV(), edge1.getU(), '#');
                    addEdge(from, edge1.getU(), '#');
                    addEdge(edge1.getV(), end, '#');
                    addEdge(from, end, '#');
                    //作为nfa存入栈
                    Edge edge = new Edge(from, end, item);
                    stackNFA.push(edge);
                } else if (item.equals('.')) {
                    Edge edge1 = stackNFA.pop();
                    Edge edge2 = stackNFA.pop();
                    //加一条空边
                    addEdge(edge2.getV(), edge1.getU(), '#');
                    //作为nfa存入栈
                    stackNFA.push(new Edge(edge2.getU(), edge1.getV(), item));
                }
            }
        }
        System.out.println("-----NFA-----");
        System.out.println("NFA: " + edgeNFA);
        //开始节点和终态节点
        startNode = stackNFA.get(0).getU();
        endNode = stackNFA.get(0).getV();
        System.out.println("startNode: " + startNode + "\tendNode: " + endNode);
        return edgeNFA;
    }

    //4.NFA->DFA
    public void toDFA() {
        //遍历nodeAll得到状态转换表
        //nfa的开始节点,沿着空边可到达的节点为开始《节点集合》
        List<Integer> select = new ArrayList<>();
        select.add(startNode);
        HashSet<Integer> A = move(select, '#');
        A.add(startNode);
        List<Integer> a = new ArrayList<>(A);
        nodeDFA.put(0, a);
        //子集构造法
        subsetConstruction(0, input);
        //开始节点和终态节点
        for (int key : nodeDFA.keySet()) {
            if (nodeDFA.get(key).contains(startNode)) {
                startNode = key;
            }
            if (nodeDFA.get(key).contains(endNode)) {
                endNodes.add(key);
            }
        }
        //OutEdges
        OutEdges.clear();
        edgeNFA.clear();
        for (Edge edge : edgeDFA) {
            addEdge(edge.getU(), edge.getV(), edge.getKey());
        }

        System.out.println("-----DFA-----");
        System.out.println("DFA: " + edgeNFA);
        System.out.println("nodes set of NFA: " + nodeDFA);
        System.out.println("startNode: " + startNode + "\tendNodes: " + endNodes);
    }

    //5.DFA->minDFA
    public void hopcroft() {
        //一开始就把所有的节点切分成2个等价类，一个N非接受状态，一个A接受状态
        List<Integer> A = endNodes; //终态
        List<Integer> N = new ArrayList<>(); //初始状态
        for (int key : nodeDFA.keySet()) {
            if (!A.contains(key)) {
                N.add(key);
            }
        }
        //把A,N放入T中
        group.put("A", A);
        group.put("N", N);

        System.out.println("-----min DFA-----");
        System.out.println("N: " + N + "\tA: " + A);
        int time = 0;
        while (time < 2) {
            for (String key : group.keySet()) {
                List<Integer> list = group.get(key);
                if (list.size() > 1) {
                    split(key, list);
                }
            }
            time++;
        }


        //排序
        List<Integer> minSort = new ArrayList<>();
        int min = 0;
        Map<Integer, List<Integer>> groupMin = new HashMap<>();

        for (String key : group.keySet()) {
            List<Integer> list = group.get(key);
            min = Collections.min(list);
            minSort.add(min);
        }
        Collections.sort(minSort);
        for (String key : group.keySet()) {
            List<Integer> list = group.get(key);
            min = Collections.min(list);
            int nodeNum = minSort.indexOf(min);
            groupMin.put(nodeNum, list);
        }
        System.out.println("group:" + groupMin);
        //toMinDFA
        for (Edge edge : edgeDFA) {
            for (Integer key : groupMin.keySet()) {
                for (Integer key2 : groupMin.keySet()) {
                    if ((groupMin.get(key).contains(edge.getU())) && (groupMin.get(key2).contains(edge.getV()))) {
                        if (!edgeMinDFA.contains(new Edge(key, key2, edge.getKey()))) {
                            edgeMinDFA.add(new Edge(key, key2, edge.getKey()));
                        }
                    }
                }
            }
        }
        System.out.println("edgeMinDFA:" + edgeMinDFA);
    }

    int index = 0;

    public void split(String keyDel, List<Integer> set) {
        //对集合做测试
        Map<String, List<Integer>> T = new HashMap<>();
        for (char ch : input) {
            for (int n : set) {
                List<Integer> node = new ArrayList<>();
                node.add(n);
                q.clear();
                for (Edge edge : edgeNFA) {
                    edge.setMark(0);
                }
                HashSet<Integer> q1 = move(node, ch);
                //检查这些状态所属的状态集合
                for (String key : group.keySet()) {
                    for (int num : q1) {
                        if (group.get(key).contains(num)) {
                            if (T.get(key) == null) {
                                T.put(key, node);
                            } else if (!T.get(key).contains(n)) {
                                T.get(key).add(n);
                            }
                        }
                    }
                }
            }
            HashSet<String> tDelKey = new HashSet<>();
            if (T.size() != 1) {
                for (String key : T.keySet()) {
                    for (String key2 : T.keySet()) {
                        if (T.get(key).size() == 1) {
                            int del = T.get(key).get(0);
                            if ((T.get(key2).contains(del)) && (!key2.equals(key))) {
                                T.get(key2).removeIf(i -> i == del);
                            }
                        }
                        if ((T.get(key).equals((T.get(key2)))) && (!key2.equals(key))) {
                            tDelKey.add(key);
                        }
                    }
                }
                for (String key : tDelKey) {
                    T.remove(key);
                }
                if (T.size() != 0) {
                    for (String key : T.keySet()) {
                        index++;
                        group.put("T" + index, T.get(key));
                    }
                    group.remove(keyDel);
                }

            }
            T.clear();
        }
    }

    int i = 0;

    //子集构造法：从找到的第一个ch字母开始遍历，找到该字母能到达的所有状态的集合
    public void subsetConstruction(int NodeNum, HashSet<Character> character) {
        List<Integer> result = null;
        List<Integer> select0 = nodeDFA.get(NodeNum);
        HashSet<Integer> select = new HashSet<>(select0);
        Map<String, List<Integer>> dfa = new HashMap<>();

        for (char ch : character) {
            int to = nodeDFA.size();
            String que = "q" + i + ch;
            //非空边q0
            q.clear();
            for (Edge edge : edgeNFA) {
                edge.setMark(0);
            }
            List<Integer> startNodes = findStartNode(select, ch);
            if (startNodes.size() == 0) {
                continue;
            }
            HashSet<Integer> q0 = move(startNodes, ch);

            //判断是否为空
            if (q0.size() == 0) {
                continue;
            }
            result = new ArrayList<>(q0);
            //判断计算出的list与已存在的list 是否相等
            for (int key : nodeDFA.keySet()) {
                if (result.equals(nodeDFA.get(key))) {
                    to = key;
                }
            }
            dfa.put(que, result);
            edgeDFA.add(new Edge(NodeNum, to, ch));
            nodeDFA.put(to, result);
        }
        i++;

        //迭代 两种情况下停止迭代，to不再增加，到了nodeDFA的size
        if (i < nodeDFA.size()) {
            NodeNum = NodeNum + 1;
            subsetConstruction(NodeNum, character);
        }
    }

    HashSet<Integer> q = new HashSet<>();

    //1。通过上一个状态集合找到的第一个ch字母的开始的节点集
    public List<Integer> findStartNode(HashSet<Integer> select, char ch) {
        List<Integer> startNodes = new ArrayList<>();
        ArrayList<Edge> node = edgeNFA;
        for (int s : select) {
            for (Edge edge : node) {
                //1.找出开始节点 如果遇到空边，则下一个节点，如果遇到ch，则记录该边的U
                //&& edge.getMark() != -1
                if ((edge.getU() == s) && (edge.getKey() == ch)) {
                    startNodes.add(s);
                } else if ((edge.getU() == s) && (edge.getKey() == '#')) {
                    //如果符合条件的edge有多个,遍历每一条边判断
                    if (OutEdges.get(s).size() > 1) {
                        for (Integer v : OutEdges.get(s)) {
                            if ((edge.getU() == s) && (edge.getV() == v)) {
                                if (edge.getKey() == '#') {
                                    HashSet<Integer> q1 = new HashSet<>();
                                    q1.add(v);
                                    findStartNode(q1, ch);
                                } else if (edge.getKey() == ch) {
                                    startNodes.add(s);
                                }
                            }
                        }
                    } else if (OutEdges.get(s).size() == 1) { //如果只有一个edge
                        findStartNode(OutEdges.get(s), ch);
                    }
                }
            }
        }
        return startNodes;
    }

    //2。从开始节点遍历
    public HashSet<Integer> move(List<Integer> startNodes, char ch) {
        ArrayList<Edge> node = edgeNFA;
        for (int s : startNodes) {
            for (Edge edge : node) {
                if ((edge.getU() == s) && ((edge.getKey() == ch) || (edge.getKey() == '#')) && edge.getMark() == 0) {
                    //如果符合条件的edge有多个,遍历每一条边判断
                    if (OutEdges.get(s).size() > 1) {
                        for (Integer v : OutEdges.get(s)) {
                            if ((edge.getU() == s) && (edge.getV() == v)) {
                                if ((edge.getKey() == '#') || (edge.getKey() == ch)) {
                                    edge.setMark(-1);
                                    q.add(edge.getV());
                                    List<Integer> v0 = new ArrayList();
                                    v0.add(v);
                                    move(v0, ch);
                                }
                            }
                        }
                    } else if (OutEdges.get(s).size() == 1) {
                        edge.setMark(-1);
                        q.addAll(OutEdges.get(s));
                        List<Integer> v0 = new ArrayList();
                        v0.add(edge.getV());
                        move(v0, ch);
                    }
//                    else if (OutEdges.get(s) == null) { //如果没有指出去的边
//                        break;
//                    }
                }
            }
        }
        return q;
    }

    public Boolean isCharacter(Character c) {
        return (c >= 'A' && c <= 'Z')
                || (c >= 'a' && c <= 'z')
                || (c >= '0' && c <= '9');
    }
}

