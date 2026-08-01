package com.miniide.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.miniide.dao.ProjectDAO;
import com.miniide.daoimpl.ProjectDAOImpl;
import com.miniide.model.Project;
import com.miniide.util.InputValidator;

@Service
public class ProjectService {

    private ProjectDAO dao = new ProjectDAOImpl();

    @Autowired
    private PackageService packageService;

    public List<Project> displayAllProjects(String guestId) {

        List<Project> projects = dao.displayAllProjects(guestId);

        for (Project project : projects) {
        	project.setPackages(
        		    packageService.displayAllPackages(
        		        guestId,
        		        project.getProjectId()));
        }

        return projects;
    }

    public void addProject(String guestId, Project project) {

        if (!InputValidator.validProjectName(project.getProjectName())) {
            throw new IllegalArgumentException("Invalid project name.");
        }

        dao.addProject(guestId, project);
    }

    public void deleteProject(String guestId, int projectId) {

        dao.deleteProject(guestId, projectId);

    }

    public List<Project> searchProject(String guestId,
            String projectName) {

    	List<Project> projects =
    			dao.searchProject(guestId, projectName);

    	for (Project project : projects) {
    		project.setPackages(
    			    packageService.displayAllPackages(
    			        guestId,
    			        project.getProjectId()));
    	}

    	return projects;
    }
    
    public void renameProject(String guestId,
            int projectId,
            String newProjectName) {

    	if (!InputValidator.validProjectName(newProjectName)) {
    		throw new IllegalArgumentException("Invalid project name.");
    	}

    	dao.renameProject(guestId, projectId, newProjectName);
    }
    
    
}