
package server;

import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.time.Duration;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Server
 * ─────────────────────────────────────────────────────────────
 * DATA PIPELINE:  products.xml  →  XmlProductLoader  →  Server  →  Client
 *                                └──────────────→  Access DB
 *
 * Steps:
 *   1. Read products from XML using XmlProductLoader.
 *   2. Persist products into MS Access.
 *   3. For each product, create a ProductImpl remote object.
 *   4. Bind each remote object into the RMI registry.
 *   5. Client can now look them up and call methods remotely.
 */
public class Server {

    // Path to the MS Access DB (relative to where the program is run)
    private static final String DB_FILE = "server/products.accdb";
    private static final String XML_FILE = "server/products.xml";

    // RMI registry host and port
    private static final String HOST = "127.0.0.1";
    private static final int    PORT = 9100;

    private static final Duration RELOAD_DEBOUNCE = Duration.ofMillis(300);

    public static void main(String[] args) {

        try {
			System.out.println("\n RMI SERVER STARTING...");

            // ── Step 1: Set RMI hostname ─────────────────────────────
            System.setProperty("java.rmi.server.hostname", HOST);

            // ── Step 2: Get (or create) the RMI Registry ─────────────
            // Use createRegistry() to start one, or getRegistry() if already running.
            Registry registry;
            try {
                registry = LocateRegistry.createRegistry(PORT);
                System.out.println("\n[Server] RMI Registry created on port " + PORT);
            } catch (Exception e) {
                registry = LocateRegistry.getRegistry(HOST, PORT);
                System.out.println("\n[Server] Connected to existing RMI Registry on port " + PORT);
            }

            // ── Step 3: Initial load + bind ───────────────────────────
            Set<String> boundKeys = new HashSet<>();
            reloadFromXml(registry, boundKeys);

            System.out.println("\n[Server] All products registered. Server is ready.");
            System.out.println("[Server] Watching XML for changes...\n");

            // ── Step 4: Watch XML file and hot-reload ─────────────────
            watchXmlAndReload(registry, boundKeys);

        } catch (Exception e) {
            System.err.println("[Server] ERROR: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static void reloadFromXml(Registry registry, Set<String> boundKeys) throws Exception {
        // Load product data from XML
        System.out.println("\n[Server] Reading data from XML: " + XML_FILE);
        List<XmlProductLoader.ProductData> productDataList = XmlProductLoader.load(XML_FILE);

        // Persist XML data to MS Access
        System.out.println("\n[Server] Writing data to MS Access: " + DB_FILE);
        AccessDBSaver.saveProducts(DB_FILE, productDataList, true);

        // Bind products into registry
        System.out.println("\n[Server] Registering products in RMI Registry...");
        Set<String> newKeys = new HashSet<>();

        for (XmlProductLoader.ProductData data : productDataList) {
            ProductImpl productImpl = new ProductImpl(data.name, data.description, data.price);
            String bindKey = data.name.toLowerCase();
            registry.rebind(bindKey, productImpl);
            newKeys.add(bindKey);
            System.out.println("[Server] Bound -> \"" + bindKey + "\"");
        }

        // Unbind removed products
        for (String oldKey : boundKeys) {
            if (!newKeys.contains(oldKey)) {
                try {
                    registry.unbind(oldKey);
                    System.out.println("[Server] Unbound -> \"" + oldKey + "\"");
                } catch (Exception e) {
                    System.out.println("[Server] Unbind failed for \"" + oldKey + "\": " + e.getMessage());
                }
            }
        }

        boundKeys.clear();
        boundKeys.addAll(newKeys);
    }

    private static void watchXmlAndReload(Registry registry, Set<String> boundKeys) throws Exception {
        Path xmlPath = Path.of(XML_FILE).toAbsolutePath().normalize();
        Path dir = xmlPath.getParent();

        try (WatchService watchService = FileSystems.getDefault().newWatchService()) {
            dir.register(watchService, StandardWatchEventKinds.ENTRY_CREATE, StandardWatchEventKinds.ENTRY_MODIFY);

            long lastReload = 0L;
            while (true) {
                WatchKey key = watchService.take();

                for (WatchEvent<?> event : key.pollEvents()) {
                    WatchEvent.Kind<?> kind = event.kind();
                    if (kind == StandardWatchEventKinds.OVERFLOW) {
                        continue;
                    }

                    Path changed = (Path) event.context();
                    if (!xmlPath.getFileName().equals(changed)) {
                        continue;
                    }

                    long now = System.currentTimeMillis();
                    if (now - lastReload < RELOAD_DEBOUNCE.toMillis()) {
                        continue;
                    }

                    lastReload = now;
                    System.out.println("\n[Server] XML change detected. Reloading...");
                    reloadFromXml(registry, boundKeys);
                    System.out.println("[Server] Reload complete.\n");
                }

                boolean valid = key.reset();
                if (!valid) {
                    System.out.println("[Server] Watch key invalid; stopping watcher.");
                    break;
                }
            }
        }
    }
}
