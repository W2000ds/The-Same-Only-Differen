package org.example;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ParseResult;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class findfunctiontile {

    public static void main(String[] args) {
        // 定义CSV文件路径
        String csvFile = "/home/lhy/JavaCode/paerserdemo/src/main/java/org/example/hadoopdiag.csv"; // 你的CSV文件路径

        List<CSVRecord> csvRecords = readCsvFile(csvFile);

        // 实例化JavaParser
        JavaParser javaParser = new JavaParser();

        for (CSVRecord record : csvRecords) {
            try {
                // 使用JavaParser解析Java文件
                FileInputStream inputStream = new FileInputStream(record.getFilePath());
                ParseResult<CompilationUnit> parseResult = javaParser.parse(inputStream);

                // 检查解析是否成功
                if (parseResult.isSuccessful()) {
                    CompilationUnit cu = parseResult.getResult().get();
                    MethodVisitor methodVisitor = new MethodVisitor(record);
                    methodVisitor.visit(cu, null);
                } else {
                    System.out.println("Parsing failed for file " + record.getFilePath() + ": " + parseResult.getProblems());
                }

                // 关闭输入流
                inputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private static List<CSVRecord> readCsvFile(String csvFile) {
        List<CSVRecord> records = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(csvFile))) {
            String line;
            while ((line = br.readLine()) != null) {
                // 拆分每行数据
                String[] parts = line.split(","); // 使用逗号分割
                if (parts.length == 6) {
                    records.add(new CSVRecord(parts[0], parts[3], parts[4], parts[5]));
                } else {
                    System.err.println("Invalid line: " + line);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return records;
    }

    private static class CSVRecord {
        private String paramName;
        private String packageName;
        private String methodName;
        private String filePath;

        public CSVRecord(String paramName, String packageName, String methodName, String filePath) {
            this.paramName = paramName;
            this.packageName = packageName;
            this.methodName = methodName;
            this.filePath = filePath;
        }

        public String getParamName() {
            return paramName;
        }

        public String getPackageName() {
            return packageName;
        }

        public String getMethodName() {
            return methodName;
        }

        public String getFilePath() {
            return filePath;
        }
    }

    private static class MethodVisitor extends VoidVisitorAdapter<Void> {
        private CSVRecord record;
        private PrintWriter writer;

        public MethodVisitor(CSVRecord record) throws IOException {
            this.record = record;
            writer = new PrintWriter(new FileWriter("MethodSignature.txt", true));
        }

        @Override
        public void visit(ClassOrInterfaceDeclaration n, Void arg) {
            super.visit(n, arg);
            String className = n.getFullyQualifiedName().orElse("");
            if (className.equals(record.getPackageName())) {
                for (MethodDeclaration method : n.getMethods()) {
                    if (method.getNameAsString().equals(record.getMethodName())) {
                        String output = getMethodSignature(record.getParamName(), className, method);
                        System.out.println(record.getParamName() + "+" + className + ":" + method.getNameAsString());
                        writer.println(output);
                        writer.flush();
                    }
                }
            }
        }

        private String getMethodSignature(String paramName, String className, MethodDeclaration method) {
            StringBuilder signature = new StringBuilder();
            signature.append(paramName).append(": ");
            signature.append("<").append(className).append(": ");
            signature.append(method.getType().asString()).append(" ");
            signature.append(method.getNameAsString()).append("(");
            method.getParameters().forEach(param -> {
                signature.append(param.getType().asString()).append(",");
            });
            if (!method.getParameters().isEmpty()) {
                signature.setLength(signature.length() - 1); // 去掉最后一个逗号
            }
            signature.append(")>");
            return signature.toString();
        }
    }
}
