//=======================================================================================
// Copyright (c) Inovis USA, Inc 1996-2007. All Rights Reserved.
//
// All information contained herein are confidential and the sole property
// of Inovis USA, Inc.  Unauthorized redistribution and use in any form
// is strictly prohibited.
//
// Persists as      $File: //depot/Dev/J1.0/com/ipnetsolutions/utils/Quadruple.java $
// Last modified by $Author: rsaldanha $
// Last modified at $Date: 2007/11/28 $
// Revision         $Revision: #1 $
//=======================================================================================
package org.j_keepass.util;

/**
 * A simple struct that holds four objects
 *
 * @author Ryan Saldanha <ryan.saldanha@inovis.com>
 * @since  11-28-2007
 */
public class Quadruple<F, S, T, U> implements java.io.Serializable {

    private static final long serialVersionUID = 1L;

    public F first  = null;
    public S second = null;
    public T third  = null;
    public U fourth = null;

    public Quadruple(F first, S second, T third, U fourth) {
        this.first  = first;
        this.second = second;
        this.third  = third;
        this.fourth = fourth;
    }

    public Quadruple() {}

    public F getFirst() {
        return first;
    }

    public S getSecond() {
        return second;
    }

    public T getThird() {
        return third;
    }

    public U getFourth() {
        return fourth;
    }
}
