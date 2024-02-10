package com.example.myapplication;

import static android.content.ContentValues.TAG;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContract;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.mlkit.common.model.DownloadConditions;
import com.google.mlkit.nl.languageid.LanguageIdentification;
import com.google.mlkit.nl.languageid.LanguageIdentifier;
import com.google.mlkit.nl.translate.TranslateLanguage;
import com.google.mlkit.nl.translate.Translation;
import com.google.mlkit.nl.translate.Translator;
import com.google.mlkit.nl.translate.TranslatorOptions;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.text.Text;
import com.google.mlkit.vision.text.TextRecognition;
import com.google.mlkit.vision.text.TextRecognizer;
import com.google.mlkit.vision.text.latin.TextRecognizerOptions;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.text.SimpleDateFormat;
import java.util.Date;

public class MainActivity extends AppCompatActivity {

    private static final int CAMERA_REQUEST = 1888;
    private ImageButton fab_camera ;
    private ImageButton fab_gallery ;
    private ImageButton fab_detect ;
    private ImageButton fab_translate ;
    private ImageView imageView;

    /*
    private TextView textView;
    private TextView textView_translate;

     */

    private ActivityResultLauncher<String> mgetImage;

    private ActivityResultLauncher<Uri> mtakePhoto;

    private File file;
    private Uri photoURI;

    private InputImage image;

    private TextRecognizer recognizer;


    @Override
    protected void onStop() {
        super.onStop();

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        fab_camera = findViewById(R.id.fab_camera);
        fab_gallery = findViewById(R.id.fab_gallery);
        fab_detect = findViewById(R.id.fab_detect);
        fab_translate = findViewById(R.id.fab_translate);
        imageView = findViewById(R.id.imageView);
        /*
        textView = findViewById(R.id.txt);
        textView_translate = findViewById(R.id.txt_translation);

         */
        recognizer = TextRecognition.getClient
                (TextRecognizerOptions.DEFAULT_OPTIONS);






        mgetImage = registerForActivityResult(new ActivityResultContracts.GetContent(),
                new ActivityResultCallback<Uri>() {
                    @Override
                    public void onActivityResult(Uri o) {
                        imageView.setImageURI(o);
                        photoURI = o;
                        if(photoURI != null) {
                            fab_detect.setVisibility(View.VISIBLE);
                        }
                    }
                });

        fab_gallery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mgetImage.launch("image/*");
            }
        });

        mtakePhoto = registerForActivityResult(new ActivityResultContracts.TakePicture(), new ActivityResultCallback<Boolean>() {
            @Override
            public void onActivityResult(Boolean o) {
                imageView.setImageURI(photoURI);
                if(photoURI != null) {
                    fab_detect.setVisibility(View.VISIBLE);
                }

            }
        });


        fab_camera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                photoURI = null;
                file = null ;
                    try {
                        file = createImageFile(v.getContext());
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }

                if (file != null) {
                    photoURI  = FileProvider.getUriForFile(v.getContext(), getApplicationContext().getPackageName() + ".provider", file);

                    deleteFile(file.getName());
                    mtakePhoto.launch(photoURI);

                }
                imageView.setImageURI(photoURI);


            }
        });


        fab_detect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    image = InputImage.fromFilePath(v.getContext(),photoURI);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                detect(image,recognizer);
            }
        });

        /*

        fab_translate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LanguageIdentifier languageIdentifier = LanguageIdentification.getClient();
                languageIdentifier.identifyLanguage(textView.getText().toString()).addOnSuccessListener(new OnSuccessListener<String>() {
                    @Override
                    public void onSuccess(String languageCode) {
                        if (languageCode.equals("und")) {
                            Log.i(TAG, "Can't identify language.");
                        } else {
                            TranslatorOptions options = new TranslatorOptions.Builder()
                                    .setSourceLanguage(TranslateLanguage.fromLanguageTag(languageCode))
                                    .setTargetLanguage(TranslateLanguage.ARABIC)
                                    .build();
                            final Translator toArabicTranslator =
                                    Translation.getClient(options);
                            DownloadConditions conditions = new DownloadConditions.Builder()
                                    .requireWifi()
                                    .build();
                            toArabicTranslator.downloadModelIfNeeded(conditions)
                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void unused) {
                                    toArabicTranslator.translate(textView.getText().toString())
                                            .addOnSuccessListener(new OnSuccessListener<String>() {
                                        @Override
                                        public void onSuccess(String s) {
                                            textView_translate.setText(s);
                                            toArabicTranslator.close();
                                        }
                                    });
                                }
                            });
                        }
                    }
                });


            }
        });

         */







    }


    public void detect (InputImage image ,TextRecognizer recognizer ){

        Task<Text> results = recognizer.process(image).addOnSuccessListener(new OnSuccessListener<Text>() {
            @Override
            public void onSuccess(Text result) {

                String detect_result ;
                detect_result = result.getText().toString();
                Intent i = new Intent(MainActivity.this
                        ,ResultsActivity.class);
                i.putExtra("result",detect_result);
                startActivity(i);

                //textView.setText( result.getText().toString());

                /*
                for (Text.TextBlock block : result.getTextBlocks()) {
                    String blockText = block.getText();
                    Point[] blockCornerPoints = block.getCornerPoints();
                    Rect blockFrame = block.getBoundingBox();
                    for (Text.Line line : block.getLines()) {
                        String lineText = line.getText();
                        Point[] lineCornerPoints = line.getCornerPoints();
                        Rect lineFrame = line.getBoundingBox();
                        for (Text.Element element : line.getElements()) {
                            String elementText = element.getText();
                            Point[] elementCornerPoints = element.getCornerPoints();
                            Rect elementFrame = element.getBoundingBox();
                            for (Text.Symbol symbol : element.getSymbols()) {
                                String symbolText = symbol.getText();
                                Point[] symbolCornerPoints = symbol.getCornerPoints();
                                Rect symbolFrame = symbol.getBoundingBox();
                            }
                        }
                    }
                }*/
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                //textView.setText("couldn't detect any text!");
            }
        });
    }

    public static File createImageFile(Context context) throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = timeStamp;
        File storageDir = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
                ;
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir     /* directory */
        );
        return image;
    }



}