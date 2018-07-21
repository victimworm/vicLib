package giaohangtietkiem.vn.printer;

import android.graphics.Bitmap;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import giaohangtietkiem.vn.printer.Controller.PrinterController;
import giaohangtietkiem.vn.printer.Interfaces.IFSimpleCallbackController;
import giaohangtietkiem.vn.printer.Utils.Constant;
import giaohangtietkiem.vn.printer.Views.CircleView;

public class MainActivity extends AppCompatActivity {

    private Button btnPrinter;
    private ImageView imgPreview;

    private CircleView rippleDot;

    private void setupAnimation() {
        rippleDot = findViewById(R.id.activity_main_riple_dot);
        Animation ripple = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.ripple_animation);
        rippleDot.startAnimation(ripple);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        setupAnimation();

        imgPreview = findViewById(R.id.activity_main_image_view);
        btnPrinter = findViewById(R.id.activity_main_btn_print);
        btnPrinter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Bitmap bm = PrinterController.instance(getApplicationContext()).generateBitmap();
                imgPreview.setImageBitmap(bm);
                PrinterController.instance(getApplicationContext()).printImage(bm, new IFSimpleCallbackController() {
                    @Override
                    public void onSuccess(String message) {
                        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onFailure(String message) {
                        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
                    }
                });

//                PrinterController.instance(getApplicationContext()).checkPrinterConnection(Constant.printerIp, new IFSimpleCallbackController() {
//                    @Override
//                    public void onSuccess(String message) {
//                        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
//                    }
//
//                    @Override
//                    public void onFailure(String message) {
//                        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
//                    }
//                });
            }
        });
    }
}
