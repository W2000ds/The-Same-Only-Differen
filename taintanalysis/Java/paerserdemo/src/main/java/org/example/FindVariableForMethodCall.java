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


public class FindVariableForMethodCall {
    int[] targetlings = new int[3000];
    String[] filepaths = new String[3000];

    static String variableName = new String();
    static ArrayList<Integer> varlines = new ArrayList<Integer>();

    static ArrayList<String> varnamelist = new ArrayList<String>();

    static HashSet<String> result = new HashSet<String>();
    static int depth;

    static int maxDepth = 6;


    public static void main(String[] args) throws IOException {
        try (BufferedReader br = new BufferedReader(new FileReader("/home/lhy/PycharmProjects/javapathsearch/hadoopcalllines.txt"))) {
            String line;
            while ((line = br.readLine()) != null) {
                // 拆分每行数据
                String[] parts = line.split(" ");
                if (parts.length == 2) {
                    int number = Integer.parseInt(parts[0]);
                    String filePath = parts[1];
                    // 在这里可以对number和filePath执行你的操作
                    System.out.println("lineofcallget: " + number);
                    System.out.println("FilePath: " + filePath);
                    taintanalysis(filePath,number);
                    write2file(filePath);
                } else {
                    System.err.println("Invalid line: " + line);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
//        String s = "/home/lhy/JavaCode/hadoop-3.3.5-src/hadoop-hdfs-project/hadoop-hdfs/src/main/java/org/apache/hadoop/hdfs/server/blockmanagement/DatanodeManager.java";
//        taintanalysis(s,305);
        PrintWriter writer;
        writer = new PrintWriter(new FileWriter("TAresult.txt", true));
        for(String s:result){
              writer.println(s);
          }
        writer.close();
    }


    private static void taintanalysis(String filepath, int targetLineNumber){
        String[] getnames = {"getFloat","getInt","getDouble","getBoolean","getInts","getLongBytes","get","getStrings","getTimeDuration","getTrimmedStrings","getLong"};
        try {
            // 读取 Java 源代码文件
            FileInputStream in = new FileInputStream(filepath);

            JavaParser javaParser = new JavaParser();
            // 解析Java源代码文件
            ParseResult<CompilationUnit> parseResult = javaParser.parse(in);

            CompilationUnit cu = parseResult.getResult().get();

            // 定义函数调用的方法名
            varlines.clear();
            varnamelist.clear();
            depth = 0;
            // 遍历抽象语法树
            cu.accept(new VoidVisitorAdapter<Void>() {

                @Override
                public void visit(MethodCallExpr methodCall, Void arg) {
                    super.visit(methodCall, arg);
                    // 检查函数调用是否匹配
                    if (contains(getnames,methodCall.getNameAsString())) {
                        // 找到函数调用所在的语句
                        int callLineNumber = methodCall.getBegin().get().line;
                        // 检查是否在指定的行数上
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
                           if(!variablename.equals(assignExpr.getTarget().toString())){//判断赋值的对象是否目标变量
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
                return true; // 找到目标元素，返回 true
            }
        }
        return false; // 未找到目标元素，返回 false
    }

    private static void write2file(String filepath) throws IOException {

        int i = 0;

        for (Integer value : varlines) {
            String s = filepath+" "+value+" "+varnamelist.get(i++)+" "+variableName;
            result.add(s);
        }


    }
}

