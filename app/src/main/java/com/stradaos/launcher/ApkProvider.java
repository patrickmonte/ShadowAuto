package com.stradaos.launcher;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.os.ParcelFileDescriptor;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.HashMap;

/** Entrega al instalador de Android 7+ solo los APK que el usuario eligió (content:// en vez de file://). */
public class ApkProvider extends ContentProvider {
    static String AUTH = "com.stradaos.launcher.apk";
    private static final HashMap<String, File> files = new HashMap<String, File>();

    private static final java.security.SecureRandom RNG = new java.security.SecureRandom();

    static Uri share(File f) {
        byte[] b = new byte[12];
        RNG.nextBytes(b);
        StringBuilder sb = new StringBuilder(28);
        for (byte x : b) sb.append(String.format(java.util.Locale.US, "%02x", x & 0xff));
        String token = sb.append(".apk").toString();
        synchronized (files) {
            if (files.size() > 16) files.clear();
            files.put(token, f);
        }
        return Uri.parse("content://" + AUTH + "/" + token);
    }

    @Override public boolean onCreate() { AUTH = getContext().getPackageName() + ".apk"; return true; }

    @Override
    public ParcelFileDescriptor openFile(Uri uri, String mode) throws FileNotFoundException {
        File f;
        synchronized (files) { f = files.get(uri.getLastPathSegment()); }
        if (f == null) throw new FileNotFoundException();
        return ParcelFileDescriptor.open(f, ParcelFileDescriptor.MODE_READ_ONLY);
    }

    @Override public String getType(Uri uri) { return "application/vnd.android.package-archive"; }
    @Override public Cursor query(Uri u, String[] p, String s, String[] a, String o) { return null; }
    @Override public Uri insert(Uri u, ContentValues v) { return null; }
    @Override public int delete(Uri u, String s, String[] a) { return 0; }
    @Override public int update(Uri u, ContentValues v, String s, String[] a) { return 0; }
}
