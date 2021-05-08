/*
 * top.donmor.tiddloid.BackupListAdapter <= [P|Tiddloid]
 * Last modified: 03:43:00 2019/05/07
 * Copyright (c) 2019 donmor
 */

package top.donmor.tiddloid;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

public class BackupListAdapter extends RecyclerView.Adapter<BackupListAdapter.BackupListHolder> {

	private File mf;
	private File[] bk;
	private LoadListener mLoadListener;
	private BtnClickListener mBtnClickListener;
	private final LayoutInflater inflater;


	private static final int ROLLBACK = 1, DELETE = 2;

	BackupListAdapter(Context context) {
		inflater = LayoutInflater.from(context);
	}

	static class BackupListHolder extends RecyclerView.ViewHolder {
		private final ImageButton btnRollBack, btnDelBackup;
		private final TextView lblBackupFile;

		BackupListHolder(View itemView) {
			super(itemView);
			btnRollBack = itemView.findViewById(R.id.btnRollBack);
			btnDelBackup = itemView.findViewById(R.id.btnDelBackup);
			lblBackupFile = itemView.findViewById(R.id.lblBackupFile);
		}
	}

	@Override
	@NonNull
	public BackupListHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
		return new BackupListHolder(inflater.inflate(R.layout.backup_slot, parent, false));
	}

	@Override
	public void onBindViewHolder(@NonNull final BackupListHolder holder, int position) {
		try {
			String efn = bk[position].getName();
			int efp1, efp2;
			efp1 = efn.lastIndexOf('.', (efp2 = efn.lastIndexOf('.')) - 1);
			holder.lblBackupFile.setText(SimpleDateFormat.getDateTimeInstance().format(parseUTCString(efn.substring(efp1 + 1, efp2)).getTime()));
			holder.btnRollBack.setOnClickListener(v -> mBtnClickListener.onBtnClick(holder.getAdapterPosition(), ROLLBACK));
			holder.btnDelBackup.setOnClickListener(v -> mBtnClickListener.onBtnClick(holder.getAdapterPosition(), DELETE));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public int getItemCount() {
		return bk != null ? bk.length : 0;
	}


	interface BtnClickListener {
		void onBtnClick(int pos, int which);
	}

	void setOnBtnClickListener(BtnClickListener btnClickListener) {
		this.mBtnClickListener = btnClickListener;

	}


	interface LoadListener {
		void onLoad(int count);
	}

	void setOnLoadListener(LoadListener loadListener) {
		this.mLoadListener = loadListener;

	}

	File getBackupFile(int position) {
		return position < getItemCount() ? bk[position] : null;
	}

	void reload(File mainFile) {
		this.mf = mainFile;
		try {
			String mfn = mf.getName();
			File mfd = new File(mf.getParentFile(), mfn + MainActivity.BACKUP_DIRECTORY_PATH_PREFIX);
			int x;
			if (!mfd.isDirectory()) throw new IOException();
			bk = sortFile(mfd.listFiles(pathname -> MainActivity.isBackupFile(mf, pathname)), (x = mfn.lastIndexOf('.')) < 0 ? mfn : mfn.substring(0, x));
			mLoadListener.onLoad(getItemCount());
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	// 排序
	private File[] sortFile(File[] src, String mfn) {
		if (src == null || src.length == 0) return src;
		int p = mfn.length() + 1;
		for (int i = 0; i < src.length; i++) {
			for (int j = 0; j < src.length - i - 1; j++) {
				if (Long.parseLong(src[j + 1].getName().substring(p, p + 17)) < Long.parseLong(src[j].getName().substring(p, p + 17))) {
					File temp = src[j + 1];
					src[j + 1] = src[j];
					src[j] = temp;
				}
			}
		}
		return src;
	}

	private Date parseUTCString(String v) throws ParseException {
		SimpleDateFormat format = new SimpleDateFormat(MainActivity.MASK_SDF_BACKUP, Locale.US);
		format.setTimeZone(TimeZone.getTimeZone(MainActivity.KEY_TZ_UTC));
		return format.parse(v);
	}
}
