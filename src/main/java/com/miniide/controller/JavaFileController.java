package com.miniide.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import com.miniide.util.DuplicateNameException;

import com.miniide.model.JavaFile;
import com.miniide.model.PackageInfo;
import com.miniide.service.CodeGenerator;
import com.miniide.service.JavaFileService;
import com.miniide.service.PackageService;

@Controller
@RequestMapping("/javafiles")
public class JavaFileController {
	
	@Autowired
	private JavaFileService javaFileService;

	@Autowired
	private PackageService packageService;

	@Autowired
	private CodeGenerator codeGenerator;
	
	@PostMapping("/add")
	@ResponseBody
	public ResponseEntity<String> addJavaFile(
	        @RequestParam String guestId,
	        @RequestParam int packageId,
	        @RequestParam String className,
	        @RequestParam boolean mainMethod) {

	    try {

	    	PackageInfo pkg =
	    	        packageService.getPackageById(
	    	                guestId,
	    	                packageId);

	        String source =
	                codeGenerator.generateClass(
	                        pkg.getPackageName(),
	                        className,
	                        mainMethod);

	        JavaFile file = new JavaFile();

	        file.setPackageId(packageId);
	        file.setFileName(className + ".java");
	        file.setSourceCode(source);

	        javaFileService.addJavaFile(
	                guestId,
	                file);

	        return ResponseEntity.ok("success");

	    } catch (DuplicateNameException e) {

	        return ResponseEntity
	                .status(HttpStatus.CONFLICT)
	                .body(e.getMessage());
	    }
	}
	
	@PostMapping("/addInterface")
	@ResponseBody
	public ResponseEntity<String> addInterface(
	        @RequestParam String guestId,
	        @RequestParam int packageId,
	        @RequestParam String interfaceName) {

	    try {

	    	PackageInfo pkg =
	    	        packageService.getPackageById(
	    	                guestId,
	    	                packageId);

	        String source =
	                codeGenerator.generateInterface(
	                        pkg.getPackageName(),
	                        interfaceName);

	        JavaFile file = new JavaFile();

	        file.setPackageId(packageId);
	        file.setFileName(interfaceName + ".java");
	        file.setSourceCode(source);

	        javaFileService.addJavaFile(
	                guestId,
	                file);

	        return ResponseEntity.ok("success");

	    } catch (DuplicateNameException e) {

	        return ResponseEntity
	                .status(HttpStatus.CONFLICT)
	                .body(e.getMessage());
	    }
	}
	
	@PostMapping("/addEnum")
	@ResponseBody
	public ResponseEntity<String> addEnum(
	        @RequestParam String guestId,
	        @RequestParam int packageId,
	        @RequestParam String enumName) {

	    try {

	        PackageInfo pkg =
	                packageService.getPackageById(
	                        guestId,
	                        packageId);

	        String source =
	                codeGenerator.generateEnum(
	                        pkg.getPackageName(),
	                        enumName);

	        JavaFile file = new JavaFile();

	        file.setPackageId(packageId);
	        file.setFileName(enumName + ".java");
	        file.setSourceCode(source);

	        javaFileService.addJavaFile(
	                guestId,
	                file);

	        return ResponseEntity.ok("success");

	    } catch (DuplicateNameException e) {

	        return ResponseEntity
	                .status(HttpStatus.CONFLICT)
	                .body(e.getMessage());
	    }
	}
	
	@PostMapping("/addRecord")
	@ResponseBody
	public ResponseEntity<String> addRecord(
	        @RequestParam String guestId,
	        @RequestParam int packageId,
	        @RequestParam String recordName) {

	    try {

	        PackageInfo pkg =
	                packageService.getPackageById(
	                        guestId,
	                        packageId);

	        String source =
	                codeGenerator.generateRecord(
	                        pkg.getPackageName(),
	                        recordName);

	        JavaFile file = new JavaFile();

	        file.setPackageId(packageId);
	        file.setFileName(recordName + ".java");
	        file.setSourceCode(source);

	        javaFileService.addJavaFile(
	                guestId,
	                file);

	        return ResponseEntity.ok("success");

	    } catch (DuplicateNameException e) {

	        return ResponseEntity
	                .status(HttpStatus.CONFLICT)
	                .body(e.getMessage());
	    }
	}
	
	@PostMapping("/addAnnotation")
	@ResponseBody
	public ResponseEntity<String> addAnnotation(
	        @RequestParam String guestId,
	        @RequestParam int packageId,
	        @RequestParam String annotationName) {

	    try {

	        PackageInfo pkg =
	                packageService.getPackageById(
	                        guestId,
	                        packageId);

	        String source =
	                codeGenerator.generateAnnotation(
	                        pkg.getPackageName(),
	                        annotationName);

	        JavaFile file = new JavaFile();

	        file.setPackageId(packageId);
	        file.setFileName(annotationName + ".java");
	        file.setSourceCode(source);

	        javaFileService.addJavaFile(
	                guestId,
	                file);

	        return ResponseEntity.ok("success");

	    } catch (DuplicateNameException e) {

	        return ResponseEntity
	                .status(HttpStatus.CONFLICT)
	                .body(e.getMessage());
	    }
	}
	
	@PostMapping("/rename")
	@ResponseBody
	public ResponseEntity<String> renameJavaFile(
	        @RequestParam String guestId,
	        @RequestParam int fileId,
	        @RequestParam String newFileName) {

	    try {

	    	javaFileService.renameJavaFile(
	    	        guestId,
	    	        fileId,
	    	        newFileName);

	        return ResponseEntity.ok("success");

	    } catch (DuplicateNameException e) {

	        return ResponseEntity
	                .status(HttpStatus.CONFLICT)
	                .body(e.getMessage());
	    }
	}
	
	@PostMapping("/delete")
	@ResponseBody
	public String deleteJavaFile(
	        @RequestParam String guestId,
	        @RequestParam int fileId) {

		javaFileService.deleteJavaFile(
		        guestId,
		        fileId);

	    return "success";
	}

}
