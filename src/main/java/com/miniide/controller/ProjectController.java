package com.miniide.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import com.miniide.util.DuplicateNameException;
import com.miniide.model.Project;
import com.miniide.service.DownloadService;
import com.miniide.service.ProjectService;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/projects")
public class ProjectController {

    @Autowired
    private ProjectService service;

    @GetMapping
    public String projects(
            @RequestParam(required = false) String guestId,
            @RequestParam(required = false) String keyword,
            Model model) {

        if (guestId == null || guestId.isBlank()) {
            return "explorer";
        }

        if (keyword == null || keyword.trim().isEmpty()) {

            model.addAttribute(
                    "projects",
                    service.displayAllProjects(guestId));

        } else {

            model.addAttribute(
                    "projects",
                    service.searchProject(guestId, keyword));

        }

        model.addAttribute("keyword", keyword);

        return "explorer";
    }

    @PostMapping("/add")
    @ResponseBody
    public ResponseEntity<String> addProject(
            @RequestParam String guestId,
            @RequestParam String projectName) {

        try {

            Project project = new Project();

            project.setProjectName(projectName);

            service.addProject(guestId, project);

            return ResponseEntity.ok("success");

        } catch (DuplicateNameException e) {

            return ResponseEntity
                    .status(HttpStatus.CONFLICT)
                    .body(e.getMessage());
        }
    }

    @PostMapping("/save")
    public String saveProject(
            @RequestParam String guestId,
            Project project) {

    	service.addProject(guestId, project);

        return "redirect:/projects";
    }

    @PostMapping("/rename")
    @ResponseBody
    public ResponseEntity<String> renameProject(
            @RequestParam String guestId,
            @RequestParam int projectId,
            @RequestParam String newProjectName) {

        try {

        	service.renameProject(
        	        guestId,
        	        projectId,
        	        newProjectName);

            return ResponseEntity.ok("success");

        } catch (DuplicateNameException e) {

            return ResponseEntity
                    .status(HttpStatus.CONFLICT)
                    .body(e.getMessage());
        }
    }

    @PostMapping("/delete")
    @ResponseBody
    public String deleteProject(
            @RequestParam String guestId,
            @RequestParam int projectId){

    	service.deleteProject(
    	        guestId,
    	        projectId);

        return "success";
    }
    
    @GetMapping("/explorerSection")
    public String explorerSection(
            @RequestParam String guestId,
            Model model) {

    	model.addAttribute(
    	        "projects",
    	        service.displayAllProjects(guestId));

        return "explorerSection :: explorer";
    }
    
    @Autowired
    private DownloadService downloadService;
    
    @GetMapping("/download/{projectId}")
    public ResponseEntity<ByteArrayResource> downloadProject(
            @RequestParam String guestId,
            @PathVariable int projectId) throws IOException {

        return downloadService.downloadProject(
                guestId,
                projectId);

    }

}