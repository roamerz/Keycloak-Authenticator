**Keycloak-Authenticator**

This repository contains code from 2 projects that I have modified:

dasniko/keycloak-2fa-sms-authenticator and lukaszbudnik/keycloak-ip-authenticator

After compiling and installing to Keycloak you are able to configure both of them through settings in the web console.

The basic flow is:

User provides credentials
IP Authenticator plugin checks to see if the request comes from an authorized IP Address
This sets the ip_based_otp_conditional attribute to skip or force.

The next step will be the SMS Authenticator which will check this attribute.
If skip it will complete the authentication process without requiring an MFA token.
If force it will read the attribute authenticator_send_to.
If this contains a mobile # it will deliver the token to the user via Amazon SNS using SMS. 
If this contains an email address it will deliver the token to the user via Amazon SNS using email. 
If authenticator_send_to does not contain either it will then send the token to the Keycloak username via Amazon SNS using email
There is a field in the config, Email Whitelist, that must contain authorized email domains for the last step to work. This is because in my use case I have a client where their username is not a 'real' domain and the email addresses do not exist in the real world. Saves getting undeliverables. 

This is a work in progress please use at your own risk. I will try and keep this repository updated as I make changes.

Please feel free to reach out if you see something amiss or have suggestions for changes. 

This is the first time I have done anything on github with any open source projects so if I have done something wrong in that aspect please also let me know.

Hope someone finds this useful and thanks to the original authors!

![SMS Authenticator Config](https://github.com/roamerz/Keycloak-Authenticator/assets/5789526/3597fb63-ffa3-4a29-ab0d-8f2743fe2459)
![IP Authenticator Config](https://github.com/roamerz/Keycloak-Authenticator/assets/5789526/ea012ad6-9659-433e-a091-9df052d049fd)
