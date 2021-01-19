package cash.z.ecc.android.sdk.demoapp.adapter;


import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;


import java.lang.reflect.Array;
import java.util.List;

import cash.z.ecc.android.sdk.demoapp.R;

public class ClientAdapter extends ArrayAdapter<String> {

	private FragmentActivity context;
	private List<String> values;


	public ClientAdapter(FragmentActivity context, List<String> values) {
		super(context, R.layout.layout_client, values);
		this.context = context;
		this.values = values;
	}

	@Override
	public View getView(final int position, View convertView, ViewGroup parent) {
		final ViewHolder holder;
		if (convertView == null) {
			convertView = LayoutInflater.from(context).inflate(R.layout.layout_client, null);
			holder = new ViewHolder();
			holder.zAddress = convertView.findViewById(R.id.zAddress);
			holder.signedDate = convertView.findViewById(R.id.signedDate);
			holder.salt = convertView.findViewById(R.id.salt);
			convertView.setTag(holder);
		}else{
			holder = (ViewHolder) convertView.getTag();
		}
		String[] val= values.get(position).split(",");
		String add=val[0];
		String signedDate=val[1];
		String salt ="";
		if(val.length>=3){
			salt= val[2];
		}
		if(!add.isEmpty() && add.length()>30){
			add = add.substring(0, 30)+"..."+ add.substring(add.length()-8);
		}
		if(!signedDate.isEmpty()&& signedDate.length()>30){
			signedDate = signedDate.substring(0, 30)+"..."+ signedDate.substring(signedDate.length()-8,signedDate.length()-1);
		}
		holder.zAddress.setText(add);
		holder.signedDate.setText(signedDate);
		holder.salt.setText(salt);
		return convertView;
	}

	@Override
	public String getItem(int position){
		return values.get(position);

	}

	@Override
	public int getCount() {
		return values.size();
	}

	class ViewHolder {
		TextView zAddress;
		TextView signedDate;
		TextView salt;
	}
}
