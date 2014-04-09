package com.paranoid.paranoidota.updater.server;

import android.content.Context;
import android.text.TextUtils;

import com.paranoid.paranoidota.R;
import com.paranoid.paranoidota.Version;
import com.paranoid.paranoidota.updater.Server;
import com.paranoid.paranoidota.updater.UpdatePackage;
import com.paranoid.paranoidota.updater.Updater;
import com.paranoid.paranoidota.updater.Updater.PackageInfo;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

public class RemixServer implements Server {


    private static final String URL = "http://trip.tf-swufe.net/xilence/check/%s?lang=%s";

    private Context mContext;
    private String mDevice = null;
    private String mError = null;
    private Version mVersion;
    
    public RemixServer(Context context) {
        mContext = context;
    }
    
    @Override
    public String getUrl(String device, Version version) {
        mDevice = device;
        mVersion = version;
        return String.format(URL, new Object[] {device, Locale.getDefault().getLanguage().toLowerCase()});
    }

    @Override
    public List<PackageInfo> createPackageInfoList(JSONObject response) throws Exception {
        mError = null;
        List<PackageInfo> list = new ArrayList<PackageInfo>();
        mError = response.optString("error");
        if (mError == null || mError.isEmpty()) {
            JSONArray updates = response.getJSONArray("updates");
            for (int i = updates.length() - 1; i >= 0; i--) {
                JSONObject file = updates.getJSONObject(i);
                String filename = file.optString("name");
                String stripped = filename.replace(".zip", "");
                String[] parts = stripped.split("-");
                boolean isNew = parts[parts.length - 1].matches("[-+]?\\d*\\.?\\d+");
                if (!isNew) {
                    continue;
                }
                String intro = file.optString("intro");
                if (!TextUtils.isEmpty(intro)) {
                    intro = URLDecoder.decode(intro, "utf-8");
                }
                Version version = new Version(filename);
                if (Version.compare(mVersion, version) < 0) {
                    list.add(new UpdatePackage(mDevice, filename, version, file.getString("size"),
                            file.getString("url"), file.getString("md5"), intro, true));
                }
            }
        }
        Collections.sort(list, new Comparator<PackageInfo>() {

            @Override
            public int compare(PackageInfo lhs, PackageInfo rhs) {
                return Version.compare(lhs.getVersion(), rhs.getVersion());
            }

        });
        Collections.reverse(list);
        return list;
    }

    @Override
    public String getError() {
        if(mError.equals("-1")) {
            mError = mContext.getString(R.string.no_updates_found);
        } else if (mError.equals("-2")) {
            mError = mContext.getString(R.string.check_rom_updates_error);
        }
        return mError;
    }

}

