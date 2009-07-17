Sampler { // container for one or more SampleLoopers
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
		this.addMixerChannel;
	}
	
	addChannel {
		channels = channels.add(SampleLooper.new(parent));
		channels.last.makeGUI(win);
		win.bounds = Rect.new(win.bounds.left, win.bounds.top, 830, channels.size * 270);
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
		win = GUI.window.new("Sample Loopers", Rect.new(500.rand, 500.rand, 830, 270)).front;
		win.view.decorator = FlowLayout(win.view.bounds);
	}
}

SampleLooper {
	classvar <buffers, <groupNum=55;
	var parent, s, <playerNodeNum, <recorderNodeNum, playerParams, recorderParams, paused=false, activeBufferIndex=0, currentBufferArray, currBufDisplayStart, currBufDisplayEnd, waveformVZoomSpec, waveformDisplayResolution=4096, isRecording=false, loopMarkers, isPlaying=false, isRecording=false;
	// GUI objects
	var controlBackgroundColor, topView, waveformColumn, transportRow, controlColumn, presetRow, bufferRow, presetMenu, presetSaveButton, waveformControlView, /*!!!*/<>waveformMarkerBar, waveformMarkerClearButton, waveformView, waveformViewVZoomView, waveformViewVZoom, waveformViewZoom, controlView, recordButton, backButton, playButton, forwardButton, pauseButton, stopButton, playbackSpeedKnob, addFileButton, clearBufferButton, addEmptyBufferBox, addEmptyBufferButton, bufferSelectMenu, modSourceMenu, modLevelKnob, modLagKnob, speedKnob, gainKnob, inputSourceMenu, inputLevelKnob, syncOffsetKnob, recordModeButton, recordOffsetKnob;

	
	*new { |par|
		^super.new.init_samplelooper(par);
	}
	
	init_samplelooper { |par|
		parent  = par;
		s       = Server.default;
		playerNodeNum = s.nextNodeID;
		recorderNodeNum = s.nextNodeID;
		buffers = Array.new;
		waveformVZoomSpec = [1,10].asSpec;
		playerParams  = Dictionary[
			'bufnum'       -> -1, 
			'speed'        -> 1, 
			'start'        -> 0, 
			'end'          -> 1, 
			'outBus'       -> 0, 
			'trig'         -> 1, 
			'resetPos'     -> 0, 
			'recordTrig'   -> 0,
			'modBus'       -> 20, 
			'modLag'       -> 0.2, 
			'modLev'       -> 0,
			'inBus'        -> 1, 
			'recordOffset' -> 0.1, 
		];
		recorderParams = Dictionary[
			'bufnum' -> -1, 
			'trig'   -> 0, 
			'start'  -> 0, 
			'end'    -> 1,
			'inBus'  -> 20, 
			'mix'    -> 0
		];
		currentBufferArray = Array.fill(waveformDisplayResolution, { 0.5 });
		loopMarkers = Array.new;
		s.sendMsg('g_new', groupNum, 0, 1);

	}
	
	outBus {
		^playerParams['outBus'];
	}
	
	outBus_ { |val|
		playerParams['outBus'] = val;
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
	
	record { |val|
		isRecording = val;
		if(val){
			s.listSendMsg(['s_new', 'SampleLooperRecorder', recorderNodeNum, 1, groupNum] ++ recorderParams.getPairs);
		}{
			s.sendMsg('n_free', recorderNodeNum);
			this.drawWaveformView;
		};
	}
	
	forward {
		var lo,hi;
		lo = waveformMarkerBar.highlightRange['low'] + 1;
		hi = waveformMarkerBar.highlightRange['high'] + 1;
		waveformMarkerBar.setHighlightRange(lo,hi);
		this.setLoopPointParams;
	}
	
	play { |val|
		isPlaying = val;
		if(isPlaying){
			s.listSendMsg(['s_new', 'SampleLooperPlayer', playerNodeNum, 0, groupNum] ++ playerParams.getPairs);
		}{
			playButton.value_(1);
		};
	}
	
	back {
		var lo,hi;
		lo = waveformMarkerBar.highlightRange['low'] - 1;
		hi = waveformMarkerBar.highlightRange['high'] - 1;
		waveformMarkerBar.setHighlightRange(lo,hi);
		this.setLoopPointParams;
	}

	pause { |val|
		if(paused.not){
			s.sendMsg('n_set', playerNodeNum, 'speed', 0);
			paused = true;
		}{
			s.sendMsg('n_set', playerNodeNum, 'speed', playerParams['speed']);
			paused = false;
		};
	}
	
	stop {
		s.sendMsg('n_free', playerNodeNum);
		playButton.value_(0);
		if(recordButton.value == 1){
			this.drawWaveformView;
		};
		recordButton.value_(0);
	}
	
	addBufferFromFile {
		Dialog.getPaths({ |paths|
			paths.do{ |obj,ind|
				buffers = buffers.add(Buffer.read(s, obj));
				if(ind == paths.lastIndex){
					// just updating the GUI "later"
					// seems to fail with large buffers
					AppClock.sched(1, {this.updateBufferMenu; nil; });
				};
			};
		});
	}
	
	addEmptyBuffer { |length|
		// can only add empty mono buffers for now.
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
		activeBufferIndex = sel;

		this.loadSynthDef(buffers[activeBufferIndex].numChannels);
		playerParams['bufnum'] = buffers[activeBufferIndex];
		recorderParams['bufnum'] = buffers[activeBufferIndex];
		s.sendMsg('n_set', playerNodeNum, 'bufnum', playerParams['bufnum']);
		s.sendMsg('n_set', recorderNodeNum, 'bufnum', recorderParams['bufnum']);
		this.drawWaveformView;
	}
	
	drawWaveformView {
		var ret, scale, lastIndex;
		bufferSelectMenu.enabled_(false);
		lastIndex = waveformDisplayResolution - 1;
		scale = (buffers[activeBufferIndex].numFrames / waveformDisplayResolution).asInt;
		Task.new({
			waveformDisplayResolution.do{ |ind|
				var bufferIndex;
				bufferIndex = ind * scale * buffers[activeBufferIndex].numChannels;
				buffers[activeBufferIndex].get(bufferIndex, { |msg|
					currentBufferArray[ind] = (msg * 0.5) + 0.5;
				});
				0.0005.wait;
			};

			currBufDisplayStart = 0;
			currBufDisplayEnd = currentBufferArray.size;

			defer{
				waveformView.value_(currentBufferArray);
				waveformViewZoom.lo_(0).hi_(1);
				waveformViewVZoom.value_(0);
				waveformMarkerBar.clear;
				waveformMarkerBar.zoom(0, 1);
				bufferSelectMenu.enabled_(true);
			};
		}).play;
	}
	
	setWaveformVZoom { |amt|
		var scaleData;
		scaleData = ((currentBufferArray - 0.5) * amt) + 0.5;
		waveformView.value = scaleData[currBufDisplayStart..currBufDisplayEnd];
	}
		
	setWaveformZoom { |start,range|
		currBufDisplayStart = (start * currentBufferArray.size).asInt;
		currBufDisplayEnd = (range * currentBufferArray.size).asInt;
		waveformView.value = currentBufferArray[currBufDisplayStart..currBufDisplayEnd];
		waveformMarkerBar.zoom(start, range);
		this.setWaveformVZoom(waveformVZoomSpec.map(waveformViewVZoom.value));
	}
	
	clearActiveBuffer {
		buffers[activeBufferIndex].zero;
		this.drawWaveformView;
	}
	
	setLoopRange { |start, end|
		var startMarker, endMarker;
		
		startMarker = waveformMarkerBar.getIndexForLocation(start);
		endMarker = waveformMarkerBar.getIndexForLocation(end) + 1;
		
		waveformMarkerBar.setHighlightRange(startMarker, endMarker);
		
		this.setLoopPointParams(start);
		
	}
	
	setLoopPointParams { |start|
		var highlightCoords, startPoint;
		highlightCoords = waveformMarkerBar.getHighlightCoords;
		
		playerParams['start'] = highlightCoords['low'] ? 0;
		playerParams['end'] = highlightCoords['high'] ? 1;

		startPoint = start ? playerParams['start'];
		
		s.sendMsg('n_set', playerNodeNum, 'resetPos', startPoint, 'trig', 1, 'start', playerParams['start'], 'end', playerParams['end']);
	
	}
	
	setMarkers { |markers|
		loopMarkers = markers;
	}
	
	zoomToAbs { |val|
		var range;
		range = currBufDisplayEnd - currBufDisplayStart;
		^((val  * range) / currentBufferArray.size) + (currBufDisplayStart / currentBufferArray.size);
	}

	savePreset {
		"need to have a save preset action here".postln;
	}

	loadPreset { |sel|
		"loading preset name " ++ sel;
	}
	
	setModSource { |sel|
		playerParams['modSource'] = parent.audioBusRegister[sel];
		s.sendMsg('n_set', playerNodeNum, 'modBus', 'modSource');
	}

	setModLevel { |val|
		playerParams['modLevel'] = val;
		s.sendMsg('n_set', playerNodeNum, 'modLevel', playerParams['modLevel']);
	}
	
	setModLag { |val|
		playerParams['modLag'] = val;
		s.sendMsg('n_set', playerNodeNum, 'modLag', playerParams['modLag']);
	}

	setSpeed { |val|
		playerParams['speed'] = val;
		s.sendMsg('n_set', playerNodeNum, 'speed', playerParams['speed']);
	}

	setGain { |val|
		playerParams['gain'] = val;
		s.sendMsg('n_set', playerNodeNum, 'gain', playerParams['gain']);
	}

	setInputSource { |sel|
		recorderParams['inBus'] = sel;
		s.sendMsg('n_set', recorderNodeNum, 'inBus', recorderParams['inBus']);
	}
	
	setInputMix { |val| 
		playerParams['inBus'] = val;
		s.sendMsg('n_set', playerNodeNum, 'inputLevel', playerParams['inBus']);
	}

	setRecordOffset { |val|
		playerParams['recordOffset'] = val;
		s.sendMsg('n_set', playerNodeNum, 'recordOffset', playerParams['recordOffset']);
	}

	setRecordMode { |sync|
		if(sync){
		}{
		};
	}

	loadSynthDef { |numChannels=1, kTrigBus=1000, kStart=1001|
		SynthDef.new( "SampleLooperPlayer", {
			arg bufnum, speed=1, start=0, end=1, outBus=0, trig=1, resetPos=0, 
				modBus=20, modLag=0.2, modLev=0,
				inBus=1, recordOffset=0.1;
			
			var outPhase, outSig, kNumFrames, modSig, kStart, kEnd;
	
			kNumFrames = BufFrames.kr(bufnum);
			kStart = kNumFrames * start;
			kEnd = kNumFrames * end;
			
			modSig = Lag.ar(InFeedback.ar(modBus) * modLev, modLag);
			
			outPhase = Phasor.ar(trig, speed, kStart, kEnd, resetPos);
			
			outSig = BufRd.ar(numChannels, bufnum, outPhase + modSig);
			Out.ar(outBus, outSig);
			Out.kr(kTrigBus, (A2K.kr(outPhase) * -1) + ((kEnd - kStart) * 0.5));
			Out.kr(kStart);
			
		}).load(s);
		
		SynthDef.new("SampleLooperRecorder", { 
			arg bufnum, start=0, end=0, inBus=20, mix=0, recordMode=0;
			
			var aRecordHead, kEnd, inSig, kTrig, kNumFrames, skStart, iZero;
			iZero = DC.kr(0);
			kTrig = Select.kr(recordMode, [iZero, In.kr(kTrigBus)]);
			
			skStart = Select.kr(recordMode, [iZero, In.kr(kStart)]);
			aRecordHead = Phasor.ar(kTrig, 1, kStart, end);			
			inSig = (In.ar(inBus, numChannels) * (mix - 1).abs) + (BufRd.ar(numChannels, bufnum, aRecordHead) * mix);			
			kNumFrames = BufFrames.kr(bufnum);
			kStart = kNumFrames * start;
			kEnd   = kNumFrames * end;

			BufWr.ar(inSig.softclip, bufnum, aRecordHead);

		}).load(s);
		
	}
	
	makeGUI { |container|
		
		controlBackgroundColor = Color.blue.alpha_(0.2);
		
		topView = GUI.compositeView.new(container, Rect.new(0, 0, container.view.bounds.width, 245));
		topView.decorator = FlowLayout(topView.bounds);
		
		waveformColumn = GUI.vLayoutView.new(topView, Rect.new(0, 0, 600, 238));
		
		bufferRow = GUI.hLayoutView.new(waveformColumn, Rect.new(0, 0, waveformColumn.bounds.width, 25))
			.background_(Color.black);
		clearBufferButton = GUI.button.new(bufferRow, Rect.new(0, 0, 75, 0))
			.states_([["clear buffer", Color.white, controlBackgroundColor]])
		    .font_(parent.controlFont)
		    .action_({ |obj| this.clearActiveBuffer; });
		
		
		addFileButton = GUI.button.new(bufferRow, Rect.new(0, 0, 75, 0))
			.states_([["add file(s)", Color.white, controlBackgroundColor]])
		    .font_(parent.controlFont)
		    .action_({ |obj| this.addBufferFromFile; });
		
		addEmptyBufferBox = GUI.numberBox.new(bufferRow, Rect.new(0, 0, 30, 0))
		    .font_(parent.controlFont)
		    .value_(16)
		    .action_({ |obj| this.addEmptyBuffer(obj.string.interpret) });
		
		addEmptyBufferButton = GUI.button.new(bufferRow, Rect.new(0, 0, 90, 0))
			.states_([["add empty buffer", Color.white, controlBackgroundColor]])
		    .font_(parent.controlFont)
		    .action_({ |obj| this.addEmptyBuffer(addEmptyBufferBox.string.interpret) });
		
		bufferSelectMenu = GUI.popUpMenu.new(bufferRow, Rect.new(0, 0, 280, 0))
			.background_(controlBackgroundColor)
			.stringColor_(Color.white)
		    .font_(parent.controlFont)
		    .action_({ |obj| this.setActiveBuffer(obj.value); });
			
		waveformControlView = GUI.compositeView.new(waveformColumn, Rect.new(0, 0, waveformColumn.bounds.width, 180))
			.background_(Color.black);

		waveformControlView.decorator_(FlowLayout(waveformControlView.bounds));
		
		waveformMarkerBar = MarkerBar.new(waveformControlView, Rect.new(0, 0, 565, 20))
			.markerColor_(Color.white)
			.background_(Color.blue.alpha_(0.2))
			.mouseDownAction_({ |obj,x,y,mod| this.setMarkers(obj.value); });

		waveformMarkerClearButton = GUI.button.new(waveformControlView, Rect.new(0, 0, 20, 20))
			.states_([["X", Color.black, Color.yellow]])
		    .font_(parent.controlFont)
		    .action_({ |obj| waveformMarkerBar.clear; });
		
		waveformView = GUI.multiSliderView.new(waveformControlView, Rect.new(0, 0, 565, 125))
			.background_(Color.grey(0.9))
			.strokeColor_(Color.blue(0.3))
			.drawLines_(true)
			.drawRects_(false)
			.elasticMode_(1)
			.value_(Array.fill(waveformDisplayResolution, { 0.5 }))
			.editable_(false)
			.showIndex_(true)
			.selectionSize_(2)
			.startIndex_(0)
			.action_({ |obj|
				var start,end;
				start = this.zoomToAbs(obj.index / obj.value.size);
				end = this.zoomToAbs((obj.selectionSize + obj.index) / obj.value.size);
				this.setLoopRange(start, end); 
			})
			.mouseUpAction_({ |obj| s.sendMsg('n_set', playerNodeNum, 'trig', 0) });

		waveformViewVZoom = GUI.slider.new(waveformControlView, Rect.new(0, 0, 20, 125))
			.background_(controlBackgroundColor)
			.knobColor_(HiliteGradient.new(controlBackgroundColor, Color.white, \h, 64, 0.5))
			.action_({ |obj| this.setWaveformVZoom(waveformVZoomSpec.map(obj.value)); });
		
		waveformViewZoom = GUI.rangeSlider.new(waveformControlView, Rect.new(0, 0, 565, 20))
			.knobColor_(HiliteGradient.new(controlBackgroundColor, Color.white, \v, 64, 0.5))
			.background_(controlBackgroundColor)
			.lo_(0)
			.hi_(1)
			.action_({ |obj| this.setWaveformZoom(obj.lo, obj.hi); });
			
		transportRow = GUI.hLayoutView.new(waveformColumn, Rect.new(0, 0, 0, 25))
			.background_(Color.black);
			
		recordButton = GUI.button.new(transportRow, Rect.new(0, 0, 75, 25))
			.states_([
				["o", Color.red, Color.new255(25,25,25)],
				["o", Color.black, Color.red]
			])
		    .font_(parent.strongFont)
		    .action_({ |obj| this.record(obj.value.toBool); });

		backButton = GUI.button.new(transportRow, Rect.new(0, 0, 50, 25))
			.states_([["<<", Color.white, controlBackgroundColor]])
		    .font_(parent.strongFont)
		    .action_({ |obj| this.back; })
			.mouseUpAction_({ |obj| s.sendMsg('n_set', playerNodeNum, 'trig', 0) });


		playButton = GUI.button.new(transportRow, Rect.new(0, 0, 75, 25))
			.states_([
				[">", Color.green, controlBackgroundColor],
				[">", Color.black, Color.green]
			])
		    .font_(parent.strongFont)
		    .action_({ |obj| this.play(obj.value.toBool); });

		forwardButton = GUI.button.new(transportRow, Rect.new(0, 0, 50, 25))
			.states_([[">>", Color.white, controlBackgroundColor]])
		    .font_(parent.strongFont)
		    .action_({ |obj| this.forward; })
			.mouseUpAction_({ |obj| s.sendMsg('n_set', playerNodeNum, 'trig', 0) });

		    
		pauseButton = GUI.button.new(transportRow, Rect.new(0, 0, 75, 25))
			.states_([
				["||", Color.yellow, controlBackgroundColor],
				["||", Color.black, Color.yellow]
			])
		    .font_(parent.strongFont)
		    .action_({ |obj| this.pause(obj.value.toBool); });
		    
		stopButton = GUI.button.new(transportRow, Rect.new(0, 0, 75, 25))
			.states_([["[]", Color.white(0.8), controlBackgroundColor]])
		    .font_(parent.strongFont)
		    .action_({ |obj| this.stop; });
		

		
		controlColumn = GUI.vLayoutView.new(topView, Rect.new(0, 0, 200, 238));
		
		presetRow = GUI.hLayoutView.new(controlColumn, Rect.new(0, 0, 0, 25))
			.background_(Color.black);

		presetSaveButton = GUI.button.new(presetRow, Rect.new(0, 0, 45, 0))
		    .font_(parent.controlFont)
			.states_([["save", Color.white, Color.blue.alpha_(0.2)]])
		    .action_({ |obj| this.savePreset; });
		presetMenu = GUI.popUpMenu.new(presetRow, Rect.new(0, 0, 145, 0))
			.items_(["this will have the presets listed", "some day"])
			.background_(controlBackgroundColor)
		    .font_(parent.controlFont)
			.stringColor_(Color.white)
		    .action_({ |obj| this.loadPreset(obj.item); });

		controlView = GUI.compositeView.new(controlColumn, Rect.new(0, 0, 200, 210))
			.background_(Color.black);
		controlView.decorator_(FlowLayout(controlView.bounds));

		GUI.staticText.new(controlView, Rect.new(0, 0, 40, 20))
            .string_("mod")
		    .stringColor_(Color.white);
		modSourceMenu = GUI.popUpMenu.new(controlView, Rect.new(0, 0, 145, 20))
			.items_(parent.audioBusRegister.keys.asArray)
			.background_(controlBackgroundColor)
		    .font_(parent.controlFont)
			.stringColor_(Color.white)
		    .action_({ |obj| this.setModSource(obj.item); });

		modLevelKnob = EZJKnob.new(controlView, Rect.new(0, 0, 37.5, 73), "mod lev")
			.spec_([-4, 4].asSpec)
			.value_(1)
		    .font_(parent.controlFont)
			.knobColor_([Color.clear, Color.white, Color.white.alpha_(0.1), Color.white])
			.stringColor_(Color.white)
			.background_(controlBackgroundColor)
			.knobAction_({ |obj| this.setModLevel(obj.value); });
		modLagKnob = EZJKnob.new(controlView, Rect.new(0, 0, 37.5, 73), "mod lag")
			.background_(controlBackgroundColor)
			.spec_([0, 4].asSpec)
			.value_(1)
		    .font_(parent.controlFont)
			.stringColor_(Color.white)
			.knobColor_([Color.clear, Color.white, Color.white.alpha_(0.1), Color.white])
			.knobAction_({ |obj| this.setModLag(obj.value); });
		speedKnob = EZJKnob.new(controlView, Rect.new(0, 0, 37.5, 73), "speed")
			.spec_([0, 4].asSpec)
			.value_(1)
			.stringColor_(Color.white)
		    .font_(parent.controlFont)
			.background_(controlBackgroundColor)
			.knobColor_([Color.clear, Color.white, Color.white.alpha_(0.1), Color.white])
			.knobAction_({ |obj| this.setSpeed(obj.value); });
		gainKnob = EZJKnob.new(controlView, Rect.new(0, 0, 37.5, 73), "out gain")
			.spec_([0, 4].asSpec)
			.value_(1)
			.stringColor_(Color.white)
		    .font_(parent.controlFont)
			.background_(controlBackgroundColor)
			.knobColor_([Color.clear, Color.white, Color.white.alpha_(0.1), Color.white])
			.knobAction_({ |obj| this.setGain(obj.value); });


		GUI.staticText.new(controlView, Rect.new(0, 0, 40, 20))
            .string_("in")
		    .stringColor_(Color.white);
		inputSourceMenu = GUI.popUpMenu.new(controlView, Rect.new(0, 0, 145, 20))
 		    .items_(parent.audioBusRegister.keys.asArray)
			.background_(controlBackgroundColor)
		    .font_(parent.controlFont)
			.stringColor_(Color.white)
		    .action_({ |obj| this.setInputSource(obj.item); });
		inputLevelKnob = EZJKnob.new(controlView, Rect.new(0, 0, 37.5, 73), "rec mix")
			.spec_([0, 1].asSpec)
			.value_(0)
			.stringColor_(Color.white)
		    .font_(parent.controlFont)
			.background_(controlBackgroundColor)
			.knobColor_([Color.clear, Color.white, Color.white.alpha_(0.1), Color.white])
			.knobAction_({ |obj| this.setInputMix(obj.value); });
		recordOffsetKnob = EZJKnob.new(controlView, Rect.new(0, 0, 37.5, 73), "in gain")
			.spec_([0, 4].asSpec)
			.value_(1)
			.stringColor_(Color.white)
		    .font_(parent.controlFont)
			.background_(controlBackgroundColor)
			.knobColor_([Color.clear, Color.white, Color.white.alpha_(0.1), Color.white])
			.knobAction_({ |obj| this.setRecordOffset(obj.value); });
		recordModeButton = GUI.button.new(controlView, Rect.new(0, 0, 75, 25))
			.states_([
				["full", Color.yellow, controlBackgroundColor],
				["sync", Color.black, Color.yellow]
			])
		    .font_(parent.controlFont)
		    .action_({ |obj| this.setRecordMode(obj.value.toBool); });

		    
	}
	
}

