package com.plasticaudit.socket;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.*;
import java.net.Socket;

/**
 * CO4 — Java Socket-based alert client.
 * Simulates a regulator terminal connecting to the AlertServer.
 * Can be used to test real-time notifications.
 */
@Component
public class AlertClient {

    private static final Logger log = LoggerFactory.getLogger(AlertClient.class);

    @Value("${alert.socket.port:9090}")
    private int port;

    /**
     * CO4 — Connects to AlertServer, sends a message, and reads responses.
     */
    public String sendAlertCommand(String command) {
        StringBuilder response = new StringBuilder();
        try (Socket socket = new Socket("localhost", port);
                PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
                BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {

            // Read welcome message
            String welcome = in.readLine();
            log.info("[AlertClient] Connected: {}", welcome);
            response.append(welcome).append("\n");

            // Send command
            out.println(command);

            // Read ACK
            socket.setSoTimeout(3000);
            String ack = in.readLine();
            if (ack != null) {
                response.append(ack).append("\n");
                log.info("[AlertClient] ACK: {}", ack);
            }

        } catch (Exception e) {
            log.warn("[AlertClient] Could not connect to AlertServer: {}", e.getMessage());
            response.append("AlertServer not available");
        }
        return response.toString();
    }
}
