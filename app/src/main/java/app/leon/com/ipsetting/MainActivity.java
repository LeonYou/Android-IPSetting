package app.leon.com.ipsetting;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.Selection;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity implements View.OnClickListener
{
    private TextView mTvIP;
    private EditText mEtIP;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mTvIP = findViewById(R.id.tvIP);
        mEtIP = findViewById(R.id.etIP);

        findViewById(R.id.btnOK).setOnClickListener(this);

        String curIP = Util.getIp(this);
        mEtIP.setText(curIP);

        // Select last section of IP, then popup input method
        if (!curIP.equals(Util.NONE_IP))
        {
            Selection.setSelection(mEtIP.getEditableText(), curIP.lastIndexOf('.') + 1, curIP.length());

            mEtIP.setFocusable(true);
            mEtIP.setFocusableInTouchMode(true);
            mEtIP.requestFocus();

            Timer timer = new Timer();
            timer.schedule(new TimerTask() {

                @Override
                public void run() {
                    InputMethodManager imm = (InputMethodManager) MainActivity.this.getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.toggleSoftInput(0, InputMethodManager.SHOW_FORCED);
                }

            }, 300);
        }
    }

    @Override
    public void onClick(View v)
    {
        switch (v.getId())
        {
        case R.id.btnOK:
            if (Util.setIpWithStaticIp(this, mEtIP.getText().toString()))
                showToast(this, "成功");
            else
                showToast(this, "失败");
            break;
        default:
            break;
        }
    }


    public void showToast(Context conext, String str)
    {
        TextView tv = new TextView(this);
        tv.setText(str);
        tv.setTextSize(20);
        tv.setTextColor(Color.WHITE);
        tv.setGravity(Gravity.CENTER);
        tv.setLayoutParams(new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT));

        Toast toast = Toast.makeText(conext, "Change IP Address", Toast.LENGTH_LONG);
        LinearLayout toastView = (LinearLayout) toast.getView();
        toastView.setGravity(Gravity.CENTER);
        toastView.addView(tv);

        toast.setGravity(Gravity.CENTER, 0, 0);
        toast.setView(toastView);
        toast.show();
    }
}
