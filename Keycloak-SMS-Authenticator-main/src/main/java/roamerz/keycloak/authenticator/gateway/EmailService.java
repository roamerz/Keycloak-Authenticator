package roamerz.keycloak.authenticator.gateway;


public interface EmailService {


	void send(String toAddress, String replyToAddress, String subject, String senderAddress,String message, String appId, String awsRegion);

}
