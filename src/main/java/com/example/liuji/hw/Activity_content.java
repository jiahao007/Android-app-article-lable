package com.example.liuji.hw;



import android.annotation.TargetApi;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Color;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.ActionMode;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TabHost;
import android.widget.TableLayout;
import android.widget.TextView;
import android.widget.Toast;
import com.google.gson.Gson;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Activity_content extends AppCompatActivity
{
    private String token;
    private Button download, upload, logout;
    private TabHost tabHost;
    private TextView text_selected, test;
    private int size = 0;
    private Context context;
    private int sizecopy=0;

    final OkHttpClient client = new OkHttpClient();
    List<Mentity> tmp_emtity = new ArrayList<Mentity>();            //to storage entities labeled
    List<ErE> triples = new ArrayList<ErE>();                       //to storage triples labeled
    String ready_entity = "";


    final UploadEntity uploadEntity = new UploadEntity();         //to storage the entity to upload
    final Relation uploadRelation = new Relation();               //to storage the relation to upload

    /**
     * this handler is used to cope with messages downloaded.
     * according to function named postRequestEntity() and function name postRequestTriple().
     */
    private Handler mHandler = new Handler()
    {
        @Override
        public void handleMessage(Message msg)
        {
            if(msg.what == 1)
            {
                String ReturnMessage = (String)msg.obj; //obtain the message downloaded from teh server
                String tag = tabHost.getCurrentTabTag();
                //judge tag chosen
                if(tag.equals("tab1"))
                {
                    //analysis the information of json entity
                    final Entity entity = new Gson().fromJson(ReturnMessage, Entity.class);
                    //assign doc_id and sent_id to the entity uploaded
                    uploadEntity.setDoc_id(entity.getDoc_id());
                    uploadEntity.setSent_id(entity.getSent_id());
                    //show the content of the article
                    TextView textView = findViewById(R.id.tab01);
                    textView.setText(entity.getContent());
                }
                else if(tag.equals("tab2"))
                {
                    //analysis the information of json relation
                    final Relation relation = new Gson().fromJson(ReturnMessage, Relation.class);
                    //get triples from the relation
                    triples = relation.getTriples();
                    //assign some value to the relation uploaded
                    uploadRelation.setDoc_id(relation.getDoc_id());
                    uploadRelation.setSent_id(relation.getSent_id());
                    uploadRelation.setSent_ctx(relation.getSent_ctx());
                    uploadRelation.setTitle(relation.getTitle());
                    uploadRelation.setTriples(relation.getTriples());
                    uploadRelation.setTriples(triples);
                    //storage according json content that we download in order we upload relation directly
                    Gson gs = new Gson();
                    String storagecontent = gs.toJson(uploadRelation);
                    write("content.json", storagecontent);

                    //show the content of the article
                    TextView textView = findViewById(R.id.tab01);
                    textView.setText(relation.getSent_ctx());
                }

            }
        }
    };

    /**
     * In order to obtain the content and the position
     * we chosen from the text view, we rewrite callback port and
     */
    private ActionMode.Callback callback = new ActionMode.Callback()
    {
        @Override
        public boolean onCreateActionMode(ActionMode actionMode, Menu menu)
        {
            //this function should return true if we want to show the menu
            return true;
        }

        /**
         *
         * @param actionMode
         * @param menu
         * @return true
         * this function is used to clear the menu that the system owns.
         */
        @Override
        public boolean onPrepareActionMode(ActionMode actionMode, Menu menu)
        {
            MenuInflater menuInflater = actionMode.getMenuInflater();   //get according menu
            menu.clear();                                               //clear menu
            menuInflater.inflate(R.menu.selectedcontent,menu);        //add menu we define
            return true;
        }

        /**
         *
         * @param actionMode
         * @param menuItem
         * @return true
         * this function response according to the menu we choose
         */
        @Override
        public boolean onActionItemClicked(ActionMode actionMode, MenuItem menuItem)
        {
            switch (menuItem.getItemId())
            {
                //when we choose this item, we obtain the content we have chosen
                case R.id.toast:
                {
                    String tag = tabHost.getCurrentTabTag();    //obtain the tag chosen
                    if (text_selected == null) return false;
                    int min = 0;
                    int max = text_selected.length();
                    if (text_selected.isFocused())
                    {
                        final int selStart = text_selected.getSelectionStart(); //get the begin of content chosen
                        final int selEnd = text_selected.getSelectionEnd();     //get the end of content chosen
                        min = Math.max(0, Math.min(selStart, selEnd));
                        max = Math.max(0, Math.max(selStart, selEnd));
                    }
                    String content = String.valueOf(text_selected.getText().subSequence(min, max)); //obtain content chosen
                    if(tag.equals("tab1"))
                        find_entity(tmp_emtity, content, min, max, size);   //call find_entity() to cope with this content
                    else if(tag.equals("tab2")){
                        find_relation(triples,content,min,max,sizecopy);    //call find_relation() to deal with this content
                    }
                    size = tmp_emtity.size() + 1;   //this value is used to show entity chosen in the Textview.
                    sizecopy=triples.size() + 1;    //this value is used to show relation chosen in the Textview
                    break;
                }
                //when we choose this item, we modify the wrong relation.
                case R.id.finish:
                {
                    String tag = tabHost.getCurrentTabTag();
                    if(tag.equals("tab1"))
                        Toast.makeText(Activity_content.this,"请选择关系标注", Toast.LENGTH_SHORT).show();
                    else if(tag.equals("tab2")){
                        change();
                    }
                    break;
                }
            }
            return true;
        }
        @Override
        public void onDestroyActionMode(ActionMode actionMode)
        {
        }
    };
    @TargetApi(Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_content);


        token = getIntent().getStringExtra("token");
        download = (Button) findViewById(R.id.download);
        upload = (Button)findViewById(R.id.upload);
        logout = (Button)findViewById(R.id.logout);
        tabHost = (TabHost) findViewById(android.R.id.tabhost);
        tabHost.setup();
        //add the fist label page
        TabHost.TabSpec tab1 = tabHost.newTabSpec("tab1")
                .setIndicator("实体标注")// set the title
                .setContent(R.id. tab01 ); //set the content
        //add the second label page
        TabHost.TabSpec tab2 = tabHost.newTabSpec("tab2")
                .setIndicator("关系标注")
                .setContent(R.id. tab01 );
        tabHost.addTab(tab1);
        tabHost.addTab(tab2);
        //set Listener on tag to judge the change of tag and clear the textview
        tabHost.setOnTabChangedListener(new TabHost.OnTabChangeListener()
        {
            @Override
            public void onTabChanged(String tabId)
            {
                TextView tv = (TextView)findViewById(R.id.tab01);
                // when we change the label page we clear this textview
                if(tabId.equals("tab1"))
                    tv.setText("");
                else if(tabId.equals("tab2"))
                    tv.setText("");

            }
        });
        //set Listener on the download button to get a new article
        download.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                String tag = tabHost.getCurrentTabTag();
                // when tag equals tab1 we download entity article
                if(tag.equals("tab1"))
                {
                    postRequestEntity(token);
                }
                //when tag equals tab2 we download relation article
                else if(tag.equals("tab2"))
                    postRequestTriple(token);
                ready_entity="";
                test = (TextView)findViewById(R.id.tab02);
                test.setText("");
            }
        });
        upload.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v) {
                String tag = tabHost.getCurrentTabTag();
                if(tag.equals("tab1"))
                {
                    //read entities we have labeled from the local file "content.json"
                    String entities = read("content.json");
                    if(entities != null)
                    {
                        //upload entities
                        postUploadEntity(entities, token);
                        tmp_emtity.clear();
                    }
                }
                else if(tag.equals("tab2"))
                {
                    //read relation we have modify or add from the local file "content.json"
                    String relation = read("content.json");
                    if(relation != null)
                    {
                        //upload relation
                        postUploadTriple(relation, token);
                        triples.clear();
                    }
                }
            }
        });

        logout.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                if(tmp_emtity.size() != 0|| triples.size() != 0)
                {
                    // before logout the server, remind users to upload result
                    Toast. makeText (Activity_content.this, "请先上传结果再退出",
                            Toast. LENGTH_SHORT ).show();
                }
                else
                {
                    //logout the server and goto the login activity
                    postRequestLogout(token);
                    Intent intent = new Intent();
                    intent.setClass(Activity_content.this, Activity_login.class);
                    intent.putExtra("token", token);
                    startActivity(intent);
                }
            }
        });
        text_selected = (TextView)findViewById(R.id.tab01);
        float zoomScale = 0.15f;
        new ZoomTextView(text_selected, zoomScale);     //set zoom function on the textview to adjust the size of the word
        text_selected.setCustomSelectionActionModeCallback(callback);   //set callback on the textview to get the content we choose.
    }
    private void postRequestEntity(String token)
    {
        if(token != null)
        {
            RequestBody formBody = new FormBody.Builder().add("token", token).build();
            final Request request = new Request.Builder().url("http://10.15.82.223:9090/app_get_data/app_get_entity").post(formBody).build();
            //create a new thread to obtain the param of server response
            new Thread(new Runnable()
            {
                @Override
                public void run()
                {
                    Response response = null;
                    try{
                        response = client.newCall(request).execute(); //obtain response from the server
                        if(response.isSuccessful())
                        {
                            mHandler.obtainMessage(1, response.body().string()).sendToTarget();
                        }
                        else
                        {
                            throw new IOException("Unexpected code:" + response);
                        }
                    }catch(IOException e){
                        e.printStackTrace();
                    }
                }
            }).start();
        }
    }

    private Handler mLogoutHandler = new Handler()
    {
        @Override
        public void handleMessage(Message msg)
        {
            if(msg.what == 1)
            {
                String ReturnMessage = (String)msg.obj;
                final UserBean userBean = new Gson().fromJson(ReturnMessage, UserBean.class);
                final String message = userBean.getMsg();
                final String token = userBean.getToken();
                Toast. makeText (Activity_content.this, message,
                        Toast. LENGTH_SHORT ).show();
            }
        }
    };
    private void postRequestLogout(String token)
    {
        if(token != null)
        {
            RequestBody formBody = new FormBody.Builder().add("token", token).build();

            final Request request = new Request.Builder().url("http://10.15.82.223:9090/app_get_data/app_logout").post(formBody).build();
            new Thread(new Runnable()
            {
                @Override
                public void run()
                {
                    Response response = null;
                    try{
                        response = client.newCall(request).execute();
                        if(response.isSuccessful())
                        {
                            mLogoutHandler.obtainMessage(1, response.body().string()).sendToTarget();
                        }
                        else
                        {
                            throw new IOException("Unexpected code:" + response);
                        }
                    }catch(IOException e){
                        e.printStackTrace();
                    }
                }
            }).start();
        }
    }
    private void postRequestTriple (String token)
    {
        if(token != null)
        {
            RequestBody formBody = new FormBody.Builder().add("token", token).build();

            final Request request = new Request.Builder().url("http://10.15.82.223:9090/app_get_data/app_get_triple").post(formBody).build();
            new Thread(new Runnable()
            {
                @Override
                public void run()
                {
                    Response response = null;
                    try{
                        response = client.newCall(request).execute();
                        if(response.isSuccessful())
                        {
                            mHandler.obtainMessage(1, response.body().string()).sendToTarget();
                        }
                        else
                        {
                            throw new IOException("Unexpected code:" + response);
                        }
                    }catch(IOException e){
                        e.printStackTrace();
                    }
                }
            }).start();
        }
    }

    private Handler mUpHandler = new Handler()
    {
        @Override
        public void handleMessage(Message msg)
        {
            if(msg.what == 1)
            {
                String ReturnMessage = (String)msg.obj;
                final UserBean userBean = new Gson().fromJson(ReturnMessage, UserBean.class);
                final String message = userBean.getMsg();
                Toast. makeText (Activity_content.this, message,
                        Toast.LENGTH_LONG ).show();

            }
        }
    };
    private void postUploadEntity(String entities, String token)
    {
        if(token != null)
        {
            RequestBody formBody = new FormBody.Builder().add("entities", entities).add("token", token).build();

            final Request request = new Request.Builder().url("http://10.15.82.223:9090/app_get_data/app_upload_entity").post(formBody).build();
            new Thread(new Runnable()
            {
                @Override
                public void run()
                {
                    Response response = null;
                    try{
                        response = client.newCall(request).execute();
                        if(response.isSuccessful())
                        {
                            mUpHandler.obtainMessage(1, response.body().string()).sendToTarget();
                        }
                        else
                        {
                            throw new IOException("Unexpected code:" + response);
                        }
                    }catch(IOException e){
                        e.printStackTrace();
                    }
                }
            }).start();
        }
    }

    private void postUploadTriple(String triples, String token)
    {
        if(token != null)
        {
            RequestBody formBody = new FormBody.Builder().add("triples", triples).add("token", token).build();
            final Request request = new Request.Builder().url("http://10.15.82.223:9090/app_get_data/app_upload_triple").post(formBody).build();
            new Thread(new Runnable()
            {
                @Override
                public void run()
                {
                    Response response = null;
                    try{
                        response = client.newCall(request).execute();
                        if(response.isSuccessful())
                        {
                            mUpHandler.obtainMessage(1, response.body().string()).sendToTarget();
                        }
                        else
                        {
                            throw new IOException("Unexpected code:" + response);
                        }
                    }catch(IOException e){
                        e.printStackTrace();
                    }
                }
            }).start();
        }
    }

    public void find_entity(final List<Mentity> res, final String content, final int left_begin, int right_end, final int size)
    {//add entity
        String[]chars=content.split("");//split the content to single character
        List<String> list = new ArrayList<String>();
        String word="";
        for (int i=0; i<chars.length; i++) {//this is for spliting English word
            if(i==chars.length-1&&((chars[i].charAt(0)>='a'&&chars[i].charAt(0)<='z')||(chars[i].charAt(0)>='A'&&chars[i].charAt(0)<='Z'))){
                word+=chars[i];
                list.add(word);
                word="";
                break;
            }
            if(chars[i].length()>0&&((chars[i].charAt(0)>='a'&&chars[i].charAt(0)<='z')||(chars[i].charAt(0)>='A'&&chars[i].charAt(0)<='Z'))){
                word+=chars[i];
                continue;
            }
            if(chars[i].length()>0&&(chars[i].charAt(0)==' '||!((chars[i].charAt(0)>='a'&&chars[i].charAt(0)<='z')||(chars[i].charAt(0)>='A'&&chars[i].charAt(0)<='Z')))){

                if(word!="")
                    list.add(word);
                word="";
                list.add(chars[i]);
                continue;
            }
            list.add(chars[i]);
        }
        list.remove(0);
        chars =  list.toArray(new String[1]);
        TableLayout tableLayout=new TableLayout(Activity_content.this);
        ScrollView scrollView=new ScrollView(Activity_content.this);
        LinearLayout linearLayout= new LinearLayout(Activity_content.this);
        int count=0;

        final Button Btn[] = new Button[200];
        final boolean selectedIndex[]=new boolean[200];//sign for choose or not

        for(int i = 0;i < chars.length; i++) {//set for each button and add them to the view
            selectedIndex[i]=false;
            Btn[i] = new Button(Activity_content.this);
            Btn[i].setText(chars[i]);
            Btn[i].setAllCaps(false);
            Btn[i].setTextSize(10);
            Btn[i].setGravity(Gravity.CENTER);
            linearLayout.addView(Btn[i]);
            LinearLayout.LayoutParams params= (LinearLayout.LayoutParams) Btn[i].getLayoutParams();
            params.height=100;
            params.width=100;
            if(chars[i].length()>=2){
                params.width=100*((chars[i].length()-2)/6+2);
                count+=(chars[i].length()-2)/6+1;
            }
            Btn[i].setLayoutParams(params);//set button size
            count++;
            if(count>=8){//adapt for suitable number for each line
                tableLayout.addView(linearLayout);
                linearLayout= new LinearLayout(Activity_content.this);
                count=0;
            }
        }
        tableLayout.addView(linearLayout);
        Button b1=new Button(Activity_content.this);
        b1.setText("person");
        Button b2=new Button(Activity_content.this);
        b2.setText("title");
        linearLayout= new LinearLayout(Activity_content.this);
        linearLayout.addView(b1);
        linearLayout.addView(b2);
        scrollView.addView(tableLayout);
        LinearLayout l=new LinearLayout(Activity_content.this);
        l.addView(scrollView);
        LinearLayout L=new LinearLayout(Activity_content.this);
        L.setOrientation(LinearLayout.VERTICAL);
        L.addView(linearLayout);
        L.addView(l);
        for (int k = 0; k < chars.length; k++)
        {
            Btn[k].setTag(k);
            Btn[k].setOnClickListener(new Button.OnClickListener()
            {//set monitor for each button
                @Override
                public void onClick(View view)
                {//change color and choose or not when click
                    int i = (Integer) view.getTag();
                    if(selectedIndex[i]==true)
                    {
                        selectedIndex[i] = false;
                        Btn[i].setTextColor(Color.parseColor("#000000"));
                    }
                    else
                    {
                        selectedIndex[i] = true;
                        Btn[i].setTextColor(Color.parseColor("#FF7F50"));
                    }
                }
            });
        }
        final String[] finalChars = chars;
        b1.setOnClickListener(new Button.OnClickListener()
        {//monitor sign for person entity
            @Override
            public void onClick(View view)
            {
                int star=0;//start
                int fina=0;//end
                Mentity tmp = new Mentity();
                for(int j = 0;j < 200; j++)
                {
                    if(selectedIndex[j])
                        fina = j;
                }
                star = fina;
                while(star >= 0 && selectedIndex[star])
                    star--;
                if(star!=0){
                    while(star>=0&&selectedIndex[star])star--;
                    star++;
                }
                while(star<=fina&&!selectedIndex[star]){
                    star++;
                }
                if(!selectedIndex[star])star++;
                int count=0;
                for(int j=0;j<star;j++){
                    count+= finalChars[j].length();
                }
                count+=left_begin;
                tmp.Start = "" + count;//set entity start
                String s="";
                for(int j = star; j <= fina; j++)
                    s += Btn[j].getText().toString();
                for(int j=0;j<=fina;j++){
                    selectedIndex[j]=false;
                }
                count+=s.length();
                tmp.End="" + count;//set end
                tmp.EntityName = s;//set entity
                tmp.NerTag = "PERSON";
                res.add(tmp);
            }
        });
        b2.setOnClickListener(new Button.OnClickListener()
        {//monitor sign for title entity
            @Override
            public void  onClick(View view)
            {
                int star=0;
                int fina=0;
                Mentity tmp = new Mentity();
                for(int j = 0; j < 200; j++)
                {
                    if(selectedIndex[j])
                        fina = j;
                }
                star = fina;
                while(star >= 0 && selectedIndex[star])
                    star--;
                if(star!=0){
                    while(star>=0&&selectedIndex[star])star--;
                    star++;
                }
                if(!selectedIndex[star])star++;
                int count=0;
                for(int j=0;j<star;j++){
                    count+= finalChars[j].length();
                }
                count+=left_begin;
                tmp.Start = "" + count;
                String s="";
                for(int j = star;j <= fina; j++)
                    s += Btn[j].getText().toString();
                for(int j=0;j<=fina;j++){
                    selectedIndex[j]=false;
                }
                count+=s.length();
                tmp.End="" + count;
                tmp.EntityName =s;
                tmp.NerTag = "TITLE";
                res.add(tmp);
            }
        });
        AlertDialog.Builder builder = new AlertDialog.Builder(Activity_content.this);
        builder.setView(L);
        builder.setTitle("修改标注");
        builder.setPositiveButton("确定", new DialogInterface.OnClickListener()
        {
            @Override
            public void onClick(DialogInterface dialogInterface, int i)
            {
                ready_entity="";
                for(int j = 0; j < tmp_emtity.size(); j++)
                {//show all added entity
                    Mentity e = tmp_emtity.get(j);
                    ready_entity = ready_entity + e.EntityName + " ";
                }
                test = (TextView)findViewById(R.id.tab02);
                test.setText(ready_entity);
                uploadEntity.setEntities(tmp_emtity);//upload
                Gson gs = new Gson();
                String storagecontent = gs.toJson(uploadEntity);
                write("content.json", storagecontent);
            }
        });
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }


    public void find_relation(final List<ErE> res, String content, final int left_begin, int right_end, final int size){
        //add relation
        String[]chars=content.split("");//split to single character
        List<String> list = new ArrayList<String>();
        String word="";
        for (int i=0; i<chars.length; i++) {//split for English word
            if(i==chars.length-1&&((chars[i].charAt(0)>='a'&&chars[i].charAt(0)<='z')||(chars[i].charAt(0)>='A'&&chars[i].charAt(0)<='Z'))){
                word+=chars[i];
                list.add(word);
                word="";
                break;
            }
            if(chars[i].length()>0&&((chars[i].charAt(0)>='a'&&chars[i].charAt(0)<='z')||(chars[i].charAt(0)>='A'&&chars[i].charAt(0)<='Z'))){
                word+=chars[i];
                continue;
            }
            if(chars[i].length()>0&&(chars[i].charAt(0)==' '||!((chars[i].charAt(0)>='a'&&chars[i].charAt(0)<='z')||(chars[i].charAt(0)>='A'&&chars[i].charAt(0)<='Z')))){

                if(word!="")
                    list.add(word);
                word="";
                list.add(chars[i]);
                continue;
            }
            list.add(chars[i]);
        }
        list.remove(0);
        chars =  list.toArray(new String[1]);
        TableLayout tableLayout=new TableLayout(Activity_content.this);
        ScrollView scrollView=new ScrollView(Activity_content.this);
        LinearLayout linearLayout= new LinearLayout(Activity_content.this);
        int count=0;

        final Button Btn[] = new Button[200];
        final boolean selectedIndex[]=new boolean[200];

        for(int i = 0;i < chars.length; i++) {//set for each button
            selectedIndex[i]=false;
            Btn[i] = new Button(Activity_content.this);
            Btn[i].setText(chars[i]);
            Btn[i].setAllCaps(false);
            Btn[i].setTextSize(10);
            Btn[i].setGravity(Gravity.CENTER);
            linearLayout.addView(Btn[i]);
            LinearLayout.LayoutParams params= (LinearLayout.LayoutParams) Btn[i].getLayoutParams();
            params.height=100;
            params.width=100;
            if(chars[i].length()>=2){
                params.width=100*((chars[i].length()-2)/6+2);
                count+=(chars[i].length()-2)/6+1;
            }
            Btn[i].setLayoutParams(params);//change button size
            count++;
            if(count>=8){
                tableLayout.addView(linearLayout);
                linearLayout= new LinearLayout(Activity_content.this);
                count=0;
            }
        }
        tableLayout.addView(linearLayout);
        final Button b1=new Button(Activity_content.this);
        b1.setText("LEFT");
        final Button b2=new Button(Activity_content.this);
        b2.setText("RIGHT");
        final Button b3=new Button(Activity_content.this);
        b3.setText("亲属");
        linearLayout= new LinearLayout(Activity_content.this);
        linearLayout.addView(b1);
        linearLayout.addView(b2);
        linearLayout.addView(b3);
        scrollView.addView(tableLayout);
        LinearLayout l=new LinearLayout(Activity_content.this);
        l.addView(scrollView);
        LinearLayout L=new LinearLayout(Activity_content.this);
        L.setOrientation(LinearLayout.VERTICAL);
        L.addView(linearLayout);
        L.addView(l);
        for (int k = 0; k < chars.length; k++)
        {
            Btn[k].setTag(k);
            Btn[k].setOnClickListener(new Button.OnClickListener()
            {//set monitor for each button
                @Override
                public void onClick(View view)
                {//change color and sign for each button
                    int i = (Integer) view.getTag();
                    if(selectedIndex[i]==true)
                    {
                        selectedIndex[i] = false;
                        Btn[i].setTextColor(Color.parseColor("#000000"));
                    }
                    else
                    {
                        selectedIndex[i] = true;
                        Btn[i].setTextColor(Color.parseColor("#FF7F50"));
                    }
                }
            });
        }
        final ErE get=new ErE();
        final String[] finalChars = chars;
        b1.setOnClickListener(new Button.OnClickListener()
        {//monitor left entity
            @Override
            public void onClick(View view)
            {
                int star=0;
                int fina=0;
                ErE tmp = new ErE();
                for(int j = 0;j < 200; j++)
                {
                    if(selectedIndex[j])
                        fina = j;
                }
                star = fina;
                while(star >= 0 && selectedIndex[star])
                    star--;
                if(star!=0){
                    while(star>=0&&selectedIndex[star])star--;
                    star++;
                }
                if(!selectedIndex[star])star++;
                String s="";
                for(int j = star; j <= fina; j++){
                    s += Btn[j].getText().toString();
                }
                for(int j=0;j<=fina;j++){// re-initialize
                    selectedIndex[j]=false;
                    Btn[j].setTextColor(Color.parseColor("#000000"));
                }
                int number=left_begin;
                for(int k=0;k<star;k++){
                    number+= finalChars[k].length();
                }
                get.left_e_start=""+number;//set left start
                if(b1.getText().toString().equals("LEFT"))//if click again, think as reset
                    b1.setText(s);
                else{
                    b1.setText("LEFT");
                }
                number+=b1.getText().toString().length();
                get.left_e_end=""+number;
            }
        });
        b2.setOnClickListener(new Button.OnClickListener()
        {//monitor right entity like left
            @Override
            public void  onClick(View view)
            {
                int star=0;
                int fina=0;
                ErE tmp = new ErE();
                for(int j = 0; j < 200; j++)
                {
                    if(selectedIndex[j])
                        fina = j;
                }
                star = fina;
                if(star!=0){
                    while(star>=0&&selectedIndex[star])star--;
                    star++;
                }
                if(!selectedIndex[star])star++;
                String s="";
                for(int j = star; j <= fina; j++){
                    s += Btn[j].getText().toString();
                }
                for(int j=0;j<=fina;j++){
                    selectedIndex[j]=false;
                    Btn[j].setTextColor(Color.parseColor("#000000"));
                }
                int number=left_begin;
                for(int k=0;k<star;k++){
                    number+= finalChars[k].length();
                }
                get.right_e_start=""+number;
                if(b2.getText().toString().equals("RIGHT"))
                    b2.setText(s);
                else{
                    b2.setText("RIGHT");
                }
                number+=b2.getText().toString().length();
                get.right_e_end=""+number;
            }
        });
        b3.setOnClickListener(new Button.OnClickListener() {//monitor for relation
            @Override
            public void onClick(View v) {
                if(b3.getText().toString().equals("亲属")){
                    get.relation_id=""+1;
                    b3.setText("任职");
                }
                else {
                    b3.setText("亲属");
                    get.relation_id=""+0;
                }
            }
        });
        AlertDialog.Builder builder = new AlertDialog.Builder(Activity_content.this);
        builder.setView(L);
        builder.setTitle("修改标注");
        builder.setPositiveButton("确定", new DialogInterface.OnClickListener()
        {
            @Override
            public void onClick(DialogInterface dialogInterface, int i)
            {
                String str="zxcvbnmlkjhgfdsaqwertyuiopQWERTYUIOPASDFGHJKLZXCVBNM1234567890";
                Random random=new Random();
                StringBuffer sb=new StringBuffer();
                for(int p=0; p<20; p++){
                    int number=random.nextInt(62);
                    sb.append(str.charAt(number));
                }
                get.id=sb.toString();//random id
                get.status="1";
                get.relation_start=""+(-2);
                get.relation_end=""+(-1);
                get.left_entity=b1.getText().toString();
                get.right_entity=b2.getText().toString();
                if(b3.getText().toString().equals("亲属")){
                    get.relation_id=""+1;
                }
                else {
                    get.relation_id=""+0;
                }
                triples.add(get);//add back
                ready_entity="";
                for(int j = 0; j < triples.size(); j++)
                {//show all added relation
                    ErE e = triples.get(j);
                    if(e.relation_start.equals("-2")){
                        ready_entity = ready_entity +" "+e.left_entity + " "+" "+e.right_entity+" ";
                        if(e.relation_id.equals("1"))ready_entity+="亲属 ";
                        else ready_entity+="任职 ";

                    }
                }
                test = (TextView)findViewById(R.id.tab02);
                test.setText(ready_entity);
                uploadRelation.setTriples(triples);
                Gson gs = new Gson();
                String storagecontent = gs.toJson(uploadRelation);
                write("content.json", storagecontent);
            }
        });
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    public void change(){//change relation
        TableLayout tableLayout=new TableLayout(Activity_content.this);
        ScrollView scrollView=new ScrollView(Activity_content.this);
        LinearLayout linearLayout= new LinearLayout(Activity_content.this);
        final Button Btn[]=new Button[200];
        TextView textView[]=new TextView[200];
        final boolean selectedIndex[]=new boolean[200];
        for(int i = 0;i < triples.size(); i++) {//add textview for entity and button for relation
            selectedIndex[i]=triples.get(i).relation_id.equals("1");
            Btn[i] = new Button(Activity_content.this);
            textView[i]=new TextView(Activity_content.this);
            textView[i].setText(triples.get(i).left_entity+" "+triples.get(i).right_entity);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            params.weight = 1.0f;
            LinearLayout ll=new LinearLayout(Activity_content.this);
            ll.setLayoutParams(params);
            ll.setGravity(Gravity.RIGHT);
            if(selectedIndex[i]){
                Btn[i].setText("亲属");
            }
            else{
                Btn[i].setText("任职");
            }
            ll.addView(Btn[i]);
            LinearLayout.LayoutParams params1= (LinearLayout.LayoutParams) Btn[i].getLayoutParams();
            params1.height=120;//set button size
            params1.width=200;
            Btn[i].setLayoutParams(params1);
            linearLayout.addView(textView[i]);
            linearLayout.addView(ll);
            tableLayout.addView(linearLayout);
            linearLayout= new LinearLayout(Activity_content.this);
        }
        tableLayout.addView(linearLayout);
        scrollView.addView(tableLayout);
        LinearLayout L=new LinearLayout(Activity_content.this);
        L.addView(scrollView);
        for (int k = 0; k < triples.size(); k++)
        {
            Btn[k].setTag(k);
            Btn[k].setOnClickListener(new Button.OnClickListener()
            {//monitor for each button
                @Override
                public void onClick(View view)
                {//click then change state
                    int i = (Integer) view.getTag();
                    if(selectedIndex[i]==true)
                    {
                        selectedIndex[i] = false;
                        if(triples.get(i).relation_start.equals("-1")){
                            if(triples.get(i).status.equals("1"))triples.get(i).status="-1";
                            else triples.get(i).status="1";
                        }
                        Btn[i].setText("任职");//change relation
                    }
                    else
                    {
                        selectedIndex[i] = true;
                        if(triples.get(i).relation_start.equals("-1")){
                            if(triples.get(i).status.equals("1"))triples.get(i).status="-1";
                            else triples.get(i).status="1";
                        }
                        Btn[i].setText("亲属");//change relation
                    }
                }
            });
        }
        AlertDialog.Builder builder = new AlertDialog.Builder(Activity_content.this);
        builder.setView(L);
        builder.setTitle("修改标志");
        builder.setPositiveButton("确定", new DialogInterface.OnClickListener()
        {
            @Override
            public void onClick(DialogInterface dialogInterface, int i)
            {
                for(int t=0;t<triples.size();t++){//set new relation
                    if(selectedIndex[t]){
                        triples.get(t).relation_id=""+1;
                    }
                    else {
                        triples.get(t).relation_id=""+0;
                    }
                }
                uploadRelation.setTriples(triples);//upload relation
                Gson gs = new Gson();
                String storagecontent = gs.toJson(uploadRelation);
                write("content.json", storagecontent);
            }
        });
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    /**
     *
     * @param fileName
     * @return the content of the file named fileName
     * this function is used to read content from file in inner storage
     */
    private String read(String fileName)
    {
        try
        {
            FileInputStream fis = openFileInput(fileName); //open file
            byte[] buff = new byte[1024];
            int hasRead = 0;
            StringBuffer sb = new StringBuffer();
            while((hasRead = fis.read(buff)) > 0)
            {
                sb.append(new String(buff, 0, hasRead));
            }
            fis.close(); // close file
            return sb.toString();
        }catch (Exception e)
        {
            e.printStackTrace();
        }
        return null;
    }

    /**
     *
     * @param fileName
     * @param content
     * this function is used to write content to the file name fileName
     */
    private void write(String fileName, String content) {
        try {
            FileOutputStream fos = openFileOutput(fileName, MODE_PRIVATE);
            try {
                fos.write(content.getBytes());
            } catch (IOException e) {
                e.printStackTrace();
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }
}
