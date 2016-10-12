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
	
	public ReceiverQueue(Countrycolection countryCAux) throws NamingException, SAXException, IOException {
		this.cf = InitialContext.doLookup("jms/RemoteConnectionFactory");
		this.d = InitialContext.doLookup("jms/queue/PlayQueue");
		this.countryC = countryCAux;
		/*String msg = null;
		System.out.println("ReceiverQueue");
		try (JMSContext jcontex = cf.createContext("teste1", "teste1");) {
			
			JMSConsumer mc = jcontex.createConsumer(d);
			msg = mc.receiveBody(String.class);
			
		} catch (JMSRuntimeException re) {
			re.printStackTrace();
		}*/
	}

	@Override
	public void onMessage(Message textMsg) {
	
		System.out.println("onMessage");
		
		TextMessage tmsg = (TextMessage) textMsg;
		try{
			Destination replyDestination = textMsg.getJMSReplyTo();
			//check if Countrycolection is empty
            if(countryC.getCountry().isEmpty()){
                this.textMsg.setText("Empty!");
                this.producer.send(replyDestination,this.textMsg);
                return;
            }
            
            String aux = ((TextMessage)textMsg).getText();
            String[] split = aux.split("/");
            String searchType = split[0];
            String keyword = split[1];
            System.out.println("Request type: "+searchType+ "/nKeyword: "+keyword);
            
            
            String replyMsg;
            Requests newReq = new Requests();
            replyMsg = newReq.getInfo(searchType, keyword, countryC);
            
            this.textMsg.setText(replyMsg);
            this.producer.send(replyDestination, this.textMsg);
            
		}catch (JMSException e){
			e.printStackTrace();
		}
		
	}
	

}