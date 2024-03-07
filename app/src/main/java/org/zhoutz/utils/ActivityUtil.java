package org.zhoutz.utils;

import android.app.*;
import android.content.*;
import android.content.res.*;
import android.util.*;
import android.view.*;
import java.io.*;

public class ActivityUtil {
		/**
		 * 截屏
		 * @param activity
		 * @return
		 */
    private static final String TAG_FAKE_STATUS_BAR_VIEW = "statusBarView";
	private static final String TAG_MARGIN_ADDED = "marginAdded";
	public static final FileFilter DIRFILTER = new FileFilter() {
		public boolean accept(File f) {
			return f.isDirectory();
		}
	};
/*
	public static Bitmap captureScreen(Activity activity) {
// 获取屏幕大小：
		DisplayMetrics metrics = new DisplayMetrics();
		WindowManager WM = (WindowManager) activity
			.getSystemService(Context.WINDOW_SERVICE);
		Display display = WM.getDefaultDisplay();
		display.getMetrics(metrics);
		int height = metrics.heightPixels; // 屏幕高
		int width = metrics.widthPixels; // 屏幕的宽
// 获取显示方式
		int pixelformat = display.getPixelFormat();
		PixelFormat localPixelFormat1 = new PixelFormat();
		PixelFormat.getPixelFormatInfo(pixelformat, localPixelFormat1);
		int deepth = localPixelFormat1.bytesPerPixel;// 位深
		byte[] piex = new byte[height * width * deepth];
		try {
			Runtime.getRuntime().exec(
				new String[] { "/system/bin/su","-c",
					"chmod 777 /dev/graphics/fb0" });
		}
		catch (IOException e) {
			e.printStackTrace();
		}
		try {
// 获取fb0数据输入流
			InputStream stream = new FileInputStream(new File(
															 "/dev/graphics/fb0"));
			DataInputStream dStream = new DataInputStream(stream);
			dStream.readFully(piex);
		}
		catch (Exception e) {
			e.printStackTrace();
		}
// 保存图片
		int[] colors = new int[height * width];
		for (int m = 0; m < colors.length; m++) {
			int r = (piex[m * 4] & 0xFF);
			int g = (piex[m * 4 + 1] & 0xFF);
			int b = (piex[m * 4 + 2] & 0xFF);
			int a = (piex[m * 4 + 3] & 0xFF);
			colors[m] = (a << 24) + (r << 16) + (g << 8) + b;
		}
// piex生成Bitmap
		Bitmap bitmap = Bitmap.createBitmap(colors, width, height,
												Bitmap.Config.ARGB_8888);
		return bitmap;
	}

//读取asset文件
	public static byte[] readAsset(Context context, String name) throws IOException {
		AssetManager am = context.getAssets();
		InputStream is = am.open(name);
		byte[] ret= readAll(is);
		is.close();
		//am.close();
		return ret;
	}

	public static byte[] readAll(InputStream input) throws IOException {
		ByteArrayOutputStream output = new ByteArrayOutputStream(4096);
		byte[] buffer = new byte[2 ^ 32];
		int n = 0;
		while (-1 != (n = input.read(buffer))) {
			output.write(buffer, 0, n);
		}
		byte[] ret= output.toByteArray();
		output.close();
		return ret;
	}
*/
	private static boolean copyFile(InputStream in, OutputStream out) { 
		try {
			int byteread = 0; 
			byte[] buffer = new byte[0x10000]; 
			while ((byteread = in.read(buffer)) != -1) { 
				out.write(buffer, 0, byteread); 
			} 
			in.close(); 
			out.close();
		} catch (Exception e) { 
			Log.d("lua",e.getMessage());
			return false;
		} 
		return true;
	} 

	public static boolean copyDir(String from, String to) {
		return copyDir(new File(from), new File(to));
	}

	public static boolean copyDir(File from, File to) {
		boolean ret=true;
		File p=to.getParentFile();
		if (!p.exists())
			p.mkdirs();
		if (from.isDirectory()) {
			File[] fs=from.listFiles();
			if (fs != null && fs.length != 0)
				for (File f:fs)
					ret=copyDir(f, new File(to, f.getName()));
			else if (!to.exists())
				ret=to.mkdirs();
		}
		else {
			try {
				if (!to.exists())
					to.createNewFile();
				ret=copyFile(new FileInputStream(from), new FileOutputStream(to));
			}
			catch (IOException e) {
				Log.d("lua",e.getMessage());
				ret=false;
			}
		}
		return ret;
	}

	public static boolean rmDir(File dir) {
		if(dir.isDirectory()){
			File[] fs=dir.listFiles();
			for (File f:fs)
				rmDir(f);
		}
		return dir.delete();
	}

