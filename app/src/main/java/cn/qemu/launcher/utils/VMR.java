package cn.qemu.launcher.utils;
import java.util.ArrayList;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.Resources;
import cn.qemu.launcher.activity.MainActivity;
import cn.qemu.launcher.R;
import org.zhoutz.utils.ACode;
import java.io.*;
import android.support.v4.util.*;
import android.util.JsonReader;
import android.util.JsonWriter;
import java.util.regex.*;
import android.support.v7.app.AlertDialog.Builder;
import java.util.*;
import android.widget.Toast;

public class VMR implements FileFilter
{
	private String[] arch={},network_card={},vga={};
	private static String[] sh={"/system/bin/sh","-c",""};
	private static String[] su={"/system/bin/su", "-c", ""};
	private final static String jAllConf="vm_config_all.json",jMac="mac",jCpuMode="cpu_mode",jSoundhw="soundhw",jNetworkCard="network_card",jVGA="vga";
	private String fl;
	private ArrayMap<String,String[]> mac_tye = new ArrayMap<String,String[]>(),cpu_mode=new ArrayMap<String,String[]>(),soundhw=new ArrayMap<String,String[]>();
	private static ProcessBuilder pb = new ProcessBuilder();
	public static Map<String,String> ENV = pb.environment();
	private String version;

	public static void setDir(File dir) {
		pb.directory(dir);
	}

	public static String[] shellSh(String str){
		sh[2] = str;
		return sh;
	}

	public static Process exec(String cmd) throws IOException{
		return pb.command(shellSh(cmd)).start();
	}

	public static Process execsu(String cmd) throws IOException{
		su[2] = cmd;
		return pb.command(su).start();
	}

	private static String shell(String cmd) throws Exception{
		Process p = exec(cmd);
		p.waitFor();
		InputStream is = p.getInputStream();
		byte[] b = new byte[is.available()];
		is.read(b);
		is.close();
		return new String(b);
	}

	public boolean accept(File file){
		return file.isFile()&&file.getName().matches("vm_config_[\\w_]+\\.json");
	}

	public void initAll(final android.app.Activity mCont){
		final String vm_conf = MainActivity.SDHOME+"/Qemu/";
		final ACode ac = new ACode(mCont);
		if(!ACode.fi(vm_conf))
			new File(vm_conf).mkdir();
		fl = mCont.getFilesDir().getPath();
		try{
			version = ACode.fr(vm_conf+"version");
			if (version == null) {
				version = "5.1.0";
				throw new FileNotFoundException("File \""+vm_conf+"version\" was not found.");
			}
			File[] fl = new File(vm_conf).listFiles(this);
			if (fl.length == 0)
				throw new FileNotFoundException("There was no configure file.");
			ArrayList<String> arlist = new ArrayList<String>();
			for (File fn:fl) {
				if (!fn.getName().equals(jAllConf))
					arlist.add(fn.getName().substring(10,fn.getName().length()-5));
			}
			arch = arlist.toArray(new String[arlist.size()]);
			for (int i=0;i<fl.length;i++) {
			final JsonReader jr=new JsonReader(new FileReader(fl[i]));
			jr.beginObject();
			while(jr.hasNext()){
				String nam=jr.nextName();
				if(nam.matches(String.format("(%1$s|%2$s|%3$s|%4$s|%5$s)",jMac,jCpuMode,jSoundhw,jNetworkCard,jVGA))){
				final ArrayList<String> ali=new ArrayList<String>();
				jr.beginArray();
				while(jr.hasNext())
					ali.add(jr.nextString());
				jr.endArray();
				switch(nam){
					case jMac:
						mac_tye.put(arch[i],ali.toArray(new String[ali.size()]));
						break;
					case jCpuMode:
						cpu_mode.put(arch[i],ali.toArray(new String[ali.size()]));
						break;
					case jSoundhw:
						soundhw.put(arch[i],ali.toArray(new String[ali.size()]));
						break;
					case jNetworkCard:
						network_card=ali.toArray(new String[ali.size()]);
						break;
					case jVGA:
						vga=ali.toArray(new String[ali.size()]);
						break;
				}
			}else jr.skipValue();}
			jr.endObject();
			jr.close();}
		}catch(FileNotFoundException fe){
			fe.printStackTrace();
			run();
		}catch(Exception e){
			ac.tw(e.getMessage(),1,mCont.getResources().getColor(R.color.red));
		}
	}
	
