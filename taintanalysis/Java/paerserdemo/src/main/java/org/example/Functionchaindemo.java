package org.example;
import com.github.javaparser.JavaParser;
import com.github.javaparser.ParseResult;
import com.github.javaparser.Position;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;

public class Functionchaindemo {


    public static void main(String[] args) {
        // 指定要查找的Java文件的路径
        String filePath = "/home/lhy/JavaCode/hadoop-3.3.5-src/hadoop-common-project/hadoop-common/src/main/java/org/apache/hadoop/security/ShellBasedUnixGroupsMapping.java";

        // 指定要查找的函数名称
        String targetFunctionName = "main";

        // 调用处理文件的方法
        processFile(filePath, targetFunctionName);
    }

    private static void processFile(String filePath, String targetFunctionName) {
        try {
            FileInputStream in = new FileInputStream(filePath);
            JavaParser javaParser = new JavaParser();
            // 解析Java源代码文件
            ParseResult<CompilationUnit> parseResult = javaParser.parse(in);

            CompilationUnit cu = parseResult.getResult().get();

            // 创建AST遍历器
            cu.accept(new VoidVisitorAdapter<Void>() {
                @Override
                public void visit(MethodDeclaration method, Void arg) {
                    // 查找指定函数
                    //if (method.getNameAsString().equals(targetFunctionName)) {
                        // 遍历函数内部的语句，查找函数调用
                        List<MethodCallExpr> methodCalls = method.findAll(MethodCallExpr.class);
                        NodeList<MethodCallExpr> methodCallNodeList = new NodeList<>(methodCalls);
                        for (MethodCallExpr methodCall : methodCallNodeList) {
                            Position callBegin = methodCall.getBegin().get();
                            int callLine = callBegin.line;
                            System.out.println("Function call in " + callLine + ": " + methodCall.getName());
                        }
                    //}
                    super.visit(method, arg);
                }
            }, null);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
