package com.google.android.stardroid.util;

import android.content.res.AssetManager;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class AssetsUtil {
  
  public static String loadAssetTextAsString(AssetManager assetManager, String name) {
    BufferedReader in = null;
    try {
      StringBuilder buf = new StringBuilder();
      InputStream is = assetManager.open(name);
      in = new BufferedReader(new InputStreamReader(is));
      
      String str;
      boolean isFirst = true;
      while ((str = in.readLine()) != null) {
        if (isFirst)
          isFirst = false;
        else
          buf.append('\n');
        buf.append(str);
      }
      return buf.toString();
    } catch (IOException e) {
      Log.e("Res", "Error opening asset " + name);
    } finally {
      if (in != null) {
        try {
          in.close();
        } catch (IOException e) {
          Log.e("Res", "Error closing asset " + name);
        }
      }
    }
    return null;
  }
  
}
