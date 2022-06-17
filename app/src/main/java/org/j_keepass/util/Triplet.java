//=======================================================================================
// Copyright (c) Inovis USA, Inc 1996-2007. All Rights Reserved.
//
// All information contained herein are confidential and the sole property
// of Inovis USA, Inc.  Unauthorized redistribution and use in any form
// is strictly prohibited.
//
// Persists as      $File: //depot/Dev/J1.0/com/ipnetsolutions/utils/Triplet.java $
// Last modified by $Author: rsaldanha $
// Last modified at $Date: 2007/11/28 $
// Revision         $Revision: #2 $
//=======================================================================================
package org.j_keepass.util;

/**
 * A simple struct that holds three objects
 *
 * @author Ryan Saldanha <ryan.saldanha@inovis.com>
 * @since  04-21-06
 */
public class Triplet<F, S, T> implements java.io.Serializable {

    private static final long serialVersionUID = 1L;

    public F first  = null;
    public S second = null;
    public T third  = null;

    public Triplet(F first, S second, T third) {
        this.first  = first;
        this.second = second;
        this.third  = third;
    }

    public Triplet() {}

    public F getFirst() {
        return first;
    }

    public S getSecond() {
        return second;
    }

    public T getThird() {
        return third;
    }
}
