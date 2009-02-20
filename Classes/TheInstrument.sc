TheInstrument {
	*new {
		this.initializeMIDI;
		this.launchMidiResponders;
		this.launchObjects;
	}
	*initializeMIDI {
		Platform.case('osx', {
			MIDIClient.init(3, 2);
			MIDIIn.connect(1, MIDIClient.sources[1]);
		});
	}
	*launchObjects {
		~audioBusRegister = Dictionary.new;
		// this is to be used by the effects classes. using the track name as the key
		~mixer = Mixer.new;
		
		//~sampleLooper = SampleLooper.new;
		
		~eventLooper = EventLooper.new;
		// preset support
		
		~polySynth = PolySynthControl.new;
		~polySynth.att = 0.001;
		~polySynth.dec = 0.008;
		~polySynth.sus = 0.1;
		~polySynth.rel = 0.5;
		~polySynth.peakA = 1;
		~polySynth.peakB = 0.6;
		~polySynth.peakC = 0.63;
				
		~drumSynth = DrumSynth.new;
				
		~drumSynth.setDrumParam(0, 'gain', 0.1);
		~drumSynth.setDrumParam(0, 'curve', -1);
		~drumSynth.setDrumParam(0, 'rel', 0.2);
		~drumSynth.setRezParam(0, 'r1', 0.1);
		~drumSynth.setRezParam(0, 'r2', 0.1);
		~drumSynth.setRezParam(0, 'r3', 0.1);
		~drumSynth.setRezParam(0, 'f1', 30);
		~drumSynth.setRezParam(0, 'f2', 31);
		~drumSynth.setRezParam(0, 'f3', 33);
		~drumSynth.setRezParam(0, 'a1', 0.2);
		~drumSynth.setRezParam(0, 'a2', 0.22);
		~drumSynth.setRezParam(0, 'a3', 0.21);
		
		~drumSynth.setDrumParam(1, 'curve', -6);
		~drumSynth.setDrumParam(1, 'att', 0.001);
		~drumSynth.setDrumParam(1, 'rel', 1);
		~drumSynth.setDrumParam(1, 'freq', 60);
		~drumSynth.setDrumParam(1, 'modFreq', -0.125);
		~drumSynth.setDrumParam(1, 'modAmt', 12);
		~drumSynth.setDrumParam(1, 'drive', 0);
		~drumSynth.setRezParam(1, 'gain', 3);
		~drumSynth.setRezParam(1, 'freq', 180);
		~drumSynth.setRezParam(1, 'rez', 7);
		
		~drumSynth.setDrumParam(7, 'outBus', 17);
		~drumSynth.setDrumParam(7, 'freq', 8000);
		~drumSynth.setDrumParam(7, 'curve', -10);
		~drumSynth.setRezParam(7, 'freq', 8000);
		//drumSynth.setDrumParam(2, 'gain', 12);
		
		//drumSynth.initLooper;//
		 	}
	*launchMidiResponders {
		var lastChannel=0;
		NoteOnResponder({ |src,chan,num,vel|
			[src,chan,num,vel].postln;
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
		});
		NoteOffResponder({ |src,chan,num,vel|
			if(chan == 0){
				~polySynth.noteOff(src,chan,num,vel);
				if(~polySynth.looper.notNil){
					~polySynth.looper.addEvent([1,src,chan,num,0]);
				};
			};
		});
		CCResponder({ |src,chan,num,val|
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
		});
		TouchResponder({ |src,chan,val|
			~polySynth.afterTouch(src,chan,val);
			if(~polySynth.looper.notNil){
				~polySynth.looper.addEvent([3,src,chan,val]);
			};
		});
		BendResponder({ |src,chan,val|
			~polySynth.bend(src,chan,val);
			if(~polySynth.looper.notNil){
				~polySynth.looper.addEvent([4,src,chan,val]);
			};
		});
	}
}
    
