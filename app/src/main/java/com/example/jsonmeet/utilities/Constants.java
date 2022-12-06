package com.example.jsonmeet.utilities;

import java.util.HashMap;

public class Constants {

    public static final String KEY_COLLECTION_USERS = "users";
    public static final String KEY_FIRST_NAME = "first_name";
    public static final String KEY_LAST_NAME = "last_name";
    public static final String KEY_EMAIL = "email";
    public static final String KEY_PASSWORD = "password";
    public static final String KEY_ID = "user_id";

    public static final String KEY_TOKEN = "fcm_token";
    public static final String KEY_PREFERENCE_NAME = "json";
    public static final String KEY_SIGNED_IN = "isSignedIn";
    public static final String REMOTE_MSG_AUTHORIZATION = "Authorization";
    public static final String REMOTE_MSG_CONTENT_TYPE = "Content-Type";

    public static final String REMOTE_MSG_TYPE = "type";
    public static final String REMOTE_INVITATION_="invitation";
    public static final String REMOTE_MSG_MEETING_TYPE = "meetingType";
    public static final String REMOTE_MSG_INVITER_TOKEN = "inviterToken";
    public static final String REMOTE_MSG_DATA = "data";
    public static final String REMOTE_MSG_REGISTRATION_IDS = "registration_ids";

    public static final String REMOTE_INVITATION_RESPONSE = "invitationResponse";
    public static final String REMOTE_INVITATION_ACCEPTED = "accepted";
    public static final String REMOTE_INVITATION_REJECTED = "rejected";
    public static final String REMOTE_INVITATION_CANCELLED = "cancelled";

    public static final String MEETING_ROOM = "meetingRoom";

    public static HashMap<String, String> getRemoteMessageHeader() {
        HashMap<String, String> headers = new HashMap<>();
        headers.put(
                Constants.REMOTE_MSG_AUTHORIZATION,
                "key=AAAAQ73GvCs:APA91bHIeiF1htH-DeKB6ixEaR_DQo8uTA7QM8aqure-SwabeyGmFhBzGom9kbj-2Xu0M9WKYjJX5Z4M5FQiayIA3YN-ICvZj4uxXhVkfv3ckS2zZt2F9B7YX1EIyzoqETMS1OK-F2gu"
        );
        headers.put(Constants.REMOTE_MSG_CONTENT_TYPE, "application/json");
        return headers;
    }

}
