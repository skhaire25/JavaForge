package com.miniide.daoimpl;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.miniide.dao.PackageDAO;
import com.miniide.util.DuplicateNameException;
import com.miniide.model.JavaFile;
import com.miniide.model.PackageInfo;
import com.miniide.config.MemoryDatabase;

public class PackageDAOImpl implements PackageDAO {

    @Override
    public void addPackage(String guestId, PackageInfo p) {

    	List<PackageInfo> packages = MemoryDatabase.getPackages(guestId);

    	for (PackageInfo pkg : packages) {

            if (pkg.getProjectId() == p.getProjectId()
                    && pkg.getPackageName().equalsIgnoreCase(p.getPackageName())) {

                throw new DuplicateNameException("Package already exists");
            }
        }

        p.setPackageId(MemoryDatabase.nextPackageId++);
        packages.add(p);
    }

    @Override
    public void deletePackage(String guestId, int packageId) {

        Iterator<JavaFile> fileIterator = MemoryDatabase.getJavaFiles(guestId).iterator();

        while (fileIterator.hasNext()) {

            JavaFile file = fileIterator.next();

            if (file.getPackageId() == packageId) {
                fileIterator.remove();
            }
        }

        Iterator<PackageInfo> packageIterator = MemoryDatabase.getPackages(guestId).iterator();

        while (packageIterator.hasNext()) {

            PackageInfo pkg = packageIterator.next();

            if (pkg.getPackageId() == packageId) {

                packageIterator.remove();
                return;
            }
        }

        throw new RuntimeException("Package not found");
    }

    @Override
    public List<PackageInfo> searchPackage(String guestId, int projectId, String packageName){

        List<PackageInfo> list = new ArrayList<>();

        for (PackageInfo pkg : MemoryDatabase.getPackages(guestId)) {

            if (pkg.getProjectId() == projectId
                    && pkg.getPackageName().toLowerCase()
                            .startsWith(packageName.toLowerCase())) {

                list.add(pkg);
            }
        }

        return list;
    }

    @Override
    public List<PackageInfo> displayAllPackages(String guestId, int projectId) {

        List<PackageInfo> list = new ArrayList<>();

        for (PackageInfo pkg : MemoryDatabase.getPackages(guestId)) {

            if (pkg.getProjectId() == projectId) {
                list.add(pkg);
            }
        }

        return list;
    }

    @Override
    public void renamePackage(String guestId, int packageId, String newPackageName) {

        PackageInfo packageInfo = null;

        for (PackageInfo pkg : MemoryDatabase.getPackages(guestId)) {

            if (pkg.getPackageId() == packageId) {
                packageInfo = pkg;
                break;
            }
        }

        if (packageInfo == null) {
            throw new RuntimeException("Package not found");
        }

        for (PackageInfo pkg : MemoryDatabase.getPackages(guestId)) {

            if (pkg.getProjectId() == packageInfo.getProjectId()
                    && pkg.getPackageName().equalsIgnoreCase(newPackageName)
                    && pkg.getPackageId() != packageId) {

                throw new DuplicateNameException("Package already exists");
            }
        }

        String oldPackageName = packageInfo.getPackageName();

        packageInfo.setPackageName(newPackageName);

        for (JavaFile file : MemoryDatabase.getJavaFiles(guestId)) {

            if (file.getPackageId() == packageId
                    && file.getSourceCode() != null) {

                file.setSourceCode(
                        file.getSourceCode().replace(
                                "package " + oldPackageName + ";",
                                "package " + newPackageName + ";"));
            }
        }
    }

    @Override
    public PackageInfo getPackageById(String guestId, int packageId) {

    	for (PackageInfo pkg : MemoryDatabase.getPackages(guestId)) {

            if (pkg.getPackageId() == packageId) {
                return pkg;
            }
        }

        return null;
    }

}