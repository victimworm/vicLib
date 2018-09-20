package giaohangtietkiem.vn.printer;

import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;

import java.util.ArrayList;

import giaohangtietkiem.vn.printer.Controller.SendDataToPrintKeepConnThread;
import giaohangtietkiem.vn.printer.Objects.Package;
import giaohangtietkiem.vn.printer.Utils.Devices.DeviceConstant;
import giaohangtietkiem.vn.printer.Utils.Devices.WifiCommunication;
import giaohangtietkiem.vn.printer.Views.CircleView;

import static giaohangtietkiem.vn.printer.Utils.Devices.WifiCommunication.WFPRINTER_REVMSG;

public class MainActivity extends AppCompatActivity {

    private Button btnPrinter;
    private ImageView imgPreview;

    private CircleView rippleDot;

    public long[] list = {
            297881304,
            222712106,
            269835925,
            241382730
    };

    public static enum PrinterStatus {
        RUN,
        STOP,
        INIT,
        RUNINIT
    }

    private void setupAnimation() {
        rippleDot = findViewById(R.id.activity_main_riple_dot);
        Animation ripple = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.ripple_animation);
        rippleDot.startAnimation(ripple);
    }

    private DeviceConstant.PrinterStatus printerStatus;
    private WifiCommunication wifiCommunication = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        setupAnimation();

        imgPreview = findViewById(R.id.activity_main_image_view);
        btnPrinter = findViewById(R.id.activity_main_btn_print);
        final ArrayList<Package> listPkg = new ArrayList<>();
        for (int i = 0; i < list.length; i++) {
            Package pkg = new Package();
            pkg.order = list[i] + "";
            listPkg.add(pkg);
        }
        btnPrinter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                wifiCommunication = new WifiCommunication(new Handler() {
                    @Override
                    public void handleMessage(Message msg) {
                        switch (msg.what) {
                            case WifiCommunication.WFPRINTER_CONNECTED: {
                                SendDataToPrintKeepConnThread thread = new SendDataToPrintKeepConnThread(listPkg, printerStatus, getApplicationContext());
                                thread.wfComm = wifiCommunication;
                                thread.start();
                                break;
                            }
                            case WifiCommunication.WFPRINTER_DISCONNECTED: {
//                                callback.onResponse(null);
                                break;
                            }
                            case WifiCommunication.SEND_FAILED: {
//                                callback.onError("Send Data Failed,please reconnect");
                                break;
                            }
                            case WifiCommunication.WFPRINTER_CONNECTEDERR: {
//                                callback.onError("Connect the WIFI-printer error");
                                break;
                            }
                            case WFPRINTER_REVMSG: {
                                byte revData = (byte) Integer.parseInt(msg.obj.toString());
                                if (((revData >> 6) & 0x01) == 0x01) {
//                                    callback.onError("The printer has no paper");
                                }
                                break;
                            }
                            default: {
                                break;
                            }
                        }
                    }
                });

                wifiCommunication.initSocket("192.168.100.12", 9100);

////                Bitmap bm = PrinterController.instance(getApplicationContext()).generateBitmap();
//                Bitmap bm = LabelView.getInstance(getApplicationContext()).generateBitmap(new Package());
//                imgPreview.setImageBitmap(bm);
//                PrinterController.instance(getApplicationContext()).printImage(bm, new IFSimpleCallbackController() {
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
