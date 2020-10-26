SSL Certificates
------------------------------

The SSL Certificates used by the server are stored here for reference. 
It expires on 12/29/2020, 5:05:03 PM 


Using this function and latest openssl to get the base64 encoding of sha256
hash

`openssl x509 -in appspot-com.pem -pubkey -noout | openssl pkey -pubin -outform
der | openssl dgst -sha256 -binary | openssl enc -base64`
