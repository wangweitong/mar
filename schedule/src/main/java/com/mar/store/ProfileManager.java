package com.mar.store;

import com.mar.model.Profile;

public interface ProfileManager {

	Profile findByUid(String uid);
	
	void update(String uid, Profile p) throws Exception;
}
