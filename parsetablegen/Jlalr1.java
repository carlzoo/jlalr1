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

/*
class Jlr1 {
    public static final void main(String[] args) {
        Grammar grammar;
        try {
            grammar = Util.readGrammar(new Scanner(System.in));
            Util.writeGrammar(grammar);
        } catch(Error e) {
            System.err.println("Error reading grammar: "+e);
            System.exit(1);
            return;
        }
        Generator jlr = new Generator(grammar);
        try {
            jlr.computeFirstFollowNullable();
            jlr.generateLR1Table();
            jlr.generateOutput();
        } catch(Error e) {
            System.err.println("Error performing LR(1) construction: "+e);
            System.exit(1);
            return;
        } 
    }
}
class Jlr0 {
    public static final void main(String[] args) {
        Grammar grammar;
        try {
            grammar = Util.readGrammar(new Scanner(System.in));
            Util.writeGrammar(grammar);
        } catch(Error e) {
            System.err.println("Error reading grammar: "+e);
            System.exit(1);
            return;
        }
        Generator jlr = new Generator(grammar);
        try {
            jlr.computeFirstFollowNullable();
            jlr.generateLR0Table();
            jlr.generateOutput();
        } catch(Error e) {
            System.err.println("Error performing LR(0) construction: "+e);
            System.exit(1);
            return;
        } 
    }
}
class Jslr1 {
    public static final void main(String[] args) {
        Grammar grammar;
        try {
            grammar = Util.readGrammar(new Scanner(System.in));
            Util.writeGrammar(grammar);
        } catch(Error e) {
            System.err.println("Error reading grammar: "+e);
            System.exit(1);
            return;
        }
        Generator jslr = new Generator(grammar);
        try {
            jslr.computeFirstFollowNullable();
            jslr.generateSLR1Table();
            jslr.generateOutput();
        } catch(Error e) {
            System.err.println("Error performing SLR(1) construction: "+e);
            System.exit(1);
            return;
        } 
    }
}
*/
public class Jlalr1 {
    public static final void main(String[] args) {
        Grammar grammar;
        try {
            grammar = Util.readGrammar(new Scanner(System.in));
            Util.writeGrammar(grammar);
        } catch(Error e) {
            System.err.println("Error reading grammar: "+e);
            System.exit(1);
            return;
        }
        Generator jlalr = new Generator(grammar);
        try {
            jlalr.computeFirstFollowNullable();
            jlalr.generateLALR1Table();
            jlalr.generateOutput();
        } catch(Error e) {
            System.err.println("Error performing LALR(1) construction: "+e);
            System.exit(1);
            return;
        } 
    }
}

