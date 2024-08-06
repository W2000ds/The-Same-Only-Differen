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

public class functionsearch {
    public static void main(String[] args) {

        String folderPath = "/pathtoprogram";

        String targetFunctionName = "test";
        //input the search fold and target function name.
        processFile(folderPat, targetFunctionName);
    }

    private static void processFile(String folderPat, String targetFunctionName) {
        
        List<JavaFileInfo> javaFiles = new ArrayList<>();
        listJavaFiles(new File(folderPath), javaFiles);

        JavaParser javaParser = new JavaParser();

        for (JavaFileInfo fileInfo : javaFiles) {
        try {
            FileInputStream inputStream = new FileInputStream(fileInfo.getFile());
            ParseResult<CompilationUnit> parseResult = javaParser.parse(inputStream);
            JavaParser javaParser = new JavaParser();
            CompilationUnit cu = parseResult.getResult().get();

            cu.accept(new VoidVisitorAdapter<Void>() {
                @Override
                public void visit(MethodDeclaration method, Void arg) {

                    if (method.getNameAsString().equals(targetFunctionName)) {
                        List<MethodCallExpr> methodCalls = method.findAll(MethodCallExpr.class);
                        NodeList<MethodCallExpr> methodCallNodeList = new NodeList<>(methodCalls);
                        for (MethodCallExpr methodCall : methodCallNodeList) {
                            Position callBegin = methodCall.getBegin().get();
                            int callLine = callBegin.line;
                            System.out.println("Function call in " + callLine + ": " + methodCall.getName()+ ": " + fileInfo.getName());
                        }
                    }
                    super.visit(method, arg);
                }
            }, null);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    }
}
