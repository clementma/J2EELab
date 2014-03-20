package omt;

import java.io.IOException;
import java.lang.ProcessBuilder.Redirect;
import java.util.ArrayList;

import javax.ejb.EJB;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import java.rmi.RemoteException;
import java.security.*;

import javax.crypto.*;

import omt.Account;
import omt.AccountBean;

// Servlet implementation class ATM
@WebServlet("/ATM")
public class ATM extends HttpServlet {
	
	private static final long serialVersionUID = 1L;
	public Integer count = 0;
	public String user;

	@EJB
	private Account acc;
       
    // @see HttpServlet#HttpServlet()
    public ATM() {
        super();
    }
    @SuppressWarnings("unchecked")
	// @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		HttpSession session = request.getSession();
		
		//not actually doing anything with this portion of code right now -- was playing with user identification
		synchronized(session){
			/*
			Integer accessCount = (Integer) session.getAttribute("accessCount");
			if (accessCount == null){
				accessCount = new Integer(0);
			} else {
				accessCount = new Integer(accessCount.intValue() + 1);
			}
			*/
		   }//end synchronized session
		
		//begin program logic
		
		//cookie code checks cookies, if no cookies exist adds repeatvisitor and userCookie cookies 
		
		boolean newbie = true;
		Cookie[] cookies = request.getCookies();
		if (cookies != null) {
			for (int i = 0; i < cookies.length; i++) {
				Cookie c = cookies[i];
				if ((c.getName().equals("repeatVisitor"))
						&& (c.getValue().equals("yes"))) {
					newbie = false;
					//break;
				}
				//checking and setting user
				if (c.getName().equals("userCookie"))  {
					System.out.println("UserCookie present, set user to cookievalue");
					session.setAttribute("userId", c.getValue());
					user = session.getAttribute("userId").toString();				
				}
			}
		}		
		
		if (newbie) {
			Cookie returnVisitorCookie = new Cookie("repeatVisitor", "yes");
			returnVisitorCookie.setMaxAge(60*60*24*365);
			response.addCookie(returnVisitorCookie);
			
			String strCount = count.toString(); //count starts at 0
			Cookie userCookie = new Cookie("userCookie", strCount);
			userCookie.setMaxAge(60*60*24*365);
			response.addCookie(userCookie);
			user = strCount;
			System.out.println("UserCookie not present, value set to " + strCount);
			count++;
			//acc.zeroBal();
			}		
		//end cookie code
		
		ArrayList<String> transactions = (ArrayList<String>) session.getAttribute("transactions");
		generateKey();
		
		if (transactions==null){
			transactions = new ArrayList<String>();
			session.setAttribute("transactions", transactions);
		} else {
			
			if (request.getParameter("Deposit") != null) {
				
				String amount = (String) request.getParameter("amount");
				byte[] encAmount = amount.getBytes();
				//encryption here -- encrypt amount pulled from form
				System.out.println("**********************Depositing " + amount);
				//int deposit = Integer.parseInt(amount);
				//acc.deposit(deposit);
				
				sendTransaction(user,encAmount, true);				
				transactions.add("Deposit " + amount);
				
			} else if (request.getParameter("Withdraw") != null) {
				String amount = (String) request.getParameter("amount");
				byte[] encAmount = amount.getBytes();
				//encryption here
				System.out.println("**********************Withdrawing " + amount);
				//int withdraw = Integer.parseInt(amount);
				//acc.withdrawal(withdraw);
				
				sendTransaction(user, encAmount, false);
				transactions.add("Withdraw " + amount);
			} 
		}		
		
		//show balance called here, we receive encrypted byte stream from server
		//need to decrypt then pass to AccountBean
		if(user == null)
			user = "0";
		byte [] receivedBal = acc.showBalance(user);
		float decryptBal = receiveBalance(receivedBal);		
		
		AccountBean bean = new AccountBean(decryptBal);
		session.setAttribute("bean", bean);
		//request.setAttribute("bean", bean); //not sure we're actually using this call in our JSP pages...
		
		if (request.getParameter("Balance") != null) {
			
			System.out.println("**********************Show Balance");
			response.sendRedirect("Balance.jsp");
			
		} else if (request.getParameter("Logout")!= null){
			
			System.out.println("**********************Show Transactions");
			response.sendRedirect("Transactions.jsp");
			
		} else {
			//first time you open the page since balance == null, we get sent here which redirects to ATM.jsp
			RequestDispatcher dispatcher = getServletContext().getRequestDispatcher("/ATM.jsp");
			dispatcher.forward(request,  response);
				
		}
	}//end doGet
		
 

	// @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		doGet(request, response);
	}
	
	//begin encryption code
	
	private  KeyPairGenerator keyGen;
	private  KeyPair clientKey;
	private  PublicKey serverKey;
	
	public void generateKey(){
		try{
			
			keyGen = KeyPairGenerator.getInstance("RSA");
			keyGen.initialize(1024);
			clientKey = (KeyPair) keyGen.generateKeyPair();
			System.out.println("I made a client key");
			
		} catch (NoSuchAlgorithmException noe) {

		}

		try {
			serverKey = acc.returnPublicKey(clientKey.getPublic());
			System.out.println("I made a server key");
		}
		catch(RuntimeException e){
			
		}
		catch(Exception e){
			
		}
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
			cipher.init(Cipher.ENCRYPT_MODE, clientKey.getPrivate());
			return cipher.doFinal(bt);
		}
		catch(Exception ex)
		{
		 
		}
		return null;
	}
	
	public void sendTransaction(String userIndex ,byte[] cipher, boolean b)
	{
		byte[] cryptoText = new byte[0];

		// encrypt the message
		cryptoText = cipherByteStream(cipher);
		try
		{
			System.out.println("Encrypting");
			acc.receiveTransaction(userIndex, cryptoText, b);
			System.out.println("Sent");
		}
		catch (Exception e)
		{

		}
	}
	
	public float receiveBalance(byte[] cryptoText) {
		String message = "";

		message = DecipherByteStream(cryptoText);
		float msg = Float.parseFloat(message);
		return msg;

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
				cipher.init(Cipher.DECRYPT_MODE, serverKey);
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
}
