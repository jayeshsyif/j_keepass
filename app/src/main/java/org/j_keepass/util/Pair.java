//=======================================================================================
//  Persists as      $File: //depot/Dev/J1.0/com/ipnetsolutions/utils/Pair.java $
//  Last modified by $Author: rsaldanha $
//  Last modified at $Date: 2006/04/28 $
//  Revision         $Revision: #6 $
//
//  Copyright (c) IPNet Solutions, Inc 1996-2001. All Rights Reserved.
//
//  All information contained herein are confidential and the sole property
//  of IPNet Solutions, Inc.  Unauthorized redistribution and use in any form
//  is strictly prohibited.
//=======================================================================================
package org.j_keepass.util;

/**
 * A simple struct that holds two objects, analogous to the STL
 * pair template class.
 *
 * @author Frank Kieviet
 * @since  02-02-01
 *
 * @author Ryan Saldanha
 * @since  04-21-06
 */
public class Pair<F, S> implements java.io.Serializable {

    private static final long serialVersionUID = 6L;

    public F first  = null;
    public S second = null;

    //~--- constructors -------------------------------------------------------

    public Pair(F first, S second) {
        this.first  = first;
        this.second = second;
    }

    public Pair() {}

    public F getFirst() {
        return first;
    }

    public S getSecond() {
        return second;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("Pair{");
        sb.append("first=").append(first);
        sb.append(", second=").append(second);
        sb.append('}');
        return sb.toString();
    }
}
