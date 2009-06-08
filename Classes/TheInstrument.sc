/**
	BUGS:
	DrumSynth - mixer volume does not affect this instrument
	EventLooper -- probably a lot still not functioning
	PolySynthControl: envelope modulators mostly don't work, must be cleaned up

	TODO:
	add a sample looper to the project
	improve PolySynthControl class structure to accomodate several different filters


	PolySynthControl - no midi control over GUI should be the rule 
        instead, modulation amounts should add to the visible params
	remove the global variables from this project:
./util/MonoSequencer.scd
./Classes/Effects/Compressor.sc
./Classes/Effects/Distortion.sc
./Classes/Effects/MonoDelay.sc
./Classes/TheInstrument.sc
./Control/AudioInChannel.sc
./Classes/Control/PolySynthControl.sc
./Classes/Control/DrumSynth.sc
./Classes/Control/GravityGridPlayer.sc
./Classes/Control/PolySynthControl.sc
/Control/Mixer.sc
./Classes/Control/DrumSynth.sc
./Classes/TheInstrument.sc


 */
TheInstrument {
	classvar noteOnFunction, noteOffFunction, bendFunction, ccFunction, touchFunction, lastChannel=0,
        audioBusRegister, mixer, eventLooper, monoInputChannel, polySynth, drumSynth, gravitGridPlayer;
	*new { 
		this.initializeMIDI;
		this.launchMidiResponders;
		this.launchObjects;
	}
	*initializeMIDI {
		Platform.case('osx', {
			MIDIClient.init(3, 2);
			MIDIIn.connect(1, MIDIClient.sources[1]);
		},
		'linux', {
			MIDIClient.init;
			MIDIIn.connect(1, MIDIClient.sources[3]);
		});
		noteOnFunction = { |src,chan,num,vel|
			lastChannel = chan;
			switch(chan,
				0, {
					~polySynth.noteOn(src, chan, num, vel);
					if(~polySynth.looper.notNil){
						~polySynth.looper.addEvent([0,src,chan,num,vel]);
					};
				},
				9, {
					~drumSynth.noteOn(src, chan, num, vel);
					if(~drumSynth.looper.notNil){
						~drumSynth.looper.addEvent([num, vel]);
					};
				}
			);
		};
		noteOffFunction = { |src,chan,num,vel|
			if(chan == 0){
				~polySynth.noteOff(src,chan,num,vel);
				if(~polySynth.looper.notNil){
					~polySynth.looper.addEvent([1,src,chan,num,0]);
				};
			};
		};
		bendFunction = { |src,chan,val|
			~polySynth.bend(src,chan,val);
			if(~polySynth.looper.notNil){
				~polySynth.looper.addEvent([4,src,chan,val]);
			};
		};
		ccFunction = { |src,chan,num,val|
			switch(lastChannel,
				0, {
					~polySynth.cc(src,chan,num,val);
					if(~polySynth.looper.notNil){
						~polySynth.looperHandleCC(src,chan,num,val);
					};
				},
				9, {
					~drumSynth.cc(src, chan, num, val);	
				}
			);
		};
		touchFunction = { |src,chan,val|
			~polySynth.afterTouch(src,chan,val);
			if(~polySynth.looper.notNil){
				~polySynth.looper.addEvent([3,src,chan,val]);
			};
		};
	}
	
	*noteOn { |src,chan,num,vel|
		noteOnFunction.value(src,chan,num,vel);
	}
	
	*noteOff { |src,chan,num,vel|
		noteOffFunction.value(src,chan,num,vel);
	}
	
	*bend { |src,chan,val|
		bendFunction.value(src, chan, val);
	}
	
	*cc { |src,chan,num,val|
		ccFunction.value(src, chan, num, val);
	}
	
	*touch { |src,chan,val|
		touchFunction.value(src,chan.val);
	}
	
	*launchObjects {
		~audioBusRegister = Dictionary.new;
		~mixer = Mixer.new;
		
		//~sampleLooper = SampleLooper.new;
		
		~eventLooper = EventLooper.new;
		
		~monoInputChannel = MonoInputChannel.new;

		~polySynth = PolySynthControlRLPF.new;
//		~polySynth = PolySynthControl.new;

		~drumSynth = DrumSynth.new;
				
		Platform.case('osx', {
			~gravityGridPlayer = GravityGridPlayer.new;
		});
		
	}
	*launchMidiResponders {
		NoteOnResponder(noteOnFunction);
		NoteOffResponder(noteOffFunction);
		CCResponder(ccFunction);
		TouchResponder(touchFunction);
		BendResponder(bendFunction);
	}
	*loadSynthDefs {
		var path;
		path = Platform.userAppSupportDir ++ "/Extensions/theinstrument/SynthDefs/*.scd";
		path.pathMatch.do{ |obj,ind| obj.load; };
	}
}
    
  