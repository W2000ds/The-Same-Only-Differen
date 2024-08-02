package org.example;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ParseResult;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.stmt.*;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class JPdemo {



    public static void main(String[] args) {
        // 定义要解析的文件夹路径
        String folderPath = "/home/lhy/JavaCode/hadoop-3.3.5-src/";
        //String folderPath = "/home/lhy/JavaCode/paerserdemo/src/test/java/";
        List<JavaFileInfo> javaFiles = new ArrayList<>();
        listJavaFiles(new File(folderPath), javaFiles);

        // 实例化JavaParser
        JavaParser javaParser = new JavaParser();

        for (JavaFileInfo fileInfo : javaFiles) {
            try {
                // 使用JavaParser解析Java文件
                FileInputStream inputStream = new FileInputStream(fileInfo.getFile());
                ParseResult<CompilationUnit> parseResult = javaParser.parse(inputStream);

                // 检查解析是否成功
                if (parseResult.isSuccessful()) {
                    CompilationUnit cu = parseResult.getResult().get();

                    IfStatementVisitor ifStatementVisitor = new IfStatementVisitor(fileInfo);
                    ifStatementVisitor.visit(cu, null);

                } else {
                    System.out.println("Parsing failed for file " + fileInfo.getName() + ": " + parseResult.getProblems());
                }

                // 关闭输入流
                inputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }

    private static void listJavaFiles(File folder, List<JavaFileInfo> javaFiles) {
        File[] files = folder.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isFile() && file.getName().endsWith(".java")) {
                    javaFiles.add(new JavaFileInfo(file));
                } else if (file.isDirectory()) {
                    listJavaFiles(file, javaFiles);
                }
            }
        }
    }

    private static class JavaFileInfo {
        private File file;

        public JavaFileInfo(File file) {
            this.file = file;
        }

        public File getFile() {
            return file;
        }

        public String getName() {
            return file.getName();
        }

        public String getPath() {
            return file.getPath();
        }
    }

    private static class IfStatementVisitor extends VoidVisitorAdapter<Void> {
        private JavaFileInfo fileInfo;
        private PrintWriter writer;
        public IfStatementVisitor(JavaFileInfo fileInfo) throws IOException {
            this.fileInfo = fileInfo;
            writer = new PrintWriter(new FileWriter("result.txt", true));
        }



        @Override
        public void visit(IfStmt ifStmt, Void arg) {
            super.visit(ifStmt, arg);

            // 获取 if 结构的开始行号和结束行号
            int startLine = ifStmt.getRange().get().begin.line;
            int endLine = ifStmt.getRange().get().end.line;
            String output = fileInfo.getPath()+" (if) " + startLine + " " + endLine ;
            writer.println(output);
            writer.close();
        }

        @Override
        public void visit(TryStmt tryStmt, Void arg) {
            super.visit(tryStmt, arg);
            // 获取 try-catch 结构的开始行号和结束行号
            int startLine = tryStmt.getRange().get().begin.line;
            int endLine = tryStmt.getRange().get().end.line;
            String output = fileInfo.getPath() + " (trycatch) " + startLine + " " + endLine;
            writer.println(output);
            writer.close();
        }

        @Override
        public void visit(WhileStmt whileStmt, Void arg) {
            super.visit(whileStmt, arg);
            // 获取 while 结构的开始行号和结束行号
            int startLine = whileStmt.getRange().get().begin.line;
            int endLine = whileStmt.getRange().get().end.line;
            String output = fileInfo.getPath()+" (while) " + startLine + " " + endLine;
            writer.println(output);
            writer.close();
        }

        @Override
        public void visit(ForStmt forStmt, Void arg) {
            super.visit(forStmt, arg);
            // 获取 for 结构的开始行号和结束行号
            int startLine = forStmt.getRange().get().begin.line;
            int endLine = forStmt.getRange().get().end.line;
            String output = fileInfo.getPath()+" (for) " + startLine + " " + endLine;
            writer.println(output);
            writer.close();
        }
        @Override
        public void visit(DoStmt doStmt, Void arg) {
            super.visit(doStmt, arg);
            // 获取 dowhile 结构的开始行号和结束行号
            int startLine = doStmt.getRange().get().begin.line;
            int endLine = doStmt.getRange().get().end.line;
            String output = fileInfo.getPath()+" (dowhile) " + startLine + " " + endLine;
            writer.println(output);
            writer.close();
        }
        @Override
        public void visit(SwitchStmt switchStmt, Void arg) {
            super.visit(switchStmt, arg);
            // 获取 switch 结构的开始行号和结束行号
            int startLine = switchStmt.getRange().get().begin.line;
            int endLine = switchStmt.getRange().get().end.line;
            String output = fileInfo.getPath()+" (switch) " + startLine + " " + endLine;
            writer.println(output);
            writer.close();
        }



    }
    }

