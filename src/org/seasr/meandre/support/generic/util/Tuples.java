/*
 * University of Illinois/NCSA
 * Open Source License
 *
 * Copyright (c) 2008, NCSA.  All rights reserved.
 *
 * Developed by:
 * The Automated Learning Group
 * University of Illinois at Urbana-Champaign
 * http://www.seasr.org
 *
 * Permission is hereby granted, free of charge, to any person obtaining
 * a copy of this software and associated documentation files (the
 * "Software"), to deal with the Software without restriction, including
 * without limitation the rights to use, copy, modify, merge, publish,
 * distribute, sublicense, and/or sell copies of the Software, and to
 * permit persons to whom the Software is furnished to do so, subject
 * to the following conditions:
 *
 * Redistributions of source code must retain the above copyright
 * notice, this list of conditions and the following disclaimers.
 *
 * Redistributions in binary form must reproduce the above copyright
 * notice, this list of conditions and the following disclaimers in
 * the documentation and/or other materials provided with the distribution.
 *
 * Neither the names of The Automated Learning Group, University of
 * Illinois at Urbana-Champaign, nor the names of its contributors may
 * be used to endorse or promote products derived from this Software
 * without specific prior written permission.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
 * IN NO EVENT SHALL THE CONTRIBUTORS OR COPYRIGHT HOLDERS BE LIABLE
 * FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF
 * CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION
 * WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS WITH THE SOFTWARE.
 */

package org.seasr.meandre.support.generic.util;


/**
 * @author Boris Capitanu
 */

public abstract class Tuples {

    public static class Tuple2<T1, T2> {
        private final T1 _t1;
        private final T2 _t2;

        public Tuple2(T1 t1, T2 t2) {
            _t1 = t1;
            _t2 = t2;
        }

        public T1 getT1() {
            return _t1;
        }

        public T2 getT2() {
            return _t2;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == null || !(obj instanceof Tuple2))
                return false;

            Tuple2<?,?> other = (Tuple2<?,?>)obj;
            return _t1.equals(other._t1) && _t2.equals(other._t2);
        }

        @Override
        public int hashCode() {
            return _t1.hashCode() + 92821 * _t2.hashCode();
        }
    }

    public static class Tuple3<T1, T2, T3> {
        private final T1 _t1;
        private final T2 _t2;
        private final T3 _t3;

        public Tuple3(T1 t1, T2 t2, T3 t3) {
            _t1 = t1;
            _t2 = t2;
            _t3 = t3;
        }

        public T1 getT1() {
            return _t1;
        }

        public T2 getT2() {
            return _t2;
        }

        public T3 getT3() {
            return _t3;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == null || !(obj instanceof Tuple3))
                return false;

            Tuple3<?,?,?> other = (Tuple3<?,?,?>)obj;
            return _t1.equals(other._t1) && _t2.equals(other._t2) && _t3.equals(other._t3);
        }

        @Override
        public int hashCode() {
            return _t1.hashCode() + 92821 * _t2.hashCode() + 92821 * _t3.hashCode();
        }
    }

    public static class Tuple4<T1, T2, T3, T4> {
        private final T1 _t1;
        private final T2 _t2;
        private final T3 _t3;
        private final T4 _t4;

        public Tuple4(T1 t1, T2 t2, T3 t3, T4 t4) {
            _t1 = t1;
            _t2 = t2;
            _t3 = t3;
            _t4 = t4;
        }

        public T1 getT1() {
            return _t1;
        }

        public T2 getT2() {
            return _t2;
        }

        public T3 getT3() {
            return _t3;
        }

        public T4 getT4() {
            return _t4;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == null || !(obj instanceof Tuple4))
                return false;

            Tuple4<?,?,?,?> other = (Tuple4<?,?,?,?>)obj;
            return _t1.equals(other._t1) && _t2.equals(other._t2) && _t3.equals(other._t3) && _t4.equals(other._t4);
        }

        @Override
        public int hashCode() {
            return _t1.hashCode() + 92821 * _t2.hashCode() + 92821 * _t3.hashCode() + 92821 * _t4.hashCode();
        }
    }
}
