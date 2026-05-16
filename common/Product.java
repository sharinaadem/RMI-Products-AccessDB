package common;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface Product extends Remote {
    String getName() throws RemoteException;
    String getDescription() throws RemoteException;
    double getPrice() throws RemoteException;
    void changeProductName(String newName) throws RemoteException;
}