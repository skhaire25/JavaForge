package com.miniide.service;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import javax.tools.JavaFileObject;
import javax.tools.Diagnostic;
import javax.tools.DiagnosticCollector;

import javax.tools.JavaCompiler;
import javax.tools.StandardJavaFileManager;
import javax.tools.ToolProvider;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import com.miniide.config.CompilerSession;
import com.miniide.controller.ConsoleWebSocketHandler;
import com.miniide.dao.CompilerDAO;
import com.miniide.daoimpl.CompilerDAOImpl;
import com.miniide.model.JavaFile;
import com.miniide.model.PackageInfo;

@Service
public class CompilerService {
	
	@Autowired
	@Lazy
	private ConsoleWebSocketHandler socketHandler;

    private CompilerDAO dao = new CompilerDAOImpl();
    
    private final ConcurrentHashMap<String, CompilerSession> sessions =
            new ConcurrentHashMap<>();

    public String exportProject(
            String guestId,
            int fileId) {

        StringBuilder output = new StringBuilder();

        try {

        	File workspace = new File("TempWorkspace", guestId);

            if (workspace.exists()) {
                deleteDirectory(workspace);
            }

            workspace.mkdirs();

            JavaFile currentFile =
                    dao.getJavaFileById(
                            guestId,
                            fileId);

            if (currentFile == null) {
                return "Java File Not Found!";
            }

            PackageInfo currentPackage =
                    dao.getPackageById(
                            guestId,
                            currentFile.getPackageId());

            if (currentPackage == null) {
                return "Package Not Found!";
            }

            int projectId = currentPackage.getProjectId();

            List<PackageInfo> packages =
                    dao.getPackagesByProject(
                            guestId,
                            projectId);

            for (PackageInfo pkg : packages) {

            	File packageFolder = new File(
            	        workspace,
            	        pkg.getPackageName().replace('.', File.separatorChar));

                packageFolder.mkdirs();

                List<JavaFile> files =
                        dao.getJavaFilesByPackage(
                                guestId,
                                pkg.getPackageId());

                for (JavaFile file : files) {

                    String fileName = file.getFileName();

                    if (!fileName.endsWith(".java")) {
                        fileName += ".java";
                    }

                    File javaFile = new File(packageFolder, fileName);

                    FileWriter writer = new FileWriter(javaFile);

                    writer.write(file.getSourceCode());

                    writer.close();

                }

            }

            boolean compiled =
                    compileProject(
                            guestId,
                            output);

            socketHandler.send(
                    guestId,
                    output.toString());

         System.out.println("Compiled = " + compiled);

         if (compiled) {

        	 runProject(guestId,
        		        fileId);

         }

        } catch (Exception e) {

            output.append(e.getMessage());

            try {

                socketHandler.send(
                        guestId,
                        output.toString());

            } catch (Exception ignored) {

            }

        }

        return output.toString();

    }

    private boolean compileProject(
            String guestId,
            StringBuilder output) {

    	File workspace = new File(
    	        "TempWorkspace",
    	        guestId);

        List<File> javaFiles = new ArrayList<>();

        getJavaFiles(workspace, javaFiles);

        if (javaFiles.isEmpty()) {

            output.append("No Java Files Found.");

            return false;

        }

        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();

        if (compiler == null) {

            output.append("JDK Not Found.");

            return false;

        }

        DiagnosticCollector<JavaFileObject> diagnostics =
                new DiagnosticCollector<>();

        StandardJavaFileManager fileManager =
                compiler.getStandardFileManager(
                        diagnostics,
                        null,
                        null);

        Iterable<? extends JavaFileObject> compilationUnits =
                fileManager.getJavaFileObjectsFromFiles(javaFiles);

        List<String> options = List.of(
                "-d",
                workspace.getAbsolutePath()
        );

        boolean success = compiler.getTask(
                null,
                fileManager,
                diagnostics,
                options,
                null,
                compilationUnits
        ).call();

        try {

            fileManager.close();

        } catch (IOException e) {

            output.append(e.getMessage());

        }

        if (success) {

            output.append("Compilation Successful!\n\n");

        } else {

            output.append("Compilation Failed!\n\n");

            for (Diagnostic<? extends JavaFileObject> diagnostic
                    : diagnostics.getDiagnostics()) {

                output.append("File : ")
                      .append(new File(diagnostic.getSource().toUri()).getName())
                      .append("\n");

                output.append("Line : ")
                      .append(diagnostic.getLineNumber())
                      .append("\n");

                output.append("Error: ")
                      .append(diagnostic.getMessage(null))
                      .append("\n\n");
            }

        }

        return success;

    }

