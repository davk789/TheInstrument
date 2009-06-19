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
		win.bounds.height = channels.size * 200;
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
		win = GUI.window.new("Sample Loopers", Rect.new(500.rand, 500.rand, 500, 200)).front;
		win.view.decorator = FlowLayout(win.view.bounds);
	}
}

SampleLooper {
	classvar <buffers, <groupNum=55;
	var parent, s, <nodeNum, params, paused=false;
	
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
		// initialization functions
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
		
	loadSynthDef { |numChannels=1|
		SynthDef.new( "SampleLooperPlayer", {
			arg bufnum, speed, start, end, outBus, inBus, delayTime=0.1, recordOffset, record, mix;
			
			var inPhase, outPhase, outSig, inSig, kNumFrames, sRecordHead;

			kNumFrames = BufFrames.kr(bufnum);
			
			inSig = In.ar(inBus * (mix - 1).abs) + (LocalIn.ar * mix);

			outPhase = Phasor.ar(speed, start * kNumFrames, end * kNumFrames);
			inPhase = (outPhase + (recordOffset * SampleRate.ir)) % kNumFrames;

			sRecordHead = Select.ar(record, [DC.ar(0), inPhase]);
			BufWr.ar(inSig, bufnum, sRecordHead);

			// numChannels needs to be hardwired to the SynthDef
			outSig = BufRd.ar(numChannels, bufnum, outPhase); 
			Out.ar(outBus, outSig);
			
			LocalOut.ar(outSig);
		}).load(s);
	}
	
	makeGUI { |container|
		var topView, presetRow, presetMenu, presetSaveButton, waveformControlView, waveformMarkerBar, waveformMarkerClearButton, waveformView, waveformViewVZoomView, waveformViewVZoom, waveformViewZoom, transportView, bufferView;
		topView = GUI.compositeView.new(container, Rect.new(0, 0, 600, 200));
		topView.decorator = FlowLayout(topView.bounds);
		
		presetRow = GUI.hLayoutView.new(topView, Rect.new(0, 0, topView.bounds.width, 25))
			.background_(Color.black);
		presetMenu = GUI.popUpMenu.new(presetRow, Rect.new(0, 0, 200, 0))
			.items_(["this will have the presets listed", "some day"]);
		presetSaveButton = GUI.button.new(presetRow, Rect.new(0, 0, 65, 0))
			.states_([["save preset", Color.black, Color.blue.alpha_(0.9)]]);
			
		waveformControlView = GUI.compositeView.new(topView, Rect.new(0, 0, topView.bounds.width, 200))
			.background_(Color.black);
		waveformControlView.decorator_(FlowLayout(waveformControlView.bounds));
		
		waveformMarkerBar = MarkerBar.new(waveformControlView, Rect.new(0, 0, 575, 20))
			.markerColor_(Color.yellow)
			.background_(Color.blue(0.5, alpha:0.9));
		waveformMarkerClearButton = GUI.button.new(waveformControlView, Rect.new(0, 0, 20, 20))
			.states_([["X", Color.black, Color.yellow]]);
		
		waveformView = GUI.soundFileView.new(waveformControlView, Rect.new(0, 0, 575, 100))
			.background_(Color.white.alpha_(0.3));

		waveformViewVZoomView = GUI.vLayoutView.new(waveformControlView, Rect.new(0, 0, 35, 100));
		waveformViewVZoom = GUI.slider.new(waveformViewVZoomView, Rect.new(0, 0, 0, 100));
		
		waveformViewZoom = GUI.rangeSlider.new(waveformControlView, Rect.new(0, 0, topView.bounds.width, 20))
			.knobColor_(Color.new255(109, 126, 143))
			.background_(Color.white.alpha_(0.3));
		
		transportView = GUI.compositeView.new(topView, Rect.new(0, 0, 290, 150))
			.background_(Color.black);
		transportView.decorator_(FlowLayout(transportView.bounds));

		bufferView = GUI.compositeView.new(topView, Rect.new(0, 0, 290, 150))
			.background_(Color.black);
		bufferView.decorator_(FlowLayout(bufferView.bounds));

		
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

