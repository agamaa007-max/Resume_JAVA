package com.blindhire.server;

import com.blindhire.config.AppConfig;
import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.concurrent.Executors;

/**
 * HttpServerManager initializes and manages the built-in Java HTTP Server.
 */
public class HttpServerManager {

    private HttpServer server;

    public void start() throws IOException {
        int port = AppConfig.getServerPort();
        server = HttpServer.create(new InetSocketAddress(port), 0);

        // Bind all routes through RequestRouter
        server.createContext("/", new RequestRouter());

        // Thread pool for handling concurrent requests
        server.setExecutor(Executors.newFixedThreadPool(12));
        server.start();

        System.out.println("==========================================================");
        System.out.println("  BlindHire Server running at: http://localhost:" + port);
        System.out.println("==========================================================");

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("\n[SERVER] Stopping BlindHire server...");
            if (server != null) {
                server.stop(1);
            }
            System.out.println("[SERVER] BlindHire server stopped cleanly.");
        }));
    }

    public void stop() {
        if (server != null) {
            server.stop(0);
        }
    }
}
