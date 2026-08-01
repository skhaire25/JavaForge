package com.miniide.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.miniide.service.CompilerService;

@RestController
@RequestMapping("/console")
public class ConsoleController {

    @Autowired
    private CompilerService service;

    @GetMapping("/output")
    public String output(
            @RequestParam String guestId) {

        return service.getConsoleOutput(guestId);

    }

    @PostMapping("/input")
    public String input(
            @RequestParam String guestId,
            @RequestParam String text) {

        service.sendInput(
                guestId,
                text);

        return "OK";

    }

}