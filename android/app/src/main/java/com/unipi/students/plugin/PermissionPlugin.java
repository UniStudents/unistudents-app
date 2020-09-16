package com.unipi.students.plugin;

import android.content.ComponentName;
import android.content.Intent;
import android.util.Log;

import com.getcapacitor.JSObject;
import com.getcapacitor.NativePlugin;
import com.getcapacitor.Plugin;
import com.getcapacitor.PluginCall;
import com.getcapacitor.PluginMethod;

@NativePlugin()
public class PermissionPlugin extends Plugin {

    @PluginMethod()
    public void isXiaomi(PluginCall call) {
        try {
            String manufacturer = android.os.Build.MANUFACTURER;
            JSObject ret = new JSObject();
            ret.put("isXiaomi", "xiaomi".equalsIgnoreCase(manufacturer));
            call.success(ret);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @PluginMethod()
    public void openAutoStartPermission(PluginCall call) {
        try {
            Intent intent = new Intent();
            String manufacturer = android.os.Build.MANUFACTURER;
            if ("xiaomi".equalsIgnoreCase(manufacturer)) {
                intent.setComponent(new ComponentName("com.miui.securitycenter", "com.miui.permcenter.autostart.AutoStartManagementActivity"));
            }
            this.getActivity().startActivity(intent);
            call.success();
        } catch (Exception e) {
            Log.e("exc" , String.valueOf(e));
            call.reject(e.getMessage());
        }

    }
}
