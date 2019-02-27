package examples.aaronhoskins.com.loopers;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {

    private int MAIN_THREAD_TASK_1 = 1;
    private int MAIN_THREAD_TASK_2 = 2;
    private int CHILD_THREAD_QUIT_LOOPER = 3;

    private Handler mainThreadHandler;

    private MyWorkerThread workerThread = null;

    private Button runTaskOneButton;

    private Button runTaskTwoButton;

    private TextView taskStatusTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        setTitle("dev2qa.com - Child Thread Looper Handler Example");

        // Create and start the worker thread.
        workerThread = new MyWorkerThread();
        workerThread.start();

        // Handle message from main thread message queue.
        mainThreadHandler = new Handler(Looper.getMainLooper()){
            @Override
            public void handleMessage(Message msg) {
                Log.i("MAIN_THREAD", "Receive message from child thread.");
                if(msg.what == MAIN_THREAD_TASK_1)
                {
                    // If task one button is clicked.
                    taskStatusTextView.setText("Task one execute.");
                }else if(msg.what == MAIN_THREAD_TASK_2)
                {
                    // If task two button is clicked.
                    taskStatusTextView.setText("Task two execute.");
                }else if(msg.what == CHILD_THREAD_QUIT_LOOPER)
                {
                    // If quit child thread looper button is clicked.
                    taskStatusTextView.setText("Quit child thread looper.");
                }
            }
        };

        // Get run task buttons.
        runTaskOneButton = (Button)findViewById(R.id.runTaskOneButton);
        runTaskTwoButton = (Button)findViewById(R.id.runTaskTwoButton);

        // Set on click listener to each button.
        runTaskOneButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // When click this button, create a message object.
                Message msg = new Message();
                msg.what = MAIN_THREAD_TASK_1;
                // Use worker thread message Handler to put message into worker thread message queue.
                workerThread.workerThreadHandler.sendMessage(msg);
            }
        });

        // Please see comments for runTaskOneButton.
        runTaskTwoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Message msg = new Message();
                msg.what = MAIN_THREAD_TASK_2;
                workerThread.workerThreadHandler.sendMessage(msg);
            }
        });

        // Get status info TextView object.
        taskStatusTextView = (TextView)findViewById(R.id.taskStatusTextView);

        // Get the quit child thread looper button.
        Button quitChildThreadLooperButton = (Button)findViewById(R.id.quitChildThreaLooperButton);
        quitChildThreadLooperButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Click this button will quit child thread looper.
                workerThread.workerThreadHandler.getLooper().quit();
            }
        });
    }

    // This child thread class has it's own Looper and Handler object.
    private class MyWorkerThread extends Thread{
        // This is worker thread handler.
        public Handler workerThreadHandler;

        @Override
        public void run() {
            // Prepare child thread Lopper object.
            Looper.prepare();

            // Create child thread Handler.
            workerThreadHandler = new Handler(Looper.myLooper()){
                @Override
                public void handleMessage(Message msg) {
                    // When child thread handler get message from child thread message queue.
                    Log.i("CHILD_THREAD", "Receive message from main thread.");
                    Message message = new Message();
                    message.what = msg.what;
                    // Send the message back to main thread message queue use main thread message Handler.
                    mainThreadHandler.sendMessage(message);
                }
            };
            // Loop the child thread message queue.
            Looper.loop();

            // The code after Looper.loop() will not be executed until you call workerThreadHandler.getLooper().quit()
            Log.i("CHILD_THREAD", "This log is printed after Looper.loop() method. Only when this thread loop quit can this log be printed.");
            // Send a message to main thread.
            Message msg = new Message();
            msg.what = CHILD_THREAD_QUIT_LOOPER;
            mainThreadHandler.sendMessage(msg);
        }
    }
}
