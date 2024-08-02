package org.example;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ParseResult;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.body.VariableDeclarator;
import com.github.javaparser.ast.expr.BinaryExpr;
import com.github.javaparser.ast.expr.NameExpr;
import com.github.javaparser.ast.expr.StringLiteralExpr;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class printAST {

    private static Map<String, String> variableValues = new HashMap<>();

    public static void main(String[] args) {
        variableValues.put("YARN_PREFIX", "yarn."); // Preset values for known variables
        String filePath = "/home/lhy/JavaCode/hadoop-3.3.5-src/hadoop-mapreduce-project/hadoop-mapreduce-client/hadoop-mapreduce-client-app/src/main/java/org/apache/hadoop/mapreduce/v2/app/MRAppMaster.java"; // Adjust this path to your Java source file
        extractStringVariables(filePath);
    }

    private static void extractStringVariables(String filePath) {
        try {
            FileInputStream in = new FileInputStream(filePath);
            JavaParser javaParser = new JavaParser();
            ParseResult<CompilationUnit> parseResult = javaParser.parse(in);

            if (parseResult.getResult().isPresent()) {
                CompilationUnit cu = parseResult.getResult().get();

                cu.accept(new VoidVisitorAdapter<Void>() {
//                    @Override
//                    public void visit(. Void arg) {
//                        super.visit(n, arg);
//                        n.getVariables().forEach(var -> {
//                            if (var.getType().asString().equals("String")) {
//                                String varName = var.getNameAsString();
//                                String value = evaluateExpression(var.getInitializer().orElse(null));
//                                variableValues.put(varName, value);
//                                System.out.println(value + ":" + varName);
//                            }
//                        });
//                    }

                    private String evaluateExpression(com.github.javaparser.ast.Node node) {
                        if (node instanceof StringLiteralExpr) {
                            return ((StringLiteralExpr) node).asString();
                        } else if (node instanceof NameExpr) {
                            String name = ((NameExpr) node).getNameAsString();
                            return variableValues.getOrDefault(name, "undefined");
                        } else if (node instanceof BinaryExpr) {
                            BinaryExpr binaryExpr = (BinaryExpr) node;
                            return evaluateExpression(binaryExpr.getLeft()) + evaluateExpression(binaryExpr.getRight());
                        }
                        return "Not a valid expression";
                    }
                }, null);
            } else {
                System.out.println("No compilation unit found!");
            }
        } catch (IOException e) {
            System.err.println("An error occurred while trying to read the file: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
