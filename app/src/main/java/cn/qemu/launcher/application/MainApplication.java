package cn.qemu.launcher.application;
import android.app.*;
import cn.qemu.launcher.utils.VMR;
import java.io.*;
import android.content.*;
import android.os.*;

public class MainApplication extends Application {
	public final static String PREF_SETTINGS = "settings";
	public static boolean IS_DARK = false;

	@Override
	public void onCreate() {
		super.onCreate();
		String pre = getCacheDir().getAbsolutePath();
		VMR.ENV.put("TMPDIR", pre);
		VMR.ENV.put("PROOT_TMPDIR", pre);
		pre = getFilesDir().getAbsolutePath();
		VMR.setDir(new File(pre+"/bin"));
		VMR.ENV.put("PREF", android.os.Build.VERSION.SDK_INT>25?"./proot -b ../etc/resolv.conf:/etc/resolv.conf":"");
		VMR.ENV.put("DISPLAY", "127.0.0.1:0");
		VMR.ENV.put("LD_LIBRARY_PATH", pre+"/lib");
		VMR.ENV.put("QEMU_MODULE_DIR", pre+"/lib/qemu");
		SharedPreferences sp = getSharedPreferences(PREF_SETTINGS, MODE_PRIVATE);
		IS_DARK = sp.getBoolean("is_dark", false);
		SharedPreferences.Editor edt = sp.edit();
		if (!sp.contains("sh_path"))
			edt.putString("sh_path", Environment.getExternalStorageDirectory().getAbsolutePath()+"/Qemu");
		if (!sp.contains("logcat_params"))
			edt.putString("logcat_params", "*:V BLASTBufferQueue:S");
		if (!sp.contains("bios_path"))
			edt.putString("bios_path", pre+"/share/qemu");
		edt.commit();
	}
}
