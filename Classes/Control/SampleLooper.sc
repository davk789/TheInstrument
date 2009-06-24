Sampler {
	var parent, <>win, <>channels, <outBus;
	*new { |env, loopers|
		^super.new.init_sampler(env, loopers);
	}
	
	init_sampler { |env, loopers=1|
		parent = env;
		channels = Array.new;
		
		this.initGUI;
		loopers.do{ |ind|
			this.addChannel;
		};
	}
	
	addChannel {
		channels = channels.add(SampleLooper.new(parent));
		channels.last.makeGUI(win);
		win.bounds = Rect.new(win.bounds.left, win.bounds.top, 600, channels.size * 385);
	}
	
	addMixerChannel {
		parent.mixer.addMonoChannel("Sampler", 0, true);
		// keeping a member variable to describe the outbus that the channels share. 
		outBus = parent.mixer.channels["Sampler"].inBus;
		channels.do{ |obj,ind|
			obj.outBus = outBus;
		}
	}
	
	initGUI {
		win = GUI.window.new("Sample Loopers", Rect.new(500.rand, 500.rand, 600, 385)).front;
		win.view.decorator = FlowLayout(win.view.bounds);
	}
}

SampleLooper {
	classvar <buffers, <groupNum=55;
	var parent, s, <nodeNum, params, paused=false, activeBufferIndex=0;
	// GUI objects
	var controlBackgroundColor, topView, presetRow, presetMenu, presetSaveButton, waveformControlView, waveformMarkerBar, waveformMarkerClearButton, waveformView, waveformViewVZoomView, waveformViewVZoom, waveformViewZoom, transportView, bufferView, recordButton, playButton, pauseButton, stopButton, playbackSpeedSlider, addFileButton, clearBufferButton, addEmptyBufferBox, addEmptyBufferButton, bufferSelectMenu, inputLevelSlider, outputLevelSlider;

	
	*new { |par|
		^super.new.init_samplelooper(par);
	}
	
	init_samplelooper { |par|
		parent  = par;
		s       = Server.default;
		nodeNum = s.nextNodeID;
		buffers = Array.new;
		// synth parameters should be kept in a dict
		// for all classes, in general. I could add getter/setter
		// methods if necessary
		params  = Dictionary[
			'bufnum'      -> -1, 
			'speed'       -> 1,
			'start'       -> 0, 
			'end'         -> 1, 
			'outBus'      -> 0, 
			'inBus'       -> 20, 
			'delayTime'   -> 0.1,
			'reordOffset' -> 0.01,
			'record'      -> 0,
			'mix'         -> 0
		];

	}
		
	addBuffer { |length=16|
		buffers = buffers.add(Buffer.alloc(s, length * s.sampleRate));
	}
	
	addSoundFile { |filename|
		if(filename.notNil){
			buffers = buffers.add(Buffer.read(s, filename));
		}{
			buffers = buffers.add(Buffer.loadDialog(s));
		};
	}
	
	start {
		s.sendMsg('s_new', 'SampleLooperPlayer', 1, 0, nodeNum);
		s.listSendMsg(['n_set', nodeNum] ++ params.getPairs);
	}

	pause {
		if(paused.not){
			s.sendMsg('n_set', nodeNum, 'speed', 0);
			paused = true;
		}{
			s.sendMsg('n_set', nodeNum, 'speed', params['speed']);
			paused = false;
		};
	}
	
	stop {
		s.sendMsg('n_free', nodeNum);
	}
	
	addBufferFromFile {
		Dialog.getPaths({ |paths|
			paths.do{ |obj,ind|
				buffers = buffers.add(Buffer.read(s, obj));
				if(ind == paths.lastIndex){
					// just updating the GUI "later"
					AppClock.sched(1, {this.updateBufferMenu; nil; });
				};
			};
		});
	}
	
	addEmptyBuffer { |length|
		// only can add empty mono buffers for now.
		// multi-channel support should come later.
		buffers = buffers.add(Buffer.alloc(s, length * s.sampleRate, 1));
		AppClock.sched(1, {this.updateBufferMenu; nil;});
	}
	
	updateBufferMenu {
		var ret;
		ret = Array.new;
		buffers.do{ |obj,ind|
			if(obj.path.notNil){
				ret = ret.add(obj.path.basename);
			}{
				ret = ret.add(obj.bufnum.asString);
			}
		};
		bufferSelectMenu.items = ret;
	}

	setActiveBuffer { |sel|
		/* this will screw up if the indexes of the buffers array do not match with the 
			bufferSelectMenu.items. This might not be a problem though. */
		activeBufferIndex = sel;
		
	}
	
	loadSynthDef { |numChannels=1|
		SynthDef.new( "SampleLooperPlayer", {
			arg bufnum, speed, start, end, outBus, inBus, delayTime=0.1, recordOffset, record, mix;
			
			var inPhase, outPhase, outSig, inSig, kNumFrames, sRecordHead;

			kNumFrames = BufFrames.kr(bufnum);
			
			inSig = In.ar(inBus * (mix - 1).abs) + (LocalIn.ar * mix);

			outPhase = Phasor.ar(speed, start * kNumFrames, end * kNumFrames);
			inPhase = (outPhase + (recordOffset * SampleRate.ir)) % kNumFrames;

			sRecordHead = Select.ar(record, [DC.ar(0), inPhase]);
			BufWr.ar(inSig.softclip, bufnum, sRecordHead);

			// numChannels needs to be hardwired to the SynthDef
			outSig = BufRd.ar(numChannels, bufnum, outPhase);
			Out.ar(outBus, outSig);
			
			LocalOut.ar(outSig);
		}).load(s);
	}
	
	makeGUI { |container|
		
		controlBackgroundColor = Color.grey(0.3);
		
		topView = GUI.compositeView.new(container, Rect.new(0, 0, 600, 345));
		topView.decorator = FlowLayout(topView.bounds);
		
		presetRow = GUI.hLayoutView.new(topView, Rect.new(0, 0, topView.bounds.width, 25))
			.background_(Color.black);
		presetMenu = GUI.popUpMenu.new(presetRow, Rect.new(0, 0, 200, 0))
			.items_(["this will have the presets listed", "some day"])
			.background_(Color.blue.alpha_(0.2))
		    .font_(parent.controlFont)
			.stringColor_(Color.white);
		presetSaveButton = GUI.button.new(presetRow, Rect.new(0, 0, 85, 0))
		    .font_(parent.controlFont)
			.states_([["save preset", Color.white, Color.blue.alpha_(0.2)]]);
			
		waveformControlView = GUI.compositeView.new(topView, Rect.new(0, 0, topView.bounds.width, 180))
			.background_(Color.black);

		waveformControlView.decorator_(FlowLayout(waveformControlView.bounds));
		
		waveformMarkerBar = MarkerBar.new(waveformControlView, Rect.new(0, 0, 565, 20))
			.markerColor_(Color.yellow)
			.background_(Color.blue(0.5, alpha:0.9))
			.mouseDownAction_({|obj,x,y,mod| [obj,x,y,mod].postln; });
		waveformMarkerClearButton = GUI.button.new(waveformControlView, Rect.new(0, 0, 20, 20))
			.states_([["X", Color.black, Color.yellow]])
		    .font_(parent.controlFont);
		
		waveformView = GUI.soundFileView.new(waveformControlView, Rect.new(0, 0, 565, 125))
			.background_(Color.white.alpha_(0.3));
		

		waveformViewVZoom = GUI.slider.new(waveformControlView, Rect.new(0, 0, 20, 125));
		
		waveformViewZoom = GUI.rangeSlider.new(waveformControlView, Rect.new(0, 0, 565, 20))
			.knobColor_(Color.new255(109, 126, 143))
			.background_(Color.white.alpha_(0.3));
		
		// transport section
		transportView = GUI.compositeView.new(topView, Rect.new(0, 0, 290, 118))
			.background_(Color.black);
		transportView.decorator_(FlowLayout(transportView.bounds));
		
		recordButton = GUI.button.new(transportView, Rect.new(0, 0, 67, 25))
			.states_([["o", Color.red, Color.new255(25,25,25)]])
		    .font_(parent.strongFont);
		playButton = GUI.button.new(transportView, Rect.new(0, 0, 67, 25))
			.states_([[">", Color.green, controlBackgroundColor]])
		    .font_(parent.strongFont);
		pauseButton = GUI.button.new(transportView, Rect.new(0, 0, 67, 25))
			.states_([["||", Color.yellow, controlBackgroundColor]])
		    .font_(parent.strongFont);
		stopButton = GUI.button.new(transportView, Rect.new(0, 0, 67, 25))
			.states_([["[]", Color.white(0.8), controlBackgroundColor]])
		    .font_(parent.strongFont);
		

		playbackSpeedSlider = EZSlider.new(transportView, Rect.new(0, 0, 280, 20))
		    .font_(parent.controlFont)
		    .setColors(
		    	sliderBackground:controlBackgroundColor,
		    	knobColor:HiliteGradient.new(controlBackgroundColor, Color.white, \v, 64, 0.5)
		    );

		inputLevelSlider = EZSlider.new(transportView, Rect.new(0, 0, 280, 20))
		    .font_(parent.controlFont)
		    .setColors(
		    	sliderBackground:Color.green(0.3),
		    	knobColor:HiliteGradient.new(Color.red, Color.white, \v, 64, 0.5)
		    );
		outputLevelSlider = EZSlider.new(transportView, Rect.new(0, 0, 280, 20))
		    .font_(parent.controlFont)
		    .setColors(
		    	sliderBackground:Color.green(0.3),
		    	knobColor:HiliteGradient.new(Color.red, Color.white, \v, 64, 0.5)
		    );
		
		// buffer control section
		bufferView = GUI.compositeView.new(topView, Rect.new(0, 0, 290, 118))
			.background_(Color.black);
		bufferView.decorator_(FlowLayout(bufferView.bounds));
		
		clearBufferButton = GUI.button.new(bufferView, Rect.new(0, 0, 75, 25))
			.states_([["clear buffer", Color.white, controlBackgroundColor]])
		    .font_(parent.controlFont);
		
		
		addFileButton = GUI.button.new(bufferView, Rect.new(0, 0, 75, 25))
			.states_([["add file(s)", Color.white, controlBackgroundColor]])
		    .font_(parent.controlFont)
		    .action_({ |obj| this.addBufferFromFile; });
		
		addEmptyBufferBox = GUI.numberBox.new(bufferView, Rect.new(0, 0, 30, 25))
		    .font_(parent.controlFont)
		    .value_(16)
		    .action_({ |obj| this.addEmptyBuffer(obj.string.interpret) });
		
		addEmptyBufferButton = GUI.button.new(bufferView, Rect.new(0, 0, 90, 25))
			.states_([["add empty buffer", Color.white, controlBackgroundColor]])
		    .font_(parent.controlFont)
		    .action_({ |obj| this.addEmptyBuffer(addEmptyBufferBox.string.interpret) });
		
		bufferSelectMenu = GUI.popUpMenu.new(bufferView, Rect.new(0, 0, 280, 25))
			.background_(controlBackgroundColor)
			.stringColor_(Color.white)
		    .font_(parent.controlFont)
		    .action_({ |obj| this.setActiveBuffer(obj.value); });

		
		//var controlView, viewWidth, sampleViewRow, vZoomSlider, offset, markerBar, sampleView, zoomSlider;
		//controlView = GUI.compositeView.new(container, Rect.new(0, 0, 600, 200))
		//	.background_(Color.black);
		//controlView.decorator_(FlowLayout(controlView.bounds));
		
		/*
		viewWidth = view.view.bounds.width - 45;
		markerBar = MarkerBar.new(controlView, Rect.new(0, 0, viewWidth, 20))
			.markerColor_(Color.yellow)
			.background_(Color.white.alpha_(0.3));
		sampleViewRow = GUI.hLayoutView.new(controlView, Rect.new(0, 0, viewWidth + 45, 100));
		sampleView = GUI.soundFileView.new(sampleViewRow, Rect.new(0, 0, viewWidth, 0))
			.background_(Color.white.alpha_(0.3));
		vZoomSlider = GUI.vLayoutView.new(sampleViewRow, Rect.new(0, 0, 35, 0));
		GUI.slider.new(vZoomSlider, Rect.new(0, 0, 0, 100));
		zoomSlider = GUI.rangeSlider.new(controlView, Rect.new(0, 0, viewWidth, 20))
			.knobColor_(Color.new255(109, 126, 143))
			.background_(Color.white.alpha_(0.3))
			.action_({ |obj| this.zoomView(obj.lo, obj.hi, obj.range); });
		*/
	}
	
}

