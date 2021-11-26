package com.amitshekhar.tflite;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;

import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class ResultActivity extends AppCompatActivity {

    Button button;
    ImageView imageView;
    private Executor executor = Executors.newSingleThreadExecutor();

    private Classifier classifier;
    private static final String MODEL_PATH = "ad_car_class/ad_model.tflite";
    private static final boolean QUANT = true;
    private static final String LABEL_PATH = "ad_car_class/labels.txt";
    private static final int INPUT_SIZE = 224;

    //프로필 사진 요청코드
    private static final int REQUEST_CODE = 0;
    TextView textViewResult;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result);

        imageView = findViewById(R.id.imageView);

        button = findViewById(R.id.button);
        textViewResult = findViewById(R.id.tv_result);

        button.setOnClickListener(new View.OnClickListener() { //갤러리에 요청코드 보내기
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(intent, REQUEST_CODE);
            }
        });

        initTensorFlowAndLoadModel();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                try {
                    Uri uri = data.getData();
                    Glide.with(getApplicationContext())
                            .asBitmap()
                            .load(uri)
                            .into(new CustomTarget<Bitmap>() {
                                @Override
                                public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                                    imageView.setImageBitmap(Bitmap.createScaledBitmap(resource, 1200, 1000, false));
                                    resource = Bitmap.createScaledBitmap(resource, INPUT_SIZE, INPUT_SIZE, false);
                                    final List<Classifier.Recognition> results = classifier.recognizeImage(resource);
                                    textViewResult.setText(results.toString());
                                }

                                @Override
                                public void onLoadCleared(@Nullable Drawable placeholder) {

                                }
                            });
                } catch (Exception e) {

                }
            } else if (resultCode == RESULT_CANCELED) {
            }

        }
    }

    private void initTensorFlowAndLoadModel() {
        executor.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    classifier = TensorFlowImageClassifier.create(
                            getAssets(),
                            MODEL_PATH,
                            LABEL_PATH,
                            INPUT_SIZE,
                            QUANT);
//                    makeButtonVisible();
                } catch (final Exception e) {
                    throw new RuntimeException("Error initializing TensorFlow!", e);
                }
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        executor.execute(new Runnable() {
            @Override
            public void run() {
                classifier.close();
            }
        });
    }


}