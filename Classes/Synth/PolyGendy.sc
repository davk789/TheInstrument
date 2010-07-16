PolyGendy : InstrumentVoice { // a challenge to myself to finish a small project
	var activeNotes;
	*new {
		^super.new.init_polygendy;
	}

	init_polygendy {
		activeNotes = Dictionary.new;
		this.initializeMIDI;
		postln(this.class.asString ++ " initialized");
	}

	initializeMIDI {
		noteOnFunction = { |src,chan,num,vel|
			this.addActiveNote(num, s.nextNodeID);
			[src,chan,num,vel].postln;
		};

		noteOffFunction = { |src,chan,num,vel|
			[src,chan,num,vel].postln;
		};
 
		ccFunction = { |src,chan,num,val|
			[src,chan,num,val].postln;
		};

		bendFunction = { |src,chan,val|
			[src,chan,val].postln;
		};

		afterTouchFunction = { |src,chan,val|
			[src,chan,val].postln;
		};

	}

	*loadSythDef {
		SynthDef.new("PolyGendy", {
			|ampdist,durdist,adparam,ddparam,minfreq,maxfreq,durscale,initcps,scale,add|
			Gendy1.ar(ampdist,durdist,adparam,ddparam,minfreq,maxfreq,durscale,initcps,add);
		}).load(s);
	}

	addActiveNote { |noteNum,id|
		var lastNote;
		if(activeNotes[noteNum].notNil){
			lastNote = activeNotes[noteNum];
			activeNotes[noteNum] = lastNote ++ id;
		}{
			activeNotes = activeNotes.add(noteNum -> id.asArray);
		};
	}

}