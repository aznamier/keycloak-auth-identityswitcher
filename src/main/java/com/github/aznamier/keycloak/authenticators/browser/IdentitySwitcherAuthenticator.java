package com.github.aznamier.keycloak.authenticators.browser;

import org.jboss.logging.Logger;
import org.keycloak.authentication.AuthenticationFlowContext;
import org.keycloak.authentication.Authenticator;
import org.keycloak.common.util.Time;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.models.UserModel;
import org.keycloak.models.UserSessionModel;
import org.keycloak.services.managers.AuthenticationManager;
import org.keycloak.sessions.AuthenticationSessionModel;


/**
 * IdentitySwitcherAuthenticator can be used at the end of the browser workflow
 * to automatically switch the identity in the current session to new authenticated identity.
 * 
 *  This can happen if the login form is initiated with "?prompt=login" parameter 
 *  (bypassing SSO and forcing login form to appear)
 *  
 *  Normally user in such case gets an error message see org.keycloak.events.Errors.DIFFERENT_USER_AUTHENTICATED
 *  You are already authenticated as different user ''{0}'' in this session. Please log out first.
 *  
 *  This IdentitySwitcherAuthenticator allows to continue and switch users automatically.
 * 
 * @author <a href="mailto:aznamier@gmail.com">Artur Z</a>
 */
public class IdentitySwitcherAuthenticator implements Authenticator {
	
	private static final Logger LOG = Logger.getLogger(IdentitySwitcherAuthenticator.class);
	
	@Override
	public boolean requiresUser() {
		return true;
	}

	@Override
	public void authenticate(AuthenticationFlowContext context) {
		
		AuthenticationSessionModel authSession = context.getAuthenticationSession();
		KeycloakSession session = context.getSession();
		RealmModel realm = context.getRealm();
		
		UserSessionModel userSession = session.sessions().getUserSession(context.getRealm(), authSession.getParentSession().getId());
		
		if(userSession != null && AuthenticationManager.isSessionValid(realm, userSession)) {
			
			LOG.trace("Session owned by: " + userSession.getUser().getUsername());
			LOG.trace("Authenticated by: " + authSession.getAuthenticatedUser().getUsername());
			
			if(!authSession.getAuthenticatedUser().equals(userSession.getUser())) {
				LOG.debug("Detected new authentication identity: " + authSession.getAuthenticatedUser().getUsername() + 
						" in current session owned by: " + userSession.getUser().getUsername());
				
				//we have existing user in current session but its username is different that the username in incoming authentication
				//so we need to log out the existing user
				session.sessions().removeUserSession(realm, userSession);
				authSession.getParentSession().setTimestamp(Time.currentTime());

		        UserSessionModel newUserSession = session.sessions()
		        		.createUserSession(realm, authSession.getAuthenticatedUser(), 
		        				authSession.getAuthenticatedUser().getUsername(), 
		        				userSession.getIpAddress(), userSession.getAuthMethod(), 
		        				userSession.isRememberMe(), userSession.getBrokerSessionId(), userSession.getBrokerUserId());
		        
		        context.attachUserSession(newUserSession);
	            context.success();
	            
	            LOG.info("Switched to new authentication identity: " + authSession.getAuthenticatedUser().getUsername() + 
						" within current session owned by: " + userSession.getUser().getUsername());
				
				return;
			}
		}
		
		context.success();
 	}

	@Override
	public void action(AuthenticationFlowContext context) {

	}

	@Override
	public boolean configuredFor(KeycloakSession session, RealmModel realm, UserModel user) {
		return true;
	}

	@Override
	public void setRequiredActions(KeycloakSession session, RealmModel realm, UserModel user) {
	}

	@Override
	public void close() {

	}
}
