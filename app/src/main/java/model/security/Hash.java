package model.security;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Created by sauray on 21/03/15.
 */
public class Hash {

    public static final String sha256(String password) throws NoSuchAlgorithmException{
        MessageDigest digest=null;
        String hash;
        digest = MessageDigest.getInstance("SHA-256");
        digest.update(password.getBytes());
        hash = bytesToHexString(digest.digest());
        return hash;
    }

    // utility function
    private static String bytesToHexString(byte[] bytes) {
        // http://stackoverflow.com/questions/332079
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < bytes.length; i++) {
            String hex = Integer.toHexString(0xFF & bytes[i]);
            if (hex.length() == 1) {
                sb.append('0');
            }
            sb.append(hex);
        }
        return sb.toString();
    }
}
