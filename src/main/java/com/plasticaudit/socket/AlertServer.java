package com.plasticaudit.socket;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.*;
import java.net.*;
import java.util.concurrent.*;

/**
 * CO4 — Java ServerSocket-based real-time alert server.
 * Runs in a daemon thread on startup, accepts regulator client connections,
 * and broadcasts compliance alerts to all connected clients.
 *
 * Used to send live notifications when report aggregation completes or
 * when an industry breaches plastic waste thresholds.
 */
@Component
public class AlertServer {

    private static final Logger log = LoggerFactory.getLogger(AlertServer.class);

    @Value("${alert.socket.port:9090}")
    private int port;

    @Value("${alert.socket.enabled:true}")
    private boolean enabled;

    private ServerSocket serverSocket;
    private final CopyOnWriteArrayList<PrintWriter> connectedClients = new CopyOnWriteArrayList<>();
    private final ExecutorService pool = Executors.newCachedThreadPool(
            r -> {
                Thread t = new Thread(r, "AlertServer-Worker");
                t.setDaemon(true);
                return t;
            });

    /**
     * CO4 — Starts the ServerSocket listener in a daemon thread on application
     * startup.
     */
    @PostConstruct
    public void startServer() {
        if (!enabled) {
            log.info("[AlertServer] Socket server disabled via config.");
            return;
        }
        Thread serverThread = new Thread(() -> {
            try {
                serverSocket = new ServerSocket(port);
                log.info("[AlertServer] CO4 Socket server listening on port {}", port);

                while (!serverSocket.isClosed()) {
                    try {
                        Socket clientSocket = serverSocket.accept();
                        log.info("[AlertServer] New client connected: {}", clientSocket.getRemoteSocketAddress());
                        pool.submit(() -> handleClient(clientSocket));
                    } catch (SocketException se) {
                        if (!serverSocket.isClosed()) {
                            log.error("[AlertServer] Socket error", se);
                        }
                    }
                }
            } catch (IOException e) {
                log.warn("[AlertServer] Failed to start on port {}: {}", port, e.getMessage());
            }
        }, "AlertServer-Main");
        serverThread.setDaemon(true);
        serverThread.start();
    }

    /**
     * CO4 — Handles individual client connection in its own thread.
     */
    private void handleClient(Socket clientSocket) {
        try (PrintWriter writer = new PrintWriter(
                new BufferedWriter(new OutputStreamWriter(clientSocket.getOutputStream())), true);
                BufferedReader reader = new BufferedReader(
                        new InputStreamReader(clientSocket.getInputStream()))) {

            connectedClients.add(writer);
            writer.println("[CONNECTED] Plastic Waste Audit Alert Server v1.0 | SDG 12 & 14");

            String line;
            while ((line = reader.readLine()) != null) {
                log.debug("[AlertServer] Received from client: {}", line);
                // Echo acknowledgment back
                writer.println("[ACK] " + line);
            }
        } catch (IOException e) {
            log.debug("[AlertServer] Client disconnected: {}", e.getMessage());
        } finally {
            try {
                clientSocket.close();
            } catch (IOException ignored) {
            }
        }
    }

    /**
     * CO4 — Broadcasts an alert message to ALL connected regulator clients.
     */
    public void broadcastAlert(String message) {
        String alertMsg = "[ALERT] " + message;
        log.info("[AlertServer] Broadcasting to {} clients: {}", connectedClients.size(), alertMsg);
        connectedClients.removeIf(writer -> {
            try {
                writer.println(alertMsg);
                return writer.checkError(); // Remove disconnected clients
            } catch (Exception e) {
                return true;
            }
        });
    }

    @PreDestroy
    public void stopServer() {
        pool.shutdown();
        try {
            if (serverSocket != null && !serverSocket.isClosed()) {
                serverSocket.close();
            }
        } catch (IOException e) {
            log.error("[AlertServer] Error closing server socket", e);
        }
        log.info("[AlertServer] Shut down.");
    }

    public int getConnectedClientCount() {
        return connectedClients.size();
    }
}
