package org.j_keepass.util;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;

import androidx.core.content.FileProvider;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class ByteArrayOpener {

    public static void openByteArrayWithOtherApp(Context context, byte[] byteArray, String fileName) {
        // Step 1: Write the byte array to a temporary file
        File tempFile = new File(context.getExternalCacheDir(), fileName);
        try (FileOutputStream fos = new FileOutputStream(tempFile)) {
            fos.write(byteArray);
            fos.flush();
        } catch (IOException e) {
            e.printStackTrace();
            return; // Exit if there's an error writing the file
        }

        // Step 2: Create a content URI using FileProvider
        Uri uri = FileProvider.getUriForFile(context, context.getPackageName() + ".provider", tempFile);

        // Step 3: Create an intent to open the file
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setDataAndType(uri, getMimeType(fileName));
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

        // Start the activity to open the file
        context.startActivity(Intent.createChooser(intent, "Open with"));
    }

    private static String getMimeType(String fileName) {
        if (fileName.endsWith(".pdf")) {
            return "application/pdf";
        } else if (fileName.endsWith(".jpg") || fileName.endsWith(".jpeg")) {
            return "image/jpeg";
        } else if (fileName.endsWith(".png")) {
            return "image/png";
        } else if (fileName.endsWith(".txt")) {
            return "text/plain";
        }
        return "*/*"; // Default MIME type
    }
}
