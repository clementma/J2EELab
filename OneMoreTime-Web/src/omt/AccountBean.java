package omt;
public class AccountBean /*implements SessionBean*/{
	
	private float balance;
	
	public AccountBean(float balance) {
		super();
		this.balance = balance;
	}

	public float getBalance() {
		return balance;
	}

	public void setBalance(float balance) {
		this.balance = balance;
	}
/*
	@Override
	public void ejbActivate() throws EJBException, RemoteException {}

	@Override
	public void ejbPassivate() throws EJBException, RemoteException {}

	@Override
	public void ejbRemove() throws EJBException, RemoteException {}

	@Override
	public void setSessionContext(SessionContext arg0) throws EJBException, RemoteException {}
*/
}
