
Sampler { // container for one or more SampleLoopers
	var parent, <>win, activeMidiChannels, <>channels, <outBus,
		controlView, midiButtons, crossfadeSlider;
	*new { |env, loopers|
		^super.new.init_sampler(env, loopers);
	}
	
	init_sampler { |env, loopers=1|
		parent = env;
		channels = Array.new;
		midiButtons = Array.new;
		activeMidiChannels = Array.new;
		this.initGUI;
		loopers.do{ |ind|
			this.addChannel(ind);
			
		};
		this.addMixerChannel;
		this.useKeyboard;
	}
	
	useKeyboard { // this should automatically generate keys based on the number samplers
		GUI.view.globalKeyDownAction = { |obj,char,mod,unic,keyc|
			char.switch(
				$1, { midiButtons[0].valueAction_(activeMidiChannels.includes(0).not.toInt); },
				$2, { midiButtons[1].valueAction_(activeMidiChannels.includes(1).not.toInt); },
				$3, { midiButtons[2].valueAction_(activeMidiChannels.includes(2).not.toInt); },
				$4, { midiButtons[3].valueAction_(activeMidiChannels.includes(3).not.toInt); }//,
				//$5, { midiButtons[4].valueAction_(activeMidiChannels.includes(4).not.toInt); },
				//$6, { midiButtons[5].valueAction_(activeMidiChannels.includes(5).not.toInt); }
			);			
		};
	}
	
	addChannel { |num|
		channels = channels.add(SampleLooper.new(parent, this));
		//win.bounds = Rect.new(win.bounds.left, win.bounds.top, 830, channels.size * 270);
		channels.last.makeGUI(win);
		midiButtons = midiButtons.add(
			GUI.button.new(controlView, Rect.new(0, 0, controlView.bounds.height, 0))
				.states_([
					[(num + 1).asString, Color.yellow, Color.black],
					[(num + 1).asString, Color.black, Color.yellow]
				])
				.action_({ |obj| this.setActiveMidiChannel(obj.value.toBool, num); })
		);
	}
	
	setActiveMidiChannel { |sel,chan|
		if(sel){
			activeMidiChannels = activeMidiChannels.add(chan);
		}{
			activeMidiChannels.remove(chan);
		};
	}
	
	addMixerChannel {
		parent.mixer.addStereoChannel("Sampler", 0, true);
		outBus = parent.mixer.channels["Sampler"].inBus;
		channels.do{ |obj,ind|
			obj.outBus = outBus;
		}
	}
	
	initGUI {
		win = GUI.window.new("Sample Loopers", Rect.new(500.rand, 500.rand, 1700, 530)).front;
		win.view.decorator = FlowLayout(win.view.bounds);
		controlView = GUI.hLayoutView.new(win, Rect.new(0, 0, win.view.bounds.width, 25))
			.background_(Color.black);
		GUI.staticText.new(controlView, Rect.new(0, 0, 35, 0))
			.stringColor_(Color.white)
			.string_("xfade:")
			.font_(parent.controlFont);
		crossfadeSlider = GUI.slider.new(controlView, Rect.new(0, 0, 200, 0))
			.value_(0.5)
			.background_(Color.blue.alpha_(0.2))
			.knobColor_(HiliteGradient.new(Color.blue.alpha_(0.2), Color.white, \v, 64, 0.5))
			.action_({ |obj| this.setCrossfade(obj.value); });
		GUI.staticText.new(controlView, Rect.new(0, 0, 55, 0))
			.stringColor_(Color.white)
			.string_("midi active:")
			.font_(parent.controlFont);		
	}
	
	cc { |src,chan,num,val|
		channels.do{ |obj,ind|
			if(activeMidiChannels.includes(ind)){
				obj.ccFunction.value(src,chan,num,val);
			};
		};
	}
	
	updateBufferMenus {
		channels.do{ |obj,ind|
			obj.updateBufferMenu;
		};
	}
	
}

