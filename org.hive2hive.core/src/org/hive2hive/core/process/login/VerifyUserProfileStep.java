package org.hive2hive.core.process.login;

import org.hive2hive.core.model.UserProfile;
import org.hive2hive.core.process.ProcessStep;
import org.hive2hive.core.process.common.get.GetLocationsStep;
import org.hive2hive.core.process.common.get.GetUserProfileStep;

/**
 * Verifies the answer of the @link{GetUserProfileStep}. If the user profile is not found or could not be
 * decrypted, the process is stopped. Otherwise, it continues with adding this node to the locations.
 * 
 * @author Nico
 * 
 */
public class VerifyUserProfileStep extends ProcessStep {

	private GetUserProfileStep userProfileStep;
	private final String userId;

	public VerifyUserProfileStep(String userId) {
		this.userId = userId;
	}

	@Override
	public void start() {
		UserProfile userProfile = userProfileStep.getUserProfile();
		if (userProfile == null) {
			// failed for some reason
			getProcess().stop("User profile not found or wrong password");
		} else if (!userProfile.getUserId().equalsIgnoreCase(userId)) {
			// mismatch the userId (should never happen)
			getProcess().stop("UserId does not match the one in the profile");
		} else {
			// store the profile in the process
			((LoginProcess) getProcess()).getContext().setUserProfile(userProfile);

			// 1. GetLocationsStep: get the locations
			// 2. AddMyselfToLocationsStep: add ourself to the location map
			AddMyselfToLocationsStep addToLocsStep = new AddMyselfToLocationsStep(userId);
			GetLocationsStep locationsStep = new GetLocationsStep(userId, addToLocsStep);
			addToLocsStep.setPreviousStep(locationsStep);

			getProcess().setNextStep(locationsStep);
		}
	}

	@Override
	public void rollBack() {
		// do nothing
	}

	public void setPreviousStep(GetUserProfileStep userProfileStep) {
		this.userProfileStep = userProfileStep;
	}
}