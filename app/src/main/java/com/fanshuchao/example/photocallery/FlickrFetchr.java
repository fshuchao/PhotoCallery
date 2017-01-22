package com.fanshuchao.example.photocallery;

import android.net.Uri;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by fanshuchao on 2017-1-22.
 */

public class FlickrFetchr {

    private static final String TAG = "FlickFetchr";
    private static final String API_KEY = "4f721bgafa75bf6d2cb9af54f937bb70";
    //从指定URL获取原始数据并返回一个字节流数组
    public byte[] getUrlBytes(String urlSpec) throws IOException{
        URL url = new URL(urlSpec);
        HttpURLConnection connection = (HttpURLConnection)url.openConnection();

        try{
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            InputStream in = connection.getInputStream();

            if(connection.getResponseCode()!= HttpURLConnection.HTTP_OK){
                throw new IOException(connection.getResponseMessage()+ ":with "+urlSpec);
            }
            int bytesRead = 0;
            byte[] buffer = new byte[1024];
            while ((bytesRead = in.read(buffer))>0){
                out.write(buffer,0,bytesRead);
            }
            out.close();
            return out.toByteArray();
        }finally {
            connection.disconnect();
        }
    }
    //将getUrlBytes(String)方法返回的结果转换为String
    public String getUrlString(String urlSpec)throws IOException{
        return new String(getUrlBytes(urlSpec));
    }

    public List<GalleryItem> fetchItems(){
        List<GalleryItem> items = new ArrayList<>();
        try{
            String url = Uri.parse("https://api.flickr.com/services/rest/")
                    .buildUpon()
                    .appendQueryParameter("method","flickr.photos.getRecent")
                    .appendQueryParameter("api_key",API_KEY)
                    .appendQueryParameter("format","json")
                    .appendQueryParameter("nojsoncallback","1")
                    .appendQueryParameter("extras","url_s")
                    .build().toString();
            String jsonString = getUrlString(url);
            Log.i(TAG,"Received JSON: "+ jsonString);
            JSONObject jsonBody = new JSONObject(jsonString);//生成与原始JSON数据对应的对象树
            parseItems(items,jsonBody);
        }catch(JSONException je) {
            Log.e(TAG,"Failed to parse JSON",je);
        }catch(IOException ioe){
            Log.e(TAG, "Failed to fetch items",ioe );
        }
        return items;
    }

    private void parseItems(List<GalleryItem> items,JSONObject jsonBody)
            throws IOException,JSONException{
        JSONObject photosJsonObject = jsonBody.getJSONObject("photos");
        JSONArray photoJsonArray = photosJsonObject.getJSONArray("photo");

        for(int i = 0;i<photoJsonArray.length();i++){
            JSONObject photoJsonObject = photoJsonArray.getJSONObject(i);

            GalleryItem item = new GalleryItem();
            item.setId(photoJsonObject.getString("id"));
            item.setCaption(photoJsonObject.getString("title"));
            //检查是否有对应的url_s链接
            if(!photoJsonObject.has("url_s")){
                continue;
            }
            item.setUrl(photoJsonObject.getString("url_s"));
            items.add(item);
        }
    }
}
