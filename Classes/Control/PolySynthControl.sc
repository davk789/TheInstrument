PolySynthControl {
	classvar  classGroup=102, bufferIndex=68;
	var activeNotes, win, instGroup=103, s, bufferA=70, bufferB=71,
		<>att=0.05, <>dec=0.02, <>sus=0.7, <>rel=0.4, 
		<>peakA=0.6, <>peakB=0.3, <>peakC=0.6, <>mul=4, <>feedback=0, touch=0, <>lag=0.1, 
		<xfade, <fbLag, <fbMul, <freq2, <fm2,
		pitchBend=0, <>outBus=19, <recorderID="DualWavetable";
	// 	16 18 12 17 19 13 // transport cc
	// 72  8 74 71  20 22 86 73 //   cc numbers 
	*new { |name|
		bufferIndex = bufferIndex + 2;
		^super.new.init_polysynthcontrol(bufferIndex, name);
	}
	init_polysynthcontrol { |buffer, name|
		s = Server.default;
		activeNotes = Dictionary.new;
		bufferA = buffer;
		bufferB = buffer + 1;
		s.sendMsg('g_new', classGroup, 0, 1);
		s.sendMsg('b_alloc', bufferA, 1024);
		s.sendMsg('b_alloc', bufferB, 1024);
		s.sendMsg('b_gen', bufferA, 'sine2', 5, 1, 1);
		s.sendMsg('b_gen', bufferB, 'sine2', 5, 1, 1);
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
	generatePartials { |partials, bufnum|
		var partialFreqs, partialAmps;
		partialFreqs = (partials[1] * 2) * Array.series(19, 1, 1);
		partialAmps = partials[0].pow(3);
		s.listSendMsg(['b_gen', bufnum, 'sine2', 5] ++ [partialFreqs, partialAmps].lace(38));
	}
	writeToBuffer { |index, value, bufnum|
		var prevVal, bufferIndex;
		bufferIndex = index * 4;
		// blah make this work
		prevVal = s.sendMsg('b_get', bufnum, bufferIndex - 4);
	}
	setSynthPreset { |menu|
		postln("this is where the SynthControl class will switch synth modes.\n" ++ menu.value);
	}
	setXFade { |xf|
		xfade = xf;
		s.sendMsg('n_set', instGroup, 'xfade', xfade);
	}
	setLag { |lag|
		fbLag = lag;
		s.sendMsg('n_set', instGroup, 'lag', fbLag);
	}
	setFBMul { |mul|
		fbMul = mul;
		s.sendMsg('n_set', instGroup, 'fbMul', fbMul);
	}
	setFreq2 { |frq|
		freq2 = frq;
		s.sendMsg('n_set', instGroup, 'freq2', freq2);
	}
	setFM2 { |fm|
		fm2 = fm;
		s.sendMsg('n_set', instGroup, 'fmAmt', fm2);
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
	initGUI {
		var modeRow, modeMenu, xfadeKnob, fbLagKnob, fbMulKnob, partialRow1, partialAAmps, partialAFreqs, midiListMenu, midiEnvListMenu, pr2AuxControls, xFadeMenu, fbMulMenu, freq2Menu, fm2Menu, partialRow2, freq2Knob, fm2Knob, syncModeMenu, partialBAmps, partialBFreqs, envelopeView, waveformDraw, targetColumn, targetAButton, targetBButton;
		win = GUI.window.new("Dual Wavetable PolySynth", Rect.new(50,300, 400, 360)).front;
		win.view.decorator = FlowLayout(win.view.bounds);
		
		modeRow = GUI.hLayoutView.new(win, Rect.new(0, 0, win.view.bounds.width, 20))
			.background_(Color.blue(0.1, alpha:0.2));
		GUI.staticText.new(modeRow, Rect.new(0, 0, win.view.bounds.width * 0.24, 0))
			.string_("preset:");
		modeMenu = GUI.popUpMenu.new(modeRow, Rect.new(0, 0, win.view.bounds.width * 0.74, 0))
			.items_(["czFakeRez", "dualWavetable"])
			.action_({ |obj| this.setSynthPreset(obj); });
			
		// controls row 1
		partialRow1 = GUI.hLayoutView.new(win, Rect.new(0, 0, win.view.bounds.width, 75))
			.background_(Color.blue(0.1, alpha:0.2));
		GUI.staticText.new(partialRow1, Rect.new(0, 0, 10, 0))
			.string_("A")
			.background_(Color.black.alpha_(0.9))
			.stringColor_(Color.white);
		partialAAmps = GUI.multiSliderView.new(partialRow1, Rect.new(0, 0, 75, 0))
			.fillColor_(Color.blue)
			.strokeColor_(Color.blue)
			.indexThumbSize_(2.9)
			.background_(Color.black.alpha_(0.9))
			.valueThumbSize_(2.9)
			.value_([1,0,0,0, 0,0,0,0, 0,0,0,0, 0,0,0,0, 0,0,0])
			.isFilled_(true)
			.mouseUpAction_({ |obj| this.generatePartials([obj.value, partialAFreqs.value], bufferA); });
		partialAFreqs = GUI.multiSliderView.new(partialRow1, Rect.new(0, 0, 75, 0))
			.fillColor_(Color.red)
			.strokeColor_(Color.red)
			.indexThumbSize_(2.9)
			.background_(Color.black.alpha_(0.9))
			.valueThumbSize_(2.9)
			.value_(Array.fill(19, { 0.5 }))
			.mouseUpAction_({ |obj| this.generatePartials([partialAAmps.value, obj.value], bufferA); });
		GUI.staticText.new(partialRow1, Rect.new(0, 0, 10, 0))
			.string_("B")
			.background_(Color.black.alpha_(0.9))
			.stringColor_(Color.white);
		partialBAmps = GUI.multiSliderView.new(partialRow1, Rect.new(0, 0, 75, 0))
			.fillColor_(Color.blue)
			.strokeColor_(Color.blue)
			.background_(Color.black.alpha_(0.9))
			.indexThumbSize_(2.9)
			.valueThumbSize_(2.9)
			.value_([1,0,0,0, 0,0,0,0, 0,0,0,0, 0,0,0,0, 0,0,0])
			.isFilled_(true)
			.mouseUpAction_({ |obj| this.generatePartials([obj.value, partialBFreqs.value], bufferB); });
		partialBFreqs = GUI.multiSliderView.new(partialRow1, Rect.new(0, 0, 75, 0))
			.fillColor_(Color.red)
			.strokeColor_(Color.red)
			.background_(Color.black.alpha_(0.9))
			.indexThumbSize_(2.9)
			.valueThumbSize_(2.9)
			.value_(Array.fill(19, { 0.5 }))
			.mouseUpAction_({ |obj| this.generatePartials([partialBAmps.value, obj.value], bufferB); });

		// waveform edit
		waveformDraw = GUI.multiSliderView.new(win, Rect.new(0, 0, win.view.bounds.width * 0.85, 85))			.value_(Array.fill(256, {0.5}))
			.fillColor_(Color.white)
			.strokeColor_(Color.white)
			.background_(Color.black.alpha_(0.9))
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
		
		// bottom aux controls
		midiListMenu = ["<none>", "mod wheel", "aftertouch", "bend", "knob 1", "knob 2", "knob 3", "knob 4", "knob 5", "knob 6", "knob 7", "knob 8"];
		midiEnvListMenu = ["<none>", "envelope", "mod wheel", "aftertouch", "bend", "knob 1", "knob 2", "knob 3", "knob 4", "knob 5", "knob 6", "knob 7", "knob 8"];
		pr2AuxControls = GUI.hLayoutView.new(win, Rect.new(0, 0, win.view.bounds.width, 25))
			.background_(Color.blue(0.1, alpha:0.2));
		xFadeMenu = GUI.popUpMenu.new(pr2AuxControls, Rect.new(0, 0, 37.5, 0))
			.items_(midiListMenu);
		GUI.staticText.new(pr2AuxControls, Rect.new(0, 0, 37.5, 0));
		fbMulMenu = GUI.popUpMenu.new(pr2AuxControls, Rect.new(0, 0, 37.5, 0))
			.items_(midiEnvListMenu);
		freq2Menu = GUI.popUpMenu.new(pr2AuxControls, Rect.new(0, 0, 37.5, 0))
			.items_(midiEnvListMenu);
		fm2Menu = GUI.popUpMenu.new(pr2AuxControls, Rect.new(0, 0, 37.5, 0))
			.items_(midiEnvListMenu);
		syncModeMenu = GUI.popUpMenu.new(pr2AuxControls, Rect.new(0, 0, 110, 0))
			.items_(["no sync", "soft sync", "hard sync"]);

		
		// bottom control row
		partialRow2 = GUI.hLayoutView.new(win, Rect.new(0, 0, win.view.bounds.width, 75))
			.background_(Color.blue(0.1, alpha:0.2));
		xfadeKnob = EZJKnob.new(partialRow2, Rect.new(0, 0, 37.5, 73), "xfade")
			.knobColor_([Color.black, Color.green, Color.black, Color.green])
			.spec_('pan'.asSpec)
			.knobAction_({ |obj| this.setXFade(obj.value); })
			.knob.centered_(true);
		fbLagKnob = EZJKnob.new(partialRow2, Rect.new(0, 0, 37.5, 73), "fbLag")
			.knobColor_([Color.black, Color.green, Color.black, Color.green])
			.spec_([0.01, 4, 2].asSpec)
			.value_(0.3)
			.knobAction_({ |obj| this.setLag(obj.value); });
		fbMulKnob = EZJKnob.new(partialRow2, Rect.new(0, 0, 37.5, 73), "fbMul")
			.knobColor_([Color.black, Color.green, Color.black, Color.green])
			.spec_('midi'.asSpec)
			.knobAction_({ |obj| this.setFBMul(obj.value); });
		freq2Knob = EZJKnob.new(partialRow2, Rect.new(0, 0, 37.5, 73), "freq2")
			.knobColor_([Color.black, Color.green, Color.black, Color.green])
			.spec_([-127, 127].asSpec)
			.value_(0)
			.knobAction_({ |obj| this.setFreq2(obj.value); });
		fm2Knob = EZJKnob.new(partialRow2, Rect.new(0, 0, 37.5, 73), "fm")
			.knobColor_([Color.black, Color.green, Color.black, Color.green])			.spec_('midi'.asSpec)
			.knobAction_({ |obj| this.setFM2(obj.value); });
		envelopeView = GUI.envelopeView.new(partialRow2, Rect.new(0, 0, 100, 0))
			.value_([[0, 0.05, 0.15, 0.8, 1], [0, 1, 0.5, 0.65, 0]])
			.thumbSize_(3)
			.fillColor_(Color.green)
			.strokeColor_(Color.green)
			.background_(Color.black.alpha_(0.9))
			.drawLines_(true)
			.setEditable(0, false)
			.setEditable(4, false);

	}
}

