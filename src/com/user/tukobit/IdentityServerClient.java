package com.user.tukobit;

import java.util.HashMap;
import java.util.Map;

import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.context.ConfigurationContextFactory;
import org.apache.axis2.transport.http.HTTPConstants;
import org.wso2.carbon.authenticator.stub.AuthenticationAdminStub;
import org.wso2.carbon.um.ws.api.WSRealmBuilder;
import org.wso2.carbon.user.core.UserRealm;
import org.wso2.carbon.user.core.UserStoreManager;

public class IdentityServerClient {

	// ONE TIME TASKS WE NEED TO DO BEFORE EXECUTING THIS PROGRAM.

	// TASK - 1 , CREATE a LoginOnly role from IS UI Console
	// ===========================================================
	// 0. Login as admin/admin
	// 1. Go to Users and Roles
	// 2. Click on Roles
	// 3. Add New Role
	// 4. Role Name : loginOnly [please use this name, since it's referred within the code below]
	// 5. Click Next
	// 6. Select only the 'Login' permission
	// 7. Click Next
	// 8. No need to select any users
	// 9. Click Finish

	// TASK - 2 , CREATE a custom claim IS UI Console
	// ===========================================================
	// 0. Login as admin/admin
	// 1. Go to Claim Management
	// 2. Click on http://wso2.org/claims
	// 3. Click on 'Add New Claim Mapping'
	// 3.1 Display Name : Business Phone
	// 3.2 Description : Business Phone
	// 3.3 Claim Uri : http://wso2.org/claims/businessphone
	// 3.4 Mapped Attribute : http://wso2.org/claims/businessphone
	// 3.5 Support by default : Checked
	// 3.6 The rest can be kept blank

	private final static String SERVER_URL = "https://192.168.2.171:9443/services/";
	private final static String APP_ID = "ims_login";

	/**
	 * @param args
	 */
	public static void main(String[] args) {

		AuthenticationAdminStub authstub = null;
		ConfigurationContext configContext = null;
		String cookie = null;
		String newUser = "prabath2";

		System.setProperty("javax.net.ssl.trustStore", "wso2domain.jks");
		System.setProperty("javax.net.ssl.trustStorePassword", "star123");

		try {
			configContext = ConfigurationContextFactory.createConfigurationContextFromFileSystem("repo", "repository/axis2/axis2_client.xml");
			authstub = new AuthenticationAdminStub(configContext, SERVER_URL + "AuthenticationAdmin");

			// Authenticates as a user having rights to add users.
			if (authstub.login("admin", "admin", "192.168.2.171")) {
				cookie = (String) authstub._getServiceClient().getServiceContext().getProperty( HTTPConstants.COOKIE_STRING);

				UserRealm realm = WSRealmBuilder.createWSRealm(SERVER_URL, cookie, configContext);
				UserStoreManager storeManager = realm.getUserStoreManager();

				// Add a new role - with no users - with APP_ID as the role name

				if (!storeManager.isExistingRole(APP_ID)) {
					storeManager.addRole(APP_ID, null, null);
					System.out.println("The role added successfully to the system");
				} else {
					System.out.println("The role trying to add - alraedy there in the system");
				}

				if (!storeManager.isExistingUser(newUser)) {
					// Let's the this user to APP_ID role we just created.

					// First let's create claims for users.
					// If you are using a claim that does not exist in default IS instance,
					Map<String, String> claims = new HashMap<String, String>();

					// TASK-1 and TASK-2 should be completed by now.
					// Here I am using an already existing claim
					claims.put("http://wso2.org/claims/businessphone", "0112842302");

					// Here we pass null for the profile - so it will use the default profile.
					storeManager.addUser(newUser, "password", new String[] { APP_ID, "loginOnly" }, claims, null);
					System.out.println("The use added successfully to the system");
				} else {
					System.out.println("The user trying to add - alraedy there in the system");
				}

				// Now let's see the given user [newUser] belongs to the role APP_ID.
				String[] userRoles = storeManager.getRoleListOfUser(newUser);
				boolean found = false;

				if (userRoles != null) {
					for (int i = 0; i < userRoles.length; i++) {
						if (APP_ID.equals(userRoles[i])) {
							found = true;
							System.out.println("The user is in the required role");
							break;
						}
					}
				}
				
				if (!found){
					System.out.println("The user is NOT in the required role");
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
