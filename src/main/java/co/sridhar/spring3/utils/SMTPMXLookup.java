package main.java.co.sridhar.spring3.utils;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Hashtable;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.directory.DirContext;
import javax.naming.directory.InitialDirContext;

import main.java.co.sridhar.spring3.domain.Email;

import org.apache.commons.lang3.StringUtils;

public class SMTPMXLookup {
	
	private static int hear(BufferedReader in) throws IOException {
		String line = null;
		int responseCode = 0;
		while ((line = in.readLine()) != null) {
			String pfx = line.substring(0, 3);
			try {
				responseCode = Integer.parseInt(pfx);
			} catch (Exception ex) {
				responseCode = -1;
			}
			if (line.charAt(3) != '-'){
				break;
			}
		}
		return responseCode;
	}

	private static void say(BufferedWriter wr, String text) throws IOException {
		wr.write(text + "\r\n");
		wr.flush();
		return;
	}

	private static List<String> getMX(String hostName) throws NamingException {
		Hashtable<String,String> env = new Hashtable<String,String>();
		env.put("java.naming.factory.initial", "com.sun.jndi.dns.DnsContextFactory");		

		DirContext ictx = new InitialDirContext(env);
		Attributes attrs = ictx.getAttributes(hostName, new String[] { "MX" });
		Attribute attr = attrs.get("MX");

		// if we don't have an MX record, try the machine itself
		if ((attr == null) || (attr.size() == 0)) {
			attrs = ictx.getAttributes(hostName, new String[] { "A" });
			attr = attrs.get("A");
			if (attr == null){
				throw new NamingException("No match for name '" + hostName + "'");
			}
		}

		List<String> res = new ArrayList<String>();
		NamingEnumeration en = attr.getAll();

		while (en.hasMore()) {
			String mailhost;
			String x = (String) en.next();
			String f[] = x.split(" ");
			if (f.length == 1){
				mailhost = f[0];
			}else if (f[1].endsWith(".")){
				mailhost = f[1].substring(0, (f[1].length() - 1));
			}else{
				mailhost = f[1];
			}
			res.add(mailhost);
		}
		return res;
	}
	
	 public static boolean isAddressValid( String address, String domain ) {

	      // Isolate the domain/machine name and get a list of mail exchangers
	      List<String> mxServers = null;
	      try {
	         mxServers = getMX(domain);
	      } 
	      catch (NamingException ex) {
	         return false;
	      }

	      // Just because we can send mail to the domain, doesn't mean that the
	      // address is valid, but if we can't, it's a sure sign that it isn't
	      if ( mxServers.size() == 0 ) {
	    	  return false;
	      }

	      // Now, do the SMTP validation, try each mail exchanger until we get
	      // a positive acceptance. It *MAY* be possible for one MX to allow
	      // a message [store and forwarder for example] and another [like
	      // the actual mail server] to reject it. This is why we REALLY ought
	      // to take the preference into account.
	      for ( int mx = 0 ; mx < mxServers.size() ; mx++ ) {
	      String server = mxServers.get(mx);
	          boolean valid = false;
	          try {
	              int res;
	              Socket skt = new Socket(  server , 25 );
	              BufferedReader rdr = new BufferedReader
	                 ( new InputStreamReader( skt.getInputStream() ) );
	              BufferedWriter wtr = new BufferedWriter
	                 ( new OutputStreamWriter( skt.getOutputStream() ) );

	              res = hear( rdr );
	              if ( res != 220 ) {
	            	  throw new Exception( "Invalid header" );
	              }
	              say( wtr, "EHLO orbaker.com" );

	              res = hear( rdr );
	              if ( res != 250 ) {
	            	  throw new Exception( "Not ESMTP" );
	              }

	              // validate the sender address  
	              say( wtr, "MAIL FROM: <tim@orbaker.com>" );
	              res = hear( rdr );
	              if ( res != 250 ){
	            	  throw new Exception( "Sender rejected" );
	              }

	              say( wtr, "RCPT TO: <" + address + ">" );
	              res = hear( rdr );

	              // be polite
	              say( wtr, "RSET" ); hear( rdr );
	              say( wtr, "QUIT" ); hear( rdr );
	              if ( res != 250 ){
		                 throw new Exception( "Address is not valid!" );
	              }

	              valid = true;
	              rdr.close();
	              wtr.close();
	              skt.close();
	          } 
	          catch (Exception ex) {
	        	  String msg = ex.getMessage();
	        	  if("Address is not valid!".equalsIgnoreCase(msg)){
	        		  System.out.println("Invalid Address "+address);
	        		  return false;
	        	  }
	        	  System.out.println(msg);
	        	  ex.printStackTrace();
	            // Do nothing but try next host
	          } 
	          finally {
	            if (valid){ 
	            	System.out.println("Valid ID --> "+address);
	            	return true;
	            }
	          }
	      }
	      return false;
	 }
		
