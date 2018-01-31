package parsetablegen;

import java.util.HashMap;
import java.util.Map;

/** Represents an item (a dotted production). */
class Item {
    Production rule; // the production
    int pos; // position of the dot (0 <= pos <= rule.rhs.size())
    String lookahead; // the lookahead terminal
    private Item( Production rule, int pos, String lookahead ) {
        this.rule = rule;
        this.pos = pos;
        this.lookahead = lookahead;
    }
    private static Map<Pair<Production, Pair<Integer, String>>, Item> map =
        new HashMap<Pair<Production, Pair<Integer, String>>, Item>();
    public static Item v(Production rule, int pos, String lookahead) {
        Pair<Production, Pair<Integer, String>> triple =
            new Pair<Production, Pair<Integer, String>>(
                    rule, new Pair<Integer, String>(pos, lookahead));
        Item ret = map.get(triple);
        if(ret == null) {
            ret = new Item(rule, pos, lookahead);
            map.put(triple, ret);
        }
        return ret;
    }
    public static Item v(Production rule, int pos) {
        return v(rule, pos, "");
    }
    /** Returns true if the dot is not at the end of the RHS. */
    public boolean hasNextSym() {
        return pos < rule.rhs.length;
    }
    /** Returns the symbol immediately after the dot. */
    public String nextSym() {
        if(!hasNextSym()) throw new RuntimeException("Internal error: getting next symbol of an item with no next symbol");
        return rule.rhs[pos];
    }
    /** Returns the item obtained by advancing (shifting) the dot by one
     *  symbol. */
    public Item advance() {
        if(!hasNextSym()) throw new RuntimeException("Internal error: advancing an item with no next symbol");
        return Item.v(rule, pos+1, lookahead);
    }
    public String toString() {
        StringBuffer ret = new StringBuffer();
        ret.append("(");
        ret.append(rule.lhs);
        ret.append(" ->");
        int i;
        for(i = 0; i < pos; i++) ret.append(" "+rule.rhs[i]);
        ret.append(" ##");
        for(; i < rule.rhs.length; i++) ret.append(" "+rule.rhs[i]);
        ret.append(", ");
        ret.append(lookahead);
        ret.append(")");
        return ret.toString();
    }
}
