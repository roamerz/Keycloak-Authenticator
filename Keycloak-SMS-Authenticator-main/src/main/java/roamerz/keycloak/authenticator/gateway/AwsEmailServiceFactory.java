package roamerz.keycloak.authenticator.gateway;

import lombok.extern.slf4j.Slf4j;

import java.util.Map;


@Slf4j
public class AwsEmailServiceFactory {

	public static EmailService get(Map<String, String> config) {
		String ACCESSKEYID = config.get("accessKeyId");
		String SECRETACCESSKEY = config.get("secretAccessKey");
		String AWSREGION = config.get("awsRegion");




			//addedby patroam
			System.setProperty("aws.accessKeyId", ACCESSKEYID);
			System.setProperty("aws.secretAccessKey",SECRETACCESSKEY);
			System.setProperty("aws.region", AWSREGION);

			return new AwsEmailService(config);

	}

}
