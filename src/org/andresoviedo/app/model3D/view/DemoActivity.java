package org.andresoviedo.app.model3D.view;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.andresoviedo.app.model3D.model.Object3DData;
import org.andresoviedo.app.model3D.services.WavefrontLoader;
import org.andresoviedo.dddmodel.R;
import org.apache.commons.io.IOUtils;

import android.app.Activity;
import android.app.Dialog;
import android.app.ListActivity;
import android.content.Context;
import android.content.Intent;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager.LayoutParams;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class DemoActivity extends ListActivity {

	public static final String[] titles = new String[] { "Strawberry", "Banana", "Orange", "Mixed" };

	public static final String[] descriptions = new String[] { "It is an aggregate accessory fruit",
			"It is the largest herbaceous flowering plant", "Citrus Fruit", "Mixed Fruits" };

	public static final Integer[] images = { R.drawable.ic_launcher, R.drawable.ic_launcher, R.drawable.ic_launcher,
			R.drawable.ic_launcher };

	ListView listView;
	List<RowItem> rowItems;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_demo);

		AssetManager assets = getApplicationContext().getAssets();
		String[] models = null;
		try {
			models = assets.list("models");
		} catch (IOException ex) {
			Toast.makeText(getApplicationContext(), ex.getMessage(), Toast.LENGTH_LONG).show();
			return;
		}

		// add 1 entry per model found
		rowItems = new ArrayList<RowItem>();
		for (String model : models) {
			if (model.endsWith(".obj")) {
				RowItem item = new RowItem("models/" + model, model, "models/" + model + ".jpg");
				rowItems.add(item);
			}
		}
		CustomListViewAdapter adapter = new CustomListViewAdapter(this, R.layout.activity_demo, rowItems);
		setListAdapter(adapter);
	}

	@Override
	public void onListItemClick(ListView l, View v, int position, long id) {
		RowItem selectedItem = (RowItem) getListView().getItemAtPosition(position);
		String model = selectedItem.name;

		try {
			InputStream assetIs = getApplicationContext().getAssets().open(selectedItem.path);
			ByteArrayOutputStream bos = new ByteArrayOutputStream();
			IOUtils.copy(assetIs, bos);
			assetIs.close();

			ByteArrayInputStream bis = new ByteArrayInputStream(bos.toByteArray());
			WavefrontLoader wfl = new WavefrontLoader(model);
			wfl.loadModel(bis);
			bis.close();

			Object3DData data3D = new Object3DData(wfl.getVerts(), wfl.getNormals(), wfl.getTexCoords(), wfl.getFaces(),
					wfl.getFaceMats(), wfl.getMaterials());

			showLoadModelDialog(model, data3D);
		} catch (IOException ex) {
			Log.e("DemoActivity", "Problem loading data", ex);
			Toast.makeText(getApplicationContext(), "Problem loading data: " + ex.getMessage(), Toast.LENGTH_LONG)
					.show();
		}
	}

	private void showLoadModelDialog(String model, final Object3DData obj) {

		try {
			Toast.makeText(getApplicationContext(), model, Toast.LENGTH_SHORT).show();

			// custom dialog
			final Dialog dialog = new Dialog(DemoActivity.this);
			dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
			dialog.getWindow().setLayout(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
			dialog.setContentView(R.layout.dialog_load_model);

			TextView text = (TextView) dialog.findViewById(R.id.dialog_load_model_name);
			text.setText(model);
			TextView texture = (TextView) dialog.findViewById(R.id.dialog_load_model_texture);
			texture.setText("Not yet implemented");
			Button loadTextureButton = (Button) dialog.findViewById(R.id.browse_texture_button);
			// if button is clicked, close the custom dialog
			loadTextureButton.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					dialog.dismiss();
				}
			});

			Button loadButton = (Button) dialog.findViewById(R.id.dialog_load_model_load);
			// if button is clicked, close the custom dialog
			loadButton.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					dialog.dismiss();
					Intent intent = new Intent(DemoActivity.this.getApplicationContext(), ModelActivity.class);
					Bundle b = new Bundle();
					b.putSerializable("obj", obj);
					intent.putExtras(b);
					DemoActivity.this.startActivity(intent);
				}
			});

			dialog.show();

		} catch (Exception ex) {
			Toast.makeText(getApplicationContext(), ex.getMessage(), Toast.LENGTH_LONG).show();
		}

	}
}

class RowItem {
	/**
	 * Image of the 3D object (snapshot of what the model looks like)
	 */
	String image;
	/**
	 * Logical name of the 3D model that the user selected
	 */
	String name;
	/**
	 * Assets path from where to load the .obj file
	 */
	String path;

	public RowItem(String path, String name, String image) {
		this.path = path;
		this.name = name;
		this.image = image;
	}
}

class CustomListViewAdapter extends ArrayAdapter<RowItem> {

	Context context;

	public CustomListViewAdapter(Context context, int resourceId, List<RowItem> items) {
		super(context, resourceId, items);
		this.context = context;
	}

	/* private view holder class */
	private class ViewHolder {
		ImageView imageView;
		TextView txtTitle;
		TextView txtDesc;
	}

	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder holder = null;
		RowItem rowItem = getItem(position);

		LayoutInflater mInflater = (LayoutInflater) context.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
		if (convertView == null) {
			convertView = mInflater.inflate(R.layout.activity_demo_item, null);
			holder = new ViewHolder();
			// holder.txtDesc = (TextView) convertView.findViewById(R.id.desc);
			holder.txtTitle = (TextView) convertView.findViewById(R.id.demo_item_title);
			holder.imageView = (ImageView) convertView.findViewById(R.id.demo_item_icon);
			convertView.setTag(holder);
		} else
			holder = (ViewHolder) convertView.getTag();

		// holder.txtDesc.setText(rowItem.getDesc());
		holder.txtTitle.setText(rowItem.name);
		try {
			Bitmap bitmap = BitmapFactory.decodeStream(context.getAssets().open(rowItem.image));
			holder.imageView.setImageBitmap(bitmap);
		} catch (Exception e) {
			holder.imageView.setImageResource(R.drawable.ic_launcher);
		}

		return convertView;
	}
}