package parsetablegen;

import java.util.*;

/*
   Copyright 2006,2008,2009 Ondrej Lhotak. All rights reserved.

   Permission is granted for study use by
   students registered in CS 444, Winter 2017
   term.

   The contents of this file may not be
   published, in whole or in part, in print
   or electronic form.

   The contents of this file may be included
   in work submitted for CS 444 assignments in
   Winter 2017.  The contents of this file may
   not be submitted, in whole or in part, for
   credit in any other course.

*/

/*
 * JLALR constructs LALR(1) and SLR(1) parse tables from a grammar, using
 * the algorithms described in chapter 3 of Appel, _Modern Compiler       
 * Implementation in Java, second edition_, 2002. JLALR reads a grammar
 * on standard input, and writes the generated grammar and parse tables on
 * standard output.                                                            
 * 
*/


/** The main LALR/SLR generator class. */
public class Generator {
    /** The context-free grammar. */
    Grammar grammar;
    /** A map from each non-terminal to the productions that expand it. */
    Map<String,List<Production>> lhsToRules = new HashMap<String,List<Production>>();

    public Generator(Grammar grammar) {
        this.grammar = grammar;
        for(String nonterm : grammar.nonterminals) {
            lhsToRules.put(nonterm, new ArrayList<Production>());
        }
        for(Production p : grammar.productions) {
            List<Production> mapRules = lhsToRules.get(p.lhs);
            mapRules.add(p);
        }
    }

    /** Compute the closure of a set of items using the algorithm of
     * Appel, p. 60 */
    public Set<Item> closure(Set<Item> i) {
        boolean change;
        while(true) {
            Set<Item> oldI = new HashSet<Item>(i);
            for( Item item : oldI ) {
                if(!item.hasNextSym()) continue;
                String x = item.nextSym();
                if(grammar.isTerminal(x)) continue;
                for( Production r : lhsToRules.get(x)) {
                    i.add(Item.v(r, 0));
                }
            }
            if( i.equals(oldI) ) return i;
        }
    }
    /** Compute the goto set for state i and symbol x, using the algorithm
     * of Appel, p. 60 */
    public Set<Item> goto_(Set<Item> i, String x) {
        Set<Item> j = new HashSet<Item>();
        for( Item item : i ) {
            if( !item.hasNextSym() ) continue;
            if( !item.nextSym().equals(x) ) continue;
            j.add(item.advance());
        }
        return closure(j);
    }
    private Map<List<String>, Set<String>> generalFirstCache = new HashMap<List<String>, Set<String>>();
    /** Compute the generalized first using the definition of Appel, p. 50. */
    public Set<String> generalFirst(List<String> l) {
        Set<String> ret = generalFirstCache.get(l);
        if(ret == null) {
            ret = new HashSet<String>();
            if(l.isEmpty()) {
                return ret;
            } else {
                ret.addAll(first.get(l.get(0)));
                int i = 0;
                while(i < l.size()-1 && nullable.contains(l.get(i))) {
                    ret.addAll(first.get(l.get(i+1)));
                    i++;
                }
            }
            generalFirstCache.put(l, ret);
        }
        return ret;
    }

    private Map<Set<Item>, Set<Item>> closureCache = new HashMap<Set<Item>, Set<Item>>();
    /** Compute the closure of a set of LR(1) items using the algorithm of
     * Appel, p. 63 */
    public Set<Item> lr1_closure(Set<Item> i) {
        boolean change;
        Set<Item> ret = closureCache.get(i);
        if(ret == null) {
            Set<Item> origI = new HashSet<Item>(i);
            Queue<Item> q = new LinkedList<Item>(i);
            while(!q.isEmpty()) {
                Item item = q.remove();
                if(!item.hasNextSym()) continue;
                String x = item.nextSym();
                if(grammar.isTerminal(x)) continue;
                List<String> betaz = new ArrayList<String>();
                for(int p = item.pos+1; p < item.rule.rhs.length; p++) {
                    betaz.add(item.rule.rhs[p]);
                }
                betaz.add(item.lookahead);
                Collection<String> ws = generalFirst(betaz);
                for( Production r : lhsToRules.get(x)) {
                    for( String w : ws ) {
                        Item newItem = Item.v(r, 0, w);
                        if(i.add(newItem)) q.add(newItem);
                    }
                }
            }
            closureCache.put(origI, i);
            ret = i;
        }
        return ret;
    }


