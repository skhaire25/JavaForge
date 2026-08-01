package com.miniide.model;

import java.util.List;

public class PackageInfo {
	private int packageId;
    private String packageName;
    private int projectId;
    private List<JavaFile> javaFiles;

    public PackageInfo() {
        super();
    }

    public PackageInfo(int packageId, String packageName, int projectId) {
        super();
        this.packageId = packageId;
        this.packageName = packageName;
        this.projectId = projectId;
    }

    public int getPackageId() {
        return packageId;
    }

    public void setPackageId(int packageId) {
        this.packageId = packageId;
    }

    public String getPackageName() {
        return packageName;
    }

    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }

    public int getProjectId() {
        return projectId;
    }

    public void setProjectId(int projectId) {
        this.projectId = projectId;
    }
    
    public List<JavaFile> getJavaFiles() {
        return javaFiles;
    }

    public void setJavaFiles(List<JavaFile> javaFiles) {
        this.javaFiles = javaFiles;
    }

    @Override
    public String toString() {
        return "\n Package ID: " + packageId + "\n Package Name: " + packageName + "\n Project ID: " + projectId + "\n";
    }

}
