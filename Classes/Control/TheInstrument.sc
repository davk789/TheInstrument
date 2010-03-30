/**
	BUGS:
	WavetableSynth:
		envelope modulators mostly don't work, must be cleaned up
		the filter type menu does not update when the paramters are 
			changed automatically (preset loading/initialization)
	    some of the filters need extra parameters/different settings
	EventLooper
	    the first noteOn of the sequence gets dropped on playback
	DrumSynth
		Klunk resonators -- pan doesn't work
	SampleLooper
		key switched should automatically be generated based on number of channels  (not hard coded)
		

		

	TODO:
	window management for synths
	WavetableSynth - modulation amounts should add to the visible params
	Sampler  
	 - figure out some way to use CCs that can work with the wavetable synth
	 - make the waveform controls jump to the clicked/region-delimited point
	 - preset support -- save temp buffers, 
	 - EventLooper support
	 - "Quick Loop" handle levels/fade-in for laying down a single loop
	 - loops shorter than the full sample length should not click on loop
	EventLooper
	 - add tempo change on a per-instance basis

*/
ThyInstrument {
	classvar noteOnFunction, noteOffFunction, bendFunction, ccFunction, touchFunction, lastChannel=0,
        <>audioBusRegister, <>mixer, <>eventLooper, <>monoInputChannel, <>polySynth, <>drumSynth, <>gravityGridPlayer, <>sampler, keyControl, <controlFont, <strongFont, <>effectsList,
        <>filterUGens, <filterSpecs;
	*new {
		this.initializeMIDI;
		this.launchMidiResponders;
		this.createFilters;
		this.launchObjects;
	}
	
	*test {
		this.new;
		this.useComputerKeyboard;
	}
	
	*createFilters {
	/*  filterSpecs and filterUGens are shared by WavetableSynthFilter and by Filter... this is why the functions
		are in the "global" namespace */
		filterUGens = Dictionary[
			"RLPF" -> { |in, freq, rez| RLPF.ar(in, freq, rez.reciprocal); },
			"RHPF" -> { |in, freq, rez| RHPF.ar(in, freq, rez.reciprocal); },
			"MoogFF" -> { |in, freq, gain| MoogFF.ar(in, freq, gain); },
			"BLowPass4" -> { |in, freq, rez| BLowPass4.ar(in, freq, rez.reciprocal); },
			"BLowPass" -> { |in, freq, rez| BLowPass.ar(in, freq, rez.reciprocal); },
			"BHiPass4" -> { |in, freq, rez| BHiPass4.ar(in, freq, rez.reciprocal); },
			"BHiPass" -> { |in, freq, rez| BHiPass.ar(in, freq, rez.reciprocal); },
			"BBandPass" -> { |in, freq, q| BBandPass.ar(in, freq, q.reciprocal); }, 
			"BAllPass" -> { |in, freq, rez| BAllPass.ar(in, freq, rez.reciprocal); },
			"Resonz" -> { |in, freq, rez| Resonz.ar(in, freq, rez.reciprocal); }	
		];
		Platform.case('osx', { 			
			filterUGens = filterUGens.add("MoogVCF" -> { |in, freq, rez| MoogVCF.ar(in, freq, rez); });
			filterUGens = filterUGens.add("RLPFD" -> { |in, freq, rez, dist=0.8| RLPFD.ar(in, freq, rez, dist); }); 
			filterUGens = filterUGens.add("MoogLadder" -> { |in, freq, rez| MoogLadder.ar(in, freq, rez); });
			filterUGens = filterUGens.add("BMoogLPF" -> { |in, freq, rez, sat=0.5| BMoog.ar(in, freq, rez, 0, sat); });
			filterUGens = filterUGens.add("BMoogHPF" -> { |in, freq, rez, sat=0.5| BMoog.ar(in, freq, rez, 1, sat); });
			filterUGens = filterUGens.add("BMoogBPF" -> { |in, freq, rez, sat=0.5| BMoog.ar(in, freq, rez, 2, sat); });
			filterUGens = filterUGens.add("IIRFilter" -> { |in, freq, rez| IIRFilter.ar(in, freq, rez.reciprocal); });
		});
		filterSpecs = Dictionary[ 
			"RLPFD" -> [0,1].asSpec,
			"MoogLadder" -> [0,1].asSpec,
			"BMoogLPF" -> [0, 1].asSpec,
			"BMoogHPF" -> [0, 1].asSpec,
			"BMoogBPF" -> [0, 1].asSpec,
			"IIRFilter" -> [1, 100].asSpec,
			"RLPF" -> [1,100].asSpec,
			"RHPF" -> [1,100].asSpec,
			"MoogVCF" -> [0.1, 10].asSpec,
			"MoogFF" -> [0.1, 10].asSpec,
			"BLowPass4" -> [1,100].asSpec,
			"BLowPass" -> [1, 100].asSpec,
			"BHiPass4" -> [1,100].asSpec,
			"BHiPass" -> [1,100].asSpec,
			"BBandPass" -> [0.1, 10].asSpec,
			"BAllPass" -> [1,100].asSpec,
			"Resonz" -> [1,100].asSpec
		];
	}
	
	*initializeMIDI {
		noteOnFunction = { |src,chan,num,vel|
			//[src,chan,num,vel].postln;
			lastChannel = chan;
			switch(chan,
				0, { // WavetableSynth
					polySynth.noteOn(src, chan, num, vel);
				},
				9, { // DrumSynth
					drumSynth.noteOn(src, chan, num, vel);
				}
			);
		};
		noteOffFunction = { |src,chan,num,vel|
			if(chan == 0){
				polySynth.noteOff(src,chan,num,vel);
			};
		};
		bendFunction = { |src,chan,val|
			polySynth.bend(src,chan,val);
		};
		ccFunction = { |src,chan,num,val|
			switch(chan,
				0, { // WavetableSynth
					polySynth.cc(src,chan,num,val);
				},
				1, {  // Sampler
					sampler.cc(src,chan,num,val);
				},
				9, { // DrumSynth
					drumSynth.cc(src, chan, num, val);
				}
			);
		};
		touchFunction = { |src,chan,val|
			polySynth.afterTouch(src,chan,val);
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

		polySynth = WavetableSynthFilter.new(this);//, true); // enable midi thru
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
			keyControl = QuickKeyboard.new(this); // this is a really ugly class
		}{
			keyControl = nil;
		};
		
	}
}
    
  