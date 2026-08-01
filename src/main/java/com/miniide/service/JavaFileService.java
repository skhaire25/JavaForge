package com.miniide.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.miniide.daoimpl.JavaFileDAOImpl;
import com.miniide.model.JavaFile;
import com.miniide.util.InputValidator;

@Service
public class JavaFileService {

    private JavaFileDAOImpl dao = new JavaFileDAOImpl();

    public List<JavaFile> displayAllJavaFiles(
            String guestId,
            int packageId) {

        return dao.displayAllJavaFiles(
                guestId,
                packageId);
    }

    public JavaFile getJavaFileById(
            String guestId,
            int fileId) {

        return dao.getJavaFileById(
                guestId,
                fileId);
    }

    public void updateSourceCode(
            String guestId,
            int fileId,
            String sourceCode) {

        dao.updateSourceCode(
                guestId,
                fileId,
                sourceCode);
    }

    public void addJavaFile(
            String guestId,
            JavaFile file) {

        String className =
                file.getFileName().replace(".java", "");

        if (!InputValidator.validJavaFileName(className)) {
            throw new IllegalArgumentException("Invalid Java file name.");
        }

        dao.addJavaFile(
                guestId,
                file);
    }

    public void renameJavaFile(
            String guestId,
            int fileId,
            String newFileName) {

        if (!InputValidator.validJavaFileName(newFileName)) {
            throw new IllegalArgumentException("Invalid Java file name.");
        }

        dao.renameJavaFile(
                guestId,
                fileId,
                newFileName);
    }

    public void deleteJavaFile(
            String guestId,
            int fileId) {

        dao.deleteJavaFile(
                guestId,
                fileId);
    }

}