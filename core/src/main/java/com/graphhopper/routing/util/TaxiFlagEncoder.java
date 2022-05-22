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
import com.graphhopper.routing.ev.RoadClass;
import com.graphhopper.util.PMap;

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

        defaultSpeedMap.put("motorway", 120);
        defaultSpeedMap.put("motorway_link", 70);
        defaultSpeedMap.put("motorroad", 90);
        defaultSpeedMap.put("trunk", 70);
        defaultSpeedMap.put("trunk_link", 65);
        defaultSpeedMap.put("primary", 40);
        defaultSpeedMap.put("primary_link", 30);
        defaultSpeedMap.put("secondary", 30);
        defaultSpeedMap.put("secondary_link", 25);
        defaultSpeedMap.put("tertiary", 25);
        defaultSpeedMap.put("tertiary_link", 20);
        defaultSpeedMap.put("unclassified", 20);
        defaultSpeedMap.put("residential", 20);
        defaultSpeedMap.put("living_street", 5);
        defaultSpeedMap.put("service", 20);
        defaultSpeedMap.put("road", 20);
        defaultSpeedMap.put("track", 15);

        // limit speed on bad surfaces to 10 km/h
        badSurfaceSpeed = 10;
        maxPossibleSpeed = 140;
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
        if (way.hasTag("busway", "opposite_lane")|| way.hasTag("psv", "opposite_lane") || way.hasTag("bus", "opposite_lane")) { 
            return false;
        }
        return super.isOneway(way);
    }

    private double getSpeedPenalty(ReaderWay way) {
        if (way.hasTag("tunnel", "yes")) { return 0.9; }
        return 1;
    }

    private double getSpeedReductionFactor(ReaderWay way) {
        RoadClass roadClass = RoadClass.find(way.getTag("highway", ""));
        switch (roadClass) {
            case MOTORWAY:
            case TRUNK:
                return 1;
            case PRIMARY:
            case SECONDARY:
            case TERTIARY:
                return 0.6;
            case UNCLASSIFIED:
            case RESIDENTIAL:
            case LIVING_STREET:
            case ROAD:
                return 0.5;
            case OTHER:
            case SERVICE:
            case TRACK:
            case PATH:
                return 0.2;
            default:
                return 0.5;
        }
    }

    @Override
    protected double applyMaxSpeed(ReaderWay way, double speed) {
        double penalty = this.getSpeedPenalty(way);
        double maxSpeed = getMaxSpeed(way);
        // We obey speed limits
        if (isValidSpeed(maxSpeed)) {
            return maxSpeed * this.getSpeedReductionFactor(way) * penalty;
        }
        return speed * penalty;
    }

    @Override
    public String toString() {
        return "taxi";
    }
}
