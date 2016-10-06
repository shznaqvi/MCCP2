package com.example.hassannaqvi.mccp2;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.provider.Settings;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

/**
 * Created by hassan.naqvi on 5/5/2016.
 */
public class syncIms extends AsyncTask<Void, Void, String> {

    private static final String TAG = "syncIms";
    private Context mContext;
    private ProgressDialog pd;

    public syncIms(Context context) {
        mContext = context;
    }

    public static void longInfo(String str) {
        if (str.length() > 4000) {
            Log.i("TAG: ", str.substring(0, 4000));
            longInfo(str.substring(4000));
        } else
            Log.i("TAG: ", str);
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        pd = new ProgressDialog(mContext);
        pd.setTitle("Please wait... Processing IMs");
        pd.show();


    }

    @Override
    protected String doInBackground(Void... params) {
        HttpURLConnection connection = null;
        String line = "no response";
        try {
            String request = "http://192.168.1.10/appdata/census/syncim.php";
            //String request = "http://10.1.42.86/appdata/syncim.php";

            URL url = new URL(request);
            connection = (HttpURLConnection) url.openConnection();
            connection.setDoOutput(true);
            connection.setDoInput(true);
            connection.setInstanceFollowRedirects(false);
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            connection.setRequestProperty("charset", "utf-8");
            connection.setUseCaches(false);
            connection.connect();


            JSONArray jsonSync = new JSONArray();

            DataOutputStream wr = new DataOutputStream(connection.getOutputStream());
            FormsDbHelper db = new FormsDbHelper(mContext);
            String android_id = Settings.Secure.getString(mContext.getContentResolver(),
                    Settings.Secure.ANDROID_ID);

            List<ImsContract> ims = db.getAllIms();

            for (ImsContract im : ims) {

                JSONObject jsonParam = new JSONObject();

                jsonParam.put("imFrmno", im.getFrmNo());
                jsonParam.put("imChid", im.getChid());
                jsonParam.put("im", im.getIM());
                jsonParam.put("DeviceId", android_id);
                jsonSync.put(jsonParam);

            }
            wr.writeBytes(jsonSync.toString());
            longInfo(jsonSync.toString());
            wr.flush();

            int HttpResult = connection.getResponseCode();
            if (HttpResult == HttpURLConnection.HTTP_OK) {
                BufferedReader br = new BufferedReader(new InputStreamReader(
                        connection.getInputStream(), "utf-8"));
                StringBuffer sb = new StringBuffer();

                while ((line = br.readLine()) != null) {
                    sb.append(line + "\n");
                }
                br.close();

                System.out.println("" + sb.toString());
                return sb.toString();
            } else {
                System.out.println(connection.getResponseMessage());
                return connection.getResponseMessage();
            }
        } catch (MalformedURLException e) {

            e.printStackTrace();
        } catch (IOException e) {

            e.printStackTrace();
        } catch (JSONException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } finally {
            if (connection != null)
                connection.disconnect();
        }
        return line;
    }

    @Override
    protected void onPostExecute(String result) {
        super.onPostExecute(result);

        pd.setMessage("Server Response: " + result);
        pd.setTitle("Please wait... Done IMs");
        pd.show();
            /*Handler h = new Handler();
            h.postDelayed(new Runnable() {
                @Override
                public void run() {
                    pd.dismiss();
                }
            }, 3000);*/
    }
}