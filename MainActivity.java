import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.TextView;
import android.widget.EditText;  
import android.widget.Button;
import android.widget.ToggleButton;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.GregorianCalendar;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;
import java.util.Calendar;

public class MainActivity extends Activity
 {
  private static final int REQUEST_ENABLE_BT = 0;
  Button openButton;
  Button sendButton;
  Button closeButton;
  Button highButton;
  Button lowButton;
  ToggleButton onoffButton;
  EditText editTextOnHour;
  EditText editTextOnMinute;
  EditText editTextOffHour;
  EditText editTextOffMinute;
  Calendar calendar;
  EditText editTextDeviceName;
  int hour;
  int minute;
  
     TextView myLabel;
     EditText myTextbox;
     BluetoothAdapter mBluetoothAdapter;
     BluetoothSocket mmSocket;
     BluetoothDevice mmDevice;
     OutputStream mmOutputStream;
     InputStream mmInputStream;
     Thread workerThread;
     
     BluetoothAdapter bluetoothAdapter;
     
     byte[] readBuffer;
     int readBufferPosition;
     int counter;
     volatile boolean stopWorker;
     
     @Override
     public void onCreate(Bundle savedInstanceState)
     {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.activity_main);
         
         MyTimerTask myTask = new MyTimerTask();
         Timer myTimer = new Timer();
         myTimer.schedule(myTask, 500, 500);
         
         bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
                  
         openButton = (Button)findViewById(R.id.open);
         sendButton = (Button)findViewById(R.id.send);
         closeButton = (Button)findViewById(R.id.close);
         highButton = (Button)findViewById(R.id.high);
         lowButton = (Button)findViewById(R.id.low);
         onoffButton = (ToggleButton)findViewById(R.id.onoff);
         myLabel = (TextView)findViewById(R.id.label);
         myTextbox = (EditText)findViewById(R.id.editTextEntry);
         editTextOnHour = (EditText)findViewById(R.id.editTextOnHour);
         editTextOnMinute = (EditText)findViewById(R.id.editTextOnMinute);
         editTextOffHour = (EditText)findViewById(R.id.editTextOffHour);
         editTextOffMinute = (EditText)findViewById(R.id.editTextOffMinute);
         editTextDeviceName = (EditText)findViewById(R.id.editTextDeviceName);
         
         CheckBlueToothState();
         
         onoffButton.setText("Off");
         
         //Open Button
         openButton.setOnClickListener(new View.OnClickListener()
         {
             public void onClick(View v)
             {
                 try 
                 {
                     findBT();
                     openBT();
                     openButton.setEnabled(false);
                     sendButton.setEnabled(true);
                     closeButton.setEnabled(true);
                     highButton.setEnabled(true);
                     lowButton.setEnabled(true);
                     onoffButton.setEnabled(true);
                 }
                 catch (IOException ex) { }
             }
         });
         
         //Send Button
         sendButton.setOnClickListener(new View.OnClickListener()
         {
             public void onClick(View v)
             {
                 try 
                 {
                     sendData();
                 }
                 catch (IOException ex) { }
             }
         });
         
         //Close button
         closeButton.setOnClickListener(new View.OnClickListener()
         {
             public void onClick(View v)
             {
                 try 
                 {
                     closeBT();
                     openButton.setEnabled(true);
                     sendButton.setEnabled(false);
                     closeButton.setEnabled(false);
                     highButton.setEnabled(false);
                     lowButton.setEnabled(false);
                     onoffButton.setEnabled(false);
                 }
                 catch (IOException ex) { }
             }
         });
         
         //High button
         highButton.setOnClickListener(new View.OnClickListener()
         {
             public void onClick(View v)
             {
                 try 
                 {
                  mmOutputStream.write('H');
                  onoffButton.setChecked(true);
                  onoffButton.setText("On");
                  highButton.setEnabled(false);
                  lowButton.setEnabled(true);
                 }
                 catch (IOException ex) { }
             }
         });
         
         //Low button
         lowButton.setOnClickListener(new View.OnClickListener()
         {
             public void onClick(View v)
             {
                 try 
                 {
                  mmOutputStream.write('L');
                  onoffButton.setChecked(false);
                  onoffButton.setText("Off");
                  highButton.setEnabled(true);
                  lowButton.setEnabled(false);
                 }
                 catch (IOException ex) { }
             }
         });
         
       //On-Off button
         onoffButton.setOnClickListener(new View.OnClickListener()
         {
             public void onClick(View v)
             {
              if(onoffButton.isChecked())
                 {
               try 
                  {
                   mmOutputStream.write('H');
                   highButton.setEnabled(false);
                   lowButton.setEnabled(true);
                  }
                  catch (IOException ex) { }
               onoffButton.setText("On");
                 }
                 else
                 {
                  try 
                  {
                   mmOutputStream.write('L');
                   highButton.setEnabled(true);
                   lowButton.setEnabled(false);
                  }
                  catch (IOException ex) { }
                  onoffButton.setText("Off");
                 }
             }
         });
     }
     
     private void CheckBlueToothState(){
         if (bluetoothAdapter == null){
          myLabel.setText("Bluetooth NOT support");
            }else{
             if (bluetoothAdapter.isEnabled()){
              if(bluetoothAdapter.isDiscovering()){
               myLabel.setText("Bluetooth is currently in device discovery process.");
              }else{
               myLabel.setText("Bluetooth is Enabled");
              }
             }else{
              myLabel.setText("Bluetooth is NOT Enabled!");
              Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                 startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
             }
            }
        }
     
     void findBT()
     {
         mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
         if(mBluetoothAdapter == null)
         {
             myLabel.setText("No bluetooth adapter available");
         }
         
         if(!mBluetoothAdapter.isEnabled())
         {
             Intent enableBluetooth = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
             startActivityForResult(enableBluetooth, 0);
         }
         
         Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();
         if(pairedDevices.size() > 0)
         {
             for(BluetoothDevice device : pairedDevices)
             {
              //Name of BT device
                 if(device.getName().equals(editTextDeviceName.getText().toString())) 
                 {
                     mmDevice = device;
                     break;
                 }
             }
         }
         myLabel.setText("Bluetooth Device Found");
     }
     
     void openBT() throws IOException
     {
         UUID uuid = UUID.fromString("00001101-0000-1000-8000-00805f9b34fb"); //Standard SerialPortService ID
         mmSocket = mmDevice.createRfcommSocketToServiceRecord(uuid);        
         mmSocket.connect();
         mmOutputStream = mmSocket.getOutputStream();
         mmInputStream = mmSocket.getInputStream();
         
         beginListenForData();
         
         myLabel.setText("Bluetooth Opened");
     }
     
     void beginListenForData()
     {
         final Handler handler = new Handler(); 
         final byte delimiter = 10; //This is the ASCII code for a newline character
         
         stopWorker = false;
         readBufferPosition = 0;
         readBuffer = new byte[1024];
         workerThread = new Thread(new Runnable()
         {
             public void run()
             {                
                while(!Thread.currentThread().isInterrupted() && !stopWorker)
                {
                     try 
                     {
                         int bytesAvailable = mmInputStream.available();                        
                         if(bytesAvailable > 0)
                         {
                             byte[] packetBytes = new byte[bytesAvailable];
                             mmInputStream.read(packetBytes);
                             for(int i=0;i<bytesAvailable;i++)
                             {
                                 byte b = packetBytes[i];
                                 if(b == delimiter)
                                 {
                                     byte[] encodedBytes = new byte[readBufferPosition];
                                     System.arraycopy(readBuffer, 0, encodedBytes, 0, encodedBytes.length);
                                     final String data = new String(encodedBytes, "US-ASCII");
                                     readBufferPosition = 0;
                                     
                                     handler.post(new Runnable()
                                     {
                                         public void run()
                                         {
                                             myLabel.setText(data);
                                         }
                                     });
                                 }
                                 else
                                 {
                                     readBuffer[readBufferPosition++] = b;
                                 }
                             }
                         }
                     } 
                     catch (IOException ex) 
                     {
                         stopWorker = true;
                     }
                }
             }
         });

          workerThread.start();
     }
     
     void sendData() throws IOException
     {
         String msg = myTextbox.getText().toString();
         //msg += "\n";
         mmOutputStream.write(msg.getBytes());
         myLabel.setText("Data Sent");
     }
     
     void closeBT() throws IOException
     {
         stopWorker = true;
         mmOutputStream.close();
         mmInputStream.close();
         mmSocket.close();
         myLabel.setText("Bluetooth Closed");
     }
     
     class MyTimerTask extends TimerTask {
      public void run() {
     calendar = new GregorianCalendar();
     hour = calendar.get(Calendar.HOUR_OF_DAY);
         minute = calendar.get(Calendar.MINUTE);
     if (Integer.parseInt(editTextOnHour.getText().toString()) == hour && Integer.parseInt(editTextOnMinute.getText().toString()) == minute)
         {
          try 
                {
                 mmOutputStream.write('H');
                 onoffButton.setChecked(true);
                 onoffButton.setText("On");
                 highButton.setEnabled(false);
                 lowButton.setEnabled(true);
                }
                catch (IOException ex) { }
         }
     
     if (Integer.parseInt(editTextOffHour.getText().toString()) == hour && Integer.parseInt(editTextOffMinute.getText().toString()) == minute)
         {
          try 
                {
                 mmOutputStream.write('L');
                 onoffButton.setChecked(false);
                 onoffButton.setText("Off");
                 highButton.setEnabled(true);
                 lowButton.setEnabled(false);
                }
                catch (IOException ex) { }
         }
      }
    }
 }
