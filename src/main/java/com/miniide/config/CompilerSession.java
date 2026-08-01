package com.miniide.config;

import java.io.BufferedWriter;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class CompilerSession {

    private Process process;

    private BufferedWriter writer;

    private final BlockingQueue<String> inputQueue =
            new LinkedBlockingQueue<>();

    private final StringBuilder consoleOutput =
            new StringBuilder();

    public Process getProcess() {
        return process;
    }

    public void setProcess(Process process) {
        this.process = process;
    }

    public BufferedWriter getWriter() {
        return writer;
    }

    public void setWriter(BufferedWriter writer) {
        this.writer = writer;
    }

    public BlockingQueue<String> getInputQueue() {
        return inputQueue;
    }

    public StringBuilder getConsoleOutput() {
        return consoleOutput;
    }

}