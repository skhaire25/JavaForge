package com.miniide.daoimpl;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.miniide.dao.JavaFileDAO;
import com.miniide.util.DuplicateNameException;
import com.miniide.model.JavaFile;
import com.miniide.config.MemoryDatabase;

public class JavaFileDAOImpl implements JavaFileDAO {

    @Override
    public void addJavaFile(String guestId, JavaFile j) {

    	List<JavaFile> javaFiles =
    	        MemoryDatabase.getJavaFiles(guestId);

    	for (JavaFile file : javaFiles) {

            if (file.getPackageId() == j.getPackageId()
                    && file.getFileName().equalsIgnoreCase(j.getFileName())) {

                throw new DuplicateNameException("Java File already exists");
            }
        }

        j.setFileId(MemoryDatabase.nextJavaFileId++);
        javaFiles.add(j);
    }

    @Override
    public void deleteJavaFile(String guestId, int fileId) {

        Iterator<JavaFile> iterator = MemoryDatabase.getJavaFiles(guestId).iterator();

        while (iterator.hasNext()) {

            JavaFile file = iterator.next();

            if (file.getFileId() == fileId) {

                iterator.remove();
                return;
            }
        }

        throw new RuntimeException("Java File not found");
    }

    @Override
    public List<JavaFile> searchJavaFile(
            String guestId,
            int packageId,
            String fileName) {

        List<JavaFile> list = new ArrayList<>();

        for (JavaFile file :
            MemoryDatabase.getJavaFiles(guestId)) {

            if (file.getPackageId() == packageId
                    && file.getFileName().toLowerCase()
                            .startsWith(fileName.toLowerCase())) {

                list.add(file);
            }
        }

        return list;
    }

    @Override
    public List<JavaFile> displayAllJavaFiles(
            String guestId,
            int packageId) {

        List<JavaFile> list = new ArrayList<>();

        for (JavaFile file :
            MemoryDatabase.getJavaFiles(guestId)) {

            if (file.getPackageId() == packageId) {
                list.add(file);
            }
        }

        return list;
    }

    @Override
    public void updateSourceCode(
            String guestId,
            int fileId,
            String sourceCode) {

    	for (JavaFile file :
            MemoryDatabase.getJavaFiles(guestId)) {

            if (file.getFileId() == fileId) {

                file.setSourceCode(sourceCode);
                return;
            }
        }

        throw new RuntimeException("Java File not found");
    }

    @Override
    public void renameJavaFile(
            String guestId,
            int fileId,
            String newFileName) {

        JavaFile target = null;

        for (JavaFile file : MemoryDatabase.getJavaFiles(guestId)) {

            if (file.getFileId() == fileId) {
                target = file;
                break;
            }
        }

        if (target == null) {
            throw new RuntimeException("Java File not found");
        }

        String newFile = newFileName + ".java";

        for (JavaFile file : MemoryDatabase.getJavaFiles(guestId)) {

            if (file.getPackageId() == target.getPackageId()
                    && file.getFileName().equalsIgnoreCase(newFile)
                    && file.getFileId() != fileId) {

                throw new DuplicateNameException("Java File already exists");
            }
        }

        String oldClass = target.getFileName().replace(".java", "");
        String sourceCode = target.getSourceCode();

        if (sourceCode != null) {

            sourceCode = sourceCode.replaceFirst(
                    "class\\s+" + oldClass + "\\b",
                    "class " + newFileName);

            sourceCode = sourceCode.replaceFirst(
                    "interface\\s+" + oldClass + "\\b",
                    "interface " + newFileName);

            sourceCode = sourceCode.replaceFirst(
                    "enum\\s+" + oldClass + "\\b",
                    "enum " + newFileName);

            sourceCode = sourceCode.replaceFirst(
                    "record\\s+" + oldClass + "\\b",
                    "record " + newFileName);

            sourceCode = sourceCode.replaceFirst(
                    "@interface\\s+" + oldClass + "\\b",
                    "@interface " + newFileName);

        }

        target.setFileName(newFile);
        target.setSourceCode(sourceCode);
    }

    @Override
    public JavaFile getJavaFileById(
            String guestId,
            int fileId) {

    	for (JavaFile file :
            MemoryDatabase.getJavaFiles(guestId)) {

            if (file.getFileId() == fileId) {
                return file;
            }
        }

        return null;
    }

}