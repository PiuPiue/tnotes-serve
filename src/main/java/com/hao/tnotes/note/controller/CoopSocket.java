package com.hao.tnotes.note.controller;

import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class CoopSocket extends TextWebSocketHandler {

    private final Map<String, Map<String, WebSocketSession>> documentSessions = new ConcurrentHashMap<>();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        String noteId = session.getUri().getQuery().split("=")[1];
        String clientId = session.getId();

        documentSessions.computeIfAbsent(noteId, k -> new ConcurrentHashMap<>()).put(clientId, session);


        broadcastSystemMessage(noteId, clientId + " joined the session.");
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        String noteId = session.getUri().getQuery().split("=")[1];
        String clientId = session.getId();
        String payload = message.getPayload();



        broadcastTextMessage(noteId, payload, clientId);
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        String noteId = session.getUri().getQuery().split("=")[1];
        String clientId = session.getId();

        documentSessions.get(noteId).remove(clientId);

        broadcastSystemMessage(noteId, clientId + " left the session.");
    }

    private void broadcastTextMessage(String noteId, String message, String senderId) throws IOException {
        Map<String, WebSocketSession> sessions = documentSessions.get(noteId);
        if (sessions != null) {
            for (WebSocketSession session : sessions.values()) {
                if (session.isOpen() && !session.getId().equals(senderId)) {
                    session.sendMessage(new TextMessage(message));
                }
            }
        }
    }

    private void broadcastSystemMessage(String noteId, String message) throws IOException {
        Map<String, WebSocketSession> sessions = documentSessions.get(noteId);
        if (sessions != null) {
            for (WebSocketSession session : sessions.values()) {
                if (session.isOpen()) {
                    session.sendMessage(new TextMessage(message));
                }
            }
        }
    }
}