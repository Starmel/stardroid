package com.google.android.stardroid.layers;

import android.content.res.Resources;
import android.graphics.Color;
import android.util.Log;

import com.google.android.stardroid.R;
import com.google.android.stardroid.base.TimeConstants;
import com.google.android.stardroid.control.AstronomerModel;
import com.google.android.stardroid.renderer.RendererObjectManager;
import com.google.android.stardroid.source.AbstractAstronomicalSource;
import com.google.android.stardroid.source.AstronomicalSource;
import com.google.android.stardroid.source.ImageSource;
import com.google.android.stardroid.source.LineSource;
import com.google.android.stardroid.source.Sources;
import com.google.android.stardroid.source.TextSource;
import com.google.android.stardroid.source.impl.ImageSourceImpl;
import com.google.android.stardroid.source.impl.LineSourceImpl;
import com.google.android.stardroid.source.impl.TextSourceImpl;
import com.google.android.stardroid.units.GeocentricCoordinates;
import com.google.android.stardroid.units.LatLong;
import com.google.android.stardroid.units.Vector3;
import com.google.android.stardroid.util.SatCalculator;
import com.google.common.io.Closeables;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.EnumSet;
import java.util.List;
import java.util.concurrent.CountDownLatch;

public class SatelliteLayer extends AbstractSourceLayer {
  private final AstronomerModel model;
  private List<Satellite> sats = new ArrayList<>();
  
  public SatelliteLayer(AstronomerModel model, Resources resources) {
    super(resources, true);
    this.model = model;
    initializeSatillites();
  }
  
  private void initializeSatillites() {
    final CountDownLatch latch = new CountDownLatch(1);
    new Thread(new Runnable() {
      @Override
      public void run() {
        ArrayList<String> tleList =
            getOrbitalElements("http://www.celestrak.com/NORAD/elements/visual.txt");
//        tleList.addAll(getOrbitalElements("http://www.celestrak.com/NORAD/elements/gps-ops.txt"));
//        tleList.addAll(getOrbitalElements("http://www.celestrak.com/NORAD/elements/iridium.txt"));

//        tleList.addAll(getOrbitalElements("http://www.celestrak.com/NORAD/elements/geo.txt"));
//        tleList.addAll(getOrbitalElements("http://www.celestrak.com/NORAD/elements/ses.txt"));
//        tleList.addAll(getOrbitalElements("http://www.celestrak.com/NORAD/elements/intelsat
// .txt"));
//        tleList.addAll(getOrbitalElements("http://www.celestrak.com/NORAD/elements/science.txt"));
//        tleList.addAll(getOrbitalElements("http://www.celestrak.com/NORAD/elements/noaa.txt"));
//        tleList.addAll(getOrbitalElements("http://www.celestrak.com/NORAD/elements/goes.txt"));
//        tleList.addAll(getOrbitalElements("http://www.celestrak.com/NORAD/elements/resource
// .txt"));
//        tleList.addAll(getOrbitalElements("http://www.celestrak.com/NORAD/elements/dmc.txt"));
//        tleList.addAll(getOrbitalElements("http://www.celestrak.com/NORAD/elements/tdrss.txt"));
//        tleList.addAll(getOrbitalElements("http://www.celestrak.com/NORAD/elements/weather.txt"));
//        tleList.addAll(getOrbitalElements("http://www.celestrak.com/NORAD/elements/orbcomm.txt"));
//        tleList.addAll(getOrbitalElements("http://www.celestrak.com/NORAD/elements/iridium-NEXT
// .txt"));
        
        if (tleList != null && !tleList.isEmpty()) {
          for (String tle : tleList) {
            SatCalculator.Position position = SatCalculator.INSTANCE
                .calculatePosition(SatCalculator.INSTANCE.getTLEInformation(tle),
                    new Date(System.currentTimeMillis()));
            
            
            sats.add(new Satellite(tle));
          }
        }
        Log.d(getClass().getCanonicalName(), "run: total satellites count = " + sats.size());
        latch.countDown();
      }
    }).start();
    
    try {
      latch.await();
    } catch (InterruptedException e) {
      e.printStackTrace();
    }
  }
  
  private ArrayList<String> getOrbitalElements(String urlString) {
    BufferedReader in = null;
    try {
      URLConnection connection = new URL(urlString).openConnection();
      in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
      return parseOrbitalElements(in);
    } catch (IOException e) {
      e.printStackTrace();
      Log.e("oOps", "Error reading Orbital Elements");
    } finally {
      Closeables.closeQuietly(in);
    }
    Log.d(getClass().getCanonicalName(), "Fetching ISS data error");
    return null;
  }
  
