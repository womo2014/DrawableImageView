package cc.wo_mo.drawableimageviewdemo;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Environment;
import android.os.Vibrator;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Random;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.OnLongClick;
import cc.wo_mo.drawableimageviewdemo.DrawableImageView.DrawOperation;
import cc.wo_mo.drawableimageviewdemo.DrawableImageView.DrawableImageView;

import static android.os.Environment.DIRECTORY_PICTURES;

public class MainActivity extends AppCompatActivity {
    private static final int SELECT_PHOTO = 0;
    private Random mRandom = new Random();
    private Vibrator mVibrator;
    @BindView(R.id.drawable_image_view) DrawableImageView mDrawableImageView;
    @BindView(R.id.draw_button) Button mDrawButton;
    @BindView(R.id.erase_button) Button mEraseButton;
    @BindView(R.id.undo_button) Button mUndoButton;
    @BindView(R.id.redo_button) Button mRedoButton;
    @BindView(R.id.select_photo_button) Button mSelectPhotoButton;
    @BindView(R.id.save_photo_button)  Button mSavePhotoButton;
    @BindView(R.id.edit_mode_button) Button mEditModeButton;
    @BindView(R.id.change_color_button) Button mChangeColorButton;
    @BindView(R.id.pen_width_seek_bar) SeekBar mSeekBar;
    @BindView(R.id.pen_width) TextView mPenWidth;

    @OnClick(R.id.draw_button)
    public void setDrawMode() {
        if (mDrawableImageView.isInEditMode()) {
            mDrawableImageView.setDrawingMode(DrawOperation.DrawingMode.DRAW);
            refreshButtonStatus();
        }
    }

    @OnClick(R.id.erase_button)
    public void setEraseMode() {
        if (mDrawableImageView.isInEditMode()) {
            mDrawableImageView.setDrawingMode(DrawOperation.DrawingMode.ERASE);
            refreshButtonStatus();
        }
    }

    @OnClick(R.id.undo_button)
    public void undo() {
        if (mDrawableImageView.isInEditMode()) {
            mDrawableImageView.undo();
            refreshButtonStatus();
        }
    }

    @OnClick(R.id.redo_button)
    public void redo() {
        if (mDrawableImageView.isInEditMode()) {
            mDrawableImageView.redo();
            refreshButtonStatus();
        }
    }

    @OnClick(R.id.select_photo_button)
    public void selectPhoto() {
        Intent intent = new Intent(Intent.ACTION_PICK, null);
        intent.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*");
        startActivityForResult(intent, SELECT_PHOTO);
    }

    @OnClick(R.id.save_photo_button)
    public void savePhoto() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View view = LayoutInflater.from(this).inflate(R.layout.dialog, null);

        ImageView iv = ButterKnife.findById(view, R.id.image);
        iv.setImageBitmap(mDrawableImageView.createCaptureBitmap());
        builder.setView(view).setTitle("是否保存图片")
                .setNegativeButton("取消", null)
                .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        File image = new File(
                                Environment.getExternalStoragePublicDirectory(DIRECTORY_PICTURES),
                                String.format("paint_%s.png",System.currentTimeMillis()));
                        try {
                            FileOutputStream fos = new FileOutputStream(image);
                            fos.write(mDrawableImageView.createCaptureBytes());
                            fos.flush();
                            fos.close();
                            Toast.makeText(MainActivity.this, "保存成功", Toast.LENGTH_SHORT).show();
                        } catch (FileNotFoundException e) {
                            e.printStackTrace();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }).create().show();
    }

    @OnClick(R.id.edit_mode_button)
    public void toggleEditMode() {
        if (mDrawableImageView.isInEditMode()) {
            mDrawableImageView.setInEditMode(false);
        } else {
            mDrawableImageView.setInEditMode(true);
        }
        refreshButtonStatus();
    }

    @OnClick(R.id.change_color_button)
    public void changePenColor() {
        if (mDrawableImageView.isInEditMode()) {
            mDrawableImageView.setPenColor(0xff000000+mRandom.nextInt(0x00ffffff));
            mPenWidth.setTextColor(mDrawableImageView.getPenColor());
        }
    }

    @OnLongClick(R.id.drawable_image_view)
    public boolean drawLine() {
        Log.d("on long click", "run here");
        if (mDrawableImageView.isInEditMode()) {
            mDrawableImageView.setPenType(DrawOperation.PenType.LINE);
            mVibrator.vibrate(40);
        }
        return true;
    }

    void refreshButtonStatus() {
        if (mDrawableImageView.canUndo()) {
            mUndoButton.setTypeface(null, Typeface.BOLD);
        } else {
            mUndoButton.setTypeface(null, Typeface.NORMAL);
        }
        if (mDrawableImageView.canRedo()) {
            mRedoButton.setTypeface(null, Typeface.BOLD);
        } else {
            mRedoButton.setTypeface(null, Typeface.NORMAL);
        }
        switch (mDrawableImageView.getDrawingMode()) {
            case DRAW:
                mDrawButton.setTypeface(null, Typeface.BOLD);
                mEraseButton.setTypeface(null, Typeface.NORMAL);
                break;
            case ERASE:
                mDrawButton.setTypeface(null, Typeface.NORMAL);
                mEraseButton.setTypeface(null, Typeface.BOLD);
                break;
        }
        if (mDrawableImageView.isInEditMode()) {
            mEditModeButton.setTypeface(null, Typeface.BOLD);
        } else {
            mEditModeButton.setTypeface(null, Typeface.NORMAL);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        init();
    }


    void init() {
        // init vibrator
        mVibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
        // init seek bar
        mSeekBar.setMax(60);
        mSeekBar.setProgress(mDrawableImageView.getPenWidth());
        mSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                mPenWidth.setText(""+progress);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                mDrawableImageView.setPenWidth(seekBar.getProgress());
            }
        });
        mDrawButton.setTypeface(null, Typeface.BOLD);
        mPenWidth.setTextColor(mDrawableImageView.getPenColor());


        // when add a new draw operation, refresh undo and redo button status
        mDrawableImageView.setOnStopDrawingListener(new DrawableImageView.OnStopDrawingListener() {
            @Override
            public void onStopDrawing() {
                refreshButtonStatus();
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == SELECT_PHOTO && resultCode == RESULT_OK) {
            Log.d("select photo", "set image view");
            mDrawableImageView.setImageURI(data.getData());
        }
    }
}
