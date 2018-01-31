package parsetablegen;

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

/** Represents a reduce parser action. */
class ReduceAction extends Action {
    Production rule; // the production to reduce by
    public ReduceAction(Production rule) {
        this.rule = rule;
    }
    public int hashCode() { return rule.hashCode(); }
    public boolean equals(Object other) {
        if(!(other instanceof ReduceAction)) return false;
        ReduceAction o = (ReduceAction) other;
        return rule.equals(o.rule);
    }
    public String toString() {
        return "reduce " + rule;
    }
}

