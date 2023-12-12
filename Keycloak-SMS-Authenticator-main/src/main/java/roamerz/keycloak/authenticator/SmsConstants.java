package roamerz.keycloak.authenticator;

import lombok.experimental.UtilityClass;

@UtilityClass
public class SmsConstants {
	public String CODE = "code";
	public String CODE_LENGTH = "length";
	public String CODE_TTL = "ttl";
	public String SENDER_ID = "senderId";
	public String AWS_ACCESSKEYID = "accessKeyId";
	public String AWS_SECRETACCESSKEY = "secretAccessKey";
	public String AWS_REGION = "awsRegion";
	public String ENABLE_AWS_EMAIL = "enableEmail";

	public String FAILSAFE_EMAIL_WHITELIST = "emailWhitelist";

	public String ENABLE_IP_AUTHENTICATION = "enableIpAuthentication";
}
