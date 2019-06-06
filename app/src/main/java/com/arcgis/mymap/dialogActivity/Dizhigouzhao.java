package com.arcgis.mymap.dialogActivity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import com.arcgis.mymap.Geological.Voice.VoiceToText;
import com.arcgis.mymap.R;
import com.arcgis.mymap.bean.JsonBean;
import com.arcgis.mymap.contacts.MyDatabaseHelper;
import com.arcgis.mymap.utils.GetJsonDataUtil;
import com.arcgis.mymap.utils.LogUtils;
import com.bigkoo.pickerview.builder.OptionsPickerBuilder;
import com.bigkoo.pickerview.listener.OnOptionsSelectListener;
import com.bigkoo.pickerview.view.OptionsPickerView;
import com.google.gson.Gson;
import org.json.JSONArray;
import java.util.ArrayList;

public class Dizhigouzhao extends Activity implements View.OnClickListener{
    private Button bt1,bt2,cancel,sure;
    private EditText et1,et;
    public ArrayList<JsonBean> options1Items = new ArrayList<>();//省;
    public ArrayList<ArrayList<String>> options2Items = new ArrayList<>();//市;
    public ArrayList<ArrayList<ArrayList<String>>> options3Items = new ArrayList<>();//区;
    private String pposition;
    private MyDatabaseHelper dbHelper;
    private SQLiteDatabase db;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dizhigouzhao);
        getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        initView();
    }
    private void initView() {
        bt1 = (Button) findViewById(R.id.lxBt);
        bt2 = (Button) findViewById(R.id.msBt);
        et = (EditText) findViewById(R.id.nameEt);
        et1 = (EditText) findViewById(R.id.msEt);
        cancel = (Button) findViewById(R.id.cancel);
        sure = (Button) findViewById(R.id.sure);
        cancel.setOnClickListener(this);
        sure.setOnClickListener(this);
        bt1.setOnClickListener(this);
        bt2.setOnClickListener(this);

        dbHelper=new MyDatabaseHelper(this,"pointsStore.db",null,5);
        db=dbHelper.getWritableDatabase();

        Intent intent = getIntent();
        pposition = intent.getStringExtra("position");
        Cursor utc=db.query("Geomorelines"+pposition,null,"name != ? and name != ? and name != ? and name != ? and name != ? and name != ? and name != ?",
                new String[]{"H","Z","J","P","T","R","FY"},null,null,null);
        Cursor cursor = db.query("Geomorelines"+pposition,null,null,null,null,null,null);
        String str = FirstNameUtils.getName(utc,"DZ");
        String classification = FirstNameUtils.getClassification2(cursor,"背斜褶皱");
        et.setText(str);
        bt1.setText(classification);
    }
    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.lxBt:
                initJsonData(Dizhigouzhao.this,"dizhigouzhao.json");
                setpvOptions("类型",bt1,options1Items);
                break;
            case R.id.msBt:
                VoiceToText.startSpeechDialog(Dizhigouzhao.this,et1);
                break;
            case R.id.cancel:
                finish();
                break;
            case R.id.sure:
                Intent intent = new Intent();
                intent.putExtra("name",et.getText().toString());
                intent.putExtra("lx",bt1.getText().toString());
                intent.putExtra("ms",et1.getText().toString());
                setResult(10005, intent);
                finish();
                break;
        }
    }
    /**
     * 三级联动
     * @param text
     * @param bt
     * @param options1Items
     * @param options2Items
     * @param options3Items
     */
    public void setpvOptions(String text, final Button bt, final ArrayList<JsonBean> options1Items , final ArrayList<ArrayList<String>> options2Items, final ArrayList<ArrayList<ArrayList<String>>> options3Items ){
        OptionsPickerView pvOptions = new OptionsPickerBuilder(Dizhigouzhao.this, new OnOptionsSelectListener() {
            @Override
            public void onOptionsSelect(int options1, int options2, int options3, View v) {
                String tx = options1Items.get(options1).getPickerViewText()+
                        options2Items.get(options1).get(options2)+
                        options3Items.get(options1).get(options2).get(options3);
                bt.setText(tx);
            }
        }).setTitleText(text)
                .setDividerColor(Color.BLACK)
                .setTextColorCenter(Color.BLACK) //设置选中项文字颜色
                .setContentTextSize(20)
                .build();
            pvOptions.setPicker(options1Items, options2Items,options3Items);//三级选择器
        pvOptions.show();
    }

    /**
     * 二级联动
     * @param text
     * @param bt
     * @param options1Items
     * @param options2Items
     */
    public void setpvOptions(String text, final Button bt, final ArrayList<JsonBean> options1Items , final ArrayList<ArrayList<String>> options2Items ){
        OptionsPickerView pvOptions = new OptionsPickerBuilder(Dizhigouzhao.this, new OnOptionsSelectListener() {
            @Override
            public void onOptionsSelect(int options1, int options2, int options3, View v) {
                String tx = options1Items.get(options1).getPickerViewText()+
                        options2Items.get(options1).get(options2);
                bt.setText(tx);
            }
        }).setTitleText(text)
                .setDividerColor(Color.BLACK)
                .setTextColorCenter(Color.BLACK) //设置选中项文字颜色
                .setContentTextSize(20)
                .build();
        pvOptions.setPicker(options1Items, options2Items);//二级选择器
        pvOptions.show();
    }

    /**
     * 一级联动
     * @param text
     * @param bt
     * @param options1Items
     */
    public void setpvOptions(String text, final Button bt, final ArrayList<JsonBean> options1Items  ){
        OptionsPickerView pvOptions = new OptionsPickerBuilder(Dizhigouzhao.this, new OnOptionsSelectListener() {
            @Override
            public void onOptionsSelect(int options1, int options2, int options3, View v) {
                String tx = options1Items.get(options1).getPickerViewText();
                bt.setText(tx);
            }
        }).setTitleText(text)
                .setDividerColor(Color.BLACK)
                .setTextColorCenter(Color.BLACK) //设置选中项文字颜色
                .setContentTextSize(20)
                .build();
        pvOptions.setPicker(options1Items);//一级选择器
        pvOptions.show();
    }
    public void initJsonData(Context context,String name) {//解析数据

        /**
         * 注意：assets 目录下的Json文件仅供参考，实际使用可自行替换文件
         * 关键逻辑在于循环体
         *
         * */
        String JsonData = new GetJsonDataUtil().getJson(context, name);//获取assets目录下的json文件数据
        LogUtils.i("TAG","data="+JsonData);

        ArrayList<JsonBean> jsonBean = parseData(JsonData);//用Gson 转成实体

        /**
         * 添加省份数据
         *
         * 注意：如果是添加的JavaBean实体，则实体类需要实现 IPickerViewData 接口，
         * PickerView会通过getPickerViewText方法获取字符串显示出来。
         */
        /**
         * 清空数据
         */
        options1Items.clear();
        options2Items.clear();
        options3Items.clear();

        options1Items = jsonBean;

        for (int i = 0; i < jsonBean.size(); i++) {//遍历省份
            ArrayList<String> CityList = new ArrayList<>();//该省的城市列表（第二级）
            ArrayList<ArrayList<String>> Province_AreaList = new ArrayList<>();//该省的所有地区列表（第三极）

            for (int c = 0; c < jsonBean.get(i).getCityList().size(); c++) {//遍历该省份的所有城市
                String CityName = jsonBean.get(i).getCityList().get(c).getName();
                CityList.add(CityName);//添加城市

                ArrayList<String> City_AreaList = new ArrayList<>();//该城市的所有地区列表

                //如果无地区数据，建议添加空字符串，防止数据为null 导致三个选项长度不匹配造成崩溃
                if (jsonBean.get(i).getCityList().get(c).getArea() == null
                        || jsonBean.get(i).getCityList().get(c).getArea().size() == 0) {
                    City_AreaList.add("");
                } else {

                    for (int d = 0; d < jsonBean.get(i).getCityList().get(c).getArea().size(); d++) {//该城市对应地区所有数据
                        String AreaName = jsonBean.get(i).getCityList().get(c).getArea().get(d);

                        City_AreaList.add(AreaName);//添加该城市所有地区数据
                    }
                }
                Province_AreaList.add(City_AreaList);//添加该省所有地区数据
            }

            /**
             * 添加城市数据
             */
            options2Items.add(CityList);

            /**
             * 添加地区数据
             */
            options3Items.add(Province_AreaList);
        }

    }
    public ArrayList<JsonBean> parseData(String result) {//Gson 解析
        ArrayList<JsonBean> detail = new ArrayList<>();
        try {
            JSONArray data = new JSONArray(result);
            Gson gson = new Gson();
            for (int i = 0; i < data.length(); i++) {
                JsonBean entity = gson.fromJson(data.optJSONObject(i).toString(), JsonBean.class);
                detail.add(entity);
            }
        } catch (Exception e) {
            e.printStackTrace();

        }
        return detail;

    }
}
