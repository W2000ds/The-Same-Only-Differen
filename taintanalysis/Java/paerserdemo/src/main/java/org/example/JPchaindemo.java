package org.example;

import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;

public class JPchaindemo extends VoidVisitorAdapter<Void> {

    private String targetFunctionName = "main";

    public void FunctionCallVisitor(String targetFunctionName) {
        this.targetFunctionName = targetFunctionName;
    }

    @Override
    public void visit(MethodCallExpr callExpr, Void arg) {
        if (callExpr.getNameAsString().equals(targetFunctionName)) {
            // 找到目标函数的调用,向上递归追踪调用链
            traceCallChain(callExpr);
        }
        super.visit(callExpr, arg);
    }

    private void traceCallChain(Node node) {
        // 递归遍历node的父节点,打印调用链信息
        if (node.getParentNode().isPresent()) {
            Node parent = node.getParentNode().get();
            System.out.println(parent + " calls " + targetFunctionName);
            traceCallChain(parent);
        }
    }

}
