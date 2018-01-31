package parsetablegen;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

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

public class State {
    public Set<Item> items;
    private State(Set<Item> items) {
        this.items = items;
    }
    private static Map<Set<Item>, State> map = new HashMap<Set<Item>, State>();
    public static State v(Set<Item> items) {
        State ret = map.get(items);
        if(ret == null) {
            ret = new State(items);
            map.put(items, ret);
        }
        return ret;
    }
    public String toString() {
        StringBuffer ret = new StringBuffer();
        ret.append("\n");
        for(Item item : items) {
            ret.append(item);
            ret.append("\n");
        }
        return ret.toString();
    }
}


