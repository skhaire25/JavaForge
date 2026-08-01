package com.miniide.dao;

import java.util.List;

import com.miniide.model.PackageInfo;

public interface PackageDAO {

    void addPackage(String guestId, PackageInfo p);

    void deletePackage(String guestId, int packageId);

    List<PackageInfo> searchPackage(
            String guestId,
            int projectId,
            String packageName);

    List<PackageInfo> displayAllPackages(
            String guestId,
            int projectId);

    void renamePackage(
            String guestId,
            int packageId,
            String newPackageName);

    PackageInfo getPackageById(
            String guestId,
            int packageId);

}