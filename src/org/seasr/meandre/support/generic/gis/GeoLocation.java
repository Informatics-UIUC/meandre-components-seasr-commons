/**
 * University of Illinois/NCSA
 * Open Source License
 *
 * Copyright (c) 2008, Board of Trustees-University of Illinois.
 * All rights reserved.
 *
 * Developed by:
 *
 * Automated Learning Group
 * National Center for Supercomputing Applications
 * http://www.seasr.org
 *
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to
 * deal with the Software without restriction, including without limitation the
 * rights to use, copy, modify, merge, publish, distribute, sublicense, and/or
 * sell copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 *  * Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimers.
 *
 *  * Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimers in the
 *    documentation and/or other materials provided with the distribution.
 *
 *  * Neither the names of Automated Learning Group, The National Center for
 *    Supercomputing Applications, or University of Illinois, nor the names of
 *    its contributors may be used to endorse or promote products derived from
 *    this Software without specific prior written permission.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.  IN NO EVENT SHALL THE
 * CONTRIBUTORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
 * FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS
 * WITH THE SOFTWARE.
 */

package org.seasr.meandre.support.generic.gis;

import java.io.IOException;
import java.net.URLEncoder;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.seasr.meandre.support.generic.io.HttpUtils;


/**
 * Provides geocoding functionality based on the Yahoo geocoding service
 * http://developer.yahoo.com/geo/placefinder/guide/index.html
 *
 * SEASR API KEY: yFUeASDV34FRJWiaM8pxF0eJ7d2MizbUNVB2K6in0Ybwji5YB0D4ZODR2y3LqQ--
 *
 * @author Boris Capitanu
 */
public class GeoLocation {
    //
    // GeoLocation Service
    //
    private static final String API_URL = "http://where.yahooapis.com/geocode";
    private static String API_KEY;

    private String _queryPlaceName;
    private String _locale;
    private int _quality;
    private String _latitude;
    private String _longitude;
    private String _offsetLat;
    private String _offsetLon;
    private int _radius;
    private String _name;
    private String[] _lines;
    private AddressDetails _addressDetails;
    private String _hash;
    private int _woeid;
    private int _woeType;
    private boolean _isCreated;

    private GeoLocation() { }


    public String getQueryPlaceName() {
        return _queryPlaceName;
    }

    public String getLocale() {
        return _locale;
    }

    public int getQuality() {
        return _quality;
    }

    public String getLatitude() {
        return _latitude;
    }

    public String getLongitude() {
        return _longitude;
    }

    public String getOffsetLat() {
        return _offsetLat;
    }

    public String getOffsetLon() {
        return _offsetLon;
    }

    public int getRadius() {
        return _radius;
    }

    public String getName() {
        return _name;
    }

