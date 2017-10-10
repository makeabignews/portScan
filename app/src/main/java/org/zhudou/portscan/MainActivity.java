package org.zhudou.portscan;

import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import org.w3c.dom.Text;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    public List<Map<String, Object>> data = new ArrayList<Map<String, Object>>();

    SimpleAdapter simpleAdapter;

    int finish=0;
    int finish_max=254;

    public Handler handler = new Handler() {
        @Override
        public void handleMessage(android.os.Message msg) {  //这个是发送过来的消息
            Message message=handler.obtainMessage();
            Bundle data=msg.getData();
            switch(msg.what) {
                case 1:
                    //String answer_id=data.get("answer_id").toString();
                    simpleAdapter.notifyDataSetChanged();
                    break;
                case 2:
                    setTitle("PortScan 扫描完毕");
                    break;
                default:
                    break;
            }
            super.handleMessage(msg);
        }
    };
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ListView list=(ListView)findViewById(R.id.list);

        simpleAdapter = new SimpleAdapter(getBaseContext(), data,
                R.layout.layout_item, new String[] { "title"},
                new int[] { R.id.title});
        //绑定
        list.setAdapter(simpleAdapter);


        //1）判断是否在内网
        Ip ipHelper=new Ip();
        final String local_ip=ipHelper.getLocalIP();
        final String[] ipArray=ipHelper.getAllIp();

        TextView msg=(TextView)findViewById(R.id.msg);
        msg.setText("本机ip："+local_ip);
        //2）获得内网IP数组
        if(ipArray!=null){
            //3）对IP地址群逐个端口进行扫描
            setTitle("PortScan 正在扫描...");
            (new Thread(new Runnable(){
                @Override
                public void run() {
                    for (int i = 0; i < ipArray.length; i++) {
                        String target_ip=ipArray[i];
                        if(!target_ip.equals(local_ip)){
                            //扫描
                            int target_port=80;
                            Socket socket = new Socket();
                            SocketAddress socketAddress = new InetSocketAddress(target_ip, target_port);
                            try {
                                socket.connect(socketAddress, 200);

                                socket.close();
                                //
                                Map<String, Object> item = new HashMap<String, Object>();
                                item.put("title",target_ip+":"+target_port);
                                data.add(item);
                                Message message= handler.obtainMessage();
                                message.what=1;
                                Bundle msgData=new Bundle();
                                //msgData.putString("answer_id",result);
                                //message.setData(msgData);
                                message.sendToTarget();

                                //
                            } catch (IOException e) {
                                // TODO Auto-generated catch block
                                //e.printStackTrace();
                            }
                        }
                        finish+=1;
                        if(finish==finish_max){
                            Message message= handler.obtainMessage();
                            message.what=2;
                            Bundle msgData=new Bundle();
                            //msgData.putString("answer_id",result);
                            //message.setData(msgData);
                            message.sendToTarget();
                        }
                    }
                }
            })).start();
        }
    }
}
