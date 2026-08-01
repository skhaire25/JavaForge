package com.miniide.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.miniide.model.JavaFile;
import com.miniide.service.JavaFileService;

@RestController
@RequestMapping("/editor")
public class EditorController {

    @Autowired
    private JavaFileService service;

    @GetMapping("/{id}")
    public JavaFile openFile(
            @RequestParam String guestId,
            @PathVariable int id) {

        return service.getJavaFileById(
                guestId,
                id);

    }
    
    @PostMapping("/save")
    @ResponseBody
    public String saveFile(
            @RequestParam String guestId,
            @RequestParam int fileId,
            @RequestParam String sourceCode) {

        service.updateSourceCode(
                guestId,
                fileId,
                sourceCode);

        return "Saved";
    }
}