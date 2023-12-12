package roamerz.keycloak.authenticator;

import roamerz.keycloak.authenticator.gateway.AwsEmailServiceFactory;
import roamerz.keycloak.authenticator.gateway.SmsServiceFactory;
import jakarta.ws.rs.core.Response;
import org.keycloak.authentication.AuthenticationFlowContext;
import org.keycloak.authentication.AuthenticationFlowError;
import org.keycloak.authentication.Authenticator;
import org.keycloak.common.util.SecretGenerator;
import org.keycloak.models.AuthenticationExecutionModel;
import org.keycloak.models.AuthenticatorConfigModel;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.models.UserModel;
import org.keycloak.sessions.AuthenticationSessionModel;
import org.keycloak.theme.Theme;

import java.util.Collections;
import java.util.Locale;



public class SmsAuthenticator implements Authenticator {

	//private static final String MOBILE_NUMBER_FIELD = "mobile_number";

	private static final String AUTHENTICATOR_SEND_TO = "authenticator_send_to";	//contains email or mobile number
	private static final String IP_BASED_OTP_CONDITIONAL = "ip_based_otp_conditional";
	private static final String TPL_CODE = "login-sms.ftl";






	@Override
	public void authenticate(AuthenticationFlowContext context) {
		AuthenticatorConfigModel config = context.getAuthenticatorConfig();
		KeycloakSession session = context.getSession();
		UserModel user = context.getUser();

		int length = Integer.parseInt(config.getConfig().get(SmsConstants.CODE_LENGTH));
		int ttl = Integer.parseInt(config.getConfig().get(SmsConstants.CODE_TTL));
		String  awsRegion = config.getConfig().get(SmsConstants.AWS_REGION);
		String emailWhitelist =  config.getConfig().get(SmsConstants.FAILSAFE_EMAIL_WHITELIST);

		String email = user.getEmail();
		String sendfunction = "sendSms";



		String code = SecretGenerator.getInstance().randomString(length, SecretGenerator.DIGITS);
		AuthenticationSessionModel authSession = context.getAuthenticationSession();
		authSession.setAuthNote(SmsConstants.CODE, code);
		authSession.setAuthNote(SmsConstants.CODE_TTL, Long.toString(System.currentTimeMillis() + (ttl * 1000L)));


		String authenticator_send_to = user.getFirstAttribute(AUTHENTICATOR_SEND_TO);

		if (authenticator_send_to == null) {
			user.setAttribute(AUTHENTICATOR_SEND_TO, Collections.singletonList("Please enter valid mobile number or email address"));
			authenticator_send_to = "Please enter valid mobile number or email address";
		}




		String ip_based_otp_conditional = user.getFirstAttribute(IP_BASED_OTP_CONDITIONAL);

		try{
			if(Boolean.parseBoolean(config.getConfig().get(SmsConstants.ENABLE_IP_AUTHENTICATION))) {

				if (ip_based_otp_conditional.equals("skip")) {
					context.success();
					return;
				}
			}
		}
		catch (Exception e) {
			context.failureChallenge(AuthenticationFlowError.INTERNAL_ERROR,
				context.form().setError("smsAuthSmsNotSent", e.getMessage())
					.createErrorPage(Response.Status.INTERNAL_SERVER_ERROR));
		}


		try {

			//add email detection address to function

			if (authenticator_send_to.contains("@")) {

				sendfunction = "sendemailtomfa";

			}

			else {

				authenticator_send_to = authenticator_send_to.replaceAll("[^0-9]", "");

				if (authenticator_send_to.startsWith("1") && authenticator_send_to.length() == 11) {

					authenticator_send_to = "+" + authenticator_send_to;        //prefix with "+" sign

				} else if (authenticator_send_to.length() == 10) {

					authenticator_send_to = "+1" + authenticator_send_to;        //prefix with "+1" U.S. country code

				} else {

					//mobile # is invalid


					if (Boolean.parseBoolean(config.getConfig().get(SmsConstants.ENABLE_AWS_EMAIL))) {

						context.failureChallenge(AuthenticationFlowError.INTERNAL_ERROR,
							context.form().setError("smsAuthSmsNotSent", "Your account does not have MFA setup. If enabled for your agency, we will now attempt to send an MFA token to your email on file.")
								.createErrorPage(Response.Status.INTERNAL_SERVER_ERROR));
						sendfunction = "sendemailtoUsername";

					} else {
						context.failureChallenge(AuthenticationFlowError.INTERNAL_ERROR,
							context.form().setError("smsAuthSmsNotSent", "Invalid SMS Number. Must be in the format of ###-###-####.")
								.createErrorPage(Response.Status.INTERNAL_SERVER_ERROR));

					}


				}

			}
		}
		catch (Exception e) {
			context.failureChallenge(AuthenticationFlowError.INTERNAL_ERROR,
				context.form().setError("smsAuthSmsNotSent", e.getMessage())
					.createErrorPage(Response.Status.INTERNAL_SERVER_ERROR));
		}




		try {
			Theme theme = session.theme().getTheme(Theme.Type.LOGIN);
			Locale locale = session.getContext().resolveLocale(user);
			String smsAuthText = theme.getMessages(locale).getProperty("smsAuthText");
			String smsText = String.format(smsAuthText, code, Math.floorDiv(ttl, 60));

			//send sms

			if(sendfunction == "sendSms"){

				SmsServiceFactory.get(config.getConfig()).send(authenticator_send_to, smsText);

			}

			else if(sendfunction == "sendemailtomfa")
			{

				AwsEmailServiceFactory.get(config.getConfig()).send(authenticator_send_to, "automated@crimeix.com",smsText,"automated@crimeix.com",smsText,"9ba42eca42fa42b6a83dd0d4026a5b4d", awsRegion);


			}


			else if(sendfunction == "sendemailtoUsername") {

				if (emailWhitelist != null) {
					if (email.contains(emailWhitelist)) {
						AwsEmailServiceFactory.get(config.getConfig()).send(email, "automated@crimeix.com", smsText, "automated@crimeix.com", smsText, "9ba42eca42fa42b6a83dd0d4026a5b4d", awsRegion);
					}
				}
			}

			context.challenge(context.form().setAttribute("realm", context.getRealm()).createForm(TPL_CODE));

		} catch (Exception e) {
			context.failureChallenge(AuthenticationFlowError.INTERNAL_ERROR,
				context.form().setError("smsAuthSmsNotSent", e.getMessage())
					.createErrorPage(Response.Status.INTERNAL_SERVER_ERROR));
		}
	}

