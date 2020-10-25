Consent API
---------------------------

*Register Patient* - To register a patient with health authorities after accepting the terms and conditions just like in CovidNotify.
*Admission Set Diagnosis Status* - Set Diagnosis Status - We will set a user as positive or negative based on this. This is a proxy to actually testing done by the health authorities.

*Get Patient Status* -  Given a UUID, It gets the patient details from the user.

*Add Consent* - Given a UUID and host, It adds a consent to the user. We can create a TAN based flow here. You can write the server code here.

*Revoke Consent* - To revoke the consent given to a host by an uuid.

*Authenticate Consent* - To authenticate with the health care API, if the user shared a consent to get test results. Can be used internally or by the vertification server when it requests health care details to let upload of TEKs
