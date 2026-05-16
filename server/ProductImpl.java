package server;

import common.Product;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

public class ProductImpl extends UnicastRemoteObject implements Product{
	private static final long serialVersionUID = 1L;
	// Attributes of product
	private String name;
	private String description;
	private double price;

	public ProductImpl(String name, String description, double price) throws RemoteException {
        super(); // UnicastRemoteObject constructor — exports the object automatically
        this.name        = name;
        this.description = description;
        this.price       = price;
    }
	public String getName() throws RemoteException{
		return this.name;
	}
	public String getDescription() throws RemoteException{
		return this.description;
	}
	public double getPrice() throws RemoteException{
		return this.price;
	}
	public void changeProductName(String newName) throws RemoteException{
		this.name = newName;
	}
}