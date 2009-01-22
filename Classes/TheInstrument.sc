TheInstrument {
	*new {
		this.launchObjects;
		this.launchMidiResponders;
	}
	*launchObjects {
		~audioBusRegister = Dictionary.new;
		// this is going to be used by the effects classes. using the track name as the key
		~mixer = Mixer.new;
		
		~eventLooper = EventLooper.new;
		// preset support
		
		~czSynth = CZSynthControl.new;
		~czSynth.att = 0.001;
		~czSynth.dec = 0.008;
		~czSynth.sus = 0.1;
		~czSynth.rel = 0.5;
		~czSynth.peakA = 1;
		~czSynth.peakB = 0.6;
		~czSynth.peakC = 0.63;
		
		//~wvSynth = WavetableSynth.new;
		
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
			//switch(chan,
			//	0, {
					~czSynth.noteOn(src, chan, num, vel);
					if(~czSynth.looper.notNil){
						~czSynth.looper.addEvent([0,src,chan,num,vel]);
					};
			//	},
			//	9, {
					~drumSynth.noteOn(src, chan, num, vel);
					if(~drumSynth.looper.notNil){
						~drumSynth.looper.addEvent([num, vel]);
						// not automating cc controls yet
					};
			//	}
			//);
		});
		NoteOffResponder({ |src,chan,num,vel|
			if(chan == 0){
				~czSynth.noteOff(src,chan,num,vel);
				if(~czSynth.looper.notNil){
					~czSynth.looper.addEvent([1,src,chan,num,0]);
				};
			};
		});
		CCResponder({ |src,chan,num,val|
			switch(lastChannel,
				0, {
					~czSynth.cc(src,chan,num,val);
					if(~czSynth.looper.notNil){
						~czSynth.looperHandleCC(src,chan,num,val);
					};
				},
				9, {
					~drumSynth.cc(src, chan, num, val);	
				}
			);
		});
		TouchResponder({ |src,chan,val|
			~czSynth.afterTouch(src,chan,val);
			if(~czSynth.looper.notNil){
				~czSynth.looper.addEvent([3,src,chan,val]);
			};
		});
		BendResponder({ |src,chan,val|
			~czSynth.bend(src,chan,val);
			if(~czSynth.looper.notNil){
				~czSynth.looper.addEvent([4,src,chan,val]);
			};
		});
	}
}