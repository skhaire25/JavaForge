package com.miniide.daoimpl;

import java.util.ArrayList;
import java.util.List;

import com.miniide.dao.CompilerDAO;
import com.miniide.model.JavaFile;
import com.miniide.model.PackageInfo;
import com.miniide.config.MemoryDatabase;

public class CompilerDAOImpl implements CompilerDAO {

    @Override
    public JavaFile getJavaFileById(
            String guestId,
            int fileId) {
    	
    	System.out.println("Searching File ID = " + fileId);

    	for (JavaFile file : MemoryDatabase.getJavaFiles(guestId)) {

            if (file.getFileId() == fileId) {
                return file;
            }
        }
        
        System.out.println("File Not Found");

        return null;
    }

    @Override
    public PackageInfo getPackageById(
            String guestId,
            int packageId) {

    	for (PackageInfo pkg : MemoryDatabase.getPackages(guestId)) {

            if (pkg.getPackageId() == packageId) {
                return pkg;
            }
        }

        return null;
    }

    @Override
    public List<PackageInfo> getPackagesByProject(
            String guestId,
            int projectId) {

        List<PackageInfo> list = new ArrayList<>();

        for (PackageInfo pkg : MemoryDatabase.getPackages(guestId)) {

            if (pkg.getProjectId() == projectId) {
                list.add(pkg);
            }
        }

        return list;
    }

    @Override
    public List<JavaFile> getJavaFilesByPackage(
            String guestId,
            int packageId) {

        List<JavaFile> list = new ArrayList<>();

        for (JavaFile file : MemoryDatabase.getJavaFiles(guestId)) {

            if (file.getPackageId() == packageId) {
                list.add(file);
            }
        }

        return list;
    }

}