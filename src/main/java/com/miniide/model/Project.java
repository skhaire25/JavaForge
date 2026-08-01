package com.miniide.model;

import java.util.List;

public class Project {

    private int projectId;
    private String projectName;

    private List<PackageInfo> packages;

    public Project() {
        super();
    }

    public Project(int projectId, String projectName) {
        super();
        this.projectId = projectId;
        this.projectName = projectName;
    }

    @Override
    public String toString() {
        return "\n Project Id: " + projectId +
               "\n Project Name: " + projectName + "\n";
    }

    public int getProjectId() {
        return projectId;
    }

    public void setProjectId(int projectId) {
        this.projectId = projectId;
    }

    public String getProjectName() {
        return projectName;
    }

    public void setProjectName(String projectName) {
        this.projectName = projectName;
    }

    public List<PackageInfo> getPackages() {
        return packages;
    }

    public void setPackages(List<PackageInfo> packages) {
        this.packages = packages;
    }
}