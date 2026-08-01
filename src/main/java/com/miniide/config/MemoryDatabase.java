package com.miniide.config;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.miniide.model.JavaFile;
import com.miniide.model.PackageInfo;
import com.miniide.model.Project;

public class MemoryDatabase {

	public static final Map<String, List<Project>> projects =
	        new HashMap<>();
	public static final Map<String, List<PackageInfo>> packages =
	        new HashMap<>();
	public static final Map<String, List<JavaFile>> javaFiles =
	        new HashMap<>();

    public static int nextProjectId = 1;
    public static int nextPackageId = 1;
    public static int nextJavaFileId = 1;
    
    public static List<Project> getProjects(String guestId){

        return projects.computeIfAbsent(
                guestId,
                k -> new ArrayList<>());

    }

    public static List<PackageInfo> getPackages(String guestId){

        return packages.computeIfAbsent(
                guestId,
                k -> new ArrayList<>());

    }

    public static List<JavaFile> getJavaFiles(String guestId){

        return javaFiles.computeIfAbsent(
                guestId,
                k -> new ArrayList<>());

    }

}


