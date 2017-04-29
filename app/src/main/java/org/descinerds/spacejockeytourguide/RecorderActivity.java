package org.descinerds.spacejockeytourguide;

import android.app.AlertDialog;
import android.app.Service;
import android.content.DialogInterface;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Vibrator;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.github.chrisbanes.photoview.PhotoView;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.io.IOException;

public class RecorderActivity extends AppCompatActivity implements View.OnClickListener,
        MediaPlayer.OnCompletionListener {

    private static final int VIBRATE_TIME = 150;

    private String path;

    private ImageView btn_record, btn_play, btn_stop, btn_delete, btn_upload;
    private ProgressBar progressbar;
    private TextView text_record_info;

    private Vibrator vibrator;
    private MediaRecorder recorder;
    private MediaPlayer player;
    private final Handler handler = new Handler();
    private boolean is_recording = false;
    private boolean is_playing = false;
    private int record_position = 0;
    private String fullFilePath;
    private String recordFilePath;
    private String userId;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recorder);

        PhotoView photoView = (PhotoView) findViewById(R.id.photo_view);
        btn_record = (ImageView) findViewById(R.id.btn_record);
        btn_play = (ImageView) findViewById(R.id.btn_play);
        btn_stop = (ImageView) findViewById(R.id.btn_stop);
        btn_delete = (ImageView) findViewById(R.id.btn_delete);
        btn_upload = (ImageView) findViewById(R.id.btn_upload);
        progressbar = (ProgressBar) findViewById(R.id.progressbar);
        text_record_info = (TextView) findViewById(R.id.text_record_info);
        btn_record.setOnClickListener(this);
        btn_play.setOnClickListener(this);
        btn_stop.setOnClickListener(this);
        btn_delete.setOnClickListener(this);
        btn_upload.setOnClickListener(this);
        vibrator = (Vibrator) getSystemService(Service.VIBRATOR_SERVICE);

        String imageUrl = getIntent().getStringExtra("IMAGE");
        Glide.with(this).load(imageUrl).into(photoView);

        userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        path = getIntent().getStringExtra(Keys.Companion.getPATH());
        recordFilePath = path + "/" + userId + "_record.mp4";
        fullFilePath = getFilesDir() + "/" + recordFilePath;
        File dir = new File(getFilesDir()+"/"+path);
        if (!dir.exists()) dir.mkdirs();

        checkRecordExist();
    }

    private void checkRecordExist() {
        if (new File(fullFilePath).exists()) {
            btn_play.setEnabled(true);
            btn_delete.setEnabled(true);
        } else {
            btn_play.setEnabled(false);
            btn_delete.setEnabled(false);
        }
        btn_stop.setEnabled(false);
    }

    @Override
    public void onClick(View v) {
        if (v == btn_record) {
            vibrator.vibrate(VIBRATE_TIME);

            startRecord();
        } else if (v == btn_play) {
            vibrator.vibrate(VIBRATE_TIME);

            playRecord();
        } else if (v == btn_stop) {
            vibrator.vibrate(VIBRATE_TIME);

            stopRecord();
        } else if (v == btn_delete) {
            vibrator.vibrate(VIBRATE_TIME);

            openSureDeleteRecordDialog();
        } else if (v == btn_upload) {
            vibrator.vibrate(VIBRATE_TIME);

            uploadRecord();
        }
    }

    private void startRecord() {
        is_recording = true;

        recorder = new MediaRecorder();

        recorder.setAudioSource(MediaRecorder.AudioSource.DEFAULT);
        recorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
        recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
        MediaRecorder.getAudioSourceMax();
        recorder.setAudioSamplingRate(22050);// 44100
        recorder.setAudioEncodingBitRate(24000);// 96000
        recorder.setOutputFile(fullFilePath);

        record_position = 0;
        showMediaRecorderProgress();

        try {
            recorder.prepare();
            recorder.start();
        } catch (IOException e) {
            e.printStackTrace();
        }

        btn_play.setEnabled(false);
        btn_record.setEnabled(false);
        btn_stop.setEnabled(true);
        btn_delete.setEnabled(false);
    }

    private void playRecord() {
        is_playing = true;

        player = new MediaPlayer();
        player.setOnCompletionListener(this);
        player.stop();
        player.reset();
        try {
            player.setDataSource(fullFilePath);
            player.prepare();
            player.start();
        } catch (IllegalArgumentException | SecurityException | IllegalStateException | IOException e) {
            e.printStackTrace();
        }

        player.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {

            @Override
            public void onPrepared(MediaPlayer mp) {
                if (mp != null) {
                    int record_length = player.getDuration();
                    progressbar.setMax(record_length);
                    showMediaPlayerProgress(record_length);
                }
            }
        });

        btn_record.setEnabled(false);
        btn_play.setEnabled(false);
        btn_stop.setEnabled(true);
        btn_delete.setEnabled(false);
    }

    private void stopRecord() {
        try {
            if (is_recording && !is_playing) {
                recorder.stop();
                recorder.reset();
                recorder.release();
                recorder = null;
            } else if (!is_recording && is_playing) {
                player.stop();
                player.reset();
                player.release();
                player = null;
            }

            is_recording = false;
            is_playing = false;

            btn_play.setEnabled(true);
            btn_record.setEnabled(true);
            btn_stop.setEnabled(false);
            btn_delete.setEnabled(true);
        } catch (IllegalStateException e) {
            e.printStackTrace();
        }
    }

    private void openSureDeleteRecordDialog() {
        new AlertDialog.Builder(this)
                .setIcon(android.R.drawable.ic_menu_info_details)
                .setTitle(R.string.warning)
                .setMessage(R.string.sure_delete_record)
                .setPositiveButton(android.R.string.ok,
                        new DialogInterface.OnClickListener() {

                            @Override
                            public void onClick(DialogInterface dialog,
                                                int which) {
                                new File(fullFilePath).delete();

                                btn_play.setEnabled(false);
                                btn_delete.setEnabled(false);
                            }
                        }).setNegativeButton(android.R.string.cancel, null).show();
    }

    private void uploadRecord() {
        final StorageReference ref = FirebaseStorage.getInstance().getReference(recordFilePath);
        ref.putFile(Uri.fromFile(new File(fullFilePath))).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                Toast.makeText(RecorderActivity.this, "上傳檔案成功!", Toast.LENGTH_SHORT).show();

                setRecordUrl(ref);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(RecorderActivity.this, "上傳檔案失敗!", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setRecordUrl(StorageReference ref) {
        ref.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                DatabaseReference ref = FirebaseDatabase.getInstance()
                        .getReference(path).child("recordings").child(userId);
                ref.child("url").setValue(uri.toString());
                ref.child("like").setValue(0);

                Toast.makeText(RecorderActivity.this, "設定資料庫成功!", Toast.LENGTH_SHORT).show();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                e.printStackTrace();
                Toast.makeText(RecorderActivity.this, "設定資料庫失敗!", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        if (mp != null) {
            stopRecord();
        }
    }

    private void showMediaPlayerProgress(final int record_length) {
        if (player != null) {
            int current_position = player.getCurrentPosition();
            progressbar.setProgress(current_position);
            setRecordInfoText(current_position, record_length);

            if (player.isPlaying()) {
                Runnable runnable = new Runnable() {

                    @Override
                    public void run() {
                        showMediaPlayerProgress(record_length);
                    }
                };
                handler.postDelayed(runnable, 250);
            }
        } else {
            progressbar.setProgress(0);
            setRecordInfoText(0, 0);
        }
    }

    private void showMediaRecorderProgress() {
        if (recorder != null) {
            if (is_recording) {
                setRecordInfoText(record_position * 1000, 0);
                record_position++;
                Runnable runnable = new Runnable() {

                    @Override
                    public void run() {
                        showMediaRecorderProgress();
                    }
                };
                handler.postDelayed(runnable, 1000);
            }
        }
    }

    private void setRecordInfoText(int current_position, int length) {
        if (current_position > 0) {
            if (length > 0) {
                text_record_info.setText(String.format(
                        "(%02d:%02d) / (%02d:%02d)",
                        current_position / 1000 / 60, current_position / 1000,
                        length / 1000 / 60, length / 1000));
            } else {
                text_record_info.setText(String.format("(%02d:%02d)",
                        current_position / 1000 / 60, current_position / 1000));
            }
        } else {
            text_record_info.setText("");
        }
    }
}
