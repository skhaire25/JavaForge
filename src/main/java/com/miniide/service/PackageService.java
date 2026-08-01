package com.miniide.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.miniide.daoimpl.PackageDAOImpl;
import com.miniide.model.PackageInfo;
import com.miniide.util.InputValidator;

@Service
public class PackageService {

    private PackageDAOImpl dao = new PackageDAOImpl();

    @Autowired
    private JavaFileService javaFileService;

    public void addPackage(String guestId, PackageInfo pkg) {

        if (!InputValidator.validPackageName(pkg.getPackageName())) {
            throw new IllegalArgumentException("Invalid package name.");
        }

        dao.addPackage(guestId, pkg);

    }

    public List<PackageInfo> displayAllPackages(
            String guestId,
            int projectId) {

        List<PackageInfo> packages =
                dao.displayAllPackages(guestId, projectId);

        for (PackageInfo pkg : packages) {

            pkg.setJavaFiles(
                    javaFileService.displayAllJavaFiles(
                            guestId,
                            pkg.getPackageId()));

        }

        return packages;
    }

    public void deletePackage(
            String guestId,
            int packageId){

        dao.deletePackage(guestId, packageId);

    }

    public void renamePackage(
            String guestId,
            int packageId,
            String newPackageName) {

        if (!InputValidator.validPackageName(newPackageName)) {
            throw new IllegalArgumentException("Invalid package name.");
        }

        dao.renamePackage(
                guestId,
                packageId,
                newPackageName);
    }

    public List<PackageInfo> searchPackage(
            String guestId,
            int projectId,
            String packageName) {

        return dao.searchPackage(
                guestId,
                projectId,
                packageName);

    }
    
    public PackageInfo getPackageById(
            String guestId,
            int packageId){

        return dao.getPackageById(
                guestId,
                packageId);

    }
}