    private Map<Pair<State, String>, State> gotoCache = new HashMap<Pair<State, String>, State>();
    /** Compute the LR(1) goto set for state i and symbol x, using the algorithm
     * of Appel, p. 63 */
    public State lr1_goto_(State i, String x) {
        Pair<State, String> pair = new Pair<State, String>(i, x);
        State ret = gotoCache.get(pair);
        if(ret == null) {
            Set<Item> j = new HashSet<Item>();
            for( Item item : i.items ) {
                if( !item.hasNextSym() ) continue;
                if( !item.nextSym().equals(x) ) continue;
                j.add(item.advance());
            }
            ret = State.v(lr1_closure(j));
            gotoCache.put(pair, ret);
        }
        return ret;
    }
    /** Add the action a to the parse table for the state state and
     * symbol sym. Report a conflict if the table already contains
     * an action for the same state and symbol. */
    private boolean addAction( State state, String sym, Action a ) {
        boolean ret = false;
        Pair<State,String> p = new Pair<State,String>(state, sym);
        Action old = table.get(p);
        if(old != null && !old.equals(a)) {
            throw new Error(
                "Conflict on symbol "+sym+" in state "+state+"\n"+
                "Possible actions:\n"+
                old+"\n"+a);
        }
        if(old == null || !old.equals(a)) ret = true;
        table.put(p, a);
        return ret;
    }
    /** Return true if all the symbols in l are in the set nullable. */
    private boolean allNullable(String[] l) {
        return allNullable(l, 0, l.length);
    }
    /** Return true if the symbols start..end in l are in the set nullable. */
    private boolean allNullable(String[] l, int start, int end) {
        boolean ret = true;
        for(int i = start; i < end; i++) {
            if(!nullable.contains(l[i])) ret = false;
        }
        return ret;
    }
    // The NULLABLE, FIRST, and FOLLOW sets. See Appel, pp. 47-49
    Set<String> nullable = new HashSet<String>();
    Map<String,Set<String>> first = new HashMap<String,Set<String>>();
    Map<String,Set<String>> follow = new HashMap<String,Set<String>>();
    /** Computes NULLABLE, FIRST, and FOLLOW sets using the algorithm
     * of Appel, p. 49 */
    public void computeFirstFollowNullable() {
        for( String z : grammar.syms() ) {
            first.put(z, new HashSet<String>());
            if(grammar.isTerminal(z)) first.get(z).add(z);
            follow.put(z, new HashSet<String>());
        }
        boolean change;
        do {
            change = false;
            for( Production rule : grammar.productions ) {
                if(allNullable(rule.rhs)) {
                    if( nullable.add(rule.lhs) ) change = true;
                }
                int k = rule.rhs.length;
                for(int i = 0; i < k; i++) {
                    if(allNullable(rule.rhs, 0, i)) {
                        if( first.get(rule.lhs).addAll(
                                first.get(rule.rhs[i])))
                            change = true;
                    }
                    if(allNullable(rule.rhs, i+1,k)) {
                        if( follow.get(rule.rhs[i]).addAll(
                                follow.get(rule.lhs)))
                            change = true;
                    }
                    for(int j = i+1; j < k; j++) {
                        if(allNullable(rule.rhs, i+1,j)) {
                            if( follow.get(rule.rhs[i]).addAll(
                                    first.get(rule.rhs[j])))
                                change = true;
                        }
                    }
                }
            }
        } while(change);
    }
    /** The computed parse table. */
    Map<Pair<State,String>,Action> table = 
        new HashMap<Pair<State,String>,Action>();
    State initialState;

