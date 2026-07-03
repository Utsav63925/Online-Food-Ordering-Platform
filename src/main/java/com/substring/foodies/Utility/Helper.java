package com.substring.foodies.Utility;

import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class Helper {

    public static String generateRandomId(){
        return UUID.randomUUID().toString();
    }
    public static int generateIntRandomId(){
        return UUID.randomUUID().hashCode();
    }

    public static String normalize(String name) {
        return name.toLowerCase().replaceAll("[^a-z0-9]", "");
    }

    public static String hashToken(String token) {
        return DigestUtils.sha256Hex(token);
    }
}