    private void runProject(
            String guestId,
            int fileId) {
    	
    	CompilerSession session =
    	        sessions.computeIfAbsent(
    	                guestId,
    	                id -> new CompilerSession());

        System.out.println("runProject called");

        File workspace = new File(
                "TempWorkspace",
                guestId);

        JavaFile currentFile =
                dao.getJavaFileById(
                        guestId,
                        fileId);

        if (currentFile == null) {
        	session.getConsoleOutput().append("Java File Not Found.\n");
            System.out.println("Java File Not Found");
            return;
        }

        PackageInfo currentPackage =
                dao.getPackageById(
                        guestId,
                        currentFile.getPackageId());

        if (currentPackage == null) {
        	session.getConsoleOutput().append("Package Not Found.\n");
            System.out.println("Package Not Found");
            return;
        }

        String className = currentFile.getFileName();

        if (className.endsWith(".java")) {
            className = className.substring(0, className.length() - 5);
        }

        String mainClass = currentPackage.getPackageName() + "." + className;

        System.out.println("Main Class = " + mainClass);
        System.out.println("Workspace = " + workspace.getAbsolutePath());

        try {

            ProcessBuilder pb = new ProcessBuilder(
                    "java",
                    "-cp",
                    workspace.getAbsolutePath(),
                    mainClass);

            pb.redirectErrorStream(true);

            session.getConsoleOutput().setLength(0);
            
            session.getInputQueue().clear();

            session.setProcess(pb.start());
            
            session.setWriter(
            	    new BufferedWriter(
            	        new OutputStreamWriter(
            	            session.getProcess().getOutputStream(),
            	            StandardCharsets.UTF_8)));
            
            Thread inputThread = new Thread(() -> {

                try {

                	while (session.getProcess().isAlive()) {

                		String input = session.getInputQueue().take();

                        System.out.println("PROCESS <- " + input);

                        session.getWriter().write(input);
                        session.getWriter().newLine();
                        session.getWriter().flush();

                    }

                } catch (Exception e) {

                    e.printStackTrace();

                }

            });

            inputThread.setDaemon(true);

            inputThread.start();

            System.out.println("Process Started");

            session.getConsoleOutput().append("Process Started\n");

            Thread readerThread = new Thread(() -> {

                try (InputStreamReader reader =
                	    new InputStreamReader(
                	    		session.getProcess().getInputStream(),
                	            StandardCharsets.UTF_8);) {

                    System.out.println("Reader Thread Started");

                    int ch;

                    while ((ch = reader.read()) != -1) {

                        char c = (char) ch;

                        session.getConsoleOutput().append(c);

                        System.out.print(c);
                        
                        socketHandler.send(
                                guestId,
                                String.valueOf(c));

                    }

                    int exitCode = session.getProcess().waitFor();

                    System.out.println();
                    System.out.println("Process Finished. Exit Code = " + exitCode);

                } catch (Exception e) {

                    e.printStackTrace();

                }

            });

            readerThread.setDaemon(true);
            readerThread.start();

        } catch (Exception e) {

            e.printStackTrace();

            session.getConsoleOutput()
            .append(e.getMessage())
            .append("\n");

        }
    }

    private String findMainClass(File folder, String packageName) {

        File[] files = folder.listFiles();

        if (files == null)
            return null;

        for (File file : files) {

            if (file.isDirectory()) {

                String pkg = packageName.isEmpty()
                        ? file.getName()
                        : packageName + "." + file.getName();

                String result = findMainClass(file, pkg);

                if (result != null)
                    return result;

            } else if (file.getName().endsWith(".java")) {

                try {

                    String source = Files.readString(file.toPath());

                    if (source.contains("public static void main")) {

                        String className =
                                file.getName().replace(".java", "");

                        if (packageName.isEmpty()) {

                            return className;

                        }

                        return packageName + "." + className;

                    }

                } catch (IOException e) {

                    e.printStackTrace();

                }

            }

        }

        return null;

    }

    private void getJavaFiles(File folder, List<File> javaFiles) {

        File[] files = folder.listFiles();

        if (files == null)
            return;

        for (File file : files) {

            if (file.isDirectory()) {

                getJavaFiles(file, javaFiles);

            } else if (file.getName().endsWith(".java")) {

                javaFiles.add(file);

            }

        }

    }

    private void deleteDirectory(File file) {

        if (file.isDirectory()) {

            File[] files = file.listFiles();

            if (files != null) {

                for (File f : files) {

                    deleteDirectory(f);

                }

            }

        }

        file.delete();

    }
    
    public String getConsoleOutput(String guestId) {

        CompilerSession session =
                sessions.computeIfAbsent(
                        guestId,
                        id -> new CompilerSession());

        return session.getConsoleOutput().toString();

    }

    public void clearConsole(String guestId) {

        CompilerSession session =
                sessions.computeIfAbsent(
                        guestId,
                        id -> new CompilerSession());

        session.getConsoleOutput().setLength(0);

    }

    public void sendInput(
            String guestId,
            String input) {

        CompilerSession session =
                sessions.computeIfAbsent(
                        guestId,
                        id -> new CompilerSession());

        System.out.println("QUEUE <- " + input);

        session.getInputQueue().offer(input);

    }

}