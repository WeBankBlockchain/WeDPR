package com.webank.wedpr.components.token.auth.demo;

import com.webank.wedpr.components.token.auth.TokenUtils;
import com.webank.wedpr.components.token.auth.model.TokenContents;
import com.webank.wedpr.components.token.auth.model.UserToken;

/** Created by caryliao on 2024/7/26 16:03 */
public class Demo {
    public static void main(String[] args) throws Exception {
        String token =
                "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJleHAiOjE3MjIwMTc1MTgsInVzZXIiOiJ7XCJ1c2VyXCI6XCJhbGljZVwiLFwicGFzc3dkXCI6bnVsbCxcInVzZXJHcm91cFwiOlwieHh4eFwiLFwicm9sZVwiOlwiYWRtaW5fdXNlclwiLFwiYWRtaW5cIjpmYWxzZX0ifQ.KH-QAKNUr4qD9DGWogjWUR01seqKIiq_ItyzYI198lo";
        TokenContents tokenContent = TokenUtils.getJWTTokenContent(token);
        System.out.println("tokenContent = " + tokenContent);
        UserToken userToken = tokenContent.getUserToken();
        System.out.println("userToken = " + userToken);
    }
}
