package main.java.co.sridhar.spring3.domain;

import java.util.concurrent.Callable;

import main.java.co.sridhar.spring3.utils.SMTPMXLookup;

public class Email implements Callable<Email> {
	
	private String emailId;
	
	private String domain;
	
	private Boolean isValid = false;
	
	public Boolean getIsValid() {
		return isValid;
	}

	public void setIsValid(Boolean isValid) {
		this.isValid = isValid;
	}

	@Override
	public String toString(){
		return this.emailId;
	}
	
	public Email(String emailId, String domain){
		this.emailId = emailId;
		this.domain = domain;
	}
	
	@Override
	public Email call(){
		try{
			this.setIsValid(SMTPMXLookup.isAddressValid(this.emailId, this.domain));
		}catch(Exception ex){
			ex.printStackTrace();
		}
		return this;
	}

	public String getDomain() {
		return domain;
	}

	public void setDomain(String domain) {
		this.domain = domain;
	}

	public String getEmailId() {
		return emailId;
	}

	public void setEmailId(String emailId) {
		this.emailId = emailId;
	}	

}
