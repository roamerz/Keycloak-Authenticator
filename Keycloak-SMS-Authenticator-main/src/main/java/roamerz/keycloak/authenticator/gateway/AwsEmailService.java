package roamerz.keycloak.authenticator.gateway;


import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.pinpoint.PinpointClient;
import software.amazon.awssdk.services.pinpoint.model.AddressConfiguration;
import software.amazon.awssdk.services.pinpoint.model.ChannelType;
import software.amazon.awssdk.services.pinpoint.model.SimpleEmailPart;
import software.amazon.awssdk.services.pinpoint.model.SimpleEmail;
import software.amazon.awssdk.services.pinpoint.model.EmailMessage;
import software.amazon.awssdk.services.pinpoint.model.DirectMessageConfiguration;
import software.amazon.awssdk.services.pinpoint.model.MessageRequest;
import software.amazon.awssdk.services.pinpoint.model.SendMessagesRequest;
import java.util.HashMap;
import java.util.Map;

public class AwsEmailService implements EmailService {

	private final String senderId;
	AwsEmailService(Map<String, String> config) {
		senderId = config.get("senderId");
	}


	public void send(String toAddress, String replyToAddress, String subject, String senderAddress,String message, String appId, String awsRegion) {



			PinpointClient pinpoint = PinpointClient.builder()
			.region(Region.of(awsRegion))
			.build();



		String htmlBody = message,charset = "UTF-8";

		Map<String, AddressConfiguration> addressMap = new HashMap<>();
		AddressConfiguration configuration = AddressConfiguration.builder()
			.channelType(ChannelType.EMAIL)
			.build();

		addressMap.put(toAddress, configuration);
		SimpleEmailPart emailPart = SimpleEmailPart.builder()
			.data(htmlBody)
			.charset(charset)
			.build();

		SimpleEmailPart subjectPart = SimpleEmailPart.builder()
			.data(subject)
			.charset(charset)
			.build();

		SimpleEmail simpleEmail = SimpleEmail.builder()
			.htmlPart(emailPart)
			.subject(subjectPart)
			.build();

		EmailMessage emailMessage = EmailMessage.builder()
			.body(htmlBody)
			.replyToAddresses(replyToAddress)
			.fromAddress(senderAddress)
			.simpleEmail(simpleEmail)
			.build();

		DirectMessageConfiguration directMessageConfiguration = DirectMessageConfiguration.builder()
			.emailMessage(emailMessage)
			.build();

		MessageRequest messageRequest = MessageRequest.builder()
			.addresses(addressMap)
			.messageConfiguration(directMessageConfiguration)
			.build();

		SendMessagesRequest messagesRequest = SendMessagesRequest.builder()
			.applicationId(appId)
			.messageRequest(messageRequest)
			.build();

		pinpoint.sendMessages(messagesRequest);

	}
}
