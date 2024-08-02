package org.example;
import com.github.javaparser.JavaParser;
import com.github.javaparser.ParseResult;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.expr.ClassExpr;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.expr.SimpleName;
import com.github.javaparser.ast.type.ClassOrInterfaceType;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.*;


public class ProjectASTBuilder {

    public static void main(String[] args) {
        // 指定项目根目录
        String projectRoot = "/home/lhy/JavaCode/paerserdemo/src/test/java";

        // 构建整个项目的AST
        List<CompilationUnit> projectAST = buildProjectAST(projectRoot);


        // 要查找的起始函数名称
        String startFunctionName = "hi";

        // 查找调用链
        List<String> callChain = findFunctionCallChain(projectAST, startFunctionName);

        // 打印调用链
        for (String functionName : callChain) {
            System.out.println("Function called: " + functionName);
        }
    }

    private static List<String> findFunctionCallChain(List<CompilationUnit> projectAST, String startFunctionName) {
        List<String> callChain = new ArrayList<>();

        // 创建AST遍历器
        VoidVisitorAdapter<String> visitor = new VoidVisitorAdapter<String>() {
            @Override
            public void visit(MethodDeclaration methodDeclaration, String functionName) {
                if(methodDeclaration==null){
                    return;
                }
                if (methodDeclaration.getNameAsString().equals(functionName)) {
                    // 找到目标函数，添加到调用链中
                    System.out.println(functionName);
                    callChain.add(functionName);
                    // 遍历函数内部的方法调用
                    List<MethodCallExpr> methodCalls = methodDeclaration.findAll(MethodCallExpr.class);
                    for (MethodCallExpr methodCall : methodCalls) {
                        System.out.println(methodCall.getScope());
                        String calledFunctionName = methodCall.getNameAsString();
                        // 然后在该类中查找方法声明
                        String classname = methodCall.getScope().get().getClass().getSimpleName();


                        List<MethodDeclaration> methods = findMethodDeclaration(projectAST, classname, methodCall.getNameAsString());
                        for(MethodDeclaration startMethod : methods) {
                            visit(startMethod, calledFunctionName);
                        }// 递归遍历调用链
                    }
                }
            }
        };

        // 在每个CompilationUnit上应用AST遍历器
        //for (CompilationUnit cu : projectAST) {
            // 使用辅助方法查找函数定义并开始遍历
            List<MethodDeclaration> methodDeclarations = findallMethodDeclaration(projectAST, startFunctionName);
            if (methodDeclarations != null) {
                for(MethodDeclaration startMethod : methodDeclarations) {
                    visitor.visit(startMethod, startFunctionName);
                }
            }
        //}
        return callChain;
    }

    // 辅助方法：根据函数名查找函数定义
    private static List<MethodDeclaration> findMethodDeclaration(List<CompilationUnit> projectAST, String className, String methodName) {

        List<MethodDeclaration> methods = new ArrayList<>();

        for (CompilationUnit cu : projectAST) {
            cu.accept(new VoidVisitorAdapter<Void>() {
                @Override
                public void visit(MethodDeclaration methodDeclaration, Void arg) {
                    // 检查方法名和所在类名是否匹配
                    if (methodDeclaration.getNameAsString().equals(methodName) && isMethodInClass(methodDeclaration, className)) {
                        methods.add(methodDeclaration);
                    }
                    super.visit(methodDeclaration, arg);
                }
            }, null);
        }

        return methods;

    }

    private static boolean isMethodInClass(MethodDeclaration methodDeclaration, String className) {
        // 获取方法所在的类名
        Optional<ClassOrInterfaceDeclaration> containingClass = methodDeclaration.findAncestor(ClassOrInterfaceDeclaration.class);
        if (containingClass.isPresent()) {
            String classSimpleName = containingClass.get().getNameAsString();
            if (classSimpleName.equals(className)) {
                return true;
            }


        }
        return false;
    }
    private static List<MethodDeclaration> findallMethodDeclaration(List<CompilationUnit> projectAST, String functionName) {
        List<MethodDeclaration> methodDeclarations= new ArrayList<>();
        for (CompilationUnit cu : projectAST) {
            for (MethodDeclaration methodDeclaration : cu.findAll(MethodDeclaration.class)) {
                if (methodDeclaration.getNameAsString().equals(functionName)) {
                    methodDeclarations.add(methodDeclaration);
                }
            }
        }
        return methodDeclarations;
    }

    private static List<CompilationUnit> buildProjectAST(String projectRoot) {
        List<CompilationUnit> projectAST = new ArrayList<>();
        File projectDir = new File(projectRoot);

        // 遍历项目根目录下的所有Java源代码文件
        for (File file : listJavaFiles(projectDir)) {
            try {
                FileInputStream in = new FileInputStream(file);
                JavaParser javaParser = new JavaParser();
                ParseResult<CompilationUnit> parseResult = javaParser.parse(in);
                CompilationUnit cu = parseResult.getResult().orElse(null);

                if (cu != null) {
                    projectAST.add(cu);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return projectAST;
    }

    private static List<File> listJavaFiles(File directory) {
        List<File> javaFiles = new ArrayList<>();
        File[] files = directory.listFiles();

        if (files != null) {
            for (File file : files) {
                if (file.isDirectory()) {
                    // 递归遍历子目录
                    javaFiles.addAll(listJavaFiles(file));
                } else if (file.getName().endsWith(".java")) {
                    // 找到Java源代码文件
                    javaFiles.add(file);
                }
            }
        }

        return javaFiles;
    }
}