	public static String querySMTP(String firstName, String lastName, String domain){		
		firstName = firstName.toLowerCase().trim();
		lastName = lastName.toLowerCase().trim();
		domain = domain.toLowerCase().trim();		
		String validEmail = "Email Id not found";		
		List<Callable<Email>> emailIds = new ArrayList<Callable<Email>>();
		
		for(String emailId : patterns(firstName, lastName, domain)){
			Email email = new Email(emailId, domain);
			emailIds.add(email);
		}
		
		List<Future<Email>> processedEmailIds = Collections.emptyList();
		ExecutorService executorService = Executors.newFixedThreadPool(15);

		try {
			processedEmailIds = executorService.invokeAll(emailIds);
			System.out.println("submitted");
			executorService.shutdown();
			System.out.println("thread shutdown");
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		System.out.println("before checking");
		int index = 0;
		for(Future<Email> processedEmailId : processedEmailIds){
			try {
				index++;
				Email email = processedEmailId.get();
				if(email.getIsValid()){
					int nextElementIndex = index + 1;
					if(nextElementIndex >= processedEmailIds.size()){
						nextElementIndex = processedEmailIds.size()-1;
					}
					Future<Email> nextElement = processedEmailIds.get(nextElementIndex);
					Email nextEmail = nextElement.get(); 
					if(nextEmail.getIsValid() && !email.equals(nextEmail)){
						validEmail = "Catch-all";
						break;	
					}
					validEmail = email.getEmailId();
					break;
				}
			} catch (Exception ex) {
				ex.printStackTrace();
			} 
		}		
		return validEmail;
	}
	
	private static String[] patterns(String firstName, String lastName, String domain){		
		String atDomain = "@" + domain;
		String firstNameFirstChar = getFirstChar(firstName);

		List<String> emails = new ArrayList<String>();
		emails.add(firstName + atDomain); 
		if(StringUtils.isNotBlank(lastName)){
			String lastNameFirstChar = getFirstChar(lastName);			
			emails.add(firstName + "." + lastName+atDomain);
			emails.add(firstNameFirstChar + lastName + atDomain);
			emails.add(firstName + "_" + lastNameFirstChar + atDomain);
			emails.add(firstNameFirstChar + "_" + lastName + atDomain);
			emails.add(firstName + "-" + lastNameFirstChar + atDomain);
			emails.add(firstNameFirstChar + "-" + lastName + atDomain);
			emails.add(firstNameFirstChar + lastNameFirstChar + atDomain);
			emails.add(lastName + firstNameFirstChar + atDomain);
			emails.add(lastName + "." + firstName +atDomain);			
		}	
		emails.add(firstNameFirstChar + atDomain);
		return emails.toArray(new String[]{});
	}
	
	private static String getFirstChar(String name){
		if(StringUtils.isBlank(name)){
			return "";
		}
		return name.charAt(0) + "";
	}
	
}