package com.example.myapplication;

import static android.content.ContentValues.TAG;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.mlkit.common.model.DownloadConditions;
import com.google.mlkit.nl.languageid.LanguageIdentification;
import com.google.mlkit.nl.languageid.LanguageIdentifier;
import com.google.mlkit.nl.translate.TranslateLanguage;
import com.google.mlkit.nl.translate.Translation;
import com.google.mlkit.nl.translate.Translator;
import com.google.mlkit.nl.translate.TranslatorOptions;
import com.google.mlkit.vision.text.TextRecognizer;

import java.util.HashMap;
import java.util.Locale;

public class ResultsActivity extends AppCompatActivity
        implements TextToSpeech.OnInitListener {

    private TextRecognizer recognizer;
    private TextView textView;
    private TextView textView_translate;

    private ImageButton voice_t;
    private ImageButton voice_d;
    private ImageButton fab_translate;

    private LinearLayout translate_cont;

    private TextToSpeech mTts;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_results);

        Intent checkIntent = new Intent();
        checkIntent.setAction(TextToSpeech.Engine.ACTION_CHECK_TTS_DATA);
        startActivityForResult(checkIntent, 500);
        HashMap<String, String> myHashAlarm = new HashMap();

        textView = findViewById(R.id.detect_text);
        textView_translate = findViewById(R.id.translate_text);
        voice_d = findViewById(R.id.voice_btn_d);
        voice_t = findViewById(R.id.voice_btn_t);
        fab_translate = findViewById(R.id.fab_translate);
        translate_cont = findViewById(R.id.translate_container);
        translate_cont.setVisibility(View.GONE);

        String detected = getIntent().getStringExtra("result");

        textView.setText(detected);

        voice_t.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mTts.isLanguageAvailable(Locale.forLanguageTag(TranslateLanguage.ARABIC))
                        == TextToSpeech.LANG_AVAILABLE) {


                    HashMap<String, String> params = new HashMap<String, String>();

                    params.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID,"stringId");
                    mTts.speak(textView_translate.getText().toString()
                            , TextToSpeech.QUEUE_FLUSH, params);
                    voice_d.setVisibility(View.GONE);
                    voice_t.setVisibility(View.GONE);
                }
                else{
                    Toast.makeText(getApplicationContext(),"no voice "
                            ,Toast.LENGTH_LONG).show();
                }
            }
        });

        voice_d.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LanguageIdentifier languageIdentifier = LanguageIdentification.getClient();
                languageIdentifier.identifyLanguage(textView.getText().toString()).addOnSuccessListener(new OnSuccessListener<String>() {
                    @Override
                    public void onSuccess(String languageCode) {
                        if (languageCode.equals("und")) {
                            Log.i(TAG, "Can't identify language.");
                        } else {
                            if (mTts.isLanguageAvailable(new Locale(languageCode))
                                    == TextToSpeech.LANG_AVAILABLE) {

                                HashMap<String, String> params = new HashMap<String, String>();

                                params.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID,"stringId");
                                mTts.speak(detected, TextToSpeech.QUEUE_FLUSH, params);
                                voice_d.setVisibility(View.GONE);
                                voice_t.setVisibility(View.GONE);
                            }
                        }
                    }
                });



            }

        });
        fab_translate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                translate_cont.setVisibility(View.VISIBLE);
                fab_translate.setVisibility(View.GONE);
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
                                                            translate_cont.setVisibility(View.VISIBLE);
                                                        }
                                                    });
                                        }
                                    });
                        }
                    }
                });


            }
        });

    }
    protected void onActivityResult(
            int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 500) {
            if (resultCode == TextToSpeech.Engine.CHECK_VOICE_DATA_PASS) {
                // success, create the TTS instance
                mTts = new TextToSpeech(this.getApplicationContext(),this);

            } else {
                // missing data, install it
                Intent installIntent = new Intent();
                installIntent.setAction(
                        TextToSpeech.Engine.ACTION_INSTALL_TTS_DATA);
                startActivity(installIntent);
            }
        }
    }
    public void onUtteranceCompleted(String uttId) {

    }

    @Override
    public void onInit(int status) {

        mTts.setOnUtteranceProgressListener(new UtteranceProgressListener() {
            @Override
            public void onStart(String utteranceId) {

            }

            @Override
            public void onDone(String utteranceId) {
                runOnUiThread(new Runnable() {

                    @Override
                    public void run() {
                        //UI changes

                        voice_d.setVisibility(View.VISIBLE);
                        voice_t.setVisibility(View.VISIBLE);

                    }
                });
            }

            @Override
            public void onError(String utteranceId) {
                Toast.makeText(getApplicationContext(),"error "
                        ,Toast.LENGTH_LONG).show();
            }
        });

    }
}