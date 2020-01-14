package com.oblivion.studio;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import com.oblivion.editor.R;
import com.oblivion.studio.utility.FileUtility;
import java.io.File;
import java.io.IOException;
import java.text.DecimalFormat;


import id.zelory.compressor.Compressor;

public class MainActivity extends AppCompatActivity {

    private int PICK_MEDIA_GALLERY = 1001;
    private final int PERMISSION_CODE = 1000;
    private ImageView imageView1, imageView2;
    private TextView textView1, textView2;
    private EditText quality_edt;

    File actualImage;
    private File compressedImage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        imageView1 = findViewById(R.id.gambar_asli);
        imageView2 = findViewById(R.id.gambar_compress);
        textView1 = findViewById(R.id.uk_asli);
        textView2 = findViewById(R.id.uk_compress);
        quality_edt = findViewById(R.id.quality_id);


    }

    public void pilihGambar_btn(View view) {

        if (Build.VERSION.SDK_INT>= Build.VERSION_CODES.M){
            if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED
                    || checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) ==
                    PackageManager.PERMISSION_DENIED){

                String[] permission = {Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE};

                requestPermissions(permission, PERMISSION_CODE);

            }else {
                //permission granted
                galleryOpen();
            }
        }else {
            galleryOpen();
        }
    }

    public void compress_btn(View view) {

        String qty = quality_edt.getText().toString();

        if (qty.isEmpty()){
            quality_edt.setError("Silahkan masukkan kualitas gambar");
        }else if (Integer.parseInt(qty) > 100){
            quality_edt.setError("Kualitas tidak boleh melebihi 100");
        }else if (actualImage == null) {
            Toast.makeText(this, "Anda belum memilih gambar", Toast.LENGTH_SHORT).show();
        }
        else {

            try {
                compressedImage = new Compressor(this)
                        .setMaxWidth(640)
                        .setMaxHeight(480)
                        .setQuality(Integer.parseInt(qty))
                        .setCompressFormat(Bitmap.CompressFormat.JPEG)
                        .setDestinationDirectoryPath(Environment.getExternalStoragePublicDirectory(
                                Environment.DIRECTORY_PICTURES).getAbsolutePath())
                        .compressToFile(actualImage);

                setCompressedImage();
            } catch (IOException e) {
                e.printStackTrace();
            }

        }




    }

    private void setCompressedImage() {

        imageView2.setImageBitmap(BitmapFactory.decodeFile(compressedImage.getAbsolutePath()));
        textView2.setText(String.format("Size : %s", getReadableFileSize(compressedImage.length())));

        Toast.makeText(this, "Gambar telah dikompres dan disimpan di dalam " + compressedImage.getPath(), Toast.LENGTH_LONG).show();
        Log.d("Compressor", "Compressed image save in " + compressedImage.getPath());

    }

    private void galleryOpen(){

        Intent intent = new Intent();
        intent.setType("image/*");
       // intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent,"pilih gambar"), PICK_MEDIA_GALLERY);

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_MEDIA_GALLERY && resultCode == RESULT_OK) {
            if (data == null) {
                Toast.makeText(this, "Error", Toast.LENGTH_SHORT).show();
                return;
            }
            try {
                actualImage = FileUtility.from(this, data.getData());
                imageView1.setImageBitmap(BitmapFactory.decodeFile(actualImage.getAbsolutePath()));
                textView1.setText(String.format("Size : %s", getReadableFileSize(actualImage.length())));

            } catch (IOException e) {
                Toast.makeText(this, "Error", Toast.LENGTH_SHORT).show();
                e.printStackTrace();
            }
        }

    }



    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        switch (requestCode) {
            case PERMISSION_CODE:{
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    galleryOpen();
                }else {
                    Toast.makeText(this, "Izin Akses Gallery Diperlukan", Toast.LENGTH_SHORT).show();
                }
            }

        }
    }

    public String getReadableFileSize(long size) {
        if (size <= 0) {
            return "0";
        }
        final String[] units = new String[]{"B", "KB", "MB", "GB", "TB"};
        int digitGroups = (int) (Math.log10(size) / Math.log10(1024));
        return new DecimalFormat("#,##0.#").format(size / Math.pow(1024, digitGroups)) + " " + units[digitGroups];
    }
}
