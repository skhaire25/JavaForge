package com.miniide.daoimpl;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.miniide.dao.ProjectDAO;
import com.miniide.model.JavaFile;
import com.miniide.model.PackageInfo;
import com.miniide.model.Project;
import com.miniide.util.DuplicateNameException;
import com.miniide.config.MemoryDatabase;

public class ProjectDAOImpl implements ProjectDAO {

    @Override
    public void addProject(String guestId, Project p) {

    	List<Project> projects = MemoryDatabase.getProjects(guestId);

    	for (Project project : projects) {
            if (project.getProjectName().equalsIgnoreCase(p.getProjectName())) {
            	throw new DuplicateNameException("Project already exists");
            }
        }

        p.setProjectId(MemoryDatabase.nextProjectId++);
        projects.add(p);
    }

    @Override
    public void deleteProject(String guestId, int projectId) {

        Iterator<Project> projectIterator = MemoryDatabase.getProjects(guestId).iterator();

        while (projectIterator.hasNext()) {

            Project project = projectIterator.next();

            if (project.getProjectId() == projectId) {

                Iterator<PackageInfo> packageIterator = MemoryDatabase.getPackages(guestId).iterator();

                while (packageIterator.hasNext()) {

                    PackageInfo pkg = packageIterator.next();

                    if (pkg.getProjectId() == projectId) {

                        int packageId = pkg.getPackageId();

                        Iterator<JavaFile> fileIterator = MemoryDatabase.getJavaFiles(guestId).iterator();

                        while (fileIterator.hasNext()) {

                            JavaFile file = fileIterator.next();

                            if (file.getPackageId() == packageId) {
                                fileIterator.remove();
                            }
                        }

                        packageIterator.remove();
                    }
                }

                projectIterator.remove();
                return;
            }
        }

        throw new RuntimeException("Project not found");
    }

    @Override
    public List<Project> searchProject(
            String guestId,
            String projectName) {

        List<Project> list = new ArrayList<>();

        for(Project project : MemoryDatabase.getProjects(guestId)) {

            if (project.getProjectName().toLowerCase()
                    .startsWith(projectName.toLowerCase())) {

                list.add(project);
            }
        }

        return list;
    }

    @Override
    public List<Project> displayAllProjects(String guestId) {

    	return new ArrayList<>(
    	        MemoryDatabase.getProjects(guestId));

    }

    @Override
    public void renameProject(
            String guestId,
            int projectId,
            String newProjectName) {

        for (Project project : MemoryDatabase.getProjects(guestId)) {

            if (project.getProjectName().equalsIgnoreCase(newProjectName)
                    && project.getProjectId() != projectId) {

            	throw new DuplicateNameException("Project already exists");
            }
        }

        for (Project project : MemoryDatabase.getProjects(guestId)) {

            if (project.getProjectId() == projectId) {

                project.setProjectName(newProjectName);
                return;
            }
        }

        throw new RuntimeException("Project not found");
    }

    @Override
    public void displayProjectTree(
            String guestId,
            int projectId) {

        Project project = null;

        for (Project p : MemoryDatabase.getProjects(guestId)) {

            if (p.getProjectId() == projectId) {
                project = p;
                break;
            }
        }

        if (project == null) {
            throw new RuntimeException("Project not found");
        }

        project.setPackages(new ArrayList<>());

        for (PackageInfo pkg : MemoryDatabase.getPackages(guestId)) {

            if (pkg.getProjectId() == projectId) {

                pkg.setJavaFiles(new ArrayList<>());

                for (JavaFile file : MemoryDatabase.getJavaFiles(guestId)) {

                    if (file.getPackageId() == pkg.getPackageId()) {
                        pkg.getJavaFiles().add(file);
                    }
                }

                project.getPackages().add(pkg);
            }
        }
    }
}