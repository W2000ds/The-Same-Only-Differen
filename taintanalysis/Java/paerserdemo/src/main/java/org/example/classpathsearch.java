package org.example;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;

public class classpathsearch {
    public static void main(String[] args) throws MalformedURLException {
        String rootFolderPath = "/home/lhy/JavaCode/soottest"; // 修改为你的文件夹路径
        String packageName = "org.example";
        String className = "Main";

        searchFilePath(rootFolderPath, packageName, className);
    }

    private static void searchFilePath(String folderPath, String packageName, String className) throws MalformedURLException {
        File folder = new File(folderPath);
        if (folder.isDirectory()) {
            File[] files = folder.listFiles();
            if (files != null) {
                for (File file : files) {
                    if (file.isDirectory()) {
                        searchFilePath(file.getAbsolutePath(), packageName, className);
                    } else {
                        String fileName = file.getName();
                        String filePackage = file.getParent().replace(File.separator, ".");
                        String fqcn = filePackage + "." + fileName.substring(0, fileName.lastIndexOf('.'));

                        if (fqcn.equals(packageName + "." + className)) {
                            URL url = file.toURI().toURL();
                            String filePath = url.getFile();

                            System.out.println("Found file path: " + filePath);
                            return; // 如果找到，可以选择结束搜索
                        }
                    }
                }
            }
        }
    }
}
