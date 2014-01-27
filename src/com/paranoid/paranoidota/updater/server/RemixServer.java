package com.paranoid.paranoidota.updater.server;

import android.content.Context;

import com.paranoid.paranoidota.R;
import com.paranoid.paranoidota.Version;
import com.paranoid.paranoidota.updater.Server;
import com.paranoid.paranoidota.updater.UpdatePackage;
import com.paranoid.paranoidota.updater.Updater;
import com.paranoid.paranoidota.updater.Updater.PackageInfo;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class RemixServer implements Server {


    private static final String URL = "http://trip.tf-swufe.net/xilence/check?d=%s";

    private Context mContext;
    private String mDevice = null;
    private String mError = null;
    private Version mVersion;
    private boolean mIsRom;
    
    public RemixServer(Context context, boolean isRom) {
        mContext = context;
        mIsRom = isRom;
    }
    
    @Override
    public String getUrl(String device, Version version) {
        mDevice = device;
        mVersion = version;
        return String.format(URL, new Object[] {device});
    }

    @Override
    public List<PackageInfo> createPackageInfoList(JSONObject response) throws Exception {
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
                Version version = new Version(filename);
                if (Version.compare(mVersion, version) < 0) {
                    list.add(new UpdatePackage(mDevice, filename, version, file.getString("size"),
                            file.getString("url"), file.getString("md5"), true));
                }
            }
        }
        Collections.sort(list, new Comparator<PackageInfo>() {

            @Override
            public int compare(PackageInfo lhs, PackageInfo rhs) {
                return Version.compare(lhs.getVersion(), rhs.getVersion());
            }

        });
        return list;
    }

    @Override
    public String getError() {
        return mError;
    }

}
