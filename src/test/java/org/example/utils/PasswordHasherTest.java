package org.example.utils;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PasswordHasherTest {

    @Test
    void hashesUseDifferentSaltsAndVerifyOnlyMatchingPasswords() {
        char[] password = "contrasena-segura".toCharArray();

        String firstHash = PasswordHasher.hash(password);
        String secondHash = PasswordHasher.hash(password);

        assertNotEquals(firstHash, secondHash);
        assertFalse(firstHash.contains(new String(password)));
        assertTrue(PasswordHasher.verify(password, firstHash));
        assertFalse(PasswordHasher.verify("otra-contrasena".toCharArray(), firstHash));
    }
}
