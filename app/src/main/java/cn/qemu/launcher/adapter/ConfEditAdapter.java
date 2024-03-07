package cn.qemu.launcher.adapter;
import android.widget.*;
import android.view.*;
import android.content.*;
import java.util.*;
import cn.qemu.launcher.R;
import android.support.annotation.*;

public class ConfEditAdapter extends BaseAdapter
{
	Context cot;
	ArrayList<ConfItem> list;

	public ConfEditAdapter(Context context, ArrayList<ConfItem> lists){
		cot=context;
		list=lists;
	}
	
	public void setLists(ArrayList<ConfItem> lists){
		list=lists;
		notifyDataSetChanged();
	}

	@Override
	public int getCount(){
		return list.size();
	}

	@Override
	public long getItemId(int p1){
		return p1;
	}

	@Override
	public ConfItem getItem(int p1){
		return list.get(p1);
	}

	@Override
	public View getView(int p1, View convertView, ViewGroup p3){
		TTHolder holder = null;
        if (convertView==null) {
            convertView = LayoutInflater.from(cot).inflate(R.layout.conf_edit,null);  //将布局转换成视图
            holder = new TTHolder();
            holder.t1 = convertView.findViewById(R.id.confeditTextView1);
            holder.t2 = convertView.findViewById(R.id.confeditTextView2);
            convertView.setTag(holder);
        } else
			holder = (TTHolder)convertView.getTag();
		ConfItem item = list.get(p1);
        holder.t1.setText(item.name);
        holder.t2.setText(item.value==null?item.sub:item.sub.replace(item.type,item.value));
        return convertView;
	}
}
