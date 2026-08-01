package com.miniide.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.miniide.service.CompilerService;

@RestController
@RequestMapping("/compiler")
public class CompilerController {

    @Autowired
    private CompilerService compilerService;

    @PostMapping("/run")
    public String run(
            @RequestParam String guestId,
            @RequestParam int fileId) {

    	compilerService.clearConsole(guestId);

        compilerService.exportProject(
                guestId,
                fileId);

        return "STARTED";

    }

}