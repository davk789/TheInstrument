TheInstrument {
	classvar <>noteOnFunction, <>noteOffFunction, <>bendFunction, <>ccFunction, <touchFunction, lastChannel=0;
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
	*launchObjects {
		~audioBusRegister = Dictionary.new;
		~mixer = Mixer.new;
		
		//~sampleLooper = SampleLooper.new;
		
		~eventLooper = EventLooper.new;
		
		~monoInputChannel = MonoInputChannel.new;

//		~polySynth = PolySynthControlRLPF.new;
		~polySynth = PolySynthControl.new;

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
    
  