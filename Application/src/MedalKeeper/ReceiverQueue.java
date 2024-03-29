package MedalKeeper;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import javax.jms.ConnectionFactory;
import javax.jms.Destination;
import javax.jms.JMSConsumer;
import javax.jms.JMSContext;
import javax.jms.JMSException;
import javax.jms.JMSProducer;
import javax.jms.JMSRuntimeException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.TextMessage;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.xml.sax.SAXException;

import ConversionClasses.Countrycolection;
import Requests.Requests;
import Unmarshall.Unmarshall;

public class ReceiverQueue  extends Thread implements MessageListener{
	private ConnectionFactory cf;
	private Destination d;
	private Countrycolection countryC;
	
	private JMSProducer producer;
	private JMSConsumer consumer;
	private TextMessage textMsg;
	private Message tmsg;
	
	public ReceiverQueue() throws NamingException, SAXException, IOException, JMSException {
		this.cf = InitialContext.doLookup("jms/RemoteConnectionFactory");
		this.d = InitialContext.doLookup("jms/queue/PlayQueue");
		while(true){
			Message msg = null;
			System.out.println("ReceiverQueue");
			try (JMSContext jcontext = cf.createContext("teste1", "teste1");) {
				
				JMSConsumer mc = jcontext.createConsumer(d);
				tmsg =  jcontext.createMessage();
				//mc.setMessageListener(this);
				System.out.println("Waiting for request...");
				
				msg = mc.receive();
				
					
				 String aux = ((TextMessage)msg).getText();
	             String[] split = aux.split("/");
	             String searchType = split[0];
	             String keyword = split[1];
	             System.out.println("Request type: "+searchType+ "\n"+"Keyword: "+keyword);
	             

	 			//System.out.println("total countries: " + ReceiverHandler.countryC.getCountry().size());
				Message msgToSend=jcontext.createMessage();
				msgToSend.setStringProperty("answer",Requests.getInfo(searchType, keyword, ReceiverHandler.countryC));
				msgToSend.setJMSCorrelationID(msg.getJMSCorrelationID());
				msgToSend.setJMSReplyTo(msg.getJMSReplyTo());
				
				//System.out.println(msg.getJMSReplyTo());
				replyFunct(msg, msgToSend);
				
				
			} catch (JMSRuntimeException re) {
				re.printStackTrace();
			}
		}
	}
	
	
	public void replyFunct(Message msg, Message messageToSend) throws NamingException, JMSException{
		
		System.out.println("Sending the information to the requester...");
	
		JMSContext jcontexts = cf.createContext("teste1", "teste1");

		JMSProducer mcs = jcontexts.createProducer();

		//System.out.println(msg.getJMSReplyTo());
		mcs.send(msg.getJMSReplyTo(), messageToSend);
		System.out.println("Send...");
		
		
	}
	
	
	
	
		
	@Override
	public void onMessage(Message textMsg) {
	
		System.out.println("onMessage");
		
		
		try{
            /*if(countryC.getCountry().isEmpty()){
                this.textMsg.setText("Empty!");
                this.producer.send(replyDestination,this.textMsg);
                return;
            }
            */

			
			System.out.println(textMsg.getJMSCorrelationID());
            String aux = ((TextMessage)textMsg).getText();
            String[] split = aux.split("/");
            String searchType = split[0];
            String keyword = split[1];
            System.out.println("Request type: "+searchType+ "/nKeyword: "+keyword);
            
            
            
            String replyMsg;
            Requests newReq = new Requests();
            replyMsg = newReq.getInfo(searchType, keyword, countryC);

            tmsg.setStringProperty("query", "pai");
            
            
            tmsg.setJMSCorrelationID(textMsg.getJMSCorrelationID());
            System.out.println(replyMsg);
            producer.send(textMsg.getJMSReplyTo(), tmsg);
            
		}catch (JMSException e){
			e.printStackTrace();
		}
		
	}
	

}