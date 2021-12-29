package com.firebirdberlin.nightdream.ui;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.graphics.PorterDuff;
import android.graphics.Typeface;
import android.net.Uri;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.RadioButton;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.firebirdberlin.nightdream.R;
import com.firebirdberlin.nightdream.models.FileUri;
import com.firebirdberlin.nightdream.models.FontCache;

import java.util.List;


class FontAdapter extends ArrayAdapter<FileUri> {
    private Context context = null;
    private int viewId = -1;
    private int selectedPosition = -1;
    private OnDeleteRequestListener listener;

    FontAdapter(Context context, int viewId, List<FileUri> values) {
        super(context, viewId, R.id.text1, values);
        this.context = context;
        this.viewId = viewId;
    }

    void setOnDeleteRequestListener(OnDeleteRequestListener listener) {
        this.listener = listener;
    }

    @SuppressLint("ClickableViewAccessibility")
    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        super.getView(position, convertView, parent);
        LayoutInflater inflater = ((Activity) context).getLayoutInflater();
        View v = inflater.inflate(viewId, parent, false);
        RadioButton button = v.findViewById(R.id.text1);
        final FileUri item = getItem(position);

        Typeface typeface = loadTypefaceForItem(item);
        button.setTypeface(typeface);
        button.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20);
        button.setText(item != null ? item.name : "");
        button.setChecked(position == selectedPosition);
        button.setTag(position);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                selectedPosition = (Integer) view.getTag();
                notifyDataSetChanged();
            }
        });
        ImageView buttonDelete = v.findViewById(R.id.buttonDelete);

        buttonDelete.setVisibility(
                item != null && "file".equals(item.uri.getScheme()) &&
                        !item.uri.toString().contains("android_asset")
                        ? View.VISIBLE : View.GONE);
        buttonDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.i("DIALOG", "delete clicked");
                if (item == null) {
                    return;
                }
                if (listener != null) {
                    listener.onDeleteRequested(item);
                }

                FileUri selected = getSelectedUri();
                remove(item);
                notifyDataSetChanged();
                setSelectedUri(selected.uri);

            }

        });

        // click effect
        buttonDelete.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN: {
                        ImageView view = (ImageView) v;
                        //overlay is black with transparency of 0x77 (119)
                        view.getDrawable().setColorFilter(0x77000000, PorterDuff.Mode.SRC_ATOP);
                        view.invalidate();
                        break;
                    }
                    case MotionEvent.ACTION_UP:
                    case MotionEvent.ACTION_CANCEL: {
                        ImageView view = (ImageView) v;
                        //clear the overlay
                        view.getDrawable().clearColorFilter();
                        view.invalidate();
                        break;
                    }
                }

                return false;
            }
        });

        return v;
    }


    private Typeface loadTypefaceForItem(FileUri item) {
        return FontCache.get(context, item.uri.toString());
    }

    public void release() {

    }

    public FileUri getSelectedUri() {
        if (selectedPosition < 0 || selectedPosition >= getCount()) {
            return null;
        }

        FileUri item = getItem(selectedPosition);
        return item;
    }

    public void setSelectedUri(Uri uri) {
        if (uri == null) return;
        for (int i = 0; i < getCount(); i++) {
            FileUri item = getItem(i);
            if (uri.equals(item.uri)) {
                selectedPosition = i;
                notifyDataSetChanged();
                return;
            }
        }
        if (getCount() > 0) {
            selectedPosition = 0;
            notifyDataSetChanged();
        }
    }

    interface OnDeleteRequestListener {
        void onDeleteRequested(FileUri file);
    }
}
