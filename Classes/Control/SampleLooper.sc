// starting again from scratch here
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
			'bufnum'    -> buffers[0].bufnum, 
			'speed'     -> 1,
			'start'     -> 0, 
			'end'       -> 1, 
			'outBus'    -> 22, 
			'inBus'     -> 20, 
			'delayTime' -> 0.1
		];
		// 
	}
	
	addMixerChannel {
		parent.mixer.addMonoChannel("SampleLooper");
		params['outBus'] = parent.mixer.channels["SampleLooper"].inBus;
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
			arg bufnum, speed, start, end, outBus, inBus, delayTime=0.1;
			
			var inPhase, outPhase, outSig, inSig;
			
			inPhase = Phasor.ar(speed, start * BufFrames.kr(bufnum), end * BufFrames.kr(bufnum));
			outPhase = DelayC.ar(inPhase, 1, delayTime); // max 1 sec delay
			BufWr.ar(inSig, bufnum, inPhase);
			outSig = BufRd.ar(numChannels, bufnum, outPhase); // numChannels needs to be hardwired to the SynthDef
			Out.ar(outBus, outSig)
		}).load(s);
	}
	
}

SampleLooper_old {
	var win, parent, buffers, s, sampleView, markerBar, zoomSlider, soundFile;
	*new { |par|
		^super.new.init_samplelooper(par);
	}
	init_samplelooper { |par|
		parent = par;
		soundFile = SoundFile.new;
		s = Server.default;
		//this.initGUI;
		//this.loadSoundFile;
	}
	
	loadBufferFromFile { |file|
		s.sendMsg('b_allocRead', s.nextNodeID, file);
	}
	
	loadSoundFile {
		soundFile.openRead("/Applications/SC3/sounds/a11wlk01.wav");
		sampleView.soundfile = soundFile;
		sampleView.read(0, soundFile.numFrames);
	}
	
	zoomView { |lo,hi,range|
		if(range.isNil){ range = hi - lo; };
		sampleView.zoomToFrac(range.max( sampleView.bounds.width / sampleView.numFrames.max( 1 )));
		if(range < 1){
			sampleView.scrollTo(lo / (1 - range));
		};
		markerBar.zoom(lo, hi);
	}
	
	initGUI {
		var winWidth, sampleViewRow, vZoomSlider;
		win = GUI.window.new("Sample Looper", Rect.new(50, 500, 600, 200))
			.front;
		win.view.background = Color.black;
		win.view.decorator = FlowLayout(win.view.bounds);
		winWidth = win.view.bounds.width - 45;
		markerBar = MarkerBar.new(win, Rect.new(0, 0, winWidth, 20))
			.markerColor_(Color.yellow)
			.background_(Color.white.alpha_(0.3));
		sampleViewRow = GUI.hLayoutView.new(win, Rect.new(0, 0, winWidth + 45, 100));
		sampleView = GUI.soundFileView.new(sampleViewRow, Rect.new(0, 0, winWidth, 0))
			.background_(Color.white.alpha_(0.3));
		vZoomSlider = GUI.vLayoutView.new(sampleViewRow, Rect.new(0, 0, 35, 0));
		GUI.slider.new(vZoomSlider, Rect.new(0, 0, 0, 100));
		zoomSlider = GUI.rangeSlider.new(win, Rect.new(0, 0, winWidth, 20))
			.knobColor_(Color.new255(109, 126, 143))
			.background_(Color.white.alpha_(0.3))
			.action_({ |obj| this.zoomView(obj.lo, obj.hi, obj.range); });
	}
}


       