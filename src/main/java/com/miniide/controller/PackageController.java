package com.miniide.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import com.miniide.util.DuplicateNameException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.miniide.model.PackageInfo;
import com.miniide.service.PackageService;

@Controller
@RequestMapping("/packages")
public class PackageController {

    @Autowired
    private PackageService packageService;

    @PostMapping("/add")
    @ResponseBody
    public ResponseEntity<String> addPackage(
            @RequestParam String guestId,
            @RequestParam int projectId,
            @RequestParam String packageName) {

        try {

            PackageInfo pkg = new PackageInfo();

            pkg.setProjectId(projectId);
            pkg.setPackageName(packageName);

            packageService.addPackage(guestId, pkg);

            return ResponseEntity.ok("success");

        } catch (DuplicateNameException e) {

            return ResponseEntity
                    .status(HttpStatus.CONFLICT)
                    .body(e.getMessage());
        }
    }

    @PostMapping("/rename")
    @ResponseBody
    public ResponseEntity<String> renamePackage(
            @RequestParam String guestId,
            @RequestParam int packageId,
            @RequestParam String newPackageName) {

        try {

        	packageService.renamePackage(
        	        guestId,
        	        packageId,
        	        newPackageName);

            return ResponseEntity.ok("success");

        } catch (DuplicateNameException e) {

            return ResponseEntity
                    .status(HttpStatus.CONFLICT)
                    .body(e.getMessage());
        }
    }
    
    @PostMapping("/delete")
    @ResponseBody
    public String deletePackage(
            @RequestParam String guestId,
            @RequestParam int packageId) {

    	packageService.deletePackage(
    	        guestId,
    	        packageId);

        return "success";
    }

}