package com.miniide.controller;

import java.util.concurrent.ConcurrentHashMap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import com.miniide.service.CompilerService;

@Component
public class ConsoleWebSocketHandler extends TextWebSocketHandler {

    @Autowired
    private CompilerService compilerService;

    private final ConcurrentHashMap<String, WebSocketSession> sessions =
            new ConcurrentHashMap<>();
    private final ConcurrentHashMap<WebSocketSession, String> guestIds =
            new ConcurrentHashMap<>();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {

        String query = session.getUri().getQuery();

        String guestId = "";

        if (query != null) {

            for (String param : query.split("&")) {

                if (param.startsWith("guestId=")) {

                    guestId = param.substring("guestId=".length());

                    break;

                }

            }

        }

        sessions.put(guestId, session);
        guestIds.put(session, guestId);

        System.out.println("WebSocket Connected : " + guestId);

    }

    @Override
    public void afterConnectionClosed(
            WebSocketSession session,
            CloseStatus status) {

        String guestId = guestIds.remove(session);

        if (guestId != null) {

            sessions.remove(guestId);

        }

    }

    @Override
    protected void handleTextMessage(
            WebSocketSession session,
            TextMessage message) throws Exception {

        String guestId = guestIds.get(session);

        if (guestId != null) {

            compilerService.sendInput(
                    guestId,
                    message.getPayload());

        }

    }

    public void send(
            String guestId,
            String text) throws Exception {

        WebSocketSession session =
                sessions.get(guestId);

        if (session != null && session.isOpen()) {

            session.sendMessage(new TextMessage(text));

        }

    }
}