    public String getAddress() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0, iMax = _lines.length; i < iMax; i++)
            if (_lines[i].length() > 0)
                sb.append(_lines[i]).append("\n");

        return sb.toString();
    }

    public AddressDetails getAddressDetails() {
        return _addressDetails;
    }

    public String getHash() {
        return _hash;
    }

    public int getWOEId() {
        return _woeid;
    }

    public int getWOEType() {
        return _woeType;
    }

    /**
     * Was this location created manually through the 'createLocation' factory method (vs. obtained by invoking the geocoding service)?
     *
     * @return True for yes, False for no
     */
    public boolean isCreated() {
        return _isCreated;
    }

    public boolean isValid() {
        return _latitude != null && _latitude.length() != 0 && _longitude != null && _longitude.length() != 0;
    }

    private void setQueryPlaceName(String queryPlaceName) {
        _queryPlaceName = queryPlaceName;
    }

    private void setLocale(String locale) {
        _locale = locale;
    }

    private void setQuality(int quality) {
        _quality = quality;
    }

    private void setLocation(String latitude, String longitude) {
        _latitude = latitude;
        _longitude = longitude;
    }

    private void setOffsetLocation(String offsetLat, String offsetLon) {
        _offsetLat = offsetLat;
        _offsetLon = offsetLon;
    }

    private void setRadius(int radius) {
        _radius = radius;
    }

    private void setName(String name) {
        _name = name;
    }

    private void setAddress(String line1, String line2, String line3, String line4) {
        _lines = new String[4];
        _lines[0] = line1;
        _lines[1] = line2;
        _lines[2] = line3;
        _lines[3] = line4;
    }

    private void setAddressDetails(AddressDetails addressDetails) {
        _addressDetails = addressDetails;
    }

    private void setHash(String hash) {
        _hash = hash;
    }

    private void setWOE(int woeid, int woeType) {
        _woeid = woeid;
        _woeType = woeType;
    }


    private static ConcurrentMap<String, GeoLocation[]> _cache = new ConcurrentHashMap<String, GeoLocation[]>();
    private static ConcurrentMap<String, Object> _locks = new ConcurrentHashMap<String, Object>();

    public static void setAPIKey(String key) {
        API_KEY = key;
    }

    public static GeoLocation[] geocode(String placeName) throws GeocodingException, IOException {
        return geocode(placeName, true);
    }

    public static GeoLocation[] geocode(String placeName, boolean useCache) throws GeocodingException, IOException {
        if (!useCache)
            return geocodeInternal(placeName);

        String key = placeName.trim().toLowerCase();
        synchronized (getCacheSyncObject(key)) {
            if (_cache.containsKey(key))
                return _cache.get(key);

            GeoLocation[] locations = geocodeInternal(placeName);
            _cache.put(key, locations);

            return locations;
        }
    }

    public static GeoLocation createLocation(double latitude, double longitude) {
        GeoLocation location = new GeoLocation();
        location._isCreated = true;
        location.setLocation(Double.toString(latitude), Double.toString(longitude));

        return location;
    }

    private static GeoLocation[] geocodeInternal(String placeName) throws GeocodingException, IOException {
        if (API_KEY == null)
            throw new IllegalArgumentException("Yahoo API key not set!  Use " + GeoLocation.class.getSimpleName() +
                    ".setAPIKey(key) to set the API key before invoking any other methods.");

        String request = String.format("%s?q=%s&flags=GJ&appid=%s", API_URL, URLEncoder.encode(placeName, "UTF-8"), API_KEY);
        String response = HttpUtils.doGET(request, null);

        try {
            JSONObject joResponse = new JSONObject(response);
            JSONObject joResultSet = joResponse.getJSONObject("ResultSet");
            int errCode = joResultSet.getInt("Error");
            String errMsg = joResultSet.getString("ErrorMessage");
            if (errCode != 0) throw new GeocodingException(errMsg);

            GeoLocation[] locations = null;

            int found = joResultSet.getInt("Found");
            if (found > 0) {
                locations = new GeoLocation[found];
                String locale = joResultSet.getString("Locale");
                JSONArray jaResults = joResultSet.getJSONArray("Results");
                for (int i = 0, iMax = jaResults.length(); i < iMax; i++) {
                    JSONObject joLocation = jaResults.getJSONObject(i);
                    GeoLocation location = new GeoLocation();
                    location._isCreated = false;
                    location.setQueryPlaceName(placeName);
                    location.setLocale(locale);
                    if (joLocation.has("quality"))
                        location.setQuality(joLocation.getInt("quality"));
                    if (joLocation.has("latitude") && joLocation.has("longitude"))
                        location.setLocation(joLocation.getString("latitude"), joLocation.getString("longitude"));
                    if (joLocation.has("offsetlat") && joLocation.has("offsetlon"))
                        location.setOffsetLocation(joLocation.getString("offsetlat"), joLocation.getString("offsetlon"));
                    if (joLocation.has("radius"))
                        location.setRadius(joLocation.getInt("radius"));
                    if (joLocation.has("name"))
                        location.setName(joLocation.getString("name"));
                    if (joLocation.has("line1") && joLocation.has("line2") && joLocation.has("line3") && joLocation.has("line4"))
                        location.setAddress(joLocation.getString("line1"), joLocation.getString("line2"),
                                joLocation.getString("line3"), joLocation.getString("line4"));
                    AddressDetails addressDetails = new AddressDetails(
                            joLocation.has("house") ? joLocation.getString("house") : null,
                            joLocation.has("street") ? joLocation.getString("street") : null,
                            joLocation.has("xstreet") ? joLocation.getString("xstreet") : null,
                            joLocation.has("unittype") ? joLocation.getString("unittype") : null,
                            joLocation.has("unit") ? joLocation.getString("unit") : null,
                            joLocation.has("postal") ? joLocation.getString("postal") : null,
                            joLocation.has("level4") ? joLocation.getString("level4") : null,
                            joLocation.has("level3") ? joLocation.getString("level3") : null,
                            joLocation.has("level2") ? joLocation.getString("level2") : null,
                            joLocation.has("level2code") ? joLocation.getString("level2code") : null,
                            joLocation.has("level1") ? joLocation.getString("level1") : null,
                            joLocation.has("level1code") ? joLocation.getString("level1code") : null,
                            joLocation.has("level0") ? joLocation.getString("level0") : null,
                            joLocation.has("level0code") ? joLocation.getString("level0code") : null,
                            joLocation.has("uzip") ? joLocation.getString("uzip") : null);
                    location.setAddressDetails(addressDetails);
                    if (joLocation.has("hash"))
                        location.setHash(joLocation.getString("hash"));
                    if (joLocation.has("woeid") && joLocation.has("woetype"))
                        location.setWOE(joLocation.getInt("woeid"), joLocation.getInt("woetype"));
                    locations[i] = location;
                }
            }

            return locations;
        }
        catch (JSONException e) {
            throw new IOException("Error parsing geocoding response", e);
        }
    }

    private static Object getCacheSyncObject(final String value) {
        _locks.putIfAbsent(value, new Object());
        return _cache.get(value);
    }

    public static void disposeCache() {
        _cache.clear();
        _locks.clear();
    }
}