	public static void rmDir(File dir, String ext) {
		if(dir.isDirectory()){
			File[] fs=dir.listFiles();
			for (File f:fs)
				rmDir(f,ext);
			dir.delete();
		}
		if (dir.getName().endsWith(ext))
			dir.delete();
	}

// 计算文件的 MD5 值
/*
	public static String getFileMD5(File file) {
		try {
			return getFileMD5(new FileInputStream(file));
		}
		catch (FileNotFoundException e) {
			return null;
		}
	}
	public static String getFileMD5(InputStream in) {
		byte buffer[] = new byte[8192];
		int len;
		try {
			MessageDigest digest =MessageDigest.getInstance("MD5");
			while ((len = in.read(buffer)) != -1) {
				digest.update(buffer, 0, len);
			}
			BigInteger bigInt = new BigInteger(1, digest.digest());
			return bigInt.toString(16);
		}
		catch (Exception e) {
			e.printStackTrace();
			return null;
		}
		finally {
			try {
				in.close();
			}
			catch (Exception e) {
				e.printStackTrace();
			}
		}

	}

// 计算文件的 SHA-1 值
	public static String getFileSha1(InputStream in) {
		byte buffer[] = new byte[8192];
		int len;
		try {
			MessageDigest digest =MessageDigest.getInstance("SHA-1");
			while ((len = in.read(buffer)) != -1) {
				digest.update(buffer, 0, len);
			}
			BigInteger bigInt = new BigInteger(1, digest.digest());
			return bigInt.toString(16);
		}
		catch (Exception e) {
			e.printStackTrace();
			return null;
		}
		finally {
			try {
				in.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}*/

	public static void setStatusBarColor(Activity activity, int statusColor) {
		Window window = activity.getWindow();
		//设置Window为全透明
		window.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);

		ViewGroup mContentView = (ViewGroup) window.findViewById(Window.ID_ANDROID_CONTENT);
		//获取父布局
		View mContentChild=mContentView.getChildAt(0);
		//获取状态栏高度
		int statusBarHeight=getStatusBarHeight(activity);

		//如果已经存在假状态栏则移除，防止重复添加
		removeFakeStatusBarViewIfExist(activity);
		//添加一个View来作为状态栏的填充
		addFakeStatusBarView(activity, statusColor, statusBarHeight);
		//设置子控件到状态栏的间距
		addMarginTopToContentChild(mContentChild, statusBarHeight);
		//不预留系统栏位置
		if (mContentChild != null) {
			android.support.v4.view.ViewCompat.setFitsSystemWindows(mContentChild, false);
		}
		//如果在Activity中使用了ActionBar则需要再将布局与状态栏的高度跳高一个ActionBar的高度，否则内容会被ActionBar遮挡
		int action_bar_id = activity.getResources().getIdentifier("action_bar", "id", activity.getPackageName());
		View view = activity.findViewById(action_bar_id);
		if (view != null) {
			android.util.TypedValue typedValue = new android.util.TypedValue();
			if (activity.getTheme().resolveAttribute(cn.qemu.launcher.R.attr.actionBarSize, typedValue, true)) {
				int actionBarHeight = android.util.TypedValue.complexToDimensionPixelSize(typedValue.data, activity.getResources().getDisplayMetrics());
				setContentTopPadding(activity, actionBarHeight);
			}
		}
	}

	private static int getStatusBarHeight(Context context) {
        int result = 0;
        int resId = context.getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resId > 0) {
            result = context.getResources().getDimensionPixelOffset(resId);
        }
        return result;
    }

	private static View addFakeStatusBarView(Activity activity, int statusBarColor, int statusBarHeight) {
		Window window = activity.getWindow();
		ViewGroup mDecorView = (ViewGroup) window.getDecorView();

		View mStatusBarView = new View(activity);
		android.widget.FrameLayout.LayoutParams layoutParams = new android.widget.FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, statusBarHeight);
		layoutParams.gravity = android.view.Gravity.TOP;
		mStatusBarView.setLayoutParams(layoutParams);
		mStatusBarView.setBackgroundColor(statusBarColor);
		mStatusBarView.setTag(TAG_FAKE_STATUS_BAR_VIEW);

		mDecorView.addView(mStatusBarView);
		return mStatusBarView;
	}

	private static void removeFakeStatusBarViewIfExist(Activity activity) {
		Window window = activity.getWindow();
		ViewGroup mDecorView = (ViewGroup) window.getDecorView();

		View fakeView = mDecorView.findViewWithTag(TAG_FAKE_STATUS_BAR_VIEW);
		if (fakeView != null) {
			mDecorView.removeView(fakeView);
		}
	}

	private static void addMarginTopToContentChild(View mContentChild, int statusBarHeight) {
		if (mContentChild == null) {
			return;
		}
		if (!TAG_MARGIN_ADDED.equals(mContentChild.getTag())) {
			android.widget.FrameLayout.LayoutParams lp = (android.widget.FrameLayout.LayoutParams) mContentChild.getLayoutParams();
			lp.topMargin += statusBarHeight;
			mContentChild.setLayoutParams(lp);
			mContentChild.setTag(TAG_MARGIN_ADDED);
		}
	}
	
	private static void setContentTopPadding(Activity activity, int padding) {
		ViewGroup mContentView = (ViewGroup) activity.getWindow().findViewById(Window.ID_ANDROID_CONTENT);
		mContentView.setPadding(0, padding, 0, 0);
	}

	public static String getVerName(android.content.Context context) {
        String verName = "";
        try {
            verName = context.getPackageManager().
				getPackageInfo(context.getPackageName(), 0).versionName;
        } catch (android.content.pm.PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return verName;
    }

	public static int dpToPx(int dp) {
        return (int) (dp * Resources.getSystem().getDisplayMetrics().density);
    }
}
