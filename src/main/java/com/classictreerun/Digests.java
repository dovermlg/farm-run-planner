package com.classictreerun;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/** Stable local fingerprints without per-byte formatter allocation. */
final class Digests
{
    private Digests() { }
    static String sha256(String value)
    {
        try
        {
            byte[] hash=MessageDigest.getInstance("SHA-256").digest(value.getBytes(StandardCharsets.UTF_8));
            char[] hex="0123456789abcdef".toCharArray(),text=new char[hash.length*2];
            for(int i=0;i<hash.length;i++){int part=hash[i]&255;text[i*2]=hex[part>>>4];text[i*2+1]=hex[part&15];}
            return new String(text);
        }
        catch(NoSuchAlgorithmException impossible){throw new IllegalStateException(impossible);}
    }
}
