CZSynthControl {
	var <>mul=4, currentNote, <>att=0.05, <>dec=0.02, <>sus=0.7, <>rel=0.4, 
		<>peakA=0.6, <>peakB=0.3, <>peakC=0.6, <>feedback=0, touch=0, <>lag=0.1, 
		s, pitchBend=0, synthGroup=102, <recorderID="czSynth",
		// GUI Interface variables
		win, transport, recordButton, playButton, clearButton, groups, seqMenu, 
		incseqButtonUp, incseqButtonDown, addSeqGroupButton, presets, presetListMenu, 
		presetSaveButton,
		// synth parameter variables
		<>outBus=19;
	// 	16 18 12 17 19 13 // transport cc
	// 72  8 74 71  20 22 86 73 //   cc numbers 
	*new { |name|
		^super.new.czs_init(name);
	}
	czs_init { |name|
		s = Server.default;
		if(name.notNil){ recorderID = name; };
		s.sendMsg('b_alloc', 72, 1024);
		s.sendMsg('b_gen', 72, 'sine2', 5, 1, 1);
		s.sendMsg('g_new', synthGroup, 0, 1);
		s.sendMsg('g_new', 103, 3, synthGroup);
		//s.sendMsg('s_new', 'o_czFakeRez', 1029999, 1, 103);
		this.initLooper;
		this.addMixerChannel;
		//this.initGUI;
	}
	initLooper {
		// should implement the looper soon
		if(~eventLooper.notNil){
			~eventLooper.addChannel(1, recorderID);
			~eventLooper.channels[recorderID].action = { |values,index|
				switch(values[0],
					0, {
						this.noteOn(values[1], values[2], values[3], values[4]);
					},
					1, {
						this.noteOff(values[1], values[2], values[3], values[4]);
					},
					2, {
						this.cc(values[1], values[2], values[3], values[4]);
					},
					3, {
						this.afterTouch(values[1], values[2], values[3]);
					},
					4, {
						this.bend(values[1], values[2], values[3]);
					}
				);
			};
		};
	}
	addMixerChannel {
		~mixer.addMonoChannel("czSynth", ~mixer.mixGroup);
		outBus = ~mixer.channels["czSynth"].inBus;
	}
	looper {
		^~eventLooper.channels[recorderID];
	}
	noteOn { |src,chan,num,vel|
		currentNote = num;
		s.sendMsg('s_new', 's_czFakeRez', 1030000 + currentNote, 0, synthGroup, 'outBus', outBus);
		s.sendMsg('n_set', 1030000 + currentNote,
			'peakA', 0.6, 'peakB', 0.3, 'peakC', 0.6,
				'att', att, 'dec', dec, 'sus', sus, 'rel', rel, 'curve', -2,
				'gate', 1, 'freq1', currentNote.midicps, 'freq2', currentNote.midicps * mul, 'bend', pitchBend,
				'lev', (vel / 127).pow(2.5), 'feedback', feedback * currentNote.midicps, 'lag', 0.1
			);
	}
	noteOff { |src,chan,num,vel|
		s.sendMsg('n_set', 1030000 + num, 'gate', 0);
	}
	bend { |src,chan,val|
		pitchBend = val / 8192 - 1;
		s.sendMsg('n_set', synthGroup, 'bend', pitchBend);
	}
	afterTouch { |src,chan,val|
		feedback = val / 20;
		s.sendMsg('n_set', synthGroup, 'feedback', feedback * currentNote.midicps);
	}
	cc { |src,chan,num,val|
		// 72  8 74 71  20 22 86 73 //   cc numbers
		//[src,chan,num,val].postln; 
		switch( num,
		1, { // mod wheel
			mul = (val / 127).pow(2) * 8;
			s.sendMsg('n_set', synthGroup, 'freq2', currentNote.midicps * mul);
		},
		72, {
			att = val / 63.5;
		},
		8, {
			dec = val / 63.5;
		},
		74, {
			sus = val / 63.5;
		}, 
		71, {
			rel = val / 63.5;
		},
		20, {
			feedback = val / 5;
			s.sendMsg('n_set', synthGroup, 'feedback', feedback * currentNote.midicps);
		},
		22, {
			lag = val / 100;
			s.sendMsg('n_set', synthGroup, 'lag', lag);
			//peakA = val / 127;
		},
		86, {
			peakB = val / 127;
		},
		73, {
			peakC = val / 127;
		});
	}
	/** GUI members */
	looperHandleCC { |src,chan,num,val|
	    case{[1, 72, 8, 74, 71, 20, 22, 86, 73].includes(num)}{
	        this.looper.addEvent([2,src,chan,num,val]);
	    }
	    { num == 16 }{
	        this.looper.clear;
	    }
	    { num == 18}{
	        if(this.looper.eventCollection.notNil){
				defer{ 
					seqMenu.value = (seqMenu.value - 1) % seqMenu.items.size;
	            	this.looper.load(seqMenu.value);
				};
	        };
	    }
	    { num == 12 }{
	        if(this.looper.eventCollection.notNil){
	            defer{
	            	seqMenu.value = (seqMenu.value + 1) % seqMenu.items.size;
	            	this.looper.load(seqMenu.value);
	            };
	        };
	    }
	    { num == 17 }{
	        this.looper.stop;
	        { playButton.value = 0; }.defer;
	    }
	    { num == 19 }{
	        this.looper.start;
	        { playButton.value = 1; }.defer;
	    }
	    { num == 13 }{
			if(val > 0){
	             this.looper.startRecording;
	             { recordButton.value = 1; }.defer;
	         }{
	             this.looper.stopRecording;
	             { recordButton.value = 0; }.defer;
	         };
	    };
	}	
}
      
            