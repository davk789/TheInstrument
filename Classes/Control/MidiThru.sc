MidiThru { // tying a MidiOut to an event looper
	var midiOut, looper, recorderID, midiPort, parent;

	*new { |par,port|
		^super.new.init_midithru(par,port);
	}

	init_midithru { |par,port=2|
		recorderID = "MIDI Thru";
		"initializing midi thru".postln;
		parent = par;
		midiPort = port;
		
		midiOut = MIDIOut.new(port);
		
		this.initLooper;
	}

	initLooper {
		//postln("calling parent.eventLooper.addChannel(1, " ++ recorderID ++ "); from " ++ this.class.asString);
		parent.eventLooper.addChannel(1, recorderID);
		parent.eventLooper.channels[recorderID].action = { |values,index|
			//postln("back to a function defined in WavetableSynth the values are " ++ values);
			switch(values[0],
				0, {
					midiOut.noteOn(values[1], values[2], values[3], values[4]);
				},
				1, {
					midiOut.noteOff(values[1], values[2], values[3], values[4]);
				},
				2, {
					midiOut.cc(values[1], values[2], values[3], values[4]);
				},
				3, {
					midiOut.afterTouch(values[1], values[2], values[3]);
				},
				4, {
					midiOut.bend(values[1], values[2], values[3]);
				}
			);
		};
	}

	looper {
		^parent.eventLooper.channels[recorderID];
	}

	noteOn { |src,chan,num,vel|
		midiOut.noteOn(src,chan,num,vel);
		parent.eventLooper.addEvent([src,chan,num,vel]);
	}

	noteOff { |src,chan,num,vel|
		midiOut.noteOff(src,chan,num,vel);
		parent.eventLooper.addEvent([src,chan,num,vel]);
	}

	cc { |src,chan,num,val|
		midiOut.cc(src,chan,num,val);
		parent.eventLooper.addEvent([src,chan,num,val]);
	}

	afterTouch { |src,chan,num,val|
		midiOut.afterTouch(src,chan,num,val);
		parent.eventLooper.addEvent([src,chan,num,val]);
	}

	bend { |src,chan,num,val|
		midiOut.bend(src,chan,num,val);
		parent.eventLooper.addEvent([src,chan,num,val]);
	}

}