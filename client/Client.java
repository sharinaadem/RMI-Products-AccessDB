package client;

import common.Product;
import common.ProductRecord;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.ArrayList;
import java.util.List;

public class Client {
	private static final String HOST = "127.0.0.1";
	private static final int PORT = 9100;
	private static final String DB_FILE = "server/products.accdb";

	public static void main(String[] args) {
		try {
			Registry registry = LocateRegistry.getRegistry(HOST, PORT);

			String[] names = registry.list();
			List<ProductRecord> products = new ArrayList<>();

			for (String name : names) {
				Product product = (Product) registry.lookup(name);
				products.add(new AccessDBSaver.ProductSnapshot(
						product.getName(),
						product.getDescription(),
						product.getPrice()));
			}

			AccessDBSaver.saveProducts(DB_FILE, products, true);
			System.out.println("[Client] Saved data to MS Access database.");

		} catch (Exception e) {
			System.out.println("Client side error..." + e);
		}
	}
}