  ArrayList<String> parseOrbitalElements(BufferedReader in) throws IOException {
    String response = "";
    for (String line; (line = in.readLine()) != null; response += line + "\n") ;
    Log.d(getClass().getCanonicalName(), "parseOrbitalElements: response = " + response);
    String[] rows = response.split("\\n");
    int linesCount = rows.length;
    ArrayList<String> tleList = new ArrayList<>();
    try {
      for (int i = 0; i < linesCount; i++) {
        String tle = rows[i * 3] + "\n" + rows[i * 3 + 1] + "\n" + rows[i * 3 + 2];
        tleList.add(tle);
        Log.d(getClass().getCanonicalName(), "parseOrbitalElements: tle = " + tle);
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
    return tleList;
  }
  
  @Override
  protected void initializeAstroSources(ArrayList<AstronomicalSource> sources) {
    for (Satellite shower : sats) {
      sources.add(new SatelliteSource(model, shower, getResources()));
    }
  }
  
  @Override
  public int getLayerDepthOrder() {
    return 100;
  }
  
  @Override
  protected int getLayerNameId() {
    return R.string.sensor_absent;
  }
  
  @Override
  protected String getPreferenceId(int layerNameId) {
    return "source_provider.7";
  }
  
  private static class Satellite {
    private GeocentricCoordinates radiant;
    private String name;
    private SatCalculator.TLEParams tleParams;
    
    Satellite(String tle) {
      this.name = tle.split("\\n")[0];
      this.tleParams = SatCalculator.INSTANCE.getTLEInformation(tle);
      SatCalculator.Position position = SatCalculator.INSTANCE.calculatePosition(tleParams, null);
      this.radiant = GeocentricCoordinates.getInstance(position.getRa(), position.getDec());
    }
  }
  
  private static class SatelliteSource extends AbstractAstronomicalSource {
    private static final int LABEL_COLOR = Color.GREEN;
    private static final long UPDATE_FREQ_MS = 1L * TimeConstants.MILLISECONDS_PER_SECOND;
    private static final Vector3 UP = new Vector3(0.0f, 1.0f, 0.0f);
    
    private final List<ImageSource> imageSources = new ArrayList<>();
    private final List<TextSource> labelSources = new ArrayList<>();
    
    private final AstronomerModel model;
    
    private long lastUpdateTimeMs = 0L;
    private ImageSourceImpl theImage;
    private TextSource label;
    private Satellite satellite;
    private String name;
    private List<String> searchNames = new ArrayList<>();
    private GeocentricCoordinates coords;
    
    
    public SatelliteSource(AstronomerModel model, Satellite satellite, Resources resources) {
      this.model = model;
      this.satellite = satellite;
      this.name = satellite.name.toLowerCase();
      searchNames.add(name);
      coords = satellite.radiant;
      label = new TextSourceImpl(coords, name, LABEL_COLOR);
      labelSources.add(label);
      theImage = new ImageSourceImpl(coords, resources, R.drawable.blank, UP, 0.03f);
      
      imageSources.add(theImage);
      theImage.setUpVector(UP);
      
      theImage.setImageId(R.drawable.star_off);
    }
    
    private void updateCoords(Date time) {
      lastUpdateTimeMs = time.getTime();
      SatCalculator.Position position =
          SatCalculator.INSTANCE.calculatePosition(satellite.tleParams, time);
      
      GeocentricCoordinates calculatePosition =
          GeocentricCoordinates.getInstance(position.getRa(), position.getDec());
      coords.assign(calculatePosition.x, calculatePosition.y, calculatePosition.z);
      
      LatLong location = model.getLocation();
      double distance = SatCalculator.INSTANCE
          .distance(location.getLatitude(), location.getLongitude(), position.getLat(),
              position.getLng(), position.getAlt());
      
      double mag = (Math.round((-1.3 + 5 * Math.log(distance / 1000.0)) * 10.0) / 10.0);

//      if (mag < 3) {
//        Log.d(getClass().getCanonicalName(),
//            "updateCoords: mag = " + mag + " name =" + name + " distance = " + distance);
//      Log.d(getClass().getCanonicalName(), "updateCoords: v: " + position.getVelocity() + " s:"
// + satellite.name + "@");
      label.setText("v: " +System.currentTimeMillis());


//        theImage.setImageId(R.drawable.star_off);
//      } else {
//        label.setText(".");
//        theImage.setImageId(R.drawable.blank);
//      }
    }
    
    @Override
    public Sources initialize() {
      updateCoords(model.getTime());
      return this;
    }
    
    @Override
    public synchronized EnumSet<RendererObjectManager.UpdateType> update() {
      EnumSet<RendererObjectManager.UpdateType> updateTypes =
          EnumSet.noneOf(RendererObjectManager.UpdateType.class);
      
      Date modelTime = model.getTime();
      if (Math.abs(modelTime.getTime() - lastUpdateTimeMs) > UPDATE_FREQ_MS) {
        
        updateCoords(modelTime);
        updateTypes.add(RendererObjectManager.UpdateType.UpdatePositions);
        
      }
      return updateTypes;
    }
    
    
    @Override
    public List<String> getNames() {
      return searchNames;
    }
    
    @Override
    public GeocentricCoordinates getSearchLocation() {
      return satellite.radiant;
    }
    
    @Override
    public List<? extends ImageSource> getImages() {
      return imageSources;
    }
    
    @Override
    public List<? extends TextSource> getLabels() {
      return labelSources;
    }
    
    @Override
    public List<? extends LineSource> getLines() {
      if (name.contains("iss (zarya)")) {
        ArrayList<GeocentricCoordinates> list = new ArrayList<>();
        int count = 24;
        float part = 86400000 / count;
        for (int i = 0; i < count; i++) {
          SatCalculator.Position position =
              SatCalculator.INSTANCE.calculatePosition(satellite.tleParams,
                  new Date((long) (System.currentTimeMillis() + part * i)));
          GeocentricCoordinates coordinates =
              GeocentricCoordinates.getInstance(position.getRa(), position.getDec());
          list.add(coordinates);
        }
        return Collections.singletonList(new LineSourceImpl(Color.RED, list, 1f));
      } else {
        return new ArrayList<>();
      }
    }
  }
}
