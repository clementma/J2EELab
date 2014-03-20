package omt;
import java.io.*;
import java.rmi.RemoteException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PublicKey;
import java.util.ArrayList;
import java.util.List;

import javax.crypto.Cipher;
import javax.ejb.Singleton;


@Singleton(mappedName="AccountImpl")
public class AccountImpl implements Account {
	// private String password = "1234512345";
	
	private float[] amount = new float[]{0,0,0,0,0}; //array to store balances
	
	private  KeyPairGenerator keyGen;
	private  KeyPair serverKey;
	private  PublicKey clientKey;
	private BufferedWriter out = null;
	
	public AccountImpl() throws Exception { 
		super();
		keyGen = KeyPairGenerator.getInstance("RSA");
		keyGen.initialize(1024);
		serverKey = keyGen.generateKeyPair();
	}

	@Override
	public byte[] showBalance(String userIndex) { 
		//decrypt amount
		//return this.amount;
		int index = Integer.parseInt(userIndex);
		String strAmt = Float.toString(this.amount[index]);
		byte[] encBal = strAmt.getBytes();	
		byte[] sentBal = sendBalance(encBal);
		return sentBal;
		
	}

	@Override
	public void deposit(String userIndex, int amount)
	{
	        //decrypt amount here
			int index = Integer.parseInt(userIndex);
			this.amount[index] += amount;
			System.out.println("Balance: " + this.amount[index] );
			//re-encrypt amount
	        //return this.amount;
	}
	
	@Override
	public void withdrawal(String userIndex, int amount)
	{
	        
		int index = Integer.parseInt(userIndex);
		if(amount <= this.amount[index])
	        {
	        		//decrypt amount here	        		
	        		this.amount[index] -= amount;
	        		System.out.println("Balance: " + this.amount[index] );
	        		//re-encrypt amount
	                //return String.valueOf(this.amount);
	        }
	        else
	        {
	                //return "Not Enough Money";
	        }
	}
	
	public void zeroBal(){
		amount[0] = 0;
	}

	public PublicKey returnPublicKey(PublicKey clKey) throws RemoteException{
		clientKey = clKey;
		return serverKey.getPublic();
	}
	
	
	@Override
	public void receiveTransaction(String userIndex, byte[] cryptoText, boolean isDeposit) throws RemoteException{
 		String message = "";
 		try 
 		{ 			
 			message = DecipherByteStream(cryptoText);
 			int msg = java.lang.Integer.parseInt(message);
 			if(isDeposit){ 				
 				deposit(userIndex, msg);
 			}else{
 				withdrawal(userIndex, msg);
 			}
 			
 		} catch (Exception e)
 		{
 		
 		}
 		
	}
	
	public String DecipherByteStream(byte[] bt) 
	{
		if(serverKey == null){
			System.out.println("Returning server Null - Key Issue");
		}
		
		if(clientKey == null){
			System.out.println("Returning client Null - Key Issue");
		}
		
		if (serverKey != null && clientKey != null)
		{
			try{
				Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
				//encrypt with client private key (server can verify message is from client)
				cipher.init(Cipher.DECRYPT_MODE, clientKey);
		 		byte[] bty = cipher.doFinal(bt);
				return new String(bty);
			}
			catch(Exception e)
			{
				//JOptionPane.showMessageDialog(null, e.getClass().getName());
			}
		}
		//System.out.println("Returning Null - Key Issue");
		return null;
	}
	
	public byte[] cipherByteStream(byte[] bt) 
	{
		try
		{
			// get the cipher
			Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
			// turn string digest into byte hash
			//byte[] hash = digest.getBytes("UTF8");
			//encrypt with client private key (server can verify message is from client)
			cipher.init(Cipher.ENCRYPT_MODE, serverKey.getPrivate());
			return cipher.doFinal(bt);
		}
		catch(Exception ex)
		{
		 
		}
		return null;
	}
	
	public byte[] sendBalance(byte[] cipher)
	{
		byte[] cryptoText = new byte[0];

		// encrypt the message
		cryptoText = cipherByteStream(cipher);
		return cryptoText;
	}


}