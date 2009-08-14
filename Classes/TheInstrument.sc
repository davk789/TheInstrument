/**
	BUGS:
	WavetableSynth:
		envelope modulators mostly don't work, must be cleaned up
		the filter type menu does not update when the paramters are 
			changed automatically (preset loading/initialization)
	    some of the filters need extra parameters/different settings
		

	TODO:
	window management
	WavetableSynth - modulation amounts should add to the visible params
	Sampler  -figure out some way to use CCs that can work with the wavetable synth

*/
ThyInstrument {
	classvar noteOnFunction, noteOffFunction, bendFunction, ccFunction, touchFunction, lastChannel=0,
        <>audioBusRegister, <>mixer, <>eventLooper, <>monoInputChannel, <>polySynth, <>drumSynth, <>gravityGridPlayer, <>sampler, keyControl, <controlFont, <strongFont, <>effectsList;
	*new {
		this.initializeMIDI;
		this.launchMidiResponders;
		this.launchObjects;
	}
	
	*test {
		this.new;
		this.useComputerKeyboard;
	}
	
	*initializeMIDI {
		noteOnFunction = { |src,chan,num,vel|
			lastChannel = chan;
			switch(chan,
				0, { // WavetableSynth
					polySynth.noteOn(src, chan, num, vel);
					if(polySynth.looper.notNil){
						polySynth.looper.addEvent([0,src,chan,num,vel]);
					};
				},
				9, { // DurmSynth
					drumSynth.noteOn(src, chan, num, vel);
					if(drumSynth.looper.notNil){
						drumSynth.looper.addEvent([num, vel]);
					};
				}
			);
		};
		noteOffFunction = { |src,chan,num,vel|
			if(chan == 0){
				polySynth.noteOff(src,chan,num,vel);
				if(polySynth.looper.notNil){
					polySynth.looper.addEvent([1,src,chan,num,0]);
				};
			};
		};
		bendFunction = { |src,chan,val|
			polySynth.bend(src,chan,val);
			if(polySynth.looper.notNil){
				polySynth.looper.addEvent([4,src,chan,val]);
			};
		};
		ccFunction = { |src,chan,num,val|
			switch(chan,
				0, { // WavetableSynth
					polySynth.cc(src,chan,num,val);
					if(polySynth.looper.notNil){
						polySynth.looperHandleCC(src,chan,num,val);
					};
				},
				1, {  // Sampler
					postln(["inside 1 in switch",src,chan,num,val]);
					sampler.cc(src,chan,num,val);
					/*if(sampler.looper.notNil){ // not supporting command sequencing here yet
						sampler.looper.addEvent([src,chan,num,vel]);
					};*/
				},
				9, { // DrumSynth
					drumSynth.cc(src, chan, num, val);	
				}
			);
		};
		touchFunction = { |src,chan,val|
			polySynth.afterTouch(src,chan,val);
			if(polySynth.looper.notNil){
				polySynth.looper.addEvent([3,src,chan,val]);
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
		// global helper objects
		audioBusRegister = Dictionary.new;
		effectsList = Array.new;
		controlFont = Font.new("Helvetica", 10);
		strongFont = Font.new("Arial Black", 12);
		
		// Instrument classes
		mixer = Mixer.new(this);
				
		eventLooper = EventLooper.new(this);
		
		//monoInputChannel = MonoInputChannel.new(this);

		polySynth = WavetableSynthFilter.new(this);
//		polySynth = WavetableSynth.new(this);

		drumSynth = DrumSynth.new(this);
				
		Platform.case('osx', {
			gravityGridPlayer = GravityGridPlayer.new(this);
		});

		sampler = Sampler.new(this, 4);
		
	}
	
	*launchMidiResponders {
		NoteOnResponder(noteOnFunction);
		NoteOffResponder(noteOffFunction);
		CCResponder(ccFunction);
		TouchResponder(touchFunction);
		BendResponder(bendFunction);
	}
	
	*loadSynthDefs { // oops I deprecated this on accident.
		var path;
		path = Platform.userAppSupportDir ++ "/Extensions/theinstrument/SynthDefs/*.scd";
		path.pathMatch.do{ |obj,ind| obj.load; };
	}
	
	*useComputerKeyboard { |create=true|
		if(create){
			keyControl = QuickKeyboard.new(this);
		}{
			keyControl = nil;
		};
		
	}
}
    
  