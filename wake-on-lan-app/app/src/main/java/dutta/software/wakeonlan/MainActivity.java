package dutta.software.wakeonlan;

import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.os.Messenger;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    Handler handler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        handler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                Bundle reply = msg.getData();
                Toast.makeText(getApplicationContext(), reply.getString(MyTriggerService.PARAM_OUT_MSG), Toast.LENGTH_SHORT).show();
            }
        };

        findViewById(R.id.btWakeUp).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setTrigger(false);
            }
        });

        setTrigger(true);
    }

    private void setTrigger(boolean shouldRecur) {
        Intent msgIntent = new Intent(this, MyTriggerService.class);
        msgIntent.putExtra(MyTriggerService.PARAM_IN_MSG, shouldRecur);
        msgIntent.putExtra(MyTriggerService.HANDLER_IN_MSG, new Messenger(handler));
        startService(msgIntent);
    }
}
