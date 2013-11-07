package edu.sjsu.cmpe.procurement.jobs;

import java.util.Map;

import javax.jms.DeliveryMode;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;

import org.fusesource.stomp.jms.StompJmsDestination;
import org.fusesource.stomp.jms.message.StompJmsMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.GenericType;

import de.spinscale.dropwizard.jobs.Job;
import de.spinscale.dropwizard.jobs.annotations.Every;
import edu.sjsu.cmpe.procurement.ProcurementService;
import edu.sjsu.cmpe.procurement.domain.Book;

/**
 * This job will run at every 5 second.
 */
@Every("5mn")
public class ProcurementSchedulerJob extends Job {
	private final Logger log = LoggerFactory.getLogger(getClass());

	@Override
	public void doJob() {
		String body = "{\"id\":\"22840\",\"order_book_isbns\":[";
		Message msg = null;
		boolean hasMsg = false;
		try {
			// Client client=Client.create();

			// WebResource webResource = client
			// .resource("http://54.215.210.214:9000/orders");
			// String body = "{\"id\":\"22840\",\"order_book_isbns\":[1]}";

			long waitUntil = 500;
			while (true) {

				msg = ProcurementService.consumer.receive(waitUntil);
				if (msg instanceof StompJmsMessage) {
					StompJmsMessage smsg = ((StompJmsMessage) msg);
					body += smsg.getFrame().contentAsString().substring(10)
							+ ",";
					hasMsg = true;
				} else if (msg == null)

				{
					System.out.println("No Books in the Queue");
					System.out
							.println("No new messages. Exiting due to timeout - "
									+ waitUntil / 1000 + " sec");
					break;
				} else {
					System.out.println("Unexpected message type: "
							+ msg.getClass());
				}
			}

			if (hasMsg) {
				// System.out.println(input);
				body += "]}";

				int position = body.lastIndexOf(",");
				body = replaceCharAt(body, position, "");
				ClientResponse strResponse = null;
				try {
					strResponse = ProcurementService.jerseyClient.create()
							.resource("http://54.215.210.214:9000/orders")
							.type("application/json")
							.post(ClientResponse.class, body);
				} catch (Exception e) {
				}

				/*
				 * ClientResponse
				 * strResponse=ProcurementService.jerseyClient.create
				 * ().resource( "http://54.215.210.214:9000/orders"
				 * ).type("application/json").post(ClientResponse.class,body);
				 */

				// String strResponse =
				// ProcurementService.jerseyClient.resource(
				// "http://ip.jsontest.com/").get(String.class);
				if (strResponse.getStatus() == 200) {
					log.debug("Response from Publisher:{}",
							strResponse.getEntity(String.class));
				} else {
					log.debug("HTTP Error Code returned as:{}",
							strResponse.getStatus());
				}

			} // end hasMsg if
			
			doGetFromPublisher();

		} catch (JMSException e) {
			// TODO Auto-generated catch block
			// e.printStackTrace();
		}

		/*
		 * String strResponse1 = ProcurementService.jerseyClient.resource(
		 * "http://ip.jsontest.com/").get(String.class);
		 * log.debug("Response from jsontest.com: {}", strResponse1);
		 */
	}

	private void doGetFromPublisher() throws JMSException {
		Map<String, Book[]> collector;
		ClientResponse strResponse = ProcurementService.jerseyClient.create()
				.resource("http://54.215.210.214:9000/orders/22840")
				.accept("application/json").type("application/json")
				.get(ClientResponse.class);
		// log.debug("Response from Get Publisher:{}",
		// strResponse.getEntity(String.class));
		// TODO Auto-generated method stub
		collector = strResponse
				.getEntity(new GenericType<Map<String, Book[]>>() {
				});
		Book[] myCollection = collector.get("shipped_books");
		int size = myCollection.length;

		StompJmsMessage msg = (StompJmsMessage) ProcurementService.session
				.createTextMessage(strResponse.getEntity(String.class));
		msg.setLongProperty("id", System.currentTimeMillis());
		// System.out.println("Publisher Sending the books to Queue"+strResponse.getEntity(String.class));
		// System.out.println("Msg is"+msg);
		for (int i = 0; i < size; i++) {
			ProcurementService.topicName = "/topic/22840.book."
					+ myCollection[i].getCategory();
			Destination topicdestination = new StompJmsDestination(
					ProcurementService.topicName);
			ProcurementService.producer = ProcurementService.session
					.createProducer(topicdestination);
			String data = myCollection[i].getIsbn() + ":" + "\""
					+ myCollection[i].getTitle() + "\":\""
					+ myCollection[i].getCategory() + "\":\""
					+ myCollection[i].getCoverimage() + "\"";
			msg = (StompJmsMessage) ProcurementService.session
					.createTextMessage(data);
			System.out.println("Books sending to Library:" + data);
			ProcurementService.producer.send(msg);
			ProcurementService.producer
					.setDeliveryMode(DeliveryMode.NON_PERSISTENT);
		}

	}

	public static String replaceCharAt(String s, int pos, String c) {
		return s.substring(0, pos) + c + s.substring(pos + 1);
	}

}
