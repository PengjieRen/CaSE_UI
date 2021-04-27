package com.sdu.irlab.chatlabelling.security;

import org.springframework.security.crypto.password.PasswordEncoder;

public class CustomPasswordEncoder  implements PasswordEncoder {

    @Override
    public String encode(CharSequence rawPassword) {
        String encPassword = rawPassword.toString();
        return encPassword;
    }

    @Override
    public boolean matches(CharSequence rawPassword, String encodedPassword) {
        return encode(rawPassword).equals(encodedPassword);
    }
}
