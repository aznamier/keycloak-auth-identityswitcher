# keycloak-auth-identityswitcher

##### A Keycloak Authentication plugin that allows to replace identity in Keycloak session on the fly 


------------------------------------------------------------
###### Warning! This is a workaround that may help in your "legacy to keycloak" transition, but use with caution.
------------------------------------------------------------

##### For example in the scenario when there are two clients in the realm:
* **Website 1** where user always wants to log in via **google Idp** with `someuser@gmail.com`
* **Website 2** where user always wants to log in via **microsoft Idp** with `someuser@live.com`
	

![Problem](https://github.com/aznamier/keycloak-auth-identityswitcher/blob/readme/blobs/blob/IMG_1.jpeg?raw=true "Problem") 

Step 1. User wants to access **Website 1**
  
Step 2. User redirected into Keycloak login screen


Step 3. User selects 'Google' IDP and logins in via `someuser@gmail.com` 
  
Step 4. User now wants to access **Website 2**


Step 5. User is redirected to Keycloak server


Step 6. Keycloak has already got a session with that browser and redirects to Website 2 with `someuser@google.com`


**ERROR** in this case as Website 2 has `someuser@live.com` but was given `someuser@google.com`



##### Workaround with this plugin:
1. force ability to access the login page (to avoid autoredirection Step 6) per client using hint `&prompt=login` in the redirection url
so user can actually select Microsoft Idp

2. install and configure this plugin: IdentitySwitcher


![Solution](https://github.com/aznamier/keycloak-auth-identityswitcher/blob/readme/blobs/blob/IMG_3.jpeg?raw=true "Solution") 


###### Now on succesfull authentication with **Microsoft Idp**, their previous identity `someuser@google.com` is automatically replaced with `someuser@live.com` coming from **Microsoft Idp**


## INSTALLATION:
1. [Download the latest jar](https://github.com/aznamier/keycloak-auth-identityswitcher/blob/target/keycloak-auth-identityswitcher-1.0.jar?raw=true) or build from source: ``mvn clean install``
2. copy jar into your Keycloak `/opt/jboss/keycloak/standalone/deployments/keycloak-auth-identityswitcher-1.0.jar`
3. Configure as described below

#### Configuration 
###### Setting up identity switcher for Keycloak Login page
1. In Keycloak -> Realm -> Authentication -> Flows -> Browser -> Copy - and give it new name eg: 'Browser NO SSO'
2. In Keycloak -> Realm -> Authentication -> Bindings -> set browser flow to this new flow: 'Browser NO SSO'
3. In Keycloak -> Realm -> Authentication -> Flows -> Add Execution - add Identity Switcher as shown here:

![Setup1](https://github.com/aznamier/keycloak-auth-identityswitcher/blob/readme/blobs/blob/IMG_4.png?raw=true "Setup1") 

###### Setting up identity switcher to work with IDP Providers like Google, Microsoft etc
1. In Keycloak -> Realm -> Authentication -> Flows -> New - and give it new name eg: 'IDP Post Login Flow'
2. In Keycloak -> Realm -> Identity Providers -> in each provider set 'IDP Post Login Flow' as Post Login Flow
3. In Keycloak -> Realm -> Authentication -> Flows -> Add Execution - add Identity Switcher as shown here:
![Setup2](https://github.com/aznamier/keycloak-auth-identityswitcher/blob/readme/blobs/blob/IMG_5.png?raw=true "Setup2")



