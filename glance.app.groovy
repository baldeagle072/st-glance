/**
 *  My Home At A Glance
 *
 *  Copyright 2015 Dav Glass
 *
 *  Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License. You may obtain a copy of the License at:
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software distributed under the License is distributed
 *  on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License
 *  for the specific language governing permissions and limitations under the License.
 *
 */
definition(
    name: "My Home At A Glance",
    namespace: "davglass",
    author: "Dav Glass",
    description: "My Home At A Glance",
    category: "My Apps",
    iconUrl: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience.png",
    iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png",
    iconX3Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png")


preferences {
    section("Which Windows?") {
        input "windows", "capability.contactSensor", multiple: true, required: true
    }
    section("Which Doors?") {
        input "doors", "capability.contactSensor", multiple: true, required: true
    }
    section("Which Locks?") {
        input "locks", "capability.lock", multiple: true, required: true
    }
    section("Inside Temp.") {
        input "inside", "capability.temperatureMeasurement", title: "Inside Temp Meter", required: false, multiple: true
    }
    section("Motion Sensors") {
        input "motion", "capability.motionSensor", title: "Motion Sensors", required: false, multiple: true
    }
    section("Lights") {
        input "lights", "capability.switch", title: "Switches", required: false, multiple: true
    }
    section("Energy Meter") {
        input "energymeter", "capability.EnergyMeter", title: "Energy Meter", required: false, multiple: true
    }
    section("Which Thing?") {
        input "thing", "capability.contactSensor", multiple: false, required: true
    }
}

def installed() {
    log.debug "Installed with settings: ${settings}"
    initialize()
}

def updated() {
    log.debug "Updated with settings: ${settings}"
    unsubscribe()
    initialize()
}

def initialize() {
    subscribe(windows, "contact", handler);
    subscribe(doors, "contact", doorsHandler);
    subscribe(locks, "lock", lockHandler);
    subscribe(inside, "temperature", tempHandler);
    subscribe(motion, "motion", motionHandler);
    subscribe(energymeter, "energy", powerHandler);
    subscribe(lights, "switch", lightsHandler);
    subscribe(app, handlerAll);
}

def handlerAll(evt) {
    handler();
    tempHandler();
    motionHandler();
    powerHandler();
    doorsHandler();
    lockHandler();
    lightsHandler();
}

def powerHandler(evt) {
    def power = energymeter.latestValue("power").first();
    log.debug "Setting power to: ${power} Watts"
    thing.setPower("${power} Watts");
}

def handler(evt) {
    log.debug "handler called.."
    def opened = 0;
    windows.each {
        if (it.currentValue("contact") == "open") {
            opened++;
        }
    }
    def closed = windows.size() - opened;
    log.debug "There are ${opened} windows open and ${closed} windows closed.."
    thing.countWindows(opened, closed);
}

def lightsHandler(evt) {
    log.debug "handler called.."
    def opened = 0;
    lights.each {
        if (it.currentValue("switch") == "on") {
            opened++;
        }
    }
    def closed = lights.size() - opened;
    log.debug "There are ${opened} lights on and ${closed} lights off.."
    thing.setLights(opened, closed);
}

def lockHandler(evt) {
    log.debug "handler called.."
    def opened = 0;
    locks.each {
        if (it.currentValue("lock") == "unlocked") {
            opened++;
        }
    }
    def closed = locks.size() - opened;
    log.debug "There are ${opened} locks unlocked and ${closed} locks locked.."
    thing.setLocks(opened, closed);
}

def doorsHandler(evt) {
    log.debug "Doors handler called.."
    def opened = 0;
    doors.each {
        if (it.currentValue("contact") == "open") {
            opened++;
        }
    }
    def closed = doors.size() - opened;
    log.debug "There are ${opened} doors open and ${closed} doors closed.."
    thing.setDoors(opened, closed);
}
def motionHandler(evt) {
    log.debug "motion handler called.."
    def active = 0;
    motion.each {
        if (it.currentValue("motion") == "active") {
            active++;
        }
    }
    def inactive = motion.size() - active;
    log.debug "There are ${active} active and ${inactive} inactive motion sensors.."
    thing.setMotion(active, inactive);
    powerHandler();
}

def tempHandler(evt) {
    def temp = []
    inside.each {
        if (it && it.currentTemperature) {
            temp.add(it.currentTemperature)
        }
    }
    log.debug "Temps Inside: ${temp}"
    def average = temp.sum() / temp.size()
    log.debug "Average Inside: ${average}"
    thing.setTemp(average);
}