SampleLooper {
	classvar <buffers, <bufferMarkers, <groupNum=55;
	var parent, s, <playerNodeNum, <recorderNodeNum, playerParams, recorderParams, paused=false, synthOutputs, synthInputs, activeBufferIndex=0, activeMarkerIndex=0, currentBufferArray, currBufDisplayStart, currBufDisplayEnd, waveformVZoomSpec, waveformDisplayResolution=4096, isRecording=false, loopMarkers, isPlaying=false, isRecording=false,
	looper, <>ccFunction, <>sampler, numChannels;
	// GUI objects
	var controlBackgroundColor, topView, waveformColumn, transportRow, controlColumn, presetRow, bufferRow, presetMenu, presetSaveButton, waveformControlView, <>waveformMarkerBar, waveformMarkerClearButton, waveformView, waveformViewVZoomView, waveformViewVZoom, waveformViewZoom, controlView, recordButton, backButton, playButton, forwardButton, pauseButton, stopButton, playbackSpeedKnob, addFileButton, clearBufferButton, addEmptyBufferBox, addEmptyBufferButton, bufferSelectMenu, modBusMenu, modLevelKnob, modLagKnob, speedKnob, gainKnob, inputSourceMenu, inputLevelKnob, preLevelKnob, syncOffsetKnob, recordModeButton, recordOffsetKnob;

	
	*new { |par,samp|
		^super.new.init_samplelooper(par,samp);
	}
	
	init_samplelooper { |par,samp|
		parent  = par;
		sampler = samp;
		s       = Server.default;
		playerNodeNum = s.nextNodeID;
		recorderNodeNum = s.nextNodeID;
		buffers = Array.new;
		bufferMarkers = Array.new;
		waveformVZoomSpec = [1,10].asSpec;
		playerParams  = Dictionary[
			'bufnum'       -> -1, 
			'speed'        -> 1, 
			'start'        -> 0, 
			'end'          -> 1, 
			'outBus'       -> 0, 
			'trig'         -> 0, 
			'modBus'       -> 20, 
			'modLag'       -> 0.2, 
			'modLev'       -> 0,
			'inBus'        -> 1
		];
		recorderParams = Dictionary[
			'bufnum'       -> -1, 
			'trig'         -> 0, 
			'inBus'        -> 20,
			'stereo'       -> 0,
			'inLev'        -> 1,
			'preLev'       -> 0			
		];
		currentBufferArray = Array.fill(waveformDisplayResolution, { 0.5 });
		loopMarkers = Array.new;
		ccFunction = { |src,chan,num,val|
			defer{
				num.switch(
					20, {
						postln("what should I be user for??");
					},
					21, {
						this.back;
					},
					22, {
						this.forward;
					},
					23, {
						this.stop;
					},
					24, {
						this.play;
						playButton.value_(1);
					},
					25, {
						this.record(isRecording.not); // toggles isRecording in this function
						recordButton.value_(isRecording.toInt); 
					}
				);
			};
		};
		synthOutputs = Dictionary[
			1 -> { |bus,sig| Out.ar(bus, Pan2.ar(sig, 0)); },
			2 -> { |bus,sig| Out.ar(bus, sig); }
		];
		synthInputs = Dictionary[
			1 -> { |bus, phase, bufnum, inLev, preLev, stereo, env| 
				[(InFeedback.ar(bus, 1) * inLev * env) + (BufRd.ar(1, bufnum, phase) * preLev)];
			},
			2 -> { |bus, phase, bufnum, inLev, preLev, stereo, env|
				[
					(InFeedback.ar(bus, 1) * inLev * env) + (BufRd.ar(1, bufnum, phase) * preLev), 
					(InFeedback.ar(bus + stereo, 1) * inLev * env) + (BufRd.ar(1, bufnum, phase) * preLev)
				]
			}
		];
		s.sendMsg('g_new', groupNum, 0, 1);

	}
	
	outBus {
		^playerParams['outBus'];
	}
	
	outBus_ { |val|
		playerParams['outBus'] = val;
	}
		
/*	addBuffer { |length=16|
		buffers = buffers.add(Buffer.alloc(s, length * s.sampleRate));
		bufferMarkers = bufferMarkers.add(waveformMarkerBar.value);
	}

	addSoundFile { |filename| // public only method??
		if(filename.notNil){
			buffers = buffers.add(Buffer.read(s, filename));
			bufferMarkers = bufferMarkers.add(waveformMarkerBar.value);
		}{
			buffers = buffers.add(Buffer.loadDialog(s));
			bufferMarkers = bufferMarkers.add(waveformMarkerBar.value);
		};
	}
*/	
	record { |val|
		isRecording = val ? true;
		if(isRecording){
			s.listSendMsg(['s_new', 'SampleLooperRecorder', recorderNodeNum, 1, groupNum] ++ recorderParams.getPairs);
			s.sendMsg('n_set', recorderNodeNum, 'trig', 1);
		}{
			s.sendMsg('n_set', recorderNodeNum, 'trig', 0);
			this.drawWaveformView(false);
		};
	}

	back {
		var lo,hi, range;
		if(waveformMarkerBar.value.size > 0){
			if(waveformMarkerBar.highlightRange.size > 0){
				if(waveformMarkerBar.highlightRange['low'] <= -1){
					range = waveformMarkerBar.highlightRange['high'] - waveformMarkerBar.highlightRange['low'];
					lo = waveformMarkerBar.value.size - range;
					hi = waveformMarkerBar.value.lastIndex + 1;
				}{
					lo = waveformMarkerBar.highlightRange['low'] - 1;
					hi = waveformMarkerBar.highlightRange['high'] - 1;
				};
			}{
				lo = waveformMarkerBar.value.lastIndex;
				hi = waveformMarkerBar.value.size;
			};
			waveformMarkerBar.setHighlightRange(lo,hi);
			this.setLoopPointParams;
		};
	}

	play { |val|
		isPlaying = val ? true;
		if(isPlaying){
			s.listSendMsg(['s_new', 'SampleLooperPlayer', playerNodeNum, 0, groupNum] ++ playerParams.getPairs);
		}{
			playButton.value_(1);
		};
	}
	
	forward {
		var lo,hi;
		if(waveformMarkerBar.value.size > 0){
			if(waveformMarkerBar.highlightRange.size > 0){
				if(waveformMarkerBar.highlightRange['high'] > waveformMarkerBar.value.lastIndex){
					lo = -1;
					hi = waveformMarkerBar.highlightRange['high'] - waveformMarkerBar.highlightRange['low'] - 1;
				}{
					lo = waveformMarkerBar.highlightRange['low'] + 1;
					hi = waveformMarkerBar.highlightRange['high'] + 1;
				};
			}{
				lo = -1;
				hi = 0;
			};
			waveformMarkerBar.setHighlightRange(lo,hi);
			this.setLoopPointParams;
		};
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
			recordButton.valueAction_(0);
		};
	}
	
	addBufferFromFile {
		Dialog.getPaths({ |paths|
			paths.do{ |obj,ind|
				buffers = buffers.add(Buffer.read(s, obj));
				bufferMarkers = bufferMarkers.add(Array.new);
				if(ind == paths.lastIndex){
					// just updating the GUI "later"
					// may fail with large buffers
					AppClock.sched(1, {sampler.updateBufferMenus; nil; });
				};
			};
		});
	}
	
	addEmptyBuffer { |length|
		// can only add empty mono buffers for now.
		// multi-channel support should come later.
		buffers = buffers.add(Buffer.alloc(s, length * s.sampleRate, 1));
		bufferMarkers = bufferMarkers.add(Array.new);
		AppClock.sched(1, {sampler.updateBufferMenus; nil;});
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
		if(numChannels.isNil){
			this.loadSynthDef(1);
		};
		numChannels = buffers[activeBufferIndex].numChannels; // numChannels is set for the first time here

		activeBufferIndex = sel;
		playerParams['bufnum'] = buffers[activeBufferIndex].bufnum;
		recorderParams['bufnum'] = buffers[activeBufferIndex].bufnum;
		if(buffers[activeBufferIndex].numChannels != numChannels){
			if(isPlaying || isRecording){
				this.stop;
			};
			this.loadSynthDef(numChannels);
		};
		s.sendMsg('n_set', playerNodeNum, 'bufnum', playerParams['bufnum']);
		s.sendMsg('n_set', recorderNodeNum, 'bufnum', recorderParams['bufnum']);
		this.setActiveMarkers(sel);
		this.drawWaveformView;
	}
	
	setActiveMarkers { |sel|
		bufferMarkers[activeMarkerIndex] = waveformMarkerBar.value;
		activeMarkerIndex = sel;
		waveformMarkerBar.value = bufferMarkers[activeMarkerIndex];
	}
	
	drawWaveformView { |clearMarkerBar=true|
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
				/*if(clearMarkerBar){
					waveformMarkerBar.clear;
				};*/
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
	
	setLoopRange { |start, end, trigStart|
		var startMarker, endMarker;
		if(waveformMarkerBar.value.size > 0){
			startMarker = waveformMarkerBar.getIndexForLocation(start);
			endMarker = waveformMarkerBar.getIndexForLocation(end) + 1;
			
			waveformMarkerBar.setHighlightRange(startMarker, endMarker);
		};
		
		this.setLoopPointParams(trigStart);
		
	}
	
	setLoopPointParams { |start|
		var highlightCoords, startPoint;

		highlightCoords = waveformMarkerBar.getHighlightCoords;
		
		playerParams['start'] = highlightCoords['low'] ? 0;
		playerParams['end'] = highlightCoords['high'] ? 1;

		startPoint = start ? playerParams['start'];

		s.sendMsg('n_set', playerNodeNum, 'resetPos', startPoint, 'start', playerParams['start'], 'end', playerParams['end']);
		s.sendMsg('n_set', playerNodeNum, 'trig', 1);
	
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
	
	setModBus { |sel|
		playerParams['modBus'] = sel;
		s.sendMsg('n_set', playerNodeNum, 'modBus', playerParams['modBus']);
	}

	setModLevel { |val|
		playerParams['modLev'] = val;
		s.sendMsg('n_set', playerNodeNum, 'modLev', playerParams['modLev']);
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
	
	setInputLevel { |val| 
		recorderParams['inLev'] = val;
		s.sendMsg('n_set', recorderNodeNum, 'inLev', recorderParams['inLev']);
	}

	setPreLevel { |val| 
		recorderParams['preLev'] = val;
		s.sendMsg('n_set', recorderNodeNum, 'preLev', recorderParams['preLev']);
	}

	loadSynthDef { |numChan=1, trigBus=1000, startPointBus=1001, endPointBus=1002|
		postln("inside this.loadSynthDef(" ++ numChan ++ ");");
		SynthDef.new( "SampleLooperPlayer", {
			arg bufnum, speed=1, start=0, end=1, outBus=0, trig=0, resetPos=0, 
				modBus=20, modLag=0.2, modLev=0,
				inBus=1, recordOffset=0.1, gain=1;
			
			var outPhase, outSig, kNumFrames, modSig, kStart, kEnd, aTrig, kTrig, aSpeed;
	
			kNumFrames = BufFrames.kr(bufnum);
			kStart = kNumFrames * start;
			kEnd = kNumFrames * end;
			
			modSig = Lag.ar(InFeedback.ar(modBus) * modLev, modLag);
			aSpeed = Lag.ar(K2A.ar(speed), 1);
			outPhase = Phasor.ar(trig, aSpeed + modSig, kStart, kEnd, resetPos);
			
			outSig = BufRd.ar(numChan, bufnum, outPhase);
			SynthDef.wrap(synthOutputs[numChan], nil, [outBus, outSig * gain]);
			aTrig = ((outPhase - kStart) * -1) + ((kEnd - kStart) * 0.5);
			kTrig = A2K.kr(aTrig);

			Out.kr(trigBus, kTrig);
			Out.kr(startPointBus, kStart);
			Out.kr(endPointBus, kEnd);
			
		}).load(s);
		
		SynthDef.new("SampleLooperRecorder", { 
			arg bufnum, inBus=20, stereo=0, inNumChannels=1, inLev=1, preLev=0, recordMode=0, trig=0;
			
			var aRecordHead, inSig, skTrig, kNumFrames, kZero, aEnv;
			kZero = DC.kr(0);
			kNumFrames = BufFrames.kr(bufnum);
			
			skTrig = Select.kr(recordMode, [kZero, InTrig.kr(trigBus)]);

			aRecordHead = Phasor.ar(skTrig, BufRateScale.kr(bufnum), kZero, kNumFrames);
			aEnv = EnvGen.ar(Env.asr(0.1, 1, 0.1), trig, doneAction:2);
			BufWr.ar(SynthDef.wrap(synthInputs[numChan], nil, [inBus, aRecordHead, bufnum, inLev, preLev, stereo, aEnv]), bufnum, aRecordHead);

		}).load(s);
		
	}
	
	makeGUI { |container|
		
		controlBackgroundColor = Color.blue.alpha_(0.2);
		
		topView = GUI.compositeView.new(container, Rect.new(0, 0, 812, 245));
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
		    .value_(20)
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
				this.setLoopRange(start, end, obj.currentvalue);
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
		modBusMenu = GUI.popUpMenu.new(controlView, Rect.new(0, 0, 145, 20))
			.items_(parent.audioBusRegister.keys.asArray)
			.background_(controlBackgroundColor)
		    .font_(parent.controlFont)
			.stringColor_(Color.white)
		    .action_({ |obj| this.setModBus(parent.audioBusRegister[obj.item]); });

		modLevelKnob = EZJKnob.new(controlView, Rect.new(0, 0, 37.5, 73), "mod lev")
			.spec_([0, 100].asSpec)
			.value_(0)
		    .font_(parent.controlFont)
			.knobColor_([Color.clear, Color.white, Color.white.alpha_(0.1), Color.white])
			.stringColor_(Color.white)
			.background_(controlBackgroundColor)
			.knobAction_({ |obj| this.setModLevel(obj.value); });
		modLagKnob = EZJKnob.new(controlView, Rect.new(0, 0, 37.5, 73), "mod lag")
			.background_(controlBackgroundColor)
			.spec_([0, 1].asSpec)
			.value_(0.1)
		    .font_(parent.controlFont)
			.stringColor_(Color.white)
			.knobColor_([Color.clear, Color.white, Color.white.alpha_(0.1), Color.white])
			.knobAction_({ |obj| this.setModLag(obj.value); });
		speedKnob = EZJKnob.new(controlView, Rect.new(0, 0, 37.5, 73), "speed")
			.spec_([-4, 4].asSpec)
			.value_(1)
			.stringColor_(Color.white)
		    .font_(parent.controlFont)
			.background_(controlBackgroundColor)
			.knobColor_([Color.clear, Color.white, Color.white.alpha_(0.1), Color.white])
			.knobAction_({ |obj| this.setSpeed(obj.value); });
		gainKnob = EZJKnob.new(controlView, Rect.new(0, 0, 37.5, 73), "gain")
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
		    .action_({ |obj| this.setInputSource(parent.audioBusRegister[obj.item]); });
		inputLevelKnob = EZJKnob.new(controlView, Rect.new(0, 0, 37.5, 73), "in lev")
			.spec_([0, 1].asSpec)
			.value_(1)
			.stringColor_(Color.white)
		    .font_(parent.controlFont)
			.background_(controlBackgroundColor)
			.knobColor_([Color.clear, Color.white, Color.white.alpha_(0.1), Color.white])
			.knobAction_({ |obj| this.setInputLevel(obj.value); });
		preLevelKnob = EZJKnob.new(controlView, Rect.new(0, 0, 37.5, 73), "pre lev")
			.spec_([0, 1].asSpec)
			.value_(0)
			.stringColor_(Color.white)
		    .font_(parent.controlFont)
			.background_(controlBackgroundColor)
			.knobColor_([Color.clear, Color.white, Color.white.alpha_(0.1), Color.white])
			.knobAction_({ |obj| this.setPreLevel(obj.value); });

		    
	}
	
}

