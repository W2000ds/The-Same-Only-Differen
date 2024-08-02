package org.example;
import com.github.javaparser.JavaParser;
import com.github.javaparser.ParseResult;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;

public class MethodCallDifferentiation {
    public static void main(String[] args) {
        // 指定项目根目录
        String projectRoot = "/home/lhy/JavaCode/hadoop-3.3.5-src/hadoop-hdfs-project";

        // 构建整个项目的AST
        List<CompilationUnit> projectAST = buildProjectAST(projectRoot);

        // 查找调用链
        findFunctionCallChain(projectAST);

        // 打印调用链

    }

    private static void findFunctionCallChain(List<CompilationUnit> projectAST) {

        for (CompilationUnit cu : projectAST) {

            // 打印文件名
            System.out.println("File: " + cu.getStorage().get().getPath());
                cu.accept(new VoidVisitorAdapter<Void>() {
                    @Override
                    public void visit(MethodDeclaration methodDeclaration, Void arg) {

                        System.out.print(methodDeclaration.getNameAsString() + ": ");
                        List<MethodCallExpr> methodCalls = methodDeclaration.findAll(MethodCallExpr.class);
                        for (MethodCallExpr methodCall : methodCalls) {
                            System.out.print(methodCall.getNameAsString() + " ");
                        }
                        super.visit(methodDeclaration, arg);
                        System.out.println();
                    }
                }, null);
            }
        }



    // 辅助方法：根据函数名查找函数定义




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
                cu.setStorage(Paths.get(file.getPath()));
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