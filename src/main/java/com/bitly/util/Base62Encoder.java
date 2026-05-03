package com.bitly.util;

import org.springframework.stereotype.Component;

import java.security.SecureRandom;

/**
 * Utility class for generating Base62-encoded short codes.
 * Uses a cryptographically secure random number generator to produce
 * collision-resistant short codes from the character set [0-9a-zA-Z].
 */
@Component
public class Base62Encoder {

    private static final String BASE62_CHARS =
            "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";
    private static final int BASE = BASE62_CHARS.length();
    private static final SecureRandom RANDOM = new SecureRandom();

    /**
     * Generates a random Base62 short code of the specified length.
     * With 7 characters, this yields 62^7 ≈ 3.5 trillion unique combinations.
     *
     * @param length the desired length of the short code
     * @return a random Base62-encoded string
     */
    public String generateShortCode(int length) {
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            sb.append(BASE62_CHARS.charAt(RANDOM.nextInt(BASE)));
        }
        return sb.toString();
    }

    /**
     * Encodes a numeric value into a Base62 string.
     * Useful for deterministic encoding of database IDs.
     *
     * @param value the numeric value to encode
     * @return the Base62-encoded string
     */
    public String encode(long value) {
        if (value == 0) {
            return String.valueOf(BASE62_CHARS.charAt(0));
        }

        StringBuilder sb = new StringBuilder();
        long num = Math.abs(value);
        while (num > 0) {
            sb.append(BASE62_CHARS.charAt((int) (num % BASE)));
            num /= BASE;
        }
        return sb.reverse().toString();
    }

    /**
     * Decodes a Base62 string back to its numeric value.
     *
     * @param encoded the Base62-encoded string
     * @return the decoded numeric value
     * @throws IllegalArgumentException if the string contains invalid characters
     */
    public long decode(String encoded) {
        long result = 0;
        for (char c : encoded.toCharArray()) {
            int index = BASE62_CHARS.indexOf(c);
            if (index == -1) {
                throw new IllegalArgumentException("Invalid Base62 character: " + c);
            }
            result = result * BASE + index;
        }
        return result;
    }
}
