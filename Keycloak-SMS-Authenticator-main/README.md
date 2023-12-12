Originally authored by Niko Köbler and mucked up by me.

Keycloak Authentication Provider implementation to get a 2nd-factor authentication with a OTP/code/token send via SMS (through AWS SNS). Modified to allow setting up the AWS parameters through the KC GUI. Also added the ability to send an email with the MFA code using AWS Pinpoint if the SMS number is not set in KC.

My first ever stab at Java / Keycloak and it is a work in progress so ***use at your own risk***  If you see something wrong or something you'd like added hit me up.
