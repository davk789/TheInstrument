PolySynthControl {
	classvar  classGroup=102, bufferIndex=68;
	var activeNotes, win, instGroup=103, s, bufferA=70, bufferB=71,
		<>att=0.05, <>dec=0.02, <>sus=0.7, <>rel=0.4, 
		<>peakA=0.6, <>peakB=0.3, <>peakC=0.6, <>mul=4, <>feedback=0, touch=0, <>lag=0.1, 
		pitchBend=0, <>outBus=19, <recorderID="czSynth", 
		trigMode=0, xfade=0, fbLag=0, fbMul=0, freq2=0, fmAmt=0, fbMulEnvFlag=0, freq2EnvFlag=0, fmEnvFlag=0, envScale=1;
	// 	16 18 12 17 19 13 // transport cc
	// 72  8 74 71  20 22 86 73 //   cc numbers 
	*new { |name|
		bufferIndex = bufferIndex + 2;
		^super.new.init_polysynthcontrol(bufferIndex, name);
	}
	init_polysynthcontrol { |buf, name|
		s = Server.default;
		activeNotes = Dictionary.new;
		if(name.notNil){ recorderID = name; };
		bufferA = buf;
		bufferB = buf + 2;

		s.sendMsg('g_new', classGroup, 0, 1);
		s.sendMsg('b_alloc', bufferA, 1024);
		s.sendMsg('b_gen', bufferA, 'sine2', 5, 1, 1);
		s.sendMsg('b_alloc', bufferB, 1024);
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
//		^~eventLooper.channels[recorderID];
//
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
	setSynthPreset { |menu|
		postln("this is where the SynthControl class will switch synth modes.\n" ++ menu);
	}
	generatePartials { |freqs, amps, buffer|
		var sFrequency, sAmplitude;
		sFrequency = amps * Array.series(19, 2, 2);
		sAmplitude = amps.pow(3);
		s.listSendMsg(['b_gen', buffer, 'sine2', 5] ++ [sAmplitude, sFrequency].lace(38));
	}
	drawWaveform { |sliders, index, buffer|
		s.sendMsg('b_setn', buffer, index * 4, 4, sliders[index], sliders[index], sliders[index], sliders[index]);
	}
	setSyncMode { |flag|
		trigMode = flag;
		s.sendMsg('n_set', instGroup, 'trigMode', trigMode);
	}
	setXFade { |amt|
		xfade = amt;
		s.sendMsg('n_set', instGroup, 'xfade', xfade);
	}
	setFBLag { |amt|
		fbLag = amt;
		s.sendMsg('n_set', instGroup, 'fbLag', fbLag);
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
		fmAmt = fm;
		s.sendMsg('n_set', instGroup, 'fmAmt', fmAmt);
	}
	setFBMulEnvFlag { |fb|
		fbMulEnvFlag = fb;
		s.sendMsg('n_set', instGroup, 'fbMulEnvFlag', fbMulEnvFlag);
	}
	setFreq2EnvFlag { |frq|
		freq2EnvFlag = frq;
		s.sendMsg('n_set', instGroup, 'freq2EnvFlag', freq2EnvFlag);
	}
	setFM2EnvFlag { |fm|
		fmEnvFlag = fm;
		s.sendMsg('n_set', instGroup, 'fmEnvFlag', fmEnvFlag);
	}
	setEnvScale { |scale|
		envScale = scale;
		s.sendMsg('n_set', instGroup, 'envScale', envScale);	
	}
	setEnvelope { |env|
		att = (env[0][1] - env[0][0]);
		dec = (env[0][2] - env[0][1]);
		sus = (env[0][3] - env[0][2]);
		rel = (env[0][4] - env[0][3]);
		peakA = env[1][1];
		peakB = env[1][2];
		peakC = env[1][3];
		s.sendMsg('n_set', instGroup, 'att', att, 'dec', dec, 'sus', sus, 'rel', rel, 'peakA', peakA, 'peakB', peakB, 'peakC', peakC);
	}
	addMixerChannel {
/*		~mixer.addMonoChannel("fakeCZSynth", ~mixer.mixGroup);
		outBus = ~mixer.channels["fakeCZSynth"].inBus;
*/	}
	noteOn { |src,chan,num,vel|
		activeNotes = activeNotes.add(num -> s.nextNodeID);
		s.sendMsg('s_new', 's_dualWavetable', activeNotes[num], 0, instGroup,
			'outBus', outBus, 
			'peakA', peakA, 'peakB', peakB, 'peakC', peakC, 
			'att', att, 'dec', dec, 'sus', sus, 'rel', rel, 
			'trigMode', trigMode, 'xfade', xfade, 
			'fbLag', fbLag, 'fbMul', fbMul, 'freq2', freq2, 'fmAmt', fmAmt, 
			'fbMulEnvFlag', fbMulEnvFlag, 'freq2EnvFlag', freq2EnvFlag, 'fmEnvFlag', fmEnvFlag, 
			'envScale', envScale);
		s.sendMsg('n_set', activeNotes[num], 'gate', 1);
	}
	noteOff { |src,chan,num,vel|
		s.sendMsg('n_set', activeNotes[num], 'gate', 0);
		activeNotes.removeAt(num);
	}
	bend { |src,chan,val|
		pitchBend = val / 8192 - 1;
		s.sendMsg('n_set', instGroup, 'bend', pitchBend);
	}
	afterTouch { |src,chan,val|
		feedback = val / 20;
		s.sendMsg('n_set', instGroup, 'feedback', feedback);
	}
	modWheel { |src,chan,val|
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
		var modeRow, modeMenu, xfadeKnob, fbLagKnob, fbMulKnob, partialRow1, partialAAmps, partialAFreqs, midiListMenu, pr2AuxControls, xFadeMenu, fbMulMenu, freq2Menu, fm2Menu, partialRow2, freq2Knob, fm2Knob, syncModeMenu, partialBAmps, partialBFreqs, envelopeView, waveformDraw, targetColumn, targetAButton, targetBButton, pr2EnvRow, fbMulEnvButton, freq2EnvButton, fm2EnvButton, envScaleSlider, envScaleSpec;
		win = GUI.window.new("organum", Rect.new(50,300, 400, 360)).front;
		win.view.decorator = FlowLayout(win.view.bounds);
		
		modeRow = GUI.hLayoutView.new(win, Rect.new(0, 0, win.view.bounds.width, 20))
			.background_(Color.blue(0.1, alpha:0.2));
		GUI.staticText.new(modeRow, Rect.new(0, 0, win.view.bounds.width * 0.24, 0))
			.string_("Synth:");
		modeMenu = GUI.popUpMenu.new(modeRow, Rect.new(0, 0, win.view.bounds.width * 0.74, 0))
			.items_(["czFakeRez", "dualWavetable"])
			.action_({ |obj| this.setSynthPreset(obj); });
			
		// controls row 1
		partialRow1 = GUI.hLayoutView.new(win, Rect.new(0, 0, win.view.bounds.width, 75))
			.background_(Color.blue(0.1, alpha:0.2));
		GUI.staticText.new(partialRow1, Rect.new(0, 0, 11, 0))
			.string_("A")
			.align_('center')
			.background_(Color.black.alpha_(0.9))
			.stringColor_(Color.white);
		partialAAmps = GUI.multiSliderView.new(partialRow1, Rect.new(0, 0, 85, 0))
			.fillColor_(Color.blue)
			.strokeColor_(Color.blue)
			.indexThumbSize_(3.4)
			.background_(Color.black.alpha_(0.9))
			.valueThumbSize_(3.4)
			.value_([1,0,0,0, 0,0,0,0, 0,0,0,0, 0,0,0,0, 0,0,0])
			.isFilled_(true)
			.action_({ |obj| this.generatePartials(partialAFreqs.value, obj.value, bufferA) });
		partialAFreqs = GUI.multiSliderView.new(partialRow1, Rect.new(0, 0, 85, 0))
			.fillColor_(Color.red)
			.strokeColor_(Color.red)
			.indexThumbSize_(3.4)
			.background_(Color.black.alpha_(0.9))
			.valueThumbSize_(3.4)
			.value_(Array.fill(19, { 0.5 }))
			.action_({ |obj| this.generatePartials(obj.value, partialAAmps.value, bufferA) });
		GUI.staticText.new(partialRow1, Rect.new(0, 0, 11, 0))
			.string_("B")
			.align_('center')
			.background_(Color.black.alpha_(0.9))
			.stringColor_(Color.white);
		partialBAmps = GUI.multiSliderView.new(partialRow1, Rect.new(0, 0, 85, 0))
			.fillColor_(Color.blue)
			.strokeColor_(Color.blue)
			.background_(Color.black.alpha_(0.9))
			.indexThumbSize_(3.4)
			.valueThumbSize_(3.4)
			.value_([1,0,0,0, 0,0,0,0, 0,0,0,0, 0,0,0,0, 0,0,0])
			.isFilled_(true)
			.action_({ |obj| this.generatePartials(partialBFreqs.value, obj.value, bufferB) });
		partialBFreqs = GUI.multiSliderView.new(partialRow1, Rect.new(0, 0, 85, 0))
			.fillColor_(Color.red)
			.strokeColor_(Color.red)
			.background_(Color.black.alpha_(0.9))
			.indexThumbSize_(3.4)
			.valueThumbSize_(3.4)
			.value_(Array.fill(19, { 0.5 }))
			.action_({ |obj| this.generatePartials(obj.value, partialBAmps.value, bufferB) });

		// waveform edit
		waveformDraw = GUI.multiSliderView.new(win, Rect.new(0, 0, win.view.bounds.width * 0.85, 85))			.value_(Array.fill(256, {0.5}))
			.fillColor_(Color.white)
			.strokeColor_(Color.white)
			.background_(Color.black.alpha_(0.9))
			.indexThumbSize_(0.32)
			.valueThumbSize_(3)
			.isFilled_(false)
			.canFocus_(false)
			.action_({ |obj| 
				if( targetAButton.value == 1 ){
					this.drawWaveform(obj.value, obj.index, bufferA);
				};
				if( targetBButton.value == 1 ){
					this.drawWaveform(obj.value, obj.index, bufferB);
				};
			});
		targetColumn = GUI.vLayoutView.new(win, Rect.new(0, 0, win.view.bounds.width * 0.12, 85))
			.background_(Color.blue(0.1, alpha:0.2));
		targetAButton = GUI.button.new(targetColumn, Rect.new(0, 0, 0, targetColumn.bounds.height * 0.42))
			.states_([["A", Color.black, Color.clear],["A", Color.red, Color.yellow]]);
		targetBButton = GUI.button.new(targetColumn, Rect.new(0, 0, 0, targetColumn.bounds.height * 0.42))
			.states_([["B", Color.black, Color.clear],["B", Color.red, Color.yellow]]);
		
		// bottom aux controls
		midiListMenu = ["<none>", "mod wheel", "aftertouch", "bend", "knob 1", "knob 2", "knob 3", "knob 4", "knob 5", "knob 6", "knob 7", "knob 8"];
		pr2AuxControls = GUI.hLayoutView.new(win, Rect.new(0, 0, win.view.bounds.width, 25))
			.background_(Color.blue(0.1, alpha:0.2));
		xFadeMenu = GUI.popUpMenu.new(pr2AuxControls, Rect.new(0, 0, 37.5, 0))
			.items_(midiListMenu);
		GUI.staticText.new(pr2AuxControls, Rect.new(0, 0, 37.5, 0));
		fbMulMenu = GUI.popUpMenu.new(pr2AuxControls, Rect.new(0, 0, 37.5, 0))
			.items_(midiListMenu);
		freq2Menu = GUI.popUpMenu.new(pr2AuxControls, Rect.new(0, 0, 37.5, 0))
			.items_(midiListMenu);
		fm2Menu = GUI.popUpMenu.new(pr2AuxControls, Rect.new(0, 0, 37.5, 0))
			.items_(midiListMenu);
		syncModeMenu = GUI.popUpMenu.new(pr2AuxControls, Rect.new(0, 0, 110, 0))
			.items_(["no sync", "soft sync", "hard sync"])
			.action_({ |obj| this.setSyncMode(obj.value); });

		
		// bottom control row
		partialRow2 = GUI.hLayoutView.new(win, Rect.new(0, 0, win.view.bounds.width, 75))
			.background_(Color.blue(0.1, alpha:0.2));
		xfadeKnob = EZJKnob.new(partialRow2, Rect.new(0, 0, 37.5, 73), "xfade")
			.knobColor_([Color.black, Color.green, Color.black, Color.green])
			.value_(0.5)
			.knobAction_({ |obj| this.setXFade(obj.value); })
			.knob.centered_(true);
		fbLagKnob = EZJKnob.new(partialRow2, Rect.new(0, 0, 37.5, 73), "fbLag")
			.spec_([0.001, 4, 2.3].asSpec)
			.value_(0.3)
			.knobColor_([Color.black, Color.green, Color.black, Color.green])
			.knobAction_({ |obj| this.setFBLag(obj.value); });
		fbMulKnob = EZJKnob.new(partialRow2, Rect.new(0, 0, 37.5, 73), "fbMul")
			.spec_('midi'.asSpec)
			.knobColor_([Color.black, Color.green, Color.black, Color.green])
			.knobAction_({ |obj| this.setFBMul(obj.value); });
		freq2Knob = EZJKnob.new(partialRow2, Rect.new(0, 0, 37.5, 73), "freq2")
			.spec_([-127, 127].asSpec)
			.knobColor_([Color.black, Color.green, Color.black, Color.green])
			.knobAction_({ |obj| this.setFreq2(obj.value); })
			.knob.centered_(true);
		fm2Knob = EZJKnob.new(partialRow2, Rect.new(0, 0, 37.5, 73), "fm")
			.spec_('midi'.asSpec)
			.knobColor_([Color.black, Color.green, Color.black, Color.green])
			.knobAction_({ |obj| this.setFM2(obj.value); });
		envelopeView = GUI.envelopeView.new(partialRow2, Rect.new(0, 0, 150, 0))
			.value_([[0, 0.05, 0.15, 0.8, 1], [0, 1, 0.5, 0.65, 0]])
			.thumbSize_(3)
			.fillColor_(Color.green)
			.strokeColor_(Color.green)
			.background_(Color.black.alpha_(0.9))
			.drawLines_(true)
			.setEditable(0, false)
			.setEditable(4, false)
			.action_({ |obj| this.setEnvelope(obj.value); });

		// bottom env buttons
		pr2EnvRow = GUI.hLayoutView.new(win, Rect.new(0, 0, win.view.bounds.width, 25))
			.background_(Color.blue(0.1, alpha:0.2));
		GUI.staticText.new(pr2EnvRow, Rect.new(0, 0, 34, 0));
		GUI.staticText.new(pr2EnvRow, Rect.new(0, 0, 34, 0));
		fbMulEnvButton = GUI.button.new(pr2EnvRow, Rect.new(0, 0, 34, 0))
			.states_([["env", Color.black, Color.clear],["env", Color.red, Color.yellow]])
			.action_({|obj| this.setFBMulEnvFlag(obj.value) });
		freq2EnvButton = GUI.button.new(pr2EnvRow, Rect.new(0, 0, 34, 0))
			.states_([["env", Color.black, Color.clear],["env", Color.red, Color.yellow]])
			.action_({|obj| this.setFreq2EnvFlag(obj.value) });
		fm2EnvButton = GUI.button.new(pr2EnvRow, Rect.new(0, 0, 34, 0))
			.states_([["env", Color.black, Color.clear],["env", Color.red, Color.yellow]])
			.action_({|obj| this.setFM2EnvFlag(obj.value) });
		envScaleSpec = [0.2, 10, 2.2].asSpec;
		envScaleSlider = GUI.slider.new(pr2EnvRow, Rect.new(0, 0, 150, 0))
			.background_(Color.black.alpha_(0.9))
			.value_(envScaleSpec.unmap(1))
			.action_({ |obj| this.setEnvScale(envScaleSpec.map(obj.value)); });
	}

}

