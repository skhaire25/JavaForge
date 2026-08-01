package com.miniide.dao;

import java.util.List;

import com.miniide.model.Project;

public interface ProjectDAO {

    void addProject(String guestId, Project p);

    void deleteProject(String guestId, int projectId);

    List<Project> searchProject(String guestId, String projectName);

    List<Project> displayAllProjects(String guestId);

    void renameProject(String guestId,
                       int projectId,
                       String newProjectName);

    void displayProjectTree(String guestId,
                            int projectId);

}