    /** Generates the LR(0) parse table using the algorithms on 
     * pp. 60 of Appel. */
    public void generateLR0Table() {
        Set<Item> startRuleSet = new HashSet<Item>();
        for(Production r : lhsToRules.get(grammar.start)) {
            startRuleSet.add(Item.v(r, 0));
        }
        initialState = State.v(closure(startRuleSet));
        Set<State> t = new HashSet<State>();
        t.add(initialState);
        boolean change;
        // compute goto actions
        do {
            change = false;
            for( State i : new ArrayList<State>(t) ) {
                for( Item item : i.items ) {
                    if(!item.hasNextSym()) continue;
                    String x = item.nextSym();
                    State j = State.v(goto_(i.items, x));
                    if(t.add(j)) change = true;
                    if(addAction(i, x, new ShiftAction(j))) change = true;
                }
            }
        } while(change);
        // compute reduce actions
        for( State i : t ) {
            for( Item item : i.items ) {
                if( item.hasNextSym() ) continue;
                for( String x : grammar.syms() ) {
                    addAction(i, x, new ReduceAction(item.rule));
                }
            }
        }
    }

    /** Generates the SLR(1) parse table using the algorithms on 
     * pp. 60 and 62 of Appel. */
    public void generateSLR1Table() {
        Set<Item> startRuleSet = new HashSet<Item>();
        for(Production r : lhsToRules.get(grammar.start)) {
            startRuleSet.add(Item.v(r, 0));
        }
        initialState = State.v(closure(startRuleSet));
        Set<State> t = new HashSet<State>();
        t.add(initialState);
        boolean change;
        // compute goto actions
        do {
            change = false;
            for( State i : new ArrayList<State>(t) ) {
                for( Item item : i.items ) {
                    if(!item.hasNextSym()) continue;
                    String x = item.nextSym();
                    State j = State.v(goto_(i.items, x));
                    if(t.add(j)) change = true;
                    if(addAction(i, x, new ShiftAction(j))) change = true;
                }
            }
        } while(change);
        // compute reduce actions
        for( State i : t ) {
            for( Item item : i.items ) {
                if( item.hasNextSym() ) continue;
                for( String x : follow.get(item.rule.lhs) ) {
                    addAction(i, x, new ReduceAction(item.rule));
                }
            }
        }
    }

