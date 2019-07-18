package com.bytedance.androidcamp.network.dou;

import android.text.Html;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bytedance.androidcamp.network.dou.model.Like;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

public class likeViewHolder extends RecyclerView.ViewHolder {
    private static final SimpleDateFormat SIMPLE_DATE_FORMAT =
            new SimpleDateFormat("EEE, d MMM yyyy HH:mm:ss", Locale.ENGLISH);
    List<String> inf = Arrays.asList("You raise me up.","On your side.","You've discovered the secret."
    ,"I like your work.", "I can see progress.","Exceptional performance.","You never let me down.","Charming!");
    public TextView dataText, nameText,infText;
    public ImageView imageView;
    public likeViewHolder(@NonNull View itemView) {
        super(itemView);
        dataText=itemView.findViewById(R.id.text_date);
        nameText=itemView.findViewById(R.id.text_content);
        infText=itemView.findViewById(R.id.text_inf);
    }
    public void bind(final Like like){
        final long l = System.currentTimeMillis();
        String sequence = inf.get((int) (l % inf.size()));
        dataText.setText(SIMPLE_DATE_FORMAT.format(like.getDate()));
        nameText.setText(Html.fromHtml("<font color='#eeeeee'>点赞  </font>" + "<font color='#91DDFF'>" + like.getName() + "</font>"));
        infText.setText(sequence);
    }

}
