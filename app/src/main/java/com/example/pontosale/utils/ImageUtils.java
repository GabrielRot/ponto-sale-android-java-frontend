package com.example.pontosale.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.Toast;

import com.example.pontosale.ui.CadastrarUsuario;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class ImageUtils {

    public static byte[] getImageBytes(Context context, Uri uri, int width, int height) throws IOException {
        Bitmap bitmap = MediaStore.Images.Media.getBitmap(context.getContentResolver(), uri);

        bitmap = Bitmap.createScaledBitmap(bitmap, width, height, true);

        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 70, stream);

        return stream.toByteArray();
    }

    public static byte[] resizeUriImage(Context context, Uri uri, int width, int height) {
        try {
            return getImageBytes(context, uri,width, height);
        } catch (Exception e) {
            Log.e("imageResize", e.getMessage());

            return null;
        }
    }

    public static Bitmap resizeBitmapImage(Bitmap bitmap, int maxWidth, int maxHeight) {
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();

        float ratio = Math.min(
                (float) maxWidth / width,
                (float) maxHeight / height
        );

        int newWidth = Math.round(width * ratio);
        int newHeight = Math.round(width * ratio);

        return Bitmap.createScaledBitmap(bitmap, newWidth, newHeight, true);
    }

    public static File resizeFileImage(File file, int maxWidth, int maxHeight) {
        Bitmap bitmap = BitmapFactory.decodeFile(file.getAbsolutePath());

        bitmap = resizeBitmapImage(bitmap, maxWidth, maxHeight);

        File outputFile = new File(file.getPath());

        try {
            FileOutputStream out = new FileOutputStream(outputFile);

            bitmap.compress(Bitmap.CompressFormat.JPEG, 70, out);

            out.flush();
            out.close();

            return outputFile;
        } catch (Exception e) {
            return null;
        }
    }

}
