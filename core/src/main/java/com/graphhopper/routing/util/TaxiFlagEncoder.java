/*
 *  Licensed to GraphHopper GmbH under one or more contributor
 *  license agreements. See the NOTICE file distributed with this work for
 *  additional information regarding copyright ownership.
 *
 *  GraphHopper GmbH licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except in
 *  compliance with the License. You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package com.graphhopper.routing.util;

import com.graphhopper.reader.ReaderWay;
import com.graphhopper.routing.ev.EncodedValue;
import com.graphhopper.routing.ev.UnsignedDecimalEncodedValue;
import com.graphhopper.storage.IntsRef;
import com.graphhopper.util.Helper;
import com.graphhopper.util.PMap;

import java.util.*;

/**
 * Defines bit layout for Taxi. (speed, access, ferries, ...)
 *
 * @author Luca Gobbato
 */
public class TaxiFlagEncoder extends CarFlagEncoder {
    public TaxiFlagEncoder() {
        this(new PMap());
    }

    public TaxiFlagEncoder(int speedBits, double speedFactor, int maxTurnCosts) {
        this(new PMap().putObject("speed_bits", speedBits).putObject("speed_factor", speedFactor).
                putObject("max_turn_costs", maxTurnCosts));
    }

    public TaxiFlagEncoder(PMap properties) {
        super(properties.getInt("speed_bits", 5),
                properties.getDouble("speed_factor", 5),
                properties.getInt("max_turn_costs", properties.getBool("turn_costs", false) ? 1 : 0));
    }

    @Override
    public TransportationMode getTransportationMode() {
        return TransportationMode.TAXI;
    }

    @Override
    protected boolean isOneway(ReaderWay way) {
        if (way.hasTag("oneway:psv", "no") || way.hasTag("oneway:bus", "no")) { 
            return false;
        }
        if (way.hasTag("busway", "opposite_lane") || way.hasTag("psv", "opposite_lane") || way.hasTag("bus", "opposite_lane")) { 
            return false;
        }
        return super.isOneway(way);
    }

    @Override
    public String toString() {
        return "taxi";
    }
}
