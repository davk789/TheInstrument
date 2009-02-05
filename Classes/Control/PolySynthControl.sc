PolySynthControl {
	classvar  classGroup=102;
	var activeNotes, win, instGroup=103, s, 
		<>att=0.05, <>dec=0.02, <>sus=0.7, <>rel=0.4, 
		<>peakA=0.6, <>peakB=0.3, <>peakC=0.6, <>mul=4, <>feedback=0, touch=0, <>lag=0.1, 
		pitchBend=0, <>outBus=19, <recorderID="czSynth";
	// 	16 18 12 17 19 13 // transport cc
	// 72  8 74 71  20 22 86 73 //   cc numbers 
	*new {
		^super.new.init_polysynthcontrol(name);
	}
	init_polysynthcontrol { |name|
		s = Server.default;
		activeNotes = Dictionary.new;
		if(name.notNil){ recorderID = name; };
		s.sendMsg('g_new', classGroup, 0, 1);
		s.sendMsg('b_alloc', 72, 1024);
		s.sendMsg('b_gen', 72, 'sine2', 5, 1, 1);
		s.sendMsg('g_new', instGroup, 3, classGroup);
		this.addMixerChannel;
		this.initLooper;
		this.initGUI;
	}
	initLooper {
/*		~eventLooper.addChannel(1, recorderID);
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
		};*/
	}
	looper {
//		^~eventLooper.channels[recorderID];//
	}
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
					this.looper.seqMenu.value = (this.looper.seqMenu.value - 1) % this.looper.seqMenu.items.size;
	            	this.looper.load(this.looper.seqMenu.value);
				};
	        };
	    }
	    { num == 12 }{
	        if(this.looper.eventCollection.notNil){
	            defer{
					var menu;
					this.looper.seqMenu.value = (this.looper.seqMenu.value + 1) % this.looper.seqMenu.items.size;
	            	this.looper.load(this.looper.seqMenu.value);
	            };
	        };
	    }
	    { num == 17 }{
	        this.looper.stop;
	        { this.looper.playButton.value = 0; }.defer;
	    }
	    { num == 19 }{
	        this.looper.start;
	        { this.looper.playButton.value = 1; }.defer;
	    }
	    { num == 13 }{
			if(val > 0){
	             this.looper.startRecording;
	             { this.looper.recordButton.value = 1; }.defer;
	         }{
	             this.looper.stopRecording;
	             { this.looper.recordButton.value = 0; }.defer;
	         };
	    };
	}
	initGUI {
		var modeRow, modeMenu, partialRow1, partial1Amps, partial1Freqs, partialRow2, partial2Amps, partial2Freqs, waveformDraw, targetColumn, targetAButton, targetBButton;
		win = GUI.window.new("organum", Rect.new(50,300, 400, 300)).front;
		win.view.decorator = FlowLayout(win.view.bounds);
		
		modeRow = GUI.hLayoutView.new(win, Rect.new(0, 0, win.view.bounds.width, 20))
			.background_(Color.blue(0.1, alpha:0.2));
		GUI.staticText.new(modeRow, Rect.new(0, 0, win.view.bounds.width * 0.24, 0))
			.string_("Synth:");
		modeMenu = GUI.popUpMenu.new(modeRow, Rect.new(0, 0, win.view.bounds.width * 0.74, 0))
			.items_(["czFakeRez", "dualWavetable"])
			.action_({ |obj| this.setSynthPreset(obj); });
		partialRow1 = GUI.hLayoutView.new(win, Rect.new(0, 0, win.view.bounds.width, 75))
			.background_(Color.blue(0.1, alpha:0.2));
		partial1Amps = GUI.multiSliderView.new(partialRow1, Rect.new(0, 0, 98, 0))
			.fillColor_(Color.blue(0.6, alpha:0.3))
			.indexThumbSize_(5.0)
			.valueThumbSize_(5.0)
			.value_([1,0,0,0, 0,0,0,0, 0,0,0,0, 0,0,0,0])
			.isFilled_(true);
		partial1Freqs = GUI.multiSliderView.new(partialRow1, Rect.new(0, 0, 98, 0))
			.fillColor_(Color.red(0.6, alpha:0.3))
			.indexThumbSize_(5.0)
			.valueThumbSize_(5.0)
			.value_(Array.fill(16, { 0.5 }));
		waveformDraw = GUI.multiSliderView.new(win, Rect.new(0, 0, win.view.bounds.width * 0.85, 85))			.value_(Array.fill(256, {0.5}))
			.indexThumbSize_(0.32)
			.valueThumbSize_(3)
			.isFilled_(false)
			.canFocus_(false)
			.action_({ |obj| [obj.index, obj.currentvalue].postln;});
		targetColumn = GUI.vLayoutView.new(win, Rect.new(0, 0, win.view.bounds.width * 0.12, 85))
			.background_(Color.blue(0.1, alpha:0.2));
		targetAButton = GUI.button.new(targetColumn, Rect.new(0, 0, 0, targetColumn.bounds.height * 0.42))
			.states_([["A", Color.black, Color.clear],["A", Color.red, Color.yellow]]);
		targetBButton = GUI.button.new(targetColumn, Rect.new(0, 0, 0, targetColumn.bounds.height * 0.42))
			.states_([["B", Color.black, Color.clear],["B", Color.red, Color.yellow]]);
		partialRow2 = GUI.hLayoutView.new(win, Rect.new(0, 0, win.view.bounds.width, 75))
			.background_(Color.blue(0.1, alpha:0.2));
		partial2Amps = GUI.multiSliderView.new(partialRow2, Rect.new(0, 0, 98, 0))
			.fillColor_(Color.blue(0.6, alpha:0.3))
			.indexThumbSize_(5.0)
			.valueThumbSize_(5.0)
			.value_([1,0,0,0, 0,0,0,0, 0,0,0,0, 0,0,0,0])
			.isFilled_(true);
		partial2Freqs = GUI.multiSliderView.new(partialRow2, Rect.new(0, 0, 98, 0))
			.fillColor_(Color.red(0.6, alpha:0.3))
			.indexThumbSize_(5.0)
			.valueThumbSize_(5.0)
			.value_(Array.fill(16, { 0.5 }));
	}
	setSynthPreset { |menu|
		postln("this is where the SynthControl class will switch synth modes.\n" ++ menu.value);
	}
	addMixerChannel {
/*		~mixer.addMonoChannel("fakeCZSynth", ~mixer.mixGroup);
		outBus = ~mixer.channels["fakeCZSynth"].inBus;
*/	}
	noteOn { |src,chan,num,vel|
		activeNotes = activeNotes.add(num -> s.nextNodeID);
		s.sendMsg('s_new', 's_czFakeRez', activeNotes[num], 0, classGroup,
			'outBus', outBus, 'peakA', 0.6, 'peakB', 0.3, 'peakC', 0.6,
			'att', att, 'dec', dec, 'sus', sus, 'rel', rel, 'curve', -2,
			'freq1', num.midicps, 'freq2', mul, 'bend', pitchBend,
			'lev', (vel / 127).pow(2.5), 'feedback', feedback, 'lag', 0.1
		);
		s.sendMsg('n_set', activeNotes[num], 'gate', 1);
	}
	noteOff { |src,chan,num,vel|
		s.sendMsg('n_set', activeNotes[num], 'gate', 0);
	}
	bend { |src,chan,val|
		pitchBend = val / 8192 - 1;
		s.sendMsg('n_set', classGroup, 'bend', pitchBend);
	}
	afterTouch { |src,chan,val|
		feedback = val / 20;
		s.sendMsg('n_set', classGroup, 'feedback', feedback);
	}
	cc { |src,chan,num,val|
		// 72  8 74 71  20 22 86 73 //   cc numbers
		switch( num,
		1, { // mod wheel
			mul = (val / 127).pow(2) * 8;
			s.sendMsg('n_set', classGroup, 'freq2', mul);
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
			s.sendMsg('n_set', classGroup, 'feedback', feedback);
		},
		22, {
			lag = val / 100;
			s.sendMsg('n_set', classGroup, 'lag', lag);
			//peakA = val / 127;
		},
		86, {
			peakB = val / 127;
		},
		73, {
			peakC = val / 127;
		});
	}
}

