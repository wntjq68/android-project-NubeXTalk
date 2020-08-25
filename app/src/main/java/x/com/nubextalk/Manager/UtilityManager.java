package x.com.nubextalk.Manager;

import android.content.Context;
import android.content.res.Resources;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import com.afollestad.materialdialogs.MaterialDialog;

import java.io.IOException;
import java.io.InputStream;

import io.realm.RealmConfiguration;
import x.com.nubextalk.R;


public class UtilityManager {

    /**
     * Realm session 초기화 시 필요한 Config
     * Activity 실행시 onCreate(onResume) 등에 초기화 해야함.
     * 사용법 :  Realm realm = Realm.getInstance(UtilityManager.getRealmConfig());
     *
     * @return
     */
    public static RealmConfiguration getRealmConfig(){
        RealmConfiguration config = new RealmConfiguration.Builder()
                .schemaVersion(0)
                .deleteRealmIfMigrationNeeded() //Model 변경시에 DB 삭제 (Migration은 추후 구현)
                .build();
        return config;
    }

    /**
     * asset/json 폴더의 json 파일 로드
     *
     * @param context
     * @param fileName
     * @return
     */
    public static String loadJson(Context context, String fileName) {
        String jsonString;
        try {
            InputStream is = context.getAssets().open("json/"+fileName);
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();
            jsonString = new String(buffer, "UTF-8");
        } catch (IOException ex) {
            ex.printStackTrace();
            return null;
        }
        return jsonString;
    }

    /**
     * 문자열 유효성 체크
     *
     * @param s
     * @return
     */
    public static Boolean checkString(String s){
        if(s == null){ return false; }
        if(s.isEmpty()) { return false; }
        if(s.equals("")) { return false; }
        if(s.equals("null")) { return false; }
        return true;
    }

    /**
     * 네트워크 체크
     *  0 : 네트워크 비활성화
     *  1 : WIFI
     *  2 : LTE 등 모바일 네트워크
     *
     * @param context
     * @return
     */
    public static int checkNetwork(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context
                .getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        if (null != activeNetwork) {
            if(activeNetwork.getType() == ConnectivityManager.TYPE_WIFI)
                return 1;

            if(activeNetwork.getType() == ConnectivityManager.TYPE_MOBILE)
                return 2;
        }
        return 0;
    }

    /**
     * 로딩 Dialog 생성
     *
     * @param context
     * @return
     */
    public static MaterialDialog showLoadingDialog(Context context){
        return new MaterialDialog.Builder(context)
                .title(R.string.dlg_title_load)
                .content(R.string.dlg_desc_load)
                .progress(true, 0)
                .canceledOnTouchOutside(false)
                .cancelable(false)
                .show();
    }

    /**
     * 로딩 Dialog 해제
     *
     * @param dialog
     * @return
     */
    public static MaterialDialog dismissLoadingDlg(MaterialDialog dialog){
        if(dialog == null){ return null; }
        dialog.dismiss();
        return null;
    }

    /**
     * dp 값을 px 값으로 변환
     *
     * @param ctx
     * @param val
     * @return
     */
    public static int dpToPx(Context ctx, int val) {
        return (int) (val * Resources.getSystem().getDisplayMetrics().density);
    }

    /**
     * px 값을 dp 값으로 변환
     * @param ctx
     * @param val
     * @return
     */
    public static int pxToDp(Context ctx, int val) {
        return (int) (val / Resources.getSystem().getDisplayMetrics().density);
    }

    /**
     * deg 값을 rad 값으로 변환
     *
     * @param deg
     * @return
     */
    private static double deg2rad(double deg) {
        return (deg * Math.PI / 180.0);
    }

    /**
     * rad 값을 deg 값으로 변환
     *
     * @param rad
     * @return
     */
    private static double rad2deg(double rad) {
        return (rad * 180.0 / Math.PI);
    }
}
