package org.example;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ParseResult;
import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.VariableDeclarator;
import com.github.javaparser.ast.expr.*;
import com.github.javaparser.ast.stmt.ExpressionStmt;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;

import java.io.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

public class varsearch {
    int[] targetlings = new int[3000];
    String[] filepaths = new String[3000];

    static String variableName = new String();
    static ArrayList<Integer> varlines = new ArrayList<Integer>();

    static ArrayList<String> varnamelist = new ArrayList<String>();

    static HashSet<String> result = new HashSet<String>();
    static int depth;

    static int maxDepth = 6;


    public static void main(String[] args) throws IOException {
        try () {
            String line = "1" 
            String filePath = "test.java"//input the taint source line and file.
            int number = Integer.parseInt(line);

            System.out.println("lineofcallget: " + number);
            System.out.println("FilePath: " + filePath);
            taintanalysis(filePath,number);
            write2file(filePath);
            
        } catch (IOException e) {
            e.printStackTrace();
        }

        PrintWriter writer;
        writer = new PrintWriter(new FileWriter("TAresult.txt", true));
        for(String s:result){
              writer.println(s);
          }
        writer.close();
    }


    private static void taintanalysis(String filepath, int targetLineNumber){
          
        String[] getnames = {"getFloat","getInt","getDouble","getBoolean","getInts","getLongBytes","get","getStrings","getTimeDuration","getTrimmedStrings","getLong"};
        //input the configutation API
        try {

            FileInputStream in = new FileInputStream(filepath);

            JavaParser javaParser = new JavaParser();

            ParseResult<CompilationUnit> parseResult = javaParser.parse(in);

            CompilationUnit cu = parseResult.getResult().get();


            varlines.clear();
            varnamelist.clear();
            depth = 0;

            cu.accept(new VoidVisitorAdapter<Void>() {

                @Override
                public void visit(MethodCallExpr methodCall, Void arg) {
                    super.visit(methodCall, arg);

                    if (contains(getnames,methodCall.getNameAsString())) {
 
                        int callLineNumber = methodCall.getBegin().get().line;

                        if (callLineNumber == targetLineNumber) {
                            try{
                            Node current = methodCall;
                            while (!(current instanceof AssignExpr || current instanceof VariableDeclarator)) {
                                if(current!=null){
                                    current = current.getParentNode().orElse(null);
                                }
                                else{
                                    break;
                                }
                            }
                            System.out.println(current);


                            if(current instanceof VariableDeclarator) {
                                VariableDeclarator variableDeclarator = (VariableDeclarator) methodCall.getParentNode().get();
                                variableName = variableDeclarator.getNameAsString().replaceAll("^.*\\.", "");;
                                System.out.println("Variable name: " + variableName);
                                varlines.add(targetLineNumber);
                                varnamelist.add(variableName);
                            }
                            if(current instanceof AssignExpr) {
                                AssignExpr assignExpr = (AssignExpr) methodCall.getParentNode().get();
                                variableName = assignExpr.getTarget().toString().replaceAll("^.*\\.", "");;
                                System.out.println("Variable name: " + variableName);
                                varlines.add(targetLineNumber);
                                varnamelist.add(variableName);
                            }

                            if(current==null){
                                variableName = "null";
                            }

                            } catch(ClassCastException ce){
                                System.err.println("An error occurred: " + ce.getMessage()+"in ");
                            }
                        }

                    }
                }
            }, null);

            cu.accept(new VoidVisitorAdapter<String>() {
                @Override
                public void visit(NameExpr ne, String variablename) {
                    super.visit(ne,variablename);
                    
                   if(ne.getNameAsString().equals(variablename)){
                    System.out.println(ne.getRange().get().begin.line+" "+variablename);

                    if(varlines.add(ne.getRange().get().begin.line)){
                        varnamelist.add(ne.getNameAsString());
                    };
                    Node current = ne;
                    while (!(current instanceof AssignExpr || current instanceof VariableDeclarator)) {
                        if(current!=null){
                            current = current.getParentNode().orElse(null);
                        }
                        else{
                            break;
                        }
                    }

                    if(current instanceof VariableDeclarator){
                           VariableDeclarator variableDeclarator = (VariableDeclarator)current;
                           String sonvariableName = variableDeclarator.getNameAsString();   
                           int line = variableDeclarator.getRange().get().begin.line;                       
                           System.out.println("Found in variable declaration: " + sonvariableName+" "+line);
                           varlines.add(line);
                           varnamelist.add(sonvariableName);


                           if(depth < maxDepth) {
                            depth++;
                            cu.accept(this, sonvariableName);
                            depth--;
                            }
                       }

                    if(current instanceof AssignExpr){
                           AssignExpr assignExpr = (AssignExpr)current;
                           if(!variablename.equals(assignExpr.getTarget().toString())){
                           String sonvariableName = assignExpr.getTarget().toString();
                           int line = assignExpr.getRange().get().begin.line;     
                           System.out.println("Found in variable declaration: " + sonvariableName+" "+line);
                           varlines.add(line);
                           varnamelist.add(sonvariableName);

                           if(depth < maxDepth) {
                                   depth++;
                                   cu.accept(this, sonvariableName);
                                   depth--;
                               }
                           }                          
                       }

                   }
                }
            }, variableName);
        } catch (IOException e) {
            e.printStackTrace();
        }



    }
    public static boolean contains(String[] array, String target) {
        for (String element : array) {
            if (element != null && element.equals(target)) {
                return true; 
            }
        }
        return false; 
    }

    private static void write2file(String filepath) throws IOException {

        int i = 0;

        for (Integer value : varlines) {
            String s = filepath+" "+value+" "+varnamelist.get(i++)+" "+variableName;
            result.add(s);
        }


    }
}

