package roamerz.keycloak.authenticator;

import com.google.auto.service.AutoService;
import org.keycloak.Config;
import org.keycloak.authentication.Authenticator;
import org.keycloak.authentication.AuthenticatorFactory;
import org.keycloak.models.AuthenticationExecutionModel;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.KeycloakSessionFactory;
import org.keycloak.provider.ProviderConfigProperty;

import java.util.List;


@AutoService(AuthenticatorFactory.class)
public class SmsAuthenticatorFactory implements AuthenticatorFactory {


	public static final String PROVIDER_ID = "sms-authenticator";

	private static final SmsAuthenticator SINGLETON = new SmsAuthenticator();

	@Override
	public String getId() {
		return PROVIDER_ID;
	}

	@Override
	public String getDisplayType() {
		return "SMS Authentication";
	}

	@Override
	public String getHelpText() {
		return "Validates an OTP sent via SMS to the users mobile phone.";
	}

	@Override
	public String getReferenceCategory() {
		return "otp";
	}

	@Override
	public boolean isConfigurable() {
		return true;
	}

	@Override
	public boolean isUserSetupAllowed() {
		return true;
	}

	@Override
	public AuthenticationExecutionModel.Requirement[] getRequirementChoices() {
		return REQUIREMENT_CHOICES;
	}

	@Override
	public List<ProviderConfigProperty> getConfigProperties() {
		return List.of(
			new ProviderConfigProperty(SmsConstants.CODE_LENGTH, "Code length", "The number of digits of the generated code.", ProviderConfigProperty.STRING_TYPE, 6),
			new ProviderConfigProperty(SmsConstants.CODE_TTL, "Time-to-live", "The time to live in seconds for the code to be valid.", ProviderConfigProperty.STRING_TYPE, "300"),
			new ProviderConfigProperty(SmsConstants.SENDER_ID, "SenderId", "The sender ID is displayed as the message sender on the receiving device.", ProviderConfigProperty.STRING_TYPE, "Keycloak"),
			new ProviderConfigProperty(SmsConstants.AWS_ACCESSKEYID, "Access Key ID", "The Access Key ID is configured under policies in https://console.aws.amazon.com/iam/home.", ProviderConfigProperty.STRING_TYPE, "Access Key ID"),
			new ProviderConfigProperty(SmsConstants.AWS_SECRETACCESSKEY, "Secret Access Key", "The Secret Access Key is configured under policies in https://console.aws.amazon.com/iam/home.", ProviderConfigProperty.STRING_TYPE, "Secret Access Key"),
			new ProviderConfigProperty(SmsConstants.AWS_REGION, "Aws Region", "AWS SNS Region", ProviderConfigProperty.STRING_TYPE, "AWS SNS Region"),
			new ProviderConfigProperty(SmsConstants.ENABLE_AWS_EMAIL, "Enable AWS-Email", "If Email enabled KeyCloak will send Email if MFA configuration is invalid", ProviderConfigProperty.BOOLEAN_TYPE, true),
			new ProviderConfigProperty(SmsConstants.FAILSAFE_EMAIL_WHITELIST, "Email Whitelist", "Email/Domain Whitelist. This authenticator will, as a last effort, attempt to send an email to the users login ID. This has no affect on the authenticator_send_to emails. To succeed the Domain or the individual email address must be in this list. ", ProviderConfigProperty.STRING_TYPE, "somedomain.com"),
			new ProviderConfigProperty(SmsConstants.ENABLE_IP_AUTHENTICATION, "Enable IP Authentication", "If enabled KC will check to see if IP address is trusted source, and if so it will bypass MFA. This MUST be used with Keycloak-IP-Authenticator component.", ProviderConfigProperty.BOOLEAN_TYPE, false)
		);

	}

	@Override
	public Authenticator create(KeycloakSession session) {
		return SINGLETON;
	}

	@Override
	public void init(Config.Scope config) {
	}

	@Override
	public void postInit(KeycloakSessionFactory factory) {

	}

	@Override
	public void close() {
	}

}
