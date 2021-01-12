/*
 * Created By Jong Ho, Lee on  2020.
 * Copyright 테크하임(주). All rights reserved.
 */

package x.com.nubextalk.Manager.FireBase;

import android.util.Log;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.Date;
import java.util.Map;

import androidx.annotation.NonNull;

import io.realm.Realm;
import x.com.nubextalk.Manager.DateManager;
import x.com.nubextalk.Manager.UtilityManager;
import x.com.nubextalk.Model.ChatContent;
import x.com.nubextalk.Model.ChatRoom;
import x.com.nubextalk.Model.Config;
import x.com.nubextalk.Module.Adapter.ChatAdapter;

/**
 * Firebase Message Service
 * - onNewToken : Token 이 갱신될때 호출
 * - onMessageReceived : FCM이 수시신될떄 호출
 * - 참고 : https://firebase.google.com/docs/cloud-messaging/android/client?authuser=0
 */
public class FirebaseMsgService extends FirebaseMessagingService {

    @Override
    public void onNewToken(String s) {
        super.onNewToken(s);
        Log.d("FCM_TOKEN_OnNew : ", s);

//        Realm realm = Realm.getInstance(UtilityManager.getRealmConfig());
//        realm.executeTransaction(new Realm.Transaction() {
//            @Override
//            public void execute(Realm realm) {
//                Config userMe = realm.where(Config.class).equalTo("CODE", "USER_ME").findFirst();
//                if (userMe == null) {
//                    userMe = new Config();
//                    userMe.setCODENAME("USER");
//                    userMe.setCODE("USER_ME");
//                }
//                userMe.setExt1(s);
//
//                realm.copyToRealmOrUpdate(userMe);
//            }
//        });


        /**
         * FCM Token 확인 함수
         */
        /*FirebaseInstanceId.getInstance().getInstanceId()
                .addOnCompleteListener(new OnCompleteListener<InstanceIdResult>() {
                    @Override
                    public void onComplete(@NonNull Task<InstanceIdResult> task) {
                        if (!task.isSuccessful()) {
                            Log.w("TOKEN", "getInstanceId failed", task.getException());
                            return;
                        }
                        // Get new Instance ID token
                        String token = task.getResult().getToken();
                        Log.d("TOKEN", token);
                    }
                });*/
    }

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        Realm realm = Realm.getInstance(UtilityManager.getRealmConfig());
        DateManager dm = new DateManager();
        Map<String, String> data = remoteMessage.getData();
        Log.d("TOKEN", "RECEIVE_TOKEN\nCODE : " + data.get("CODE") + "\nDATE : " + data.get("date"));
        switch (data.get("CODE")) {

            case "CHAT_CONTENT_CREATED": //chat 받았을 때
                realm.executeTransaction(new Realm.Transaction() {
                    @Override
                    public void execute(Realm realm) {
                        ChatRoom roomInfo = realm.where(ChatRoom.class).equalTo("rid", data.get("chatRoomId")).findFirst();
                        roomInfo.setUpdatedDate(new Date());
                        realm.copyToRealmOrUpdate(roomInfo);

                        ChatContent chat = new ChatContent();

                        chat.setCid(data.get("chatContentId")); // Content ID 자동으로 유니크한 값 설정
                        chat.setUid(data.get("senderId")); // UID 보내는 사람
                        chat.setRid(data.get("chatRoomId")); // RID 채팅방 아이디
                        chat.setType(Integer.parseInt(data.get("contentType")));
                        chat.setContent(data.get("content"));
                        chat.setSendDate(DateManager.
                                convertDatebyString(data.get("sendDate"), "yyyy-MM-dd'T'hh:mm:ss"));
                        chat.setFirst(Boolean.parseBoolean(data.get("isFirst")));
                        realm.copyToRealmOrUpdate(chat);
                    }
                });

                break;
            case "FIRST_CHAT_CREATED": //채팅방이 생성되고 처음 메세지가 생성된 경우 채팅방과 채팅메세지 생성
                FirebaseFunctionsManager.getChatRoom(data.get("hospitalId"), data.get("chatRoomId"));
                realm.executeTransaction(new Realm.Transaction() {
                    @Override
                    public void execute(Realm realm) {
                        ChatContent notifyChat = new ChatContent();
                        notifyChat.setCid("sys".concat(data.get("chatContentId")));
                        notifyChat.setRid(data.get("chatRoomId"));
                        notifyChat.setType(9);
                        notifyChat.setContent("채팅방이 개설되었습니다.");
                        notifyChat.setSendDate(DateManager.
                                convertDatebyString(data.get("sendDate"), "yyyy-MM-dd'T'hh:mm:ss"));
                        notifyChat.setIsRead(true);
                        notifyChat.setFirst(false);
                        realm.copyToRealmOrUpdate(notifyChat);

                        ChatContent chat = new ChatContent();
                        chat.setCid(data.get("chatContentId"));
                        chat.setRid(data.get("chatRoomId"));
                        chat.setUid(data.get("senderId"));
                        chat.setType(Integer.parseInt(data.get("contentType")));
                        chat.setContent(data.get("content"));
                        chat.setSendDate(DateManager.
                                convertDatebyString(data.get("sendDate"), "yyyy-MM-dd'T'hh:mm:ss"));
                        chat.setFirst(Boolean.parseBoolean(data.get("isFirst")));
                        realm.copyToRealmOrUpdate(chat);
                    }
                });

            case "CHAT_ROOM_INVITED":
                Log.d("TOKEN", "room invited!!");

                // Chatting Message(Notification Message)
                // System Message
        }
    }
}
