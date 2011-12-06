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

/**
 * @author Boris Capitanu
 */

public class AddressDetails {
    private final String _house;
    private final String _street;
    private final String _xstreet;
    private final String _unitType;
    private final String _unit;
    private final String _postal;
    private final String _neighborhood;
    private final String _city;
    private final String _county;
    private final String _countyCode;
    private final String _state;
    private final String _stateCode;
    private final String _country;
    private final String _countryCode;
    private final String _zip;

    public AddressDetails(String house, String street, String xstreet, String unitType, String unit, String postal,
            String neighborhood, String city, String county, String countyCode, String state, String stateCode, String country,
            String countryCode, String zip) {
        _house = house;
        _street = street;
        _xstreet = xstreet;
        _unitType = unitType;
        _unit = unit;
        _postal = postal;
        _neighborhood = neighborhood;
        _city = city;
        _county = county;
        _countyCode = countyCode;
        _state = state;
        _stateCode = stateCode;
        _country = country;
        _countryCode = countryCode;
        _zip = zip;
    }

    public String getHouse() {
        return _house;
    }

    public String getStreet() {
        return _street;
    }

    public String getXStreet() {
        return _xstreet;
    }

    public String getUnitType() {
        return _unitType;
    }

    public String getUnit() {
        return _unit;
    }

    public String getPostal() {
        return _postal;
    }

    public String getNeighborhood() {
        return _neighborhood;
    }

    public String getCity() {
        return _city;
    }

    public String getCounty() {
        return _county;
    }

    public String getCountyCode() {
        return _countyCode;
    }

    public String getState() {
        return _state;
    }

    public String getStateCode() {
        return _stateCode;
    }

    public String getCountry() {
        return _country;
    }

    public String getCountryCode() {
        return _countryCode;
    }

    public String getZip() {
        return _zip;
    }
}