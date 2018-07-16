
package third.aliyun.work;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.widget.ImageView;

import com.bumptech.glide.Glide;

import java.io.File;

import third.aliyun.media.MediaInfo;
import third.aliyun.media.ThumbnailGenerator;

public class MediaImageLoader {
    private ThumbnailGenerator mThumbnailGenerator;
    StringBuilder mFilePath = new StringBuilder();
    public MediaImageLoader(Context context) {
        mThumbnailGenerator = new ThumbnailGenerator(context);
    }

    public void displayImage(final MediaInfo info, final ImageView view) {
        if(info.thumbnailPath != null
                && onCheckFileExistence(info.thumbnailPath)) {
            mFilePath.delete(0, mFilePath.length());
            mFilePath.append("file://").append(info.thumbnailPath);
            Glide.with(view.getContext()).load(mFilePath.toString()).into(view);
        }else {
            view.setImageDrawable(new ColorDrawable(Color.GRAY));
            mThumbnailGenerator.generateThumbnail(info.type, info.id,0,
                    new ThumbnailGenerator.OnThumbnailGenerateListener() {
                        @Override
                        public void onThumbnailGenerate(int key, Bitmap thumbnail) {
                            int currentKey = ThumbnailGenerator.generateKey(info.type, info.id);
                            if(key == currentKey){
                                view.setImageBitmap(thumbnail);
                            }
                        }
                    });
        }
    }

    private boolean onCheckFileExistence(String path) {
        Boolean res = false;
        if(path == null) {
            return res;
        }

        File file = new File(path);
        if(file.exists()) {
            res = true;
        }

        return res;
    }
}
