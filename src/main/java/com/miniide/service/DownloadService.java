package com.miniide.service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.miniide.model.JavaFile;
import com.miniide.model.PackageInfo;
import com.miniide.model.Project;

@Service
public class DownloadService {

    @Autowired
    private ProjectService projectService;

    public ResponseEntity<ByteArrayResource> downloadProject(
            String guestId,
            int projectId) throws IOException {

            List<Project> projects = projectService.displayAllProjects(guestId);

        Project project = null;

        for (Project p : projects) {

            if (p.getProjectId() == projectId) {

                project = p;
                break;

            }
        }

        if (project == null) {

            throw new RuntimeException("Project not found.");

        }

        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        ZipOutputStream zip = new ZipOutputStream(baos);

        String root = project.getProjectName() + "/";

        addText(zip,
                root + "pom.xml",
                generatePom(project.getProjectName()));

        addText(zip,
                root + ".gitignore",
                """
                target/
                *.class
                .idea/
                .vscode/
                """);

        addText(zip,
                root + "README.md",
                "# " + project.getProjectName()
                        + "\n\nGenerated using JavaForge");

        for (PackageInfo pkg : project.getPackages()) {

            String folder =
                    root
                    + "src/main/java/"
                    + pkg.getPackageName().replace(".", "/")
                    + "/";

            for (JavaFile file : pkg.getJavaFiles()) {

                addText(zip,
                        folder + file.getFileName(),
                        file.getSourceCode());

            }

        }

        zip.close();

        ByteArrayResource resource =
                new ByteArrayResource(baos.toByteArray());

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\""
                                + project.getProjectName()
                                + ".zip\"")
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .contentLength(resource.contentLength())
                .body(resource);

    }

    private void addText(
            ZipOutputStream zip,
            String path,
            String content)
            throws IOException {

        ZipEntry entry = new ZipEntry(path);

        zip.putNextEntry(entry);

        zip.write(content.getBytes(StandardCharsets.UTF_8));

        zip.closeEntry();

    }

    private String generatePom(String projectName) {

        return """
                <project xmlns="http://maven.apache.org/POM/4.0.0"
                         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
                         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 https://maven.apache.org/xsd/maven-4.0.0.xsd">

                    <modelVersion>4.0.0</modelVersion>

                    <groupId>com.javaforge</groupId>

                    <artifactId>%s</artifactId>

                    <version>1.0-SNAPSHOT</version>

                    <properties>
                        <maven.compiler.source>21</maven.compiler.source>
                        <maven.compiler.target>21</maven.compiler.target>
                    </properties>

                </project>
                """.formatted(projectName);

    }

}