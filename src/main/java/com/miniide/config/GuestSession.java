package com.miniide.config;

import java.util.UUID;

public class GuestSession {

    public static String createGuestId() {

        return UUID.randomUUID().toString();

    }

}