# <bindingName> Binding

This binding interacts with MyElas compatible Alarm Systems.
Be sure to have an account registered on https://www.myelas.com
_The account used have to be a full account registered with an email address_

## Supported Things

The only supported thing is the connection interface between OpenHab2 and MyElas server. 

## Discovery

_There is no automatic thing discovery._

## Thing Configuration

- MyElas WEB UI url : no need to change
- MyElas REST API url : no need to change 
- Timeout : no need to change
- Username : email address of your account
- Password : web password
- PIN code : System pin code  

## Channels

- Alarm offline : (switch) is  your alarm still connected to MyElas Server
- Ongoing alarm : (switch) is your alarm buzzing
- Number of partitions armed
- Number of partitions disarmed
- Number of partitions partially armed

For now, only these channels are handled.

## Release info

This is alpha release 0.1
Feel free to tell me if some of my translations are bad.
Needed to fix :
- first init cause the thing to get ONLINE and then OFFLINE because of CONFIGURATION_ERROR but everything goes well after few seconds.
- sync connection --> async connection
- status thing update each poll even if not needed. 

Improvements will come later
