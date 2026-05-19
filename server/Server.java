
package server;

import common.ProductRecord;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.List;

/**
 * Server
 * ─────────────────────────────────────────────────────────────
 * DATA PIPELINE:  products.accdb  →  AccessDBLoader  →  Server  →  Client
 *
 * Steps:
 *   1. Read products from MS Access using AccessDBLoader.
 *   2. For each product in the DB, create a ProductImpl remote object.
 *   3. Bind each remote object into the RMI registry.
 *   4. Client can now look them up and call methods remotely.
 */
public class Server {

     // Path to the MS Access DB (relative to where the program is run)
     private static final String DB_FILE = "server/products.accdb";

    // RMI registry host and port
    private static final String HOST = "127.0.0.1";
    private static final int    PORT = 9100;

    public static void main(String[] args) {

        try {
			System.out.println("\n RMI SERVER STARTING...");

            // ── Step 1: Set RMI hostname ─────────────────────────────
            System.setProperty("java.rmi.server.hostname", HOST);

            // ── Step 2: Load product data from MS Access ──────────────
            System.out.println("\n[Server] Reading data from MS Access: " + DB_FILE);
            List<ProductRecord> productDataList = AccessDBLoader.load(DB_FILE);

            // ── Step 3: Get (or create) the RMI Registry ─────────────
            // Use createRegistry() to start one, or getRegistry() if already running.
            Registry registry;
            try {
                registry = LocateRegistry.createRegistry(PORT);
                System.out.println("\n[Server] RMI Registry created on port " + PORT);
            } catch (Exception e) {
                registry = LocateRegistry.getRegistry(HOST, PORT);
                System.out.println("\n[Server] Connected to existing RMI Registry on port " + PORT);
            }

            // ── Step 4: Create ProductImpl objects from DB data and bind them ──
            System.out.println("\n[Server] Registering products in RMI Registry...");

            for (ProductRecord data : productDataList) {

                // Create the remote object
                ProductImpl productImpl = new ProductImpl(data.getName(), data.getDescription(), data.getPrice());

                // Use the product name (lowercase) as the registry key
                String bindKey = data.getName().toLowerCase();

                // Bind the remote object into the registry
                registry.rebind(bindKey, productImpl);

                System.out.println("[Server] Bound -> \"" + bindKey + "\"");
            }

            System.out.println("\n[Server] All products registered. Server is ready.");
            System.out.println("[Server] Waiting for client requests...\n");

        } catch (Exception e) {
            System.err.println("[Server] ERROR: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
