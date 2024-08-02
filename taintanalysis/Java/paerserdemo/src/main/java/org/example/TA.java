package org.example;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ParseResult;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.expr.AssignExpr;
import com.github.javaparser.ast.expr.NameExpr;
import com.github.javaparser.ast.expr.VariableDeclarationExpr;
import com.github.javaparser.ast.nodeTypes.NodeWithVariables;
import com.github.javaparser.ast.stmt.ExpressionStmt;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;

public class TA {
    public static void main(String[] args) {
        try {
            FileInputStream in = new FileInputStream("/home/lhy/JavaCode/paerserdemo/src/test/java/Testdemo.java"); // 替换为你的 Java 源代码文件路径
            JavaParser javaParser = new JavaParser();
            ParseResult<CompilationUnit> parseResult = javaParser.parse(in);

            CompilationUnit cu = parseResult.getResult().get();

            cu.accept(new VoidVisitorAdapter<Void>() {

            @Override
            public void visit(MethodDeclaration method, Void arg) {
            super.visit(method, arg);

            // 遍历方法内的语句
            List<ExpressionStmt> statements = method.findAll(ExpressionStmt.class);
            for (ExpressionStmt stmt : statements) {
                // 查找赋值语句
                if (stmt.getExpression() instanceof AssignExpr ) {
                    AssignExpr assignment = (AssignExpr) stmt.getExpression();
                    if (assignment.getTarget() instanceof NameExpr) {
                        NameExpr target = (NameExpr) assignment.getTarget();

                        // 检查是否是你感兴趣的变量（例如，变量 a）
                        if ("a".equals(target.getNameAsString())) {
                            System.out.println("Found assignment to variable 'a' in method: " + method.getNameAsString());
                            System.out.println("Statement: " + stmt.toString());
                        }
                    }
                }

                if (stmt.getExpression() instanceof VariableDeclarationExpr){
                    String j = ((NodeWithVariables<VariableDeclarationExpr>) stmt).getVariable(0).getName().asString();
                }
            }
        }
            }, null);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


}
