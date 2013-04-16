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
 * http://developer.yahoo.com/boss/geo/
 *
 * @author Boris Capitanu
 */
public class GeoLocation {
    //
    // GeoLocation Service
    //
    private static final String API_URL = "http://query.yahooapis.com/v1/public/yql";
    private static final String YQL_FORMAT = "select * from geo.placefinder where text=\"%s\"";

    private String _queryPlaceName;
    private int _quality;
    private LatLngCoord _coord;
    private LatLngCoord _offsetCoord;
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

    public int getQuality() {
        return _quality;
    }

    public Double getLatitude() {
        return _coord.getLatitude();
    }

    public Double getLongitude() {
        return _coord.getLongitude();
    }

    public LatLngCoord getCoord() {
        return _coord;
    }

    public Double getOffsetLat() {
        return _offsetCoord.getLatitude();
    }

    public Double getOffsetLon() {
        return _offsetCoord.getLongitude();
    }

    public LatLngCoord getOffsetCoord() {
        return _offsetCoord;
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
        return _coord.isValid();
    }

    private void setQueryPlaceName(String queryPlaceName) {
        _queryPlaceName = queryPlaceName;
    }

    private void setQuality(int quality) {
        _quality = quality;
    }

    private void setLocation(String latitude, String longitude) {
        _coord = new LatLngCoord(latitude, longitude);
    }

    private void setOffsetLocation(String offsetLat, String offsetLon) {
        _offsetCoord = new LatLngCoord(offsetLat, offsetLon);
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
        String request = String.format("%s?q=%s&format=json", API_URL, URLEncoder.encode(getYQLForPlacename(placeName), "UTF-8"));
        String response = HttpUtils.doGET(request, null);

        GeoLocation[] locations;
        try {
            locations = parseResponse(response);
        }
        catch (JSONException e) {
            throw new IOException("Error parsing geocoding response", e);
        }

        for (GeoLocation location : locations)
            location.setQueryPlaceName(placeName);

        return locations;
    }

    private static String getYQLForPlacename(String placeName) {
        return String.format(YQL_FORMAT, placeName);
    }

    private static GeoLocation[] parseResponse(String response) throws GeocodingException, IOException, JSONException {
        JSONObject joResponse = new JSONObject(response);
        if (joResponse.has("error")) {
            JSONObject joError = joResponse.getJSONObject("error");
            String message = joError.getString("description");
            throw new GeocodingException(message);
        }

        JSONObject joQuery = joResponse.getJSONObject("query");
        int count = joQuery.getInt("count");
        GeoLocation[] locations = new GeoLocation[count];

        if (count > 0) {
            JSONObject joResults = joQuery.getJSONObject("results");
            JSONArray jaResults;
            if (count == 1) {
                JSONObject joResult = joResults.getJSONObject("Result");
                jaResults = new JSONArray();
                jaResults.put(joResult);
            } else
                jaResults = joResults.getJSONArray("Result");

            for (int i = 0, iMax = jaResults.length(); i < iMax; i++) {
                JSONObject joLocation = jaResults.getJSONObject(i);
                GeoLocation location = new GeoLocation();
                location._isCreated = false;
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
                        joLocation.has("neighborhood") ? joLocation.getString("neighborhood") : null,
                        joLocation.has("city") ? joLocation.getString("city") : null,
                        joLocation.has("county") ? joLocation.getString("county") : null,
                        joLocation.has("countycode") ? joLocation.getString("countycode") : null,
                        joLocation.has("state") ? joLocation.getString("state") : null,
                        joLocation.has("statecode") ? joLocation.getString("statecode") : null,
                        joLocation.has("country") ? joLocation.getString("country") : null,
                        joLocation.has("countrycode") ? joLocation.getString("countrycode") : null,
                        joLocation.has("uzip") ? joLocation.getString("uzip") : null);
                location.setAddressDetails(addressDetails);
                if (joLocation.has("hash"))
                    location.setHash(joLocation.getString("hash"));
                if (joLocation.has("woeid") && joLocation.has("woetype") &&
                        (joLocation.get("woeid") instanceof Integer) && (joLocation.get("woetype") instanceof Integer))
                    location.setWOE(joLocation.getInt("woeid"), joLocation.getInt("woetype"));
                locations[i] = location;
            }
        }

        return locations;
    }

    private static Object getCacheSyncObject(final String value) {
        _locks.putIfAbsent(value, new Object());
        return _locks.get(value);
    }

    public static void disposeCache() {
        _cache.clear();
        _locks.clear();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || !(obj instanceof GeoLocation)) return false;

        GeoLocation other = (GeoLocation) obj;
        return equalsNull(this.getCoord(), other.getCoord()) &&
               equalsNull(this.getOffsetCoord(), other.getOffsetCoord()) &&
               this.getQuality() == other.getQuality() &&
               equalsNull(this.getAddressDetails(), other.getAddressDetails()) &&
               equalsNull(this.getHash(), other.getHash()) &&
               equalsNull(this.getRadius(), other.getRadius()) &&
               equalsNull(this.getWOEId(), other.getWOEId()) &&
               equalsNull(this.getWOEType(), other.getWOEType());
    }

    public boolean equalsPosition(GeoLocation other) {
        if (other == this) return true;
        if (other == null) return false;

        return equalsNull(this.getCoord(), other.getCoord());
    }

    private boolean equalsNull(Object obj1, Object obj2) {
        if (obj1 == null || obj2 == null)
            return obj1 == obj2;

        return obj1.equals(obj2);
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + hashCodeNull(_coord);
        result = prime * result + hashCodeNull(_offsetCoord);
        result = prime * result + _quality;
        result = prime * result + hashCodeNull(getAddressDetails());
        result = prime * result + hashCodeNull(_hash);
        result = prime * result + hashCodeNull(_radius);
        result = prime * result + hashCodeNull(_woeid);
        result = prime * result + hashCodeNull(_woeType);

        return result;
    }

    private int hashCodeNull(Object obj) {
        return obj == null ? 0 : obj.hashCode();
    }
}
