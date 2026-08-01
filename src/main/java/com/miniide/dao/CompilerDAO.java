package com.miniide.dao;

import java.util.List;

import com.miniide.model.JavaFile;
import com.miniide.model.PackageInfo;

public interface CompilerDAO {

    JavaFile getJavaFileById(
            String guestId,
            int fileId);

    PackageInfo getPackageById(
            String guestId,
            int packageId);

    List<PackageInfo> getPackagesByProject(
            String guestId,
            int projectId);

    List<JavaFile> getJavaFilesByPackage(
            String guestId,
            int packageId);

}