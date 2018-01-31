package parsetablegen;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
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

/** Representation of a context-free grammar. */
class Grammar {
    Set<String> terminals = new LinkedHashSet<String>();
    Set<String> nonterminals = new LinkedHashSet<String>();
    Set<Production> productions = new LinkedHashSet<Production>();
    String start;

    public boolean isTerminal(String s) {
        return terminals.contains(s);
    }
    public boolean isNonTerminal(String s) {
        return nonterminals.contains(s);
    }
    public List<String> syms() {
        List<String> ret = new ArrayList<String>();
        ret.addAll(terminals);
        ret.addAll(nonterminals);
        return ret;
    }
}