	@Override
	public void action(AuthenticationFlowContext context) {
		String enteredCode = context.getHttpRequest().getDecodedFormParameters().getFirst(SmsConstants.CODE);

		AuthenticationSessionModel authSession = context.getAuthenticationSession();
		String code = authSession.getAuthNote(SmsConstants.CODE);
		String ttl = authSession.getAuthNote(SmsConstants.CODE_TTL);

		if (code == null || ttl == null) {
			context.failureChallenge(AuthenticationFlowError.INTERNAL_ERROR,
				context.form().createErrorPage(Response.Status.INTERNAL_SERVER_ERROR));
			return;
		}

		boolean isValid = enteredCode.equals(code);
		if (isValid) {
			if (Long.parseLong(ttl) < System.currentTimeMillis()) {
				// expired
				context.failureChallenge(AuthenticationFlowError.EXPIRED_CODE,
					context.form().setError("smsAuthCodeExpired").createErrorPage(Response.Status.BAD_REQUEST));
			} else {
				// valid
				context.success();
			}
		} else {
			// invalid
			AuthenticationExecutionModel execution = context.getExecution();
			if (execution.isRequired()) {
				context.failureChallenge(AuthenticationFlowError.INVALID_CREDENTIALS,
					context.form().setAttribute("realm", context.getRealm())
						.setError("smsAuthCodeInvalid").createForm(TPL_CODE));
			} else if (execution.isConditional() || execution.isAlternative()) {
				context.attempted();
			}
		}
	}

	@Override
	public boolean requiresUser() {
		return true;
	}

	@Override
	public boolean configuredFor(KeycloakSession session, RealmModel realm, UserModel user) {


			//return user.getFirstAttribute(MOBILE_NUMBER_FIELD) != null;
			return true;
	}

	@Override
	public void setRequiredActions(KeycloakSession session, RealmModel realm, UserModel user) {
		// this will only work if you have the required action from here configured:
		// https://github.com/dasniko/keycloak-extensions-demo/tree/main/requiredaction
		//user.addRequiredAction("mobile-number-ra");
	}

	@Override
	public void close() {
	}

}