    /** Generates the LR(1) parse table using the algorithms on 
     * pp. 60, 62, and 64 of Appel. */
    public void generateLR1Table() {
        Set<Item> startRuleSet = new HashSet<Item>();
        System.err.println("Computing start state");
        for(Production r : lhsToRules.get(grammar.start)) {
            startRuleSet.add(Item.v(r, 0, grammar.terminals.iterator().next()));
        }
        initialState = State.v(lr1_closure(startRuleSet));
        Set<State> t = new HashSet<State>();
        t.add(initialState);
        Queue<State> q = new LinkedList<State>();
        q.add(initialState);
        // compute goto actions
        System.err.println("Computing goto actions");
        while(!q.isEmpty()) {
            State i = q.remove();
            for( Item item : i.items ) {
                if(!item.hasNextSym()) continue;
                String x = item.nextSym();
                State j = lr1_goto_(i, x);
                if(t.add(j)) {
                    System.err.print(".");
                    q.add(j);
                }
                addAction(i, x, new ShiftAction(j));
            }
        }
        // compute reduce actions
        System.err.println("Computing reduce actions");
        for( State i : t ) {
            for( Item item : i.items ) {
                if( item.hasNextSym() ) continue;
                addAction(i, item.lookahead, new ReduceAction(item.rule));
            }
        }
    }
    public Set<Item> core(Set<Item> items) {
        Set<Item> ret = new HashSet<Item>();
        for(Item item : items) {
            ret.add(Item.v(item.rule, item.pos));
        }
        return ret;
    }
    /** Generates the LALR(1) parse table using the algorithms on 
     * pp. 60, 62, and 64 of Appel. */
    public void generateLALR1Table() {
        Set<Item> startRuleSet = new HashSet<Item>();
        System.err.println("Computing start state");
        for(Production r : lhsToRules.get(grammar.start)) {
            startRuleSet.add(Item.v(r, 0, grammar.terminals.iterator().next()));
        }
        initialState = State.v(lr1_closure(startRuleSet));
        Set<State> t = new HashSet<State>();
        t.add(initialState);
        Queue<State> q = new LinkedList<State>();
        q.add(initialState);
        System.err.println("Computing states");
        while(!q.isEmpty()) {
            State i = q.remove();
            for( Item item : i.items ) {
                if(!item.hasNextSym()) continue;
                String x = item.nextSym();
                State j = lr1_goto_(i, x);
                if(t.add(j)) {
                    System.err.print(".");
                    q.add(j);
                }
            }
        }
        System.err.println("Merging states");
        Map<Set<Item>, Set<Item>> coreToState = new HashMap<Set<Item>, Set<Item>>();
        for(State state : t) {
            Set<Item> items = state.items;
            Set<Item> core = core(items);
            Set<Item> accum = coreToState.get(core);
            if(accum == null) {
                System.err.print(".");
                accum = new HashSet<Item>(items);
                coreToState.put(core, accum);
            } else accum.addAll(items);
        }
        Map<Set<Item>, State> coreToStateState = new HashMap<Set<Item>, State>();
        for(State state : t) {
            Set<Item> core = core(state.items);
            coreToStateState.put(core, State.v(coreToState.get(core)));
        }
        // compute goto actions
        System.err.println("Computing goto actions");
        t.clear();
        initialState = State.v(coreToState.get(core(initialState.items)));
        t.add(initialState);
        q.add(initialState);
        while(!q.isEmpty()) {
            State i = q.remove();
            for( Item item : i.items ) {
                if(!item.hasNextSym()) continue;
                String x = item.nextSym();
                State j = lr1_goto_(i, x);
                j = coreToStateState.get(core(j.items));
                if(t.add(j)) {
                    System.err.print(".");
                    q.add(j);
                }
                addAction(i, x, new ShiftAction(j));
            }
        }
        // compute reduce actions
        System.err.println("Computing reduce actions");
        for( State i : t ) {
            for( Item item : i.items ) {
                if( item.hasNextSym() ) continue;
                addAction(i, item.lookahead, new ReduceAction(item.rule));
            }
        }
    }
    /** Print the elements of a list separated by spaces. */
    public static String listToString(List l) {
        StringBuffer ret = new StringBuffer();
        boolean first = true;
        for( Object o : l ) {
            if( !first ) ret.append(" ");
            first = false;
            ret.append(o);
        }
        return ret.toString();
    }
    /** Produce output according to the output specification. */
    public void generateOutput() {
        Map<Production,Integer> ruleMap = new HashMap<Production,Integer>();
        int i = 0;
        for(Production r : grammar.productions) {
            ruleMap.put(r, i++);
        }
        Map<State,Integer> stateMap = new HashMap<State,Integer>();
        i = 0;
        stateMap.put(initialState, i++);
        for(Action a : table.values()) {
            if(!(a instanceof ShiftAction)) continue;
            State state = ((ShiftAction)a).nextState;
            if(!stateMap.containsKey(state)) {
                stateMap.put(state, i++);
            }
        }
        for(Pair<State,String> key : table.keySet()) {
            State state = key.getO1();
            if(!stateMap.containsKey(state)) {
                stateMap.put(state, i++);
            }
        }
        System.out.println(i);
        System.out.println(table.size());
        for(Map.Entry<Pair<State,String>,Action> e : table.entrySet()) {
            Pair<State,String> p = e.getKey();
            System.out.print(stateMap.get(p.getO1())+" "+p.getO2()+" ");
            Action a = e.getValue();
            if(a instanceof ShiftAction) {
                System.out.println("shift "+
                        stateMap.get(((ShiftAction)a).nextState));
            } else if(a instanceof ReduceAction) {
                System.out.println("reduce "+
                        ruleMap.get(((ReduceAction)a).rule));
            } else throw new Error("Internal error: unknown action");
        }
    }
}

