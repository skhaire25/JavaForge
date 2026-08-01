package com.miniide.dao;

import java.util.List;

import com.miniide.model.JavaFile;

public interface JavaFileDAO {

    void addJavaFile(String guestId, JavaFile j);

    void deleteJavaFile(String guestId, int fileId);

    List<JavaFile> searchJavaFile(
            String guestId,
            int packageId,
            String fileName);

    List<JavaFile> displayAllJavaFiles(
            String guestId,
            int packageId);

    void updateSourceCode(
            String guestId,
            int fileId,
            String sourceCode);

    void renameJavaFile(
            String guestId,
            int fileId,
            String newFileName);

    JavaFile getJavaFileById(
            String guestId,
            int fileId);

}