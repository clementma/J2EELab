package omt;

import java.rmi.RemoteException;
import java.security.PublicKey;

import javax.ejb.Local;

@Local
public interface Account {
	public void withdrawal(String userIndex, int amount);
	public void deposit(String userIndex, int amount);
	public byte[] showBalance(String userIndex);
	public void zeroBal();
	//public Boolean authenticate(String pass) throws RemoteException;
	//public PublicKey returnPublicKey(PublicKey clientKey) throws RemoteException;
	PublicKey returnPublicKey(PublicKey clientKey) throws RemoteException;
	void receiveTransaction(String userIndex, byte[] cryptoText, boolean isDeposit) throws RemoteException;
}