	private String[] initArch(){
		try{
			String al = shell("ls|grep qemu-system-|cut -c 13-");
			return al.split("\n");
		}catch(Exception e){
			e.printStackTrace();
			return null;
		}
	}

	private String[] initMac(String archtype) throws Exception{
		return shell(String.format("$PREF ./qemu-system-%s -machine help|./busybox awk 'NR>1{print $1}'", archtype)).split("\n");
	}

	public String getVersion(){
		return version;
	}
	
	public String[] getArchitecture(){
		return arch;
	}

	public String[] getMachine(String archtype){
		return mac_tye.get(archtype);
	}

	private String[] initCPU(String archtype)throws Exception{
		boolean isa = archtype.indexOf("ar")>-1;
		return shell(String.format("$PREF ./qemu-system-%s %s-cpu help|./busybox awk '{if(%s}}'",archtype,isa?"-M virt ":"",isa?"NR>1){print $1":"$1==\"x86\"){print $2")).split("\n");
	}
	
	public String[] getCPUMode(String archtype){
		return cpu_mode.get(archtype);
	}

	private String[] initSoundhw(String archtype)throws Exception{
		return shell(String.format("$PREF ./qemu-system-%s -soundhw help|awk 'length($1)==0{exit}NR>1{print $1}';echo all", archtype)).split("\n");
	}
	
	public String[] getSoundhw(String archtype){
		return soundhw.get(archtype);
	}

	private String[] initNetworkCard()throws Exception{
		String netc = String.format("$PREF ./qemu-system-%s%s -net user -net nic,model=help|./busybox tail -n +2",arch[0],arch[0].startsWith("a")?" -M virt":"");
		netc = shell(netc);
		return netc.split("\n");
	}

	public String[] getNetworkCard(){
		return network_card;
	}

	public String[] getVGA(){
		return vga;
	}

	private String[] initVGA() throws Exception{
		String s = String.format("$PREF ./qemu-system-%s%s -vga help|awk '{print $1}'",arch[0],arch[0].startsWith("a")?" -M virt":"");
		s = shell(s);
		return s.split("\n");
	}

	public void run(){
		try{
			Process prc = exec("$PREF ./qemu-system-i386 -version");
			prc.waitFor();
			InputStream is = prc.getInputStream();
			byte[] b = new byte[is.available()];
			is.read(b);
			String ver = new String(b);
			String cf = MainActivity.SDHOME+"/Qemu";
			Matcher ma = Pattern.compile("\\d+(\\.\\d+)+").matcher(ver);
			if (ma.find()) {
				version = ma.group();
			}
			android.util.Log.i("VMR", ver+" "+ACode.fw(cf+"/version", version));
			arch = initArch();
			for (String st:arch) {
				FileOutputStream pw=new FileOutputStream(String.format("%s/vm_config_%s.json",cf,st));
				JsonWriter jo=new JsonWriter(new OutputStreamWriter(pw,"UTF-8"));
				jo.beginObject();
				jo.name(jMac);
				jo.beginArray();
				String[] ml=initMac(st);
				mac_tye.put(st,ml);
				for(String m:ml)
					jo.value(m);
				jo.endArray();
				jo.name(jCpuMode);
				jo.beginArray();
				String[] cpl=initCPU(st);
				cpu_mode.put(st,cpl);
				for(String cp:cpl)
					jo.value(cp);
				jo.endArray();
				jo.name(jSoundhw);
				jo.beginArray();
				String[] soul=initSoundhw(st);
				soundhw.put(st,soul);
				for(String so:soul)
					jo.value(so);
				jo.endArray();
				jo.endObject();
				jo.close();
			}
			FileOutputStream ous=new FileOutputStream(String.format("%s/%s",cf,jAllConf));
			JsonWriter jw=new JsonWriter(new OutputStreamWriter(ous,"UTF-8"));
			jw.beginObject();
			jw.name(jNetworkCard);
			jw.beginArray();
			network_card=initNetworkCard();
			for(String netc:network_card)
				jw.value(netc);
			jw.endArray();
			jw.name(jVGA);
			jw.beginArray();
			vga = initVGA();
			for (String v:vga)
				jw.value(v);
			jw.endArray();
			jw.endObject();
			jw.close();
		}catch(Exception e){
			e.printStackTrace();
		}
	